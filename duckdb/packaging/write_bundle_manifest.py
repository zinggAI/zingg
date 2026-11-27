"""Write portable SHA256SUMS for every regular bundle file, including safe symlink aliases."""
from __future__ import annotations

import hashlib
import os
import re
import sys
import tempfile
import unicodedata
from pathlib import Path


INVALID_WINDOWS = re.compile(r'[<>:"|?*]')
RESERVED_WINDOWS = {
    "CON", "PRN", "AUX", "NUL",
    *(f"COM{i}" for i in range(1, 10)),
    *(f"LPT{i}" for i in range(1, 10)),
}


def _portable_key(relative: str) -> str:
    for part in Path(relative).parts:
        reserved_base = part.rstrip(" .").split(".", 1)[0].upper()
        if (not part or part.endswith((".", " ")) or INVALID_WINDOWS.search(part)
                or reserved_base in RESERVED_WINDOWS):
            raise ValueError(f"bundle path is not portable to Windows: {relative}")
    return unicodedata.normalize("NFC", relative.replace("\\", "/")).casefold()


def _digest(path: Path) -> str:
    value = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def write(bundle_arg: str) -> None:
    bundle = Path(bundle_arg).resolve(strict=True)
    if not bundle.is_dir():
        raise ValueError("bundle must be a directory")
    entries: list[tuple[str, str]] = []
    folded: set[str] = set()
    for current, directories, files in os.walk(bundle, followlinks=False):
        current_path = Path(current)
        for name in list(directories):
            if (current_path / name).is_symlink():
                raise ValueError(f"bundle contains a symbolic-link directory: {(current_path / name).relative_to(bundle)}")
        for name in files:
            path = current_path / name
            relative = path.relative_to(bundle).as_posix()
            if relative == "SHA256SUMS":
                if path.is_symlink():
                    raise ValueError("SHA256SUMS must not be a symbolic link")
                continue
            resolved = path.resolve(strict=True)
            if path.is_symlink() and bundle not in resolved.parents:
                raise ValueError(f"bundle contains an external symbolic link: {relative}")
            if not resolved.is_file():
                raise ValueError(f"bundle contains a non-regular file: {relative}")
            key = _portable_key(relative)
            if key in folded:
                raise ValueError(f"bundle paths collide on case-insensitive filesystems: {relative}")
            folded.add(key)
            entries.append((relative, _digest(resolved)))
    if not entries:
        raise ValueError("bundle contains no files")
    destination = bundle / "SHA256SUMS"
    fd, temporary_name = tempfile.mkstemp(prefix=".SHA256SUMS-", suffix=".tmp", dir=bundle)
    try:
        with os.fdopen(fd, "w", encoding="utf-8", newline="\n") as output:
            for relative, digest in sorted(entries):
                output.write(f"{digest}  {relative}\n")
            output.flush()
            os.fsync(output.fileno())
        os.replace(temporary_name, destination)
    except Exception:
        try:
            os.unlink(temporary_name)
        except FileNotFoundError:
            pass
        raise


if __name__ == "__main__":
    try:
        if len(sys.argv) != 2:
            raise ValueError("usage: write_bundle_manifest.py BUNDLE")
        write(sys.argv[1])
    except (OSError, UnicodeError, ValueError) as error:
        print(f"PACKAGE_MANIFEST_WRITE_FAILED: {error}", file=sys.stderr)
        raise SystemExit(1)
    print("PACKAGE_MANIFEST_WRITE_SUCCESS")
