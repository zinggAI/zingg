"""Validate a distribution checksum manifest and its exact on-disk inventory."""
from __future__ import annotations

import hashlib
import os
import re
import sys
import unicodedata
from pathlib import Path, PurePosixPath


MAX_MANIFEST_BYTES = 16 * 1024 * 1024
MAX_FILES = 200_000
HASH_LINE = re.compile(r"^([0-9a-fA-F]{64})  (.+)$")
INVALID_WINDOWS = re.compile(r'[<>:"|?*]')
RESERVED_WINDOWS = {
    "CON", "PRN", "AUX", "NUL",
    *(f"COM{i}" for i in range(1, 10)),
    *(f"LPT{i}" for i in range(1, 10)),
}


def _portable_key(relative: str) -> str:
    for part in PurePosixPath(relative).parts:
        reserved_base = part.rstrip(" .").split(".", 1)[0].upper()
        if (not part or part.endswith((".", " ")) or INVALID_WINDOWS.search(part)
                or reserved_base in RESERVED_WINDOWS):
            raise ValueError(f"non-portable checksum path: {relative!r}")
    return unicodedata.normalize("NFC", relative).casefold()


def _relative_path(raw: str) -> PurePosixPath:
    normalized = raw.replace("\\", "/")
    if not normalized or normalized.startswith("/") or re.match(r"^[A-Za-z]:", normalized):
        raise ValueError(f"unsafe checksum path: {raw!r}")
    parts = normalized.split("/")
    if any(part in ("", ".", "..") or ":" in part for part in parts) or normalized == "SHA256SUMS":
        raise ValueError(f"unsafe checksum path: {raw!r}")
    relative = PurePosixPath(*parts)
    _portable_key(relative.as_posix())
    return relative


def _checksums(manifest: Path) -> dict[str, str]:
    if manifest.is_symlink() or not manifest.is_file():
        raise ValueError("SHA256SUMS must be a regular file")
    if manifest.stat().st_size > MAX_MANIFEST_BYTES:
        raise ValueError("SHA256SUMS size limit exceeded")
    result: dict[str, str] = {}
    portable_names: set[str] = set()
    for line_number, line in enumerate(manifest.read_text(encoding="utf-8-sig").splitlines(), start=1):
        match = HASH_LINE.fullmatch(line)
        if match is None:
            raise ValueError(f"invalid SHA256SUMS line {line_number}")
        digest, raw = match.groups()
        relative = _relative_path(raw).as_posix()
        if relative in result:
            raise ValueError(f"duplicate SHA256SUMS path: {relative}")
        portable_name = _portable_key(relative)
        if portable_name in portable_names:
            raise ValueError(f"checksum paths collide on case-insensitive filesystems: {relative}")
        portable_names.add(portable_name)
        result[relative] = digest.lower()
        if len(result) > MAX_FILES:
            raise ValueError("SHA256SUMS file count limit exceeded")
    if not result:
        raise ValueError("SHA256SUMS contains no files")
    return result


def _file_inventory(root: Path) -> set[str]:
    result: set[str] = set()
    portable_names: set[str] = set()
    for current, directories, files in os.walk(root, followlinks=False):
        current_path = Path(current)
        for name in list(directories):
            path = current_path / name
            if path.is_symlink():
                raise ValueError(f"bundle contains a symbolic-link directory: {path.relative_to(root)}")
        for name in files:
            path = current_path / name
            relative = path.relative_to(root).as_posix()
            if path.is_symlink():
                resolved = path.resolve(strict=False)
                if root not in resolved.parents or not resolved.is_file():
                    raise ValueError(f"bundle contains an unsafe symbolic link: {relative}")
                result.add(relative)
            elif path.is_file():
                result.add(relative)
            else:
                raise ValueError(f"bundle contains a non-regular file: {relative}")
            portable_name = _portable_key(relative)
            if portable_name in portable_names:
                raise ValueError(f"bundle contains paths that collide on case-insensitive filesystems: {relative}")
            portable_names.add(portable_name)
    return result


def _hash_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def verify(bundle_path: str) -> None:
    root = Path(bundle_path).resolve(strict=True)
    if not root.is_dir():
        raise ValueError("bundle path must be a directory")
    manifest = root / "SHA256SUMS"
    expected = _checksums(manifest)
    actual = _file_inventory(root) - {"SHA256SUMS"}
    if actual != set(expected):
        missing, extra = sorted(set(expected) - actual), sorted(actual - set(expected))
        raise ValueError(f"bundle inventory differs (missing={missing[:3]}, extra={extra[:3]})")
    for relative, digest in expected.items():
        path = root.joinpath(*PurePosixPath(relative).parts)
        resolved = path.resolve(strict=True)
        if root not in resolved.parents or not resolved.is_file():
            raise ValueError(f"checksum path is not a regular file inside bundle: {relative}")
        if _hash_file(resolved) != digest:
            raise ValueError(f"checksum mismatch: {relative}")


if __name__ == "__main__":
    try:
        if len(sys.argv) != 2:
            raise ValueError("usage: verify_bundle_inventory.py BUNDLE")
        verify(sys.argv[1])
    except (OSError, UnicodeError, ValueError) as error:
        print(f"PACKAGE_INVENTORY_VERIFY_FAILED: {error}", file=sys.stderr)
        raise SystemExit(1)
    print("PACKAGE_INVENTORY_VERIFY_SUCCESS")
