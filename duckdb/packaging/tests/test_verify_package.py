"""Adversarial tests for the cross-platform package structure contract."""
from __future__ import annotations

import hashlib
import importlib.util
import json
import os
import tempfile
import unittest
import zipfile
from pathlib import Path


SCRIPT = Path(__file__).resolve().parents[1] / "verify_package.py"
SPEC = importlib.util.spec_from_file_location("verify_package", SCRIPT)
assert SPEC is not None and SPEC.loader is not None
VERIFY = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(VERIFY)


def make_bundle(root: Path) -> Path:
    bundle = root / "bundle"
    (bundle / "config").mkdir(parents=True)
    worker = bundle / "worker/runtime-worker-0.1.0-SNAPSHOT.jar"
    worker.parent.mkdir(parents=True)
    worker.write_bytes(b"worker test payload")
    for relative in VERIFY.STATIC_FILES:
        path = bundle / relative
        if relative in {
            "config/runtime-manifest.json",
            "config/compatibility-profile.json",
            "config/compatibility-capsule.json",
            "config/release-policy.json",
            "config/source-provenance.json",
        } or relative == "worker/runtime-worker-0.1.0-SNAPSHOT.jar":
            continue
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(b"fixture")
    (bundle / "config/runtime-manifest.json").write_text(json.dumps({
        "compatibilityProfile": "zingg-0.7.0-duckdb-1.5.6.0",
        "duckdbJdbc": "1.5.6.0", "java": {"major": 21, "bundled": True}
    }))
    (bundle / "config/compatibility-profile.json").write_text(json.dumps({
        "id": "zingg-0.7.0-duckdb-1.5.6.0", "duckdbJdbc": "1.5.6.0",
        "rules": {"similarities": ["exact"]}
    }))
    (bundle / "config/compatibility-capsule.json").write_text(json.dumps({
        "schema": "zingg-duckdb-compatibility-capsule-1",
        "profile": "zingg-0.7.0-duckdb-1.5.6.0", "notices": ["NOTICE.md"], "patches": []
    }))
    (bundle / "config/release-policy.json").write_text(json.dumps({
        "schema": "zingg-duckdb-release-policy-1",
        "jdk": {"major": 21, "provenanceRequired": True},
        "signing": {"authenticodeRequiredForRelease": True}
    }))
    digest = hashlib.sha256(worker.read_bytes()).hexdigest()
    source_records = [
        {"path": path, "sha256": hashlib.sha256(path.encode("utf-8")).hexdigest()}
        for path in ("pom.xml", "dependency-lock.json", "README.md", "NOTICE.md", "LICENSE")
    ]
    source_records.append({
        "path": "runtime-worker/target/runtime-worker-0.1.0-SNAPSHOT.jar", "sha256": digest
    })
    (bundle / "SHA256SUMS").write_text("fixture inventory\n")
    (bundle / "sbom.spdx.json").write_text("{}")
    (bundle / "config/source-provenance.json").write_text(json.dumps({
        "schema": "zingg-duckdb-provenance-1",
        "compatibilityProfile": "zingg-0.7.0-duckdb-1.5.6.0", "files": source_records,
        "workerArtifactSha256": digest
    }))
    return bundle


