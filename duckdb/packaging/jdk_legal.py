"""Record and verify vendor/build metadata and legal files embedded by jlink."""
from __future__ import annotations

import hashlib
import json
import os
import sys
from pathlib import Path, PurePosixPath
from typing import Any


LEGAL_INDEX = "JDK-LEGAL-MANIFEST.json"
PROVENANCE = "JDK-PROVENANCE.json"


def _digest(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def collect(jdk_home: Path, runtime_root: Path, java_name: str) -> tuple[dict[str, Any], dict[str, Any]]:
    release_path = jdk_home / "release"
    release_bytes = release_path.read_bytes()
    properties: dict[str, str] = {}
    for line in release_bytes.decode("utf-8").splitlines():
        if "=" not in line or line.lstrip().startswith("#"):
            continue
        key, value = line.split("=", 1)
        properties[key] = value.strip().strip('"')
    version = properties.get("JAVA_VERSION", "")
    vendor = properties.get("IMPLEMENTOR", "")
    if not version.startswith("21.") or not vendor:
        raise ValueError("JDK release file must identify Java 21 and a non-empty IMPLEMENTOR vendor")
    java_path = runtime_root / "bin" / java_name
    java_hash = _digest(java_path.read_bytes())
    legal_root = runtime_root / "legal"
    if not legal_root.is_dir():
        raise ValueError("jlink runtime is missing its legal/ directory")
    resources = []
    for path in sorted(legal_root.rglob("*")):
        if path.is_symlink():
            resolved = path.resolve(strict=True)
            if not resolved.is_relative_to(legal_root.resolve()) or not resolved.is_file():
                raise ValueError(f"JDK legal symlink escapes legal/ or is not a file: {path}")
            kind = "symlink"
            link_target = os.readlink(path)
            payload = resolved.read_bytes()
        elif path.is_file():
            kind = "file"
            link_target = None
            payload = path.read_bytes()
        else:
            continue
        if kind:
            relative = path.relative_to(runtime_root).as_posix()
            record = {"path": relative, "type": kind, "sha256": _digest(payload), "size": len(payload)}
            if link_target is not None:
                record["linkTarget"] = link_target
            resources.append(record)
    if not resources:
        raise ValueError("jlink runtime legal/ directory contains no files")
    legal_manifest = {
        "schema": "zingg-duckdb-jdk-legal-1",
        "description": "Exact hash inventory of legal resources retained by jlink; this is not vendor license approval.",
        "resourceCount": len(resources),
        "resources": resources,
    }
    provenance = {
        "schema": "zingg-duckdb-jdk-provenance-1",
        "javaMajor": 21,
        "javaVendor": vendor,
        "javaVersion": version,
        "javaImplementorVersion": properties.get("IMPLEMENTOR_VERSION", ""),
        "osArch": properties.get("OS_ARCH", ""),
        "javaExecutable": f"bin/{java_name}",
        "javaSha256": java_hash,
        "releaseSha256": _digest(release_bytes),
        "legalManifest": LEGAL_INDEX,
    }
    (runtime_root / LEGAL_INDEX).write_text(json.dumps(legal_manifest, indent=2) + "\n", encoding="utf-8")
    (runtime_root / PROVENANCE).write_text(json.dumps(provenance, indent=2) + "\n", encoding="utf-8")
    return provenance, legal_manifest


def verify(runtime_root: Path) -> tuple[dict[str, Any], dict[str, Any]]:
    try:
        provenance = json.loads((runtime_root / PROVENANCE).read_text(encoding="utf-8"))
        manifest = json.loads((runtime_root / LEGAL_INDEX).read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        raise ValueError(f"cannot read JDK provenance/legal manifest: {error}") from error
    if (not isinstance(provenance, dict) or provenance.get("schema") != "zingg-duckdb-jdk-provenance-1"
            or provenance.get("javaMajor") != 21 or not provenance.get("javaVendor")
            or not provenance.get("javaVersion", "").startswith("21.")
            or not provenance.get("javaSha256") or not provenance.get("releaseSha256")
            or provenance.get("legalManifest") != LEGAL_INDEX):
        raise ValueError("JDK provenance is incomplete")
    if not isinstance(manifest, dict) or manifest.get("schema") != "zingg-duckdb-jdk-legal-1":
        raise ValueError("JDK legal manifest schema is invalid")
    resources = manifest.get("resources")
    if not isinstance(resources, list) or not resources or manifest.get("resourceCount") != len(resources):
        raise ValueError("JDK legal manifest resource count is invalid")
    listed: set[str] = set()
    for record in resources:
        if not isinstance(record, dict) or not isinstance(record.get("path"), str):
            raise ValueError("JDK legal resource record is invalid")
        relative = PurePosixPath(record["path"])
        if relative.is_absolute() or ".." in relative.parts or "\\" in record["path"] or not relative.parts:
            raise ValueError(f"unsafe JDK legal resource path: {record.get('path')!r}")
        name = relative.as_posix()
        if not name.startswith("legal/") or name in listed:
            raise ValueError(f"duplicate or out-of-scope JDK legal path: {name}")
        listed.add(name)
        path = runtime_root.joinpath(*relative.parts)
        kind = record.get("type")
        if kind == "symlink":
            if not path.is_symlink() or os.readlink(path) != record.get("linkTarget"):
                raise ValueError(f"JDK legal symlink is missing or its target changed: {name}")
            resolved = path.resolve(strict=True)
            if not resolved.is_relative_to((runtime_root / "legal").resolve()) or not resolved.is_file():
                raise ValueError(f"JDK legal symlink escapes legal/ or is not a file: {name}")
            content = resolved.read_bytes()
        elif kind == "file":
            if path.is_symlink() or not path.is_file():
                raise ValueError(f"JDK legal resource is missing or not a regular file: {name}")
            content = path.read_bytes()
        else:
            raise ValueError(f"invalid JDK legal resource type: {kind!r}")
        if _digest(content) != record.get("sha256") or len(content) != record.get("size"):
            raise ValueError(f"JDK legal resource hash/size mismatch: {name}")
    actual = {path.relative_to(runtime_root).as_posix() for path in (runtime_root / "legal").rglob("*")
              if path.is_symlink() or path.is_file()}
    if actual != listed:
        raise ValueError(f"JDK legal inventory mismatch: unlisted={sorted(actual - listed)}, missing={sorted(listed - actual)}")
    executable = runtime_root / str(provenance.get("javaExecutable", ""))
    if not executable.is_file() or _digest(executable.read_bytes()) != provenance["javaSha256"]:
        raise ValueError("bundled Java executable does not match recorded SHA-256")
    return provenance, manifest


def main(argv: list[str]) -> int:
    if len(argv) < 2 or argv[1] not in {"collect", "verify"}:
        print("usage: jdk_legal.py collect <jdk-home> <runtime-root> <java-name> | verify <runtime-root>", file=sys.stderr)
        return 2
    if (argv[1] == "collect" and len(argv) != 5) or (argv[1] == "verify" and len(argv) != 3):
        print("usage: jdk_legal.py collect <jdk-home> <runtime-root> <java-name> | verify <runtime-root>", file=sys.stderr)
        return 2
    try:
        if argv[1] == "collect":
            provenance, manifest = collect(Path(argv[2]), Path(argv[3]), argv[4])
            print(f"JDK_LEGAL_COLLECT_SUCCESS vendor={provenance['javaVendor']} version={provenance['javaVersion']} resources={manifest['resourceCount']}")
        else:
            provenance, manifest = verify(Path(argv[2]))
            print(f"JDK_LEGAL_VERIFY_SUCCESS vendor={provenance['javaVendor']} version={provenance['javaVersion']} resources={manifest['resourceCount']}")
    except (OSError, UnicodeError, ValueError) as error:
        print(f"JDK legal inventory failed: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
