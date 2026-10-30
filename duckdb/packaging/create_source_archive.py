"""Create a portable, byte-reproducible source archive for any platform."""
from __future__ import annotations

import hashlib
import json
import os
import re
import stat
import sys
import tempfile
import unicodedata
import zipfile
from pathlib import Path


SCHEMA = "zingg-duckdb-source-archive-1"
PROFILE = "zingg-0.7.0-duckdb-1.5.6.0"
ARCHIVE_ROOT = "zingg-duckdb"
MAX_FILES = 200_000
MAX_TOTAL_BYTES = 4 * 1024 * 1024 * 1024
EXCLUDED_DIRS = {".git", "target", "build", "dist", "__pycache__", ".pytest_cache"}
INVALID_WINDOWS = re.compile(r'[<>:"|?*]')
RESERVED_WINDOWS = {"CON", "PRN", "AUX", "NUL", *(f"COM{i}" for i in range(1, 10)), *(f"LPT{i}" for i in range(1, 10))}


def _validate_relative(relative: Path) -> str:
    parts = relative.parts
    for part in parts:
        reserved_base = part.rstrip(" .").split(".", 1)[0].upper()
        if (not part or part in {".", ".."} or part.endswith((".", " "))
                or INVALID_WINDOWS.search(part) or reserved_base in RESERVED_WINDOWS):
            raise ValueError(f"source path is not portable: {relative.as_posix()}")
    normalized = unicodedata.normalize("NFC", relative.as_posix())
    return normalized.casefold()


def _source_files(root: Path, output: Path) -> list[tuple[str, Path, int, str]]:
    files: list[tuple[str, Path, int, str]] = []
    folded: set[str] = set()
    total = 0
    for current, directories, names in os.walk(root, followlinks=False):
        current_path = Path(current)
        kept_directories: list[str] = []
        for name in sorted(directories):
            directory = current_path / name
            relative = directory.relative_to(root)
            if any(part in EXCLUDED_DIRS or part.startswith("dist-") for part in relative.parts):
                continue
            if directory.is_symlink():
                raise ValueError(f"source tree contains a symbolic-link directory: {relative.as_posix()}")
            _validate_relative(relative)
            kept_directories.append(name)
        directories[:] = kept_directories
        for name in sorted(names):
            path = current_path / name
            relative = path.relative_to(root)
            if any(part in EXCLUDED_DIRS or part.startswith("dist-") for part in relative.parts):
                continue
            if path.resolve(strict=False) == output:
                continue
            portable_key = _validate_relative(relative)
            if portable_key in folded:
                raise ValueError(f"source paths collide on case-insensitive filesystems: {relative.as_posix()}")
            folded.add(portable_key)
            if path.is_symlink():
                raise ValueError(f"source tree contains a symbolic link: {relative.as_posix()}")
            if not path.is_file():
                raise ValueError(f"source tree contains a non-regular file: {relative.as_posix()}")
            size = path.stat().st_size
            total += size
            if total > MAX_TOTAL_BYTES:
                raise ValueError("source archive input size limit exceeded")
            if len(files) >= MAX_FILES:
                raise ValueError("source archive file count limit exceeded")
            files.append((relative.as_posix(), path, size, portable_key))
    if not files:
        raise ValueError("source tree contains no files")
    files.sort(key=lambda item: item[0])
    return files


def _file_digest(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def _zip_info(name: str, mode: int, size: int = 0) -> zipfile.ZipInfo:
    info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
    info.create_system = 3
    info.create_version = 20
    info.extract_version = 20
    info.external_attr = (stat.S_IFREG | mode) << 16
    info.internal_attr = 0
    info.compress_type = zipfile.ZIP_STORED
    info.file_size = size
    info.extra = b""
    info.comment = b""
    return info


def create(root_arg: str, output_arg: str) -> Path:
    root = Path(root_arg).resolve(strict=True)
    if not root.is_dir():
        raise ValueError("source root must be a directory")
    output = Path(output_arg)
    if not output.is_absolute():
        output = root / output
    output = output.absolute()
    output.parent.mkdir(parents=True, exist_ok=True)
    output = output.parent.resolve(strict=True) / output.name
    if output == root:
        raise ValueError("source archive output must be a file outside the source root directory")
    if root in output.parents:
        relative_output = output.relative_to(root)
        if not any(part == "dist" or part.startswith("dist-") for part in relative_output.parts):
            raise ValueError("source archive output inside the source root must be under an excluded dist directory")

    source_files = _source_files(root, output)
    entries = [{"path": relative, "sha256": _file_digest(path)} for relative, path, _, _ in source_files]
    manifest = {
        "schema": SCHEMA,
        "compatibilityProfile": PROFILE,
        "files": entries,
    }
    manifest_bytes = (json.dumps(manifest, indent=2, ensure_ascii=False) + "\n").encode("utf-8")
    fd, temporary_name = tempfile.mkstemp(prefix=".zingg-source-", suffix=".zip", dir=output.parent)
    os.close(fd)
    try:
        with zipfile.ZipFile(temporary_name, "w", compression=zipfile.ZIP_STORED, allowZip64=True) as archive:
            archive.comment = b""
            for relative, path, size, _ in source_files:
                mode = 0o755 if path.name == "mvnw" or path.suffix == ".sh" else 0o644
                info = _zip_info(f"{ARCHIVE_ROOT}/{relative}", mode, size)
                with path.open("rb") as source, archive.open(info, "w", force_zip64=size >= zipfile.ZIP64_LIMIT) as target:
                    while chunk := source.read(1024 * 1024):
                        target.write(chunk)
            archive.writestr(
                _zip_info(f"{ARCHIVE_ROOT}/SOURCE-MANIFEST.json", 0o644, len(manifest_bytes)),
                manifest_bytes,
            )
        with open(temporary_name, "rb+") as built:
            os.fsync(built.fileno())
        os.replace(temporary_name, output)
    except Exception:
        try:
            os.unlink(temporary_name)
        except FileNotFoundError:
            pass
        raise
    return output


if __name__ == "__main__":
    try:
        if len(sys.argv) != 3:
            raise ValueError("usage: create_source_archive.py ROOT OUTPUT.zip")
        created = create(sys.argv[1], sys.argv[2])
    except (OSError, UnicodeError, ValueError, zipfile.BadZipFile) as error:
        print(f"SOURCE_ARCHIVE_CREATE_FAILED: {error}", file=sys.stderr)
        raise SystemExit(1)
    print(f"SOURCE_ARCHIVE_CREATE_SUCCESS {created}")