class PackageStructureVerificationTest(unittest.TestCase):
    def test_accepts_valid_structure_and_worker_provenance(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            VERIFY.verify_structure(make_bundle(Path(directory)), require_jre=False)

    def test_rejects_missing_required_file_and_provenance_hash_mismatch(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            bundle = make_bundle(Path(directory))
            (bundle / "bin/zingg-duckdb.sh").unlink()
            with self.assertRaisesRegex(ValueError, "required bundle file is missing"):
                VERIFY.verify_structure(bundle, require_jre=False)
            (bundle / "bin/zingg-duckdb.sh").write_bytes(b"fixture")
            provenance_path = bundle / "config/source-provenance.json"
            provenance = json.loads(provenance_path.read_text())
            provenance["workerArtifactSha256"] = "0" * 64
            provenance_path.write_text(json.dumps(provenance))
            with self.assertRaisesRegex(ValueError, "provenance checksum mismatch"):
                VERIFY.verify_structure(bundle, require_jre=False)

    def test_rejects_incomplete_policy_capsule_and_runtime_contracts(self) -> None:
        for relative, mutate, message in (
            ("config/release-policy.json", lambda value: value.update(schema="wrong"), "release policy is incomplete"),
            ("config/compatibility-capsule.json", lambda value: value.update(patches=None), "compatibility capsule is incomplete"),
            ("config/runtime-manifest.json", lambda value: value["java"].update(major=17), "bundled Java 21"),
        ):
            with self.subTest(relative=relative), tempfile.TemporaryDirectory() as directory:
                bundle = make_bundle(Path(directory))
                path = bundle / relative
                value = json.loads(path.read_text())
                mutate(value)
                path.write_text(json.dumps(value))
                with self.assertRaisesRegex(ValueError, message):
                    VERIFY.verify_structure(bundle, require_jre=False)

    def test_rejects_python_cache_directories(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            bundle = make_bundle(Path(directory))
            cache = bundle / "python/zingg_duckdb/__pycache__"
            cache.mkdir()
            with self.assertRaisesRegex(ValueError, "__pycache__"):
                VERIFY.verify_structure(bundle, require_jre=False)

    def test_rejects_duplicate_json_contract_keys(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            bundle = make_bundle(Path(directory))
            manifest = bundle / "config/runtime-manifest.json"
            manifest.write_text('{"java":{"major":21,"bundled":true},"java":{"major":17,"bundled":true}}')
            with self.assertRaisesRegex(ValueError, "duplicate JSON key: java"):
                VERIFY.verify_structure(bundle, require_jre=False)

    def test_rejects_unsafe_duplicate_and_incomplete_source_inventory(self) -> None:
        cases = (
            (lambda items: items.append({"path": "../escape", "sha256": "0" * 64}), "unsafe source provenance path"),
            (lambda items: items.append(dict(items[0])), "duplicate source provenance path"),
            (lambda items: items.__setitem__(0, {"path": "pom.xml", "sha256": "invalid"}), "file entry is malformed"),
            (lambda items: items.__delitem__(0), "missing required source entries"),
        )
        for mutate, message in cases:
            with self.subTest(message=message), tempfile.TemporaryDirectory() as directory:
                bundle = make_bundle(Path(directory))
                provenance_path = bundle / "config/source-provenance.json"
                provenance = json.loads(provenance_path.read_text())
                mutate(provenance["files"])
                provenance_path.write_text(json.dumps(provenance))
                with self.assertRaisesRegex(ValueError, message):
                    VERIFY.verify_structure(bundle, require_jre=False)

    def test_cross_checks_source_provenance_against_sibling_archive(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            bundle = make_bundle(Path(directory))
            provenance = json.loads((bundle / "config/source-provenance.json").read_text())
            source_entries = [entry for entry in provenance["files"]
                              if entry["path"] != "runtime-worker/target/runtime-worker-0.1.0-SNAPSHOT.jar"]
            manifest = json.dumps({
                "schema": "zingg-duckdb-source-archive-1", "files": source_entries
            }).encode("utf-8")
            archive_path = bundle.parent / "zingg-duckdb-source.zip"
            with zipfile.ZipFile(archive_path, "w") as archive:
                for entry in source_entries:
                    info = zipfile.ZipInfo("zingg-duckdb/" + entry["path"])
                    info.external_attr = 0o100644 << 16
                    archive.writestr(info, entry["path"].encode("utf-8"))
                info = zipfile.ZipInfo("zingg-duckdb/SOURCE-MANIFEST.json")
                info.external_attr = 0o100644 << 16
                archive.writestr(info, manifest)
            VERIFY.verify_structure(bundle, require_jre=False, source_archive=archive_path)

            changed = json.loads((bundle / "config/source-provenance.json").read_text())
            next(entry for entry in changed["files"] if entry["path"] == "README.md")["sha256"] = "0" * 64
            (bundle / "config/source-provenance.json").write_text(json.dumps(changed))
            with self.assertRaisesRegex(ValueError, "source archive provenance hash mismatch: README.md"):
                VERIFY.verify_structure(bundle, require_jre=False, source_archive=archive_path)

    def test_rejects_tampered_source_archive_against_its_manifest(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            bundle = make_bundle(Path(directory))
            provenance = json.loads((bundle / "config/source-provenance.json").read_text())
            source_entries = [entry for entry in provenance["files"]
                              if entry["path"] != "runtime-worker/target/runtime-worker-0.1.0-SNAPSHOT.jar"]
            archive_path = bundle.parent / "zingg-duckdb-source.zip"
            with zipfile.ZipFile(archive_path, "w") as archive:
                for entry in source_entries:
                    payload = b"tampered" if entry["path"] == "README.md" else entry["path"].encode("utf-8")
                    info = zipfile.ZipInfo("zingg-duckdb/" + entry["path"])
                    info.external_attr = 0o100644 << 16
                    archive.writestr(info, payload)
                info = zipfile.ZipInfo("zingg-duckdb/SOURCE-MANIFEST.json")
                info.external_attr = 0o100644 << 16
                archive.writestr(info, json.dumps({
                    "schema": "zingg-duckdb-source-archive-1", "files": source_entries
                }))
            with self.assertRaisesRegex(ValueError, "source hash mismatch: README.md"):
                VERIFY.verify_structure(bundle, require_jre=False, source_archive=archive_path)

    def test_require_jre_checks_platform_binary_version_and_legal_metadata(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            bundle = make_bundle(Path(directory))
            with self.assertRaisesRegex(ValueError, "required bundle file is missing"):
                VERIFY.verify_structure(bundle, require_jre=True, windows=False)
            for relative in (*VERIFY.JRE_FILES, "runtime/java/bin/java"):
                path = bundle / relative
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text("OpenJDK Runtime Environment 21", encoding="utf-8")
            if os.name != "nt":
                with self.assertRaisesRegex(ValueError, "not executable"):
                    VERIFY.verify_structure(bundle, require_jre=True, windows=False)
                (bundle / "runtime/java/bin/java").chmod(0o755)
            VERIFY.verify_structure(bundle, require_jre=True, windows=False)


if __name__ == "__main__":
    unittest.main()
