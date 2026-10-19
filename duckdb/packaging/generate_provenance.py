#!/usr/bin/env python3
"""Generate deterministic cross-platform source and worker provenance."""
from __future__ import annotations

import hashlib
import json
import sys
from pathlib import Path


FIXED_FILES = (
    "pom.xml",
    "dependency-lock.json",
    "README.md",
    "NOTICE.md",
    "LICENSE",
    "runtime-worker/target/runtime-worker-0.1.0-SNAPSHOT.jar",
)


def generate(root: Path, output: Path) -> None:
    root = root.resolve(strict=True)
    if not root.is_dir():
        raise ValueError("source root must be a directory")
    relative_files = set(FIXED_FILES)
    for child in root.iterdir():
        if child.is_dir() and not child.is_symlink() and (child / "pom.xml").is_file():
            relative_files.add(f"{child.name}/pom.xml")
            if (child / "dependency-lock.json").is_file():
                relative_files.add(f"{child.name}/dependency-lock.json")

    records: list[dict[str, str]] = []
    for relative in sorted(relative_files):
        source = root / relative
        if not source.is_file():
            continue
        resolved = source.resolve(strict=True)
        if source.is_symlink() or root not in resolved.parents:
            raise ValueError(f"provenance source is an unsafe link: {relative}")
        records.append({"path": relative, "sha256": hashlib.sha256(resolved.read_bytes()).hexdigest()})

    worker_path = "runtime-worker/target/runtime-worker-0.1.0-SNAPSHOT.jar"
    worker_hash = next((entry["sha256"] for entry in records if entry["path"] == worker_path), "")
    if not worker_hash:
        raise ValueError("worker artifact is missing from source provenance")
    document = {
        "schema": "zingg-duckdb-provenance-1",
        "compatibilityProfile": "zingg-0.7.0-duckdb-1.5.6.0",
        "workerArtifactSha256": worker_hash,
        "files": records,
    }
    output.parent.mkdir(parents=True, exist_ok=True)
    with output.open("w", encoding="utf-8", newline="\n") as stream:
        stream.write(json.dumps(document, indent=2, ensure_ascii=False) + "\n")


def main() -> int:
    if len(sys.argv) != 3:
        print("usage: generate_provenance.py SOURCE_ROOT OUTPUT", file=sys.stderr)
        return 2
    try:
        generate(Path(sys.argv[1]), Path(sys.argv[2]))
        print("PROVENANCE_GENERATION_SUCCESS")
        return 0
    except (OSError, UnicodeError, ValueError) as error:
        print(f"provenance generation failed: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
