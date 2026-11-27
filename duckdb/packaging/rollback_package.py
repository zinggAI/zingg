"""Create and verify portable, checksum-bound rollback archives."""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import stat
import sys
import zipfile
from pathlib import Path, PurePosixPath


SCHEMA = "zingg-duckdb-rollback-1"
MANIFEST_NAME = "ROLLBACK-MANIFEST.json"
HASH_LINE = re.compile(r"^([0-9a-fA-F]{64})  (.+)$")


def _read_checksums(lines: list[str]) -> dict[str, str]:
    result: dict[str, str] = {}
    for line_number, line in enumerate(lines, start=1):
        if not line:
            continue
        match = HASH_LINE.fullmatch(line)
        if match is None:
            raise ValueError(f"invalid SHA256SUMS line {line_number}")
        digest, raw_relative = match.groups()
        relative = raw_relative.replace("\\", "/")
        path = PurePosixPath(relative)
        if (path.is_absolute() or re.match(r"^[A-Za-z]:", relative)
                or relative.startswith("//") or ".." in path.parts or "." in path.parts
                or relative == "SHA256SUMS"):
            raise ValueError(f"unsafe SHA256SUMS path: {relative}")
        normalized = path.as_posix()
        if normalized in result:
            raise ValueError(f"duplicate SHA256SUMS path: {normalized}")
        result[normalized] = digest.lower()
    if not result:
        raise ValueError("SHA256SUMS contains no files")
    return result


def _bundle_inventory(bundle: Path) -> tuple[bytes, dict[str, str], dict[str, Path]]:
    sum_path = bundle / "SHA256SUMS"
    if not sum_path.is_file() or sum_path.is_symlink():
        raise ValueError("bundle must contain a regular SHA256SUMS file")
    sum_bytes = sum_path.read_bytes()
    lines = sum_bytes.decode("utf-8-sig").splitlines()
    checksums = _read_checksums(lines)
    files: dict[str, Path] = {"SHA256SUMS": sum_path}
    for relative, expected in checksums.items():
        path = bundle.joinpath(*PurePosixPath(relative).parts)
        resolved = path.resolve(strict=False)
        if (bundle not in resolved.parents or not path.is_file()
                or (path.is_symlink() and not resolved.is_file())):
            raise ValueError(f"bundle file is missing or not regular: {relative}")
        actual = hashlib.sha256(path.read_bytes()).hexdigest()
        if actual != expected:
            raise ValueError(f"bundle checksum mismatch: {relative}")
        # jlink emits internal symlinks for repeated legal notices. Store their
        # verified contents as regular files so rollback archives are portable.
        files[relative] = resolved
    declared = set(checksums) | {"SHA256SUMS"}
    actual = {path.relative_to(bundle).as_posix() for path in bundle.rglob("*") if path.is_file()}
    if actual != declared:
        unlisted = sorted(actual - declared)
        missing = sorted(declared - actual)
        raise ValueError(f"bundle inventory differs from SHA256SUMS (unlisted={unlisted}, missing={missing})")
    for path in bundle.rglob("*"):
        if path.is_symlink():
            resolved = path.resolve(strict=False)
            if (bundle not in resolved.parents or not path.is_file()
                    or path.relative_to(bundle).as_posix() not in checksums):
                raise ValueError(f"bundle contains an unsafe or untracked symbolic link: {path.relative_to(bundle)}")
    return sum_bytes, checksums, files


def _safe_bundle_name(name: str) -> str:
    if not isinstance(name, str) or not name or name in {".", ".."} or "/" in name or "\\" in name:
        raise ValueError("bundle directory must have a simple name")
    return name


def _unique_json_object(pairs: list[tuple[str, object]]) -> dict[str, object]:
    result: dict[str, object] = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f"duplicate rollback manifest key: {key}")
        result[key] = value
    return result


