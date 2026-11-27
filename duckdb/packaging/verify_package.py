#!/usr/bin/env python3
"""Cross-platform structural release-bundle contract checks."""
from __future__ import annotations

import json
import hashlib
import importlib.util
import os
import re
import sys
import zipfile
from pathlib import Path, PurePosixPath
from typing import Any


STATIC_FILES = (
    "SHA256SUMS",
    "sbom.spdx.json",
    "licenses/third-party/DEPENDENCY-NOTICES.json",
    "config/runtime-manifest.json",
    "config/compatibility-profile.json",
    "config/compatibility-capsule.json",
    "config/release-policy.json",
    "config/source-provenance.json",
    "worker/runtime-worker-0.1.0-SNAPSHOT.jar",
    "legacy/legacy-blocking-import-zingg07-spark35-0.1.0-SNAPSHOT.jar",
    "legacy/legacy-classifier-import-zingg07-spark35-0.1.0-SNAPSHOT.jar",
    "python/zingg_duckdb/__init__.py",
    "python/zingg_duckdb/client.py",
    "python/zingg_duckdb/worker.py",
    "bin/zingg-duckdb.cmd",
    "bin/zingg-duckdb.ps1",
    "bin/zingg-duckdb.sh",
    "packaging/invoke-legacy-import.ps1",
    "packaging/sign-package.ps1",
    "packaging/dependency_licenses.py",
    "packaging/jdk_legal.py",
    "packaging/verify_bundle_inventory.py",
    "packaging/verify_package.py",
    "packaging/verify_source_archive.py",
    "packaging/verify_spdx.py",
)
JRE_FILES = (
    "runtime/java/JAVA-VERSION.txt",
    "runtime/java/JDK-PROVENANCE.json",
    "runtime/java/JDK-LEGAL-MANIFEST.json",
    "runtime/java/legal/java.base/LICENSE",
    "runtime/java/legal/java.base/ADDITIONAL_LICENSE_INFO",
)


def _unique_object(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
    result: dict[str, Any] = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f"duplicate JSON key: {key}")
        result[key] = value
    return result


def _json(path: Path) -> Any:
    try:
        source = path.read_text(encoding="utf-8")
    except (OSError, UnicodeError) as error:
        raise ValueError(f"invalid JSON document: {path.name}") from error
    try:
        return json.loads(source, object_pairs_hook=_unique_object)
    except json.JSONDecodeError as error:
        raise ValueError(f"invalid JSON document: {path.name}") from error


def verify_source_archive_provenance(provenance: dict[str, Any], archive_path: Path) -> None:
    verifier_path = Path(__file__).with_name("verify_source_archive.py")
    spec = importlib.util.spec_from_file_location("verify_source_archive", verifier_path)
    if spec is None or spec.loader is None:
        raise ValueError("source archive verifier is unavailable")
    verifier = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(verifier)
    verifier.verify(str(archive_path))
    with zipfile.ZipFile(archive_path) as archive:
        manifests = [name for name in archive.namelist() if PurePosixPath(name).name == "SOURCE-MANIFEST.json"]
        if len(manifests) != 1:
            raise ValueError("source archive manifest must occur exactly once")
        manifest = json.loads(archive.read(manifests[0]), object_pairs_hook=_unique_object)
    source_files = manifest.get("files") if isinstance(manifest, dict) else None
    if not isinstance(source_files, list):
        raise ValueError("source archive manifest has no file inventory")
    source_hashes: dict[str, str] = {}
    for entry in source_files:
        if not isinstance(entry, dict) or not isinstance(entry.get("path"), str):
            raise ValueError("invalid source archive manifest entry")
        source_hashes[entry["path"]] = entry["sha256"]
    worker_source = "runtime-worker/target/runtime-worker-0.1.0-SNAPSHOT.jar"
    for entry in provenance["files"]:
        relative = entry["path"]
        if relative == worker_source:
            continue
        expected = source_hashes.get(relative)
        if expected is None:
            raise ValueError(f"provenance source is absent from source archive: {relative}")
        if expected != entry["sha256"].lower():
            raise ValueError(f"source archive provenance hash mismatch: {relative}")


