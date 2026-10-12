"""Tests for cross-module transitive SPDX generation and relationship integrity."""
from __future__ import annotations

import importlib.util
import json
import tempfile
import unittest
import zipfile
from pathlib import Path


PACKAGING = Path(__file__).resolve().parents[1]


def load_module(name: str, path: Path):
    spec = importlib.util.spec_from_file_location(name, path)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


GENERATOR = load_module("generate_sbom", PACKAGING / "generate_sbom.py")
VERIFIER = load_module("verify_spdx", PACKAGING / "verify_spdx.py")


def node(group: str, artifact: str, version: str, scope: str = "compile", children=None):
    return {"groupId": group, "artifactId": artifact, "version": version, "type": "jar",
            "scope": scope, "classifier": "", "optional": "false", "children": children or []}


class GenerateSbomTests(unittest.TestCase):
    def test_aggregates_all_module_trees_transitive_edges_and_deduplicates_packages(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            (root / "engine" / "target").mkdir(parents=True)
            (root / "worker" / "target").mkdir(parents=True)
            lock = root / "dependency-lock.json"
            lock.write_text(json.dumps({"runtime": {"java": "21"}}), encoding="utf-8")
            jackson = node("com.fasterxml.jackson.core", "jackson-core", "2.17.2")
            arrow = node("org.apache.arrow", "arrow-vector", "19.0.0", children=[jackson])
            engine = node("io.zingg.duckdb", "engine-duckdb", "0.1.0-SNAPSHOT", children=[
                node("org.duckdb", "duckdb_jdbc", "1.5.5.1"), arrow])
            worker = node("io.zingg.duckdb", "runtime-worker", "0.1.0-SNAPSHOT", children=[
                node("io.zingg.duckdb", "engine-duckdb", "0.1.0-SNAPSHOT", children=[
                    node("org.duckdb", "duckdb_jdbc", "1.5.5.1"),
                    node("org.apache.arrow", "arrow-vector", "19.0.0", children=[
                        node("com.fasterxml.jackson.core", "jackson-core", "2.17.2")])])])
            for destination, tree in ((root / "engine" / "target" / "dependency-tree.json", engine),
                                      (root / "worker" / "target" / "dependency-tree.json", worker)):
                destination.write_text(json.dumps(tree), encoding="utf-8")

            document = GENERATOR.build_document(lock, root)
            VERIFIER.validate(document)
            coordinates = {(package["name"], package["versionInfo"]) for package in document["packages"]}
            self.assertIn(("org.duckdb:duckdb_jdbc", "1.5.5.1"), coordinates)
            self.assertIn(("org.apache.arrow:arrow-vector", "19.0.0"), coordinates)
            self.assertIn(("com.fasterxml.jackson.core:jackson-core", "2.17.2"), coordinates)
            self.assertEqual(1, sum(package["name"] == "org.apache.arrow:arrow-vector"
                                    for package in document["packages"]))
            self.assertEqual(1, sum(package["name"] == "io.zingg.duckdb:engine-duckdb"
                                    for package in document["packages"]))
            self.assertTrue(any(edge.get("comment") == "Maven scope: compile"
                                for edge in document["relationships"]))
            third_party = next(package for package in document["packages"]
                               if package["name"] == "org.apache.arrow:arrow-vector")
            self.assertEqual("NOASSERTION", third_party["licenseDeclared"])

    def test_inventories_shaded_jar_coordinates_as_distributed_components(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            tree_dir = root / "runtime-worker" / "target"
            tree_dir.mkdir(parents=True)
            lock = root / "dependency-lock.json"
            lock.write_text(json.dumps({"runtime": {"java": "21"}}), encoding="utf-8")
            dependencies = [node("io.zingg.duckdb", "runtime-worker", "0.1.0-SNAPSHOT"),
                            node("io.zingg.duckdb", "engine-api", "0.1.0-SNAPSHOT"),
                            node("org.duckdb", "duckdb_jdbc", "1.5.5.1")]
            tree = node("io.zingg.duckdb", "runtime-worker", "0.1.0-SNAPSHOT", children=dependencies[1:])
            (tree_dir / "dependency-tree.json").write_text(json.dumps(tree), encoding="utf-8")
            jar = root / "worker.jar"
            with zipfile.ZipFile(jar, "w") as archive:
                for group, artifact, version in (("io.zingg.duckdb", "runtime-worker", "0.1.0-SNAPSHOT"),
                                                 ("io.zingg.duckdb", "engine-api", "0.1.0-SNAPSHOT"),
                                                 ("org.duckdb", "duckdb_jdbc", "1.5.5.1")):
                    archive.writestr(f"META-INF/maven/{group}/{artifact}/pom.properties",
                                     f"groupId={group}\nartifactId={artifact}\nversion={version}\n")

            document = GENERATOR.build_document(lock, root, jar)
            VERIFIER.validate(document)
            product_id = next(package["SPDXID"] for package in document["packages"]
                              if package["name"] == "zingg-duckdb")
            contains = {edge["relatedSpdxElement"] for edge in document["relationships"]
                        if edge["spdxElementId"] == product_id and edge["relationshipType"] == "CONTAINS"}
            by_id = {package["SPDXID"]: package for package in document["packages"]}
            described = {(by_id[package_id]["name"], by_id[package_id]["versionInfo"]) for package_id in contains}
            self.assertTrue({("io.zingg.duckdb:runtime-worker", "0.1.0-SNAPSHOT"),
                             ("io.zingg.duckdb:engine-api", "0.1.0-SNAPSHOT"),
                             ("org.duckdb:duckdb_jdbc", "1.5.5.1")}.issubset(described))

    def test_records_embedded_metadata_missing_from_dependency_graph_for_review(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            (root / "target").mkdir()
            lock = root / "dependency-lock.json"
            lock.write_text('{"runtime":{"java":"21"}}', encoding="utf-8")
            (root / "target" / "dependency-tree.json").write_text(
                json.dumps(node("io.zingg.duckdb", "runtime-worker", "1")), encoding="utf-8")
            jar = root / "worker.jar"
            with zipfile.ZipFile(jar, "w") as archive:
                archive.writestr("META-INF/maven/org.example/missing/pom.properties",
                                 "groupId=org.example\nartifactId=missing\nversion=1\n")
            document = GENERATOR.build_document(lock, root, jar)
            package = next(package for package in document["packages"]
                           if package["name"] == "org.example:missing")
            self.assertIn("not in reactor Maven dependency trees", package["comment"])
            self.assertTrue(any(edge["relatedSpdxElement"] == package["SPDXID"]
                                and edge["relationshipType"] == "CONTAINS"
                                for edge in document["relationships"]))

    def test_rejects_missing_or_malformed_dependency_tree(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            lock = root / "dependency-lock.json"
            lock.write_text('{"runtime":{"java":"21"}}', encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "no Maven dependency-tree"):
                GENERATOR.build_document(lock, root)
            tree = root / "target" / "dependency-tree.json"
            tree.parent.mkdir()
            tree.write_text('{"artifactId":"no-group"}', encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "missing Maven coordinates"):
                GENERATOR.build_document(lock, root)

    def test_spdx_verifier_rejects_dangling_relationships_and_duplicate_ids(self):
        document = {"spdxVersion": "SPDX-2.3", "dataLicense": "CC0-1.0", "SPDXID": "SPDXRef-DOCUMENT",
                    "documentNamespace": "https://example.invalid/sbom", "creationInfo": {"creators": ["Tool: test"], "created": "2026-01-01T00:00:00Z"},
                    "packages": [{"SPDXID": "SPDXRef-Package-a", "name": "a", "versionInfo": "1",
                                  "downloadLocation": "NOASSERTION", "filesAnalyzed": False,
                                  "licenseConcluded": "NOASSERTION", "licenseDeclared": "NOASSERTION",
                                  "copyrightText": "NOASSERTION",
                                  "externalRefs": [{"referenceCategory": "PACKAGE-MANAGER",
                                                    "referenceType": "purl",
                                                    "referenceLocator": "pkg:maven/org.example/a@1"}]}],
                    "relationships": [{"spdxElementId": "SPDXRef-DOCUMENT", "relationshipType": "DESCRIBES",
                                       "relatedSpdxElement": "SPDXRef-Package-a"}]}
        VERIFIER.validate(document)
        document["relationships"].append({"spdxElementId": "SPDXRef-Package-a", "relationshipType": "DEPENDS_ON",
                                          "relatedSpdxElement": "SPDXRef-Missing"})
        with self.assertRaisesRegex(ValueError, "dangling endpoint"):
            VERIFIER.validate(document)

    def test_spdx_verifier_requires_exactly_one_valid_purl_for_each_dependency(self):
        document = {"spdxVersion": "SPDX-2.3", "dataLicense": "CC0-1.0", "SPDXID": "SPDXRef-DOCUMENT",
                    "documentNamespace": "https://example.invalid/sbom",
                    "creationInfo": {"creators": ["Tool: test"], "created": "2026-01-01T00:00:00Z"},
                    "packages": [{"SPDXID": "SPDXRef-Package-a", "name": "a", "versionInfo": "1",
                                  "downloadLocation": "NOASSERTION", "filesAnalyzed": False,
                                  "licenseConcluded": "NOASSERTION", "licenseDeclared": "NOASSERTION",
                                  "copyrightText": "NOASSERTION"}],
                    "relationships": [{"spdxElementId": "SPDXRef-DOCUMENT", "relationshipType": "DESCRIBES",
                                       "relatedSpdxElement": "SPDXRef-Package-a"}]}
        with self.assertRaisesRegex(ValueError, "exactly one package URL"):
            VERIFIER.validate(document)

        dependency = document["packages"][0]
        dependency["externalRefs"] = [{"referenceCategory": "PACKAGE-MANAGER", "referenceType": "purl",
                                       "referenceLocator": "https://example.org/not-a-purl"}]
        with self.assertRaisesRegex(ValueError, "invalid package URL"):
            VERIFIER.validate(document)

        dependency["externalRefs"].append({"referenceCategory": "PACKAGE-MANAGER", "referenceType": "purl",
                                            "referenceLocator": "pkg:maven/org.example/a@1"})
        with self.assertRaisesRegex(ValueError, "exactly one package URL"):
            VERIFIER.validate(document)


if __name__ == "__main__":
    unittest.main()
