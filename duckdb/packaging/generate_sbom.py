"""Generate a deterministic, transitive SPDX 2.3 inventory from Maven JSON trees."""
from __future__ import annotations

import hashlib
import json
import sys
import zipfile
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


def _coordinate(node: dict[str, Any]) -> str:
    group, artifact, version = (node.get(key) for key in ("groupId", "artifactId", "version"))
    if not all(isinstance(value, str) and value for value in (group, artifact, version)):
        raise ValueError(f"dependency node is missing Maven coordinates: {node!r}")
    kind = node.get("type") or "jar"
    classifier = node.get("classifier") or ""
    suffix = f":{classifier}" if classifier else ""
    return f"{group}:{artifact}:{version}:{kind}{suffix}"


def _spdx_id(coordinate: str) -> str:
    return "SPDXRef-Package-" + hashlib.sha256(coordinate.encode("utf-8")).hexdigest()[:24]


def load_trees(tree_root: Path) -> list[dict[str, Any]]:
    paths = sorted(tree_root.rglob("dependency-tree.json"))
    if not paths and tree_root.is_file():
        paths = [tree_root]
    if not paths:
        raise ValueError(f"no Maven dependency-tree.json files found under {tree_root}")
    roots = []
    for path in paths:
        try:
            node = json.loads(path.read_text(encoding="utf-8-sig"))
        except (OSError, json.JSONDecodeError) as error:
            raise ValueError(f"cannot read Maven dependency tree {path}: {error}") from error
        _coordinate(node)
        roots.append(node)
    return roots


def load_shaded_coordinates(jar_path: Path) -> set[str]:
    """Read Maven coordinates embedded by Maven Archiver in a shaded JAR."""
    if not jar_path.is_file():
        raise ValueError(f"shaded runtime JAR does not exist: {jar_path}")
    coordinates: set[str] = set()
    try:
        with zipfile.ZipFile(jar_path) as archive:
            for name in archive.namelist():
                if not name.startswith("META-INF/maven/") or not name.endswith("/pom.properties"):
                    continue
                fields: dict[str, str] = {}
                for raw_line in archive.read(name).decode("ISO-8859-1").splitlines():
                    line = raw_line.strip()
                    if not line or line.startswith(("#", "!")) or "=" not in line:
                        continue
                    key, value = line.split("=", 1)
                    fields[key.strip()] = value.strip()
                if all(fields.get(key) for key in ("groupId", "artifactId", "version")):
                    coordinates.add(f"{fields['groupId']}:{fields['artifactId']}:{fields['version']}:jar")
    except (OSError, zipfile.BadZipFile, UnicodeError) as error:
        raise ValueError(f"cannot inspect shaded runtime JAR {jar_path}: {error}") from error
    if not coordinates:
        raise ValueError(f"shaded runtime JAR has no embedded Maven pom.properties: {jar_path}")
    return coordinates


