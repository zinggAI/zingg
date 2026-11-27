"""Preserve and verify legal resources from the staged runtime dependency JARs."""
from __future__ import annotations

import hashlib
import json
import sys
import zipfile
from pathlib import Path, PurePosixPath
from typing import Any


INDEX = "DEPENDENCY-NOTICES.json"


def _sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def _is_legal_resource(name: str) -> bool:
    path = PurePosixPath(name)
    if path.is_absolute() or ".." in path.parts or "\\" in name:
        return False
    leaf = path.name.lower()
    if leaf in {"license", "licence", "notice", "copying", "copyright"}:
        return True
    if any(leaf.startswith(prefix + suffix) for prefix in ("license", "licence", "notice", "copying", "copyright")
           for suffix in (".", "-", "_")):
        return True
    return name.lower().startswith("meta-inf/licenses/")


def collect(jar_root: Path, output_root: Path) -> dict[str, Any]:
    jars = sorted(jar_root.rglob("*.jar"))
    if not jars:
        raise ValueError(f"no staged runtime dependency JARs found under {jar_root}")
    if output_root.exists() and any(output_root.iterdir()):
        raise ValueError(f"refusing to collect into a non-empty output directory: {output_root}")
    output_root.mkdir(parents=True, exist_ok=True)
    records: list[dict[str, str]] = []
    for jar in jars:
        relative_jar = jar.relative_to(jar_root).as_posix()
        artifact_root = Path(*PurePosixPath(relative_jar).parts).with_suffix("")
        try:
            with zipfile.ZipFile(jar) as archive:
                for entry in sorted(archive.infolist(), key=lambda item: item.filename):
                    name = entry.filename
                    if entry.is_dir() or not _is_legal_resource(name):
                        continue
                    payload = archive.read(entry)
                    destination_relative = artifact_root / Path(*PurePosixPath(name).parts)
                    destination = output_root / destination_relative
                    destination.parent.mkdir(parents=True, exist_ok=True)
                    destination.write_bytes(payload)
                    records.append({
                        "sourceJar": relative_jar,
                        "sourceEntry": name,
                        "path": destination_relative.as_posix(),
                        "sha256": _sha256(payload),
                        "size": str(len(payload)),
                    })
        except (OSError, zipfile.BadZipFile, RuntimeError) as error:
            raise ValueError(f"cannot read staged dependency JAR {jar}: {error}") from error
    if not records:
        raise ValueError("no license, notice, copying, or copyright resources found in staged runtime JARs")
    records.sort(key=lambda row: (row["sourceJar"], row["sourceEntry"], row["path"]))
    document = {
        "schema": "zingg-duckdb-dependency-notices-1",
        "description": "Verbatim legal resources extracted from staged runtime dependency JARs; this index is an inventory, not a license approval.",
        "sourceJarCount": len(jars),
        "resourceCount": len(records),
        "resources": records,
    }
    (output_root / INDEX).write_text(json.dumps(document, indent=2) + "\n", encoding="utf-8")
    return document


def verify(output_root: Path) -> dict[str, Any]:
    index_path = output_root / INDEX
    try:
        document = json.loads(index_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        raise ValueError(f"cannot read dependency notice index: {error}") from error
    if not isinstance(document, dict) or document.get("schema") != "zingg-duckdb-dependency-notices-1":
        raise ValueError("dependency notice index schema is invalid")
    resources = document.get("resources")
    if not isinstance(resources, list) or not resources or document.get("resourceCount") != len(resources):
        raise ValueError("dependency notice index resource count is invalid")
    listed: set[str] = set()
    for record in resources:
        if not isinstance(record, dict):
            raise ValueError("dependency notice resource record must be an object")
        relative = record.get("path")
        path = PurePosixPath(relative) if isinstance(relative, str) else PurePosixPath("/")
        if path.is_absolute() or not path.parts or ".." in path.parts or "\\" in str(relative):
            raise ValueError(f"unsafe dependency notice path: {relative!r}")
        normalized = path.as_posix()
        if normalized in listed:
            raise ValueError(f"duplicate dependency notice path: {normalized}")
        listed.add(normalized)
        payload_path = output_root.joinpath(*path.parts)
        if not payload_path.is_file():
            raise ValueError(f"dependency notice resource is missing: {normalized}")
        payload = payload_path.read_bytes()
        if _sha256(payload) != record.get("sha256") or str(len(payload)) != record.get("size"):
            raise ValueError(f"dependency notice resource hash/size mismatch: {normalized}")
    actual = {path.relative_to(output_root).as_posix() for path in output_root.rglob("*") if path.is_file() and path.name != INDEX}
    if actual != listed:
        raise ValueError("dependency notice inventory does not exactly match files: "
                         f"unlisted={sorted(actual - listed)}, missing={sorted(listed - actual)}")
    return document


def main(argv: list[str]) -> int:
    if len(argv) < 2 or argv[1] not in {"collect", "verify"}:
        print("usage: dependency_licenses.py collect <staged-jars> <output-dir> | verify <output-dir>", file=sys.stderr)
        return 2
    if (argv[1] == "collect" and len(argv) != 4) or (argv[1] == "verify" and len(argv) != 3):
        print("usage: dependency_licenses.py collect <staged-jars> <output-dir> | verify <output-dir>", file=sys.stderr)
        return 2
    try:
        if argv[1] == "collect":
            document = collect(Path(argv[2]), Path(argv[3]))
        else:
            document = verify(Path(argv[2]))
    except (OSError, ValueError) as error:
        print(f"dependency notice processing failed: {error}", file=sys.stderr)
        return 1
    print(f"DEPENDENCY_NOTICES_{argv[1].upper()}_SUCCESS jars={document['sourceJarCount']} resources={document['resourceCount']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