def verify_structure(bundle: Path, require_jre: bool, windows: bool | None = None,
                     source_archive: Path | None = None) -> None:
    root = bundle.resolve(strict=True)
    if not root.is_dir():
        raise ValueError("bundle path is not a directory")
    if windows is None:
        windows = os.name == "nt"
    required = list(STATIC_FILES)
    if require_jre:
        required.extend(("runtime/java/bin/java.exe" if windows else "runtime/java/bin/java", *JRE_FILES))
    for relative in required:
        if not (root / relative).is_file():
            raise ValueError(f"required bundle file is missing: {relative}")
    if require_jre and not windows and not os.access(root / "runtime/java/bin/java", os.X_OK):
        raise ValueError("bundled Java executable is not executable")

    profile = _json(root / "config/compatibility-profile.json")
    capsule = _json(root / "config/compatibility-capsule.json")
    policy = _json(root / "config/release-policy.json")
    provenance = _json(root / "config/source-provenance.json")
    runtime = _json(root / "config/runtime-manifest.json")

    if (not isinstance(profile, dict)
            or profile.get("id") != "zingg-0.7.0-duckdb-1.5.6.0"
            or profile.get("duckdbJdbc") != "1.5.6.0"):
        raise ValueError("unexpected compatibility profile")
    rules = profile.get("rules")
    if not isinstance(rules, dict) or not rules.get("similarities"):
        raise ValueError("similarity registry declaration is missing")
    if (not isinstance(capsule, dict)
            or capsule.get("schema") != "zingg-duckdb-compatibility-capsule-1"
            or capsule.get("profile") != profile["id"]
            or not capsule.get("notices")
            or not isinstance(capsule.get("patches"), list)):
        raise ValueError("compatibility capsule is incomplete")
    if (not isinstance(policy, dict)
            or policy.get("schema") != "zingg-duckdb-release-policy-1"
            or not isinstance(policy.get("jdk"), dict)
            or policy["jdk"].get("major") != 21
            or policy["jdk"].get("provenanceRequired") is not True
            or not isinstance(policy.get("signing"), dict)
            or policy["signing"].get("authenticodeRequiredForRelease") is not True):
        raise ValueError("release policy is incomplete")
    if (not isinstance(provenance, dict)
            or provenance.get("schema") != "zingg-duckdb-provenance-1"
            or not isinstance(provenance.get("files"), list)
            or not provenance["files"]
            or provenance.get("compatibilityProfile") != profile["id"]
            or not isinstance(provenance.get("workerArtifactSha256"), str)
            or not re.fullmatch(r"[0-9a-fA-F]{64}", provenance["workerArtifactSha256"])):
        raise ValueError("source provenance is incomplete")
    paths: set[str] = set()
    recorded_hashes: dict[str, str] = {}
    for entry in provenance["files"]:
        if (not isinstance(entry, dict) or set(entry) != {"path", "sha256"}
                or not isinstance(entry["path"], str)
                or not isinstance(entry["sha256"], str)
                or not re.fullmatch(r"[0-9a-fA-F]{64}", entry["sha256"])):
            raise ValueError("source provenance file entry is malformed")
        source_path = entry["path"]
        normalized = PurePosixPath(source_path)
        if ("\\" in source_path or ":" in source_path or normalized.is_absolute()
                or normalized.as_posix() != source_path
                or any(part in ("", ".", "..") for part in normalized.parts)
                or any(ord(character) < 0x20 for character in source_path)):
            raise ValueError(f"unsafe source provenance path: {source_path}")
        collision_key = source_path.casefold()
        if collision_key in paths:
            raise ValueError(f"duplicate source provenance path: {source_path}")
        paths.add(collision_key)
        recorded_hashes[source_path] = entry["sha256"].lower()
    required_sources = {
        "pom.xml", "dependency-lock.json", "README.md", "NOTICE.md", "LICENSE",
        "runtime-worker/target/runtime-worker-0.1.0-SNAPSHOT.jar",
    }
    if not required_sources.issubset(recorded_hashes):
        raise ValueError("source provenance inventory is missing required source entries")
    worker_hash = hashlib.sha256(
        (root / "worker/runtime-worker-0.1.0-SNAPSHOT.jar").read_bytes()
    ).hexdigest()
    if (provenance["workerArtifactSha256"].lower() != worker_hash
            or recorded_hashes["runtime-worker/target/runtime-worker-0.1.0-SNAPSHOT.jar"] != worker_hash):
        raise ValueError("worker artifact provenance checksum mismatch")
    if source_archive is not None:
        if not source_archive.is_file():
            raise ValueError(f"source archive does not exist: {source_archive}")
        verify_source_archive_provenance(provenance, source_archive)
    java = runtime.get("java") if isinstance(runtime, dict) else None
    if (not isinstance(runtime, dict)
            or runtime.get("compatibilityProfile") != profile["id"]
            or runtime.get("duckdbJdbc") != "1.5.6.0"):
        raise ValueError("runtime manifest does not match active compatibility profile")
    if not isinstance(java, dict) or java.get("major") != 21 or java.get("bundled") is not True:
        raise ValueError("runtime manifest must declare bundled Java 21")
    if require_jre:
        version = (root / "runtime/java/JAVA-VERSION.txt").read_text(encoding="utf-8").strip()
        if not version or not re.search(r"(?:^|\D)21(?:\D|$)", version):
            raise ValueError("embedded runtime must declare Java 21")

    for current, directories, _ in os.walk(root):
        if "__pycache__" in directories:
            raise ValueError("__pycache__ must not be present in bundle")


def main() -> int:
    import argparse

    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("bundle", type=Path)
    parser.add_argument("--require-jre", action="store_true")
    parser.add_argument("--source-archive", type=Path)
    args = parser.parse_args()
    try:
        verify_structure(args.bundle, args.require_jre, source_archive=args.source_archive)
        print("PACKAGE_STRUCTURE_SUCCESS")
        return 0
    except (OSError, ValueError, KeyError, zipfile.BadZipFile) as error:
        print(f"package structure verification failed: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