def build_document(lock_path: Path, tree_root: Path, shaded_jar: Path | None = None) -> dict[str, Any]:
    lock_bytes = lock_path.read_bytes()
    lock = json.loads(lock_bytes)
    roots = load_trees(tree_root)
    shaded_coordinates = load_shaded_coordinates(shaded_jar) if shaded_jar else None

    product_id = "SPDXRef-Package-" + hashlib.sha256(b"zingg-duckdb:0.1.0").hexdigest()[:24]
    product = {
        "SPDXID": product_id,
        "name": "zingg-duckdb",
        "versionInfo": "0.1.0",
        "downloadLocation": "NOASSERTION",
        "filesAnalyzed": False,
        "licenseConcluded": "NOASSERTION",
        "licenseDeclared": "AGPL-3.0-only",
        "copyrightText": "NOASSERTION",
        "comment": "Distribution release license is declared by the repository LICENSE; component-level third-party license review is pending.",
    }
    packages: dict[str, dict[str, Any]] = {"zingg-duckdb:0.1.0": product}
    edges: set[tuple[str, str, str]] = set()
    tree_coordinates: set[str] = set()
    tree_digest = hashlib.sha256(lock_bytes)

    def visit(node: dict[str, Any], parent_coordinate: str | None = None) -> None:
        coordinate = _coordinate(node)
        tree_coordinates.add(coordinate)
        group, artifact, version, kind, *classifier = coordinate.split(":")
        package_key = coordinate
        package_id = _spdx_id(coordinate)
        if package_key not in packages:
            local = group == "io.zingg.duckdb"
            package = {
                "SPDXID": package_id,
                "name": f"{group}:{artifact}",
                "versionInfo": version,
                "downloadLocation": "NOASSERTION",
                "filesAnalyzed": False,
                "licenseConcluded": "NOASSERTION",
                "licenseDeclared": "AGPL-3.0-only" if local else "NOASSERTION",
                "copyrightText": "NOASSERTION",
                "externalRefs": [{
                    "referenceCategory": "PACKAGE-MANAGER",
                    "referenceType": "purl",
                    "referenceLocator": f"pkg:maven/{group}/{artifact}@{version}",
                }],
            }
            if not local:
                package["comment"] = "License has not been established from reviewed authoritative notice metadata."
            packages[package_key] = package
        if parent_coordinate is None:
            if node.get("type") == "pom" and node.get("artifactId") == "zingg-duckdb":
                return
            edges.add((product_id, "CONTAINS", package_id))
        else:
            parent_id = _spdx_id(parent_coordinate)
            scope = node.get("scope") or "unspecified"
            edges.add((parent_id, "DEPENDS_ON", package_id + "|" + scope))
        for child in node.get("children", []):
            visit(child, coordinate)

    for root in roots:
        tree_digest.update(json.dumps(root, sort_keys=True, separators=(",", ":")).encode("utf-8"))
        visit(root)

    if shaded_coordinates is not None:
        for coordinate in sorted(shaded_coordinates):
            package_id = _spdx_id(coordinate)
            if coordinate not in packages:
                group, artifact, version, *_ = coordinate.split(":")
                local = group == "io.zingg.duckdb"
                packages[coordinate] = {
                    "SPDXID": package_id, "name": f"{group}:{artifact}", "versionInfo": version,
                    "downloadLocation": "NOASSERTION", "filesAnalyzed": False,
                    "licenseConcluded": "NOASSERTION",
                    "licenseDeclared": "AGPL-3.0-only" if local else "NOASSERTION",
                    "copyrightText": "NOASSERTION",
                    "externalRefs": [{"referenceCategory": "PACKAGE-MANAGER", "referenceType": "purl",
                                      "referenceLocator": f"pkg:maven/{group}/{artifact}@{version}"}],
                    "comment": ("Coordinate was found in embedded shaded-JAR pom.properties but not in reactor Maven dependency trees; it may be upstream-relocated or retained metadata and must be reviewed."
                                if coordinate not in tree_coordinates else
                                "Component coordinate was confirmed in the shaded runtime JAR."),
                }
            elif coordinate not in tree_coordinates:
                packages[coordinate]["comment"] = (
                    "Coordinate was found in embedded shaded-JAR pom.properties but not in reactor Maven dependency trees; "
                    "it may be upstream-relocated or retained metadata and must be reviewed.")
            edges.add((product_id, "CONTAINS", package_id))

    # The JDK version comes from the checked-in runtime lock. Its license is vendor- and
    # distribution-specific, so the SBOM deliberately does not infer a license.
    java_version = str(lock.get("runtime", {}).get("java", ""))
    if not java_version:
        raise ValueError("dependency lock is missing runtime.java")
    java_coordinate = f"org.openjdk:java-runtime:{java_version}:runtime"
    java_id = _spdx_id(java_coordinate)
    packages[java_coordinate] = {
        "SPDXID": java_id,
        "name": "Java runtime",
        "versionInfo": java_version,
        "downloadLocation": "NOASSERTION",
        "filesAnalyzed": False,
        "licenseConcluded": "NOASSERTION",
        "licenseDeclared": "NOASSERTION",
        "copyrightText": "NOASSERTION",
        "comment": "The bundle may use a system JDK or include a jlink runtime; vendor/license provenance is distribution-specific.",
        "externalRefs": [{"referenceCategory": "PACKAGE-MANAGER", "referenceType": "purl",
                          "referenceLocator": f"pkg:generic/java-runtime@{java_version}"}],
    }
    edges.add((product_id, "DEPENDS_ON", java_id + "|runtime-or-bundled"))

    package_values = sorted(packages.values(), key=lambda package: (package["name"], package["versionInfo"], package["SPDXID"]))
    relationships = [{"spdxElementId": source, "relationshipType": relation,
                      "relatedSpdxElement": target.split("|", 1)[0],
                      **({"comment": "Maven scope: " + target.split("|", 1)[1]} if "|" in target else {})}
                     for source, relation, target in sorted(edges)]
    relationships.insert(0, {"spdxElementId": "SPDXRef-DOCUMENT", "relationshipType": "DESCRIBES",
                             "relatedSpdxElement": product_id})
    namespace_hash = tree_digest.hexdigest()
    if shaded_coordinates is not None:
        for coordinate in sorted(shaded_coordinates):
            tree_digest.update(("shaded:" + coordinate + "\n").encode("utf-8"))
        namespace_hash = tree_digest.hexdigest()
    created = datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")
    return {
        "spdxVersion": "SPDX-2.3",
        "dataLicense": "CC0-1.0",
        "SPDXID": "SPDXRef-DOCUMENT",
        "name": "zingg-duckdb-0.1.0",
        "documentNamespace": f"https://zingg-duckdb.invalid/spdx/{namespace_hash}",
        "creationInfo": {"creators": ["Tool: zingg-duckdb-generate-sbom-1.0"], "created": created},
        "packages": package_values,
        "relationships": relationships,
        **({"comment": "Runtime worker shaded components are inventoried from embedded META-INF/maven/**/pom.properties; separate legacy importer JARs are inventoried in the Maven graph and distributed as distinct artifacts."}
           if shaded_coordinates is not None else {}),
    }


def main(argv: list[str]) -> int:
    if len(argv) not in (4, 5):
        print("usage: generate_sbom.py <dependency-lock.json> <output.spdx.json> <dependency-tree-root> [shaded-runtime.jar]", file=sys.stderr)
        return 2
    lock_path, output_path, tree_root = map(Path, argv[1:4])
    shaded_jar = Path(argv[4]) if len(argv) == 5 else None
    try:
        document = build_document(lock_path, tree_root, shaded_jar)
        Path(output_path).write_text(json.dumps(document, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    except (OSError, json.JSONDecodeError, ValueError) as error:
        print(f"SBOM generation failed: {error}", file=sys.stderr)
        return 1
    print(f"SBOM_GENERATION_SUCCESS packages={len(document['packages'])} relationships={len(document['relationships'])}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