def create(bundle_arg: str, output_arg: str) -> None:
    bundle = Path(bundle_arg).resolve(strict=True)
    if not bundle.is_dir():
        raise ValueError("bundle must be a directory")
    bundle_name = _safe_bundle_name(bundle.name)
    output = Path(output_arg).absolute()
    if output == bundle or bundle in output.parents:
        raise ValueError("rollback archive output must be outside the bundle")
    output.parent.mkdir(parents=True, exist_ok=True)
    output = output.parent.resolve() / output.name
    if output == bundle or bundle in output.parents:
        raise ValueError("rollback archive output must be outside the bundle")
    sum_bytes, _, files = _bundle_inventory(bundle)
    manifest = {
        "schema": SCHEMA,
        "bundleName": bundle_name,
        "bundleSha256": hashlib.sha256(sum_bytes).hexdigest(),
        "restore": "replace the active bundle only after verifying SHA256SUMS and compatibility profile",
    }
    manifest_bytes = (json.dumps(manifest, indent=2, sort_keys=True) + "\n").encode("utf-8")
    created = False
    try:
        # Exclusive creation is intentional: never destroy a prior rollback artifact.
        with output.open("xb") as raw:
            created = True
            with zipfile.ZipFile(raw, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
                for relative, path in sorted(files.items()):
                    info = zipfile.ZipInfo(f"bundle/{relative}", date_time=(1980, 1, 1, 0, 0, 0))
                    info.create_system = 3
                    mode = stat.S_IFREG | stat.S_IMODE(path.stat().st_mode)
                    info.external_attr = mode << 16
                    info.compress_type = zipfile.ZIP_DEFLATED
                    archive.writestr(info, path.read_bytes(), compress_type=zipfile.ZIP_DEFLATED, compresslevel=9)
                info = zipfile.ZipInfo(MANIFEST_NAME, date_time=(1980, 1, 1, 0, 0, 0))
                info.create_system = 3
                info.external_attr = (stat.S_IFREG | 0o644) << 16
                info.compress_type = zipfile.ZIP_DEFLATED
                archive.writestr(info, manifest_bytes, compress_type=zipfile.ZIP_DEFLATED, compresslevel=9)
    except Exception:
        if created:
            output.unlink(missing_ok=True)
        raise
    print(f"ROLLBACK_PACKAGE_SUCCESS {output}")


def verify(archive_arg: str) -> None:
    archive_path = Path(archive_arg).resolve(strict=True)
    with zipfile.ZipFile(archive_path) as archive:
        entries = archive.infolist()
        names = [entry.filename for entry in entries]
        if len(names) != len(set(names)):
            raise ValueError("rollback archive contains duplicate paths")
        by_name = {entry.filename: entry for entry in entries}
        if MANIFEST_NAME not in by_name or "bundle/SHA256SUMS" not in by_name:
            raise ValueError("rollback archive is missing its manifest or SHA256SUMS")
        for entry in entries:
            name = entry.filename
            path = PurePosixPath(name)
            if path.is_absolute() or ".." in path.parts or "\\" in name:
                raise ValueError(f"unsafe rollback archive path: {name}")
            if entry.is_dir():
                raise ValueError(f"unexpected rollback archive directory: {name}")
            unix_mode = (entry.external_attr >> 16) & 0xFFFF
            if stat.S_ISLNK(unix_mode):
                raise ValueError(f"rollback archive contains a symbolic link: {name}")
            if name != MANIFEST_NAME and not name.startswith("bundle/"):
                raise ValueError(f"unexpected rollback archive path: {name}")
        manifest = json.loads(
            archive.read(MANIFEST_NAME).decode("utf-8"), object_pairs_hook=_unique_json_object
        )
        if not isinstance(manifest, dict) or set(manifest) != {
            "schema", "bundleName", "bundleSha256", "restore"
        }:
            raise ValueError("rollback manifest fields are invalid")
        if manifest["schema"] != SCHEMA:
            raise ValueError("unsupported rollback manifest schema")
        _safe_bundle_name(manifest["bundleName"])
        if not isinstance(manifest["bundleSha256"], str) or re.fullmatch(
            r"[0-9a-f]{64}", manifest["bundleSha256"]
        ) is None:
            raise ValueError("rollback manifest checksum is invalid")
        if manifest["restore"] != (
            "replace the active bundle only after verifying SHA256SUMS and compatibility profile"
        ):
            raise ValueError("rollback manifest restore instructions are invalid")
        sum_bytes = archive.read("bundle/SHA256SUMS")
        if hashlib.sha256(sum_bytes).hexdigest() != manifest["bundleSha256"]:
            raise ValueError("rollback manifest does not match bundled SHA256SUMS")
        checksums = _read_checksums(sum_bytes.decode("utf-8-sig").splitlines())
        expected = {"bundle/SHA256SUMS", *(f"bundle/{relative}" for relative in checksums)}
        actual = {name for name, entry in by_name.items() if name.startswith("bundle/") and not entry.is_dir()}
        if actual != expected:
            raise ValueError("rollback archive contents do not match SHA256SUMS")
        for relative, expected_hash in checksums.items():
            actual_hash = hashlib.sha256(archive.read(f"bundle/{relative}")).hexdigest()
            if actual_hash != expected_hash:
                raise ValueError(f"rollback payload checksum mismatch: {relative}")
    print(f"ROLLBACK_VERIFY_SUCCESS {archive_path}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    subparsers = parser.add_subparsers(dest="command", required=True)
    create_parser = subparsers.add_parser("create")
    create_parser.add_argument("bundle")
    create_parser.add_argument("output")
    verify_parser = subparsers.add_parser("verify")
    verify_parser.add_argument("archive")
    args = parser.parse_args()
    try:
        if args.command == "create":
            create(args.bundle, args.output)
        else:
            verify(args.archive)
    except (OSError, UnicodeError, ValueError, zipfile.BadZipFile, json.JSONDecodeError) as error:
        print(f"rollback package error: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
