"""Build a platform-specific GraalVM configuration directory from checked-in metadata."""
from __future__ import annotations

import argparse
import json
import platform
import re
import shutil
from pathlib import Path


def duckdb_library(os_name: str, machine: str) -> str:
    os_name = os_name.lower()
    machine = machine.lower()
    if os_name.startswith("linux"):
        if machine in {"x86_64", "amd64"}:
            return "libduckdb_java.so_linux_amd64"
        if machine in {"aarch64", "arm64"}:
            return "libduckdb_java.so_linux_arm64"
    elif os_name in {"darwin", "mac", "macos"} and machine in {"x86_64", "amd64", "aarch64", "arm64"}:
        return "libduckdb_java.so_osx_universal"
    elif os_name.startswith("win") and machine in {"x86_64", "amd64"}:
        return "libduckdb_java.so_windows_amd64"
    raise ValueError(f"unsupported Native Image target: {os_name}/{machine}")


def prepare(source: Path, output: Path, os_name: str, machine: str) -> Path:
    source = source.resolve(strict=True)
    if not source.is_dir():
        raise ValueError(f"Native Image metadata source is not a directory: {source}")
    if output.exists():
        raise FileExistsError(f"refusing to overwrite Native Image metadata output: {output}")
    native_library = duckdb_library(os_name, machine)
    original_resource_config = source / "resource-config.json"
    if not original_resource_config.is_file():
        raise ValueError("Native Image resource-config.json is missing")
    document = json.loads(original_resource_config.read_text(encoding="utf-8"))
    includes = document.get("resources", {}).get("includes")
    if not isinstance(includes, list):
        raise ValueError("Native Image resource config must contain resources.includes")
    kept = [item for item in includes if isinstance(item, dict)
            and isinstance(item.get("pattern"), str)
            and "libduckdb_java" not in item["pattern"]]
    kept.append({"pattern": ".*" + re.escape(native_library) + "$"})
    document["resources"]["includes"] = kept

    output.mkdir(parents=True)
    for item in source.iterdir():
        if item.is_file() and item.name != "resource-config.json":
            shutil.copy2(item, output / item.name)
    (output / "resource-config.json").write_text(
        json.dumps(document, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return output


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--os", default=platform.system())
    parser.add_argument("--machine", default=platform.machine())
    args = parser.parse_args()
    result = prepare(args.source, args.output, args.os, args.machine)
    document = json.loads((result / "resource-config.json").read_text(encoding="utf-8"))
    native = next(item["pattern"] for item in document["resources"]["includes"]
                  if "libduckdb_java" in item["pattern"])
    print(f"NATIVE_IMAGE_CONFIG_SUCCESS target={args.os}/{args.machine} resource={native}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
