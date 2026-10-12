"""Validate a source ZIP without extracting untrusted paths to the filesystem."""
from __future__ import annotations

import hashlib
import json
import re
import stat
import sys
import unicodedata
import zipfile
from pathlib import PurePosixPath


MAX_ENTRIES = 200_000
MAX_UNCOMPRESSED_BYTES = 4 * 1024 * 1024 * 1024


def _unique_json_object(pairs: list[tuple[str, object]]) -> dict[str, object]:
    result: dict[str, object] = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f"duplicate source manifest key: {key}")
        result[key] = value
    return result


def _safe_name(name: str) -> PurePosixPath:
    parts = name.split("/")
    if name.endswith("/"):
        parts = parts[:-1]
    if not name or "\\" in name or name.startswith("/") or any(part in ("", ".", "..") for part in parts):
        raise ValueError(f"unsafe ZIP path: {name!r}")
    path = PurePosixPath(name)
    if any(part in ("", ".", "..") for part in path.parts):
        raise ValueError(f"unsafe ZIP path: {name!r}")
    if path.is_absolute() or (path.parts and path.parts[0].endswith(":")):
        raise ValueError(f"unsafe ZIP path: {name!r}")
    return path


def verify(archive_path: str) -> None:
    with zipfile.ZipFile(archive_path) as archive:
        infos = archive.infolist()
        if not infos or len(infos) > MAX_ENTRIES:
            raise ValueError("source archive entry count is invalid")
        by_name: dict[str, zipfile.ZipInfo] = {}
        total = 0
        for info in infos:
            path = _safe_name(info.filename.rstrip("/"))
            if info.filename in by_name:
                raise ValueError(f"duplicate ZIP entry: {info.filename}")
            if info.is_dir():
                raise ValueError(f"unexpected directory in source archive: {info.filename}")
            if info.flag_bits & 1:
                raise ValueError(f"encrypted ZIP entry is unsupported: {info.filename}")
            mode = info.external_attr >> 16
            if stat.S_ISLNK(mode):
                raise ValueError(f"symbolic link in source archive: {info.filename}")
            if not info.is_dir() and not (stat.S_ISREG(mode) or mode == 0):
                raise ValueError(f"non-regular ZIP entry: {info.filename}")
            total += info.file_size
            if total > MAX_UNCOMPRESSED_BYTES:
                raise ValueError("source archive uncompressed size limit exceeded")
            by_name[info.filename] = info

        manifests = [name for name in by_name if PurePosixPath(name).name == "SOURCE-MANIFEST.json"]
        if len(manifests) != 1:
            raise ValueError("SOURCE-MANIFEST.json must occur exactly once")
        manifest_name = manifests[0]
        base = PurePosixPath(manifest_name).parent
        manifest_info = by_name[manifest_name]
        if manifest_info.file_size > 16 * 1024 * 1024:
            raise ValueError("source manifest size limit exceeded")
        document = json.loads(archive.read(manifest_name), object_pairs_hook=_unique_json_object)
        if not isinstance(document, dict) or set(document) - {"schema", "compatibilityProfile", "files"}:
            raise ValueError("source manifest fields are invalid")
        if "compatibilityProfile" in document and (
            not isinstance(document["compatibilityProfile"], str)
            or not document["compatibilityProfile"].strip()
        ):
            raise ValueError("source manifest compatibility profile is invalid")
        if document.get("schema") not in ("zingg-duckdb-source-1", "zingg-duckdb-source-archive-1"):
            raise ValueError("unsupported source archive schema")
        entries = document.get("files")
        if not isinstance(entries, list) or not entries:
            raise ValueError("source manifest files must be an array")

        expected: dict[str, str] = {}
        portable_names: set[str] = set()
        for entry in entries:
            if (not isinstance(entry, dict) or set(entry) != {"path", "sha256"}
                    or not isinstance(entry.get("path"), str)):
                raise ValueError("invalid source manifest entry")
            relative = _safe_name(entry["path"])
            relative_name = relative.as_posix()
            if relative_name in expected:
                raise ValueError(f"duplicate source manifest path: {relative_name}")
            portable_name = unicodedata.normalize("NFC", relative_name).casefold()
            if portable_name in portable_names:
                raise ValueError(f"colliding source manifest path: {relative_name}")
            portable_names.add(portable_name)
            digest = entry.get("sha256")
            if not isinstance(digest, str) or re.fullmatch(r"[0-9a-f]{64}", digest) is None:
                raise ValueError(f"invalid SHA-256 in source manifest: {relative_name}")
            expected[relative_name] = digest

        actual: dict[str, zipfile.ZipInfo] = {}
        for name, info in by_name.items():
            path = PurePosixPath(name)
            if info.is_dir():
                continue
            try:
                relative = path.relative_to(base)
            except ValueError as error:
                raise ValueError(f"file is outside source archive root: {name}") from error
            if relative.as_posix() != "SOURCE-MANIFEST.json":
                actual[relative.as_posix()] = info
        if set(actual) != set(expected):
            missing, extra = sorted(set(expected) - set(actual)), sorted(set(actual) - set(expected))
            raise ValueError(f"source archive inventory differs (missing={missing[:3]}, extra={extra[:3]})")

        for relative, digest in expected.items():
            info = actual[relative]
            hasher = hashlib.sha256()
            with archive.open(info) as source:
                for chunk in iter(lambda: source.read(1024 * 1024), b""):
                    hasher.update(chunk)
            if hasher.hexdigest() != digest:
                raise ValueError(f"source hash mismatch: {relative}")


if __name__ == "__main__":
    try:
        if len(sys.argv) != 2:
            raise ValueError("usage: verify_source_archive.py ARCHIVE.zip")
        verify(sys.argv[1])
    except (OSError, ValueError, KeyError, json.JSONDecodeError, zipfile.BadZipFile) as error:
        print(f"SOURCE_ARCHIVE_VERIFY_FAILED: {error}", file=sys.stderr)
        raise SystemExit(1)
    print("SOURCE_ARCHIVE_VERIFY_SUCCESS")
