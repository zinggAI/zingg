"""Adversarial path and inventory tests for source archive verification."""
from __future__ import annotations

import hashlib
import importlib.util
import json
import tempfile
import unittest
import warnings
import zipfile
from pathlib import Path


SCRIPT = Path(__file__).resolve().parents[1] / "verify_source_archive.py"
SPEC = importlib.util.spec_from_file_location("verify_source_archive", SCRIPT)
assert SPEC is not None and SPEC.loader is not None
VERIFY = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(VERIFY)


def write_archive(path: Path, entries: list[tuple[str, bytes, int | None]]) -> None:
    with zipfile.ZipFile(path, "w") as archive:
        for name, data, mode in entries:
            info = zipfile.ZipInfo(name)
            if mode is not None:
                info.external_attr = mode << 16
            archive.writestr(info, data)


def valid_entries(rooted: bool = True) -> list[tuple[str, bytes, int | None]]:
    content = b"source\n"
    manifest = json.dumps({
        "schema": "zingg-duckdb-source-archive-1",
        "files": [{"path": "src/file.txt", "sha256": hashlib.sha256(content).hexdigest()}],
    }).encode()
    prefix = "zingg-duckdb/" if rooted else ""
    return [(prefix + "src/file.txt", content, 0o100644),
            (prefix + "SOURCE-MANIFEST.json", manifest, 0o100644)]


class SourceArchiveVerificationTest(unittest.TestCase):
    def test_accepts_valid_archive_rooted_at_source_directory(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            archive = Path(directory) / "source.zip"
            write_archive(archive, valid_entries())
            VERIFY.verify(str(archive))

    def test_accepts_rootless_windows_source_archive_layout(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            archive = Path(directory) / "source-rootless.zip"
            write_archive(archive, valid_entries(rooted=False))
            VERIFY.verify(str(archive))

    def test_rejects_parent_and_absolute_paths_before_extraction(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            for unsafe in ("../outside.txt", "/outside.txt", "C:/outside.txt", "a\\..\\outside.txt", "a//outside.txt"):
                with self.subTest(path=unsafe):
                    archive = Path(directory) / "unsafe.zip"
                    entries = valid_entries() + [(unsafe, b"escape", 0o100644)]
                    write_archive(archive, entries)
                    with self.assertRaisesRegex(ValueError, "unsafe ZIP path"):
                        VERIFY.verify(str(archive))

    def test_rejects_duplicate_members_and_duplicate_manifest_paths(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            archive = Path(directory) / "duplicate.zip"
            entries = valid_entries()
            entries.append(entries[0])
            with warnings.catch_warnings():
                warnings.filterwarnings("ignore", message=r"Duplicate name: .*", category=UserWarning)
                write_archive(archive, entries)
            with self.assertRaisesRegex(ValueError, "duplicate ZIP entry"):
                VERIFY.verify(str(archive))

            manifest = json.loads(valid_entries()[1][1])
            manifest["files"].append(dict(manifest["files"][0]))
            write_archive(archive, [("zingg-duckdb/src/file.txt", b"source\n", 0o100644),
                                    ("zingg-duckdb/SOURCE-MANIFEST.json", json.dumps(manifest).encode(), 0o100644)])
            with self.assertRaisesRegex(ValueError, "duplicate source manifest path"):
                VERIFY.verify(str(archive))

    def test_rejects_symlinks_and_unlisted_files(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            archive = Path(directory) / "symlink.zip"
            entries = valid_entries() + [("zingg-duckdb/link", b"target", 0o120777)]
            write_archive(archive, entries)
            with self.assertRaisesRegex(ValueError, "symbolic link"):
                VERIFY.verify(str(archive))

            entries = valid_entries() + [("zingg-duckdb/extra.txt", b"extra", 0o100644)]
            write_archive(archive, entries)
            with self.assertRaisesRegex(ValueError, "inventory differs"):
                VERIFY.verify(str(archive))

            entries = valid_entries() + [("outside.txt", b"outside", 0o100644)]
            write_archive(archive, entries)
            with self.assertRaisesRegex(ValueError, "outside source archive root"):
                VERIFY.verify(str(archive))

            entries = valid_entries() + [("zingg-duckdb/unlisted/", b"", None)]
            write_archive(archive, entries)
            with self.assertRaisesRegex(ValueError, "unexpected directory"):
                VERIFY.verify(str(archive))

    def test_rejects_ambiguous_or_nonportable_manifests(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            archive = Path(directory) / "manifest.zip"
            good = valid_entries()
            manifest_text = good[1][1].decode("utf-8").rstrip()
            duplicate_key = manifest_text[:-1] + ',"schema":"zingg-duckdb-source-1"}'
            write_archive(archive, [good[0], (good[1][0], duplicate_key.encode(), good[1][2])])
            with self.assertRaisesRegex(ValueError, "duplicate source manifest key"):
                VERIFY.verify(str(archive))

            document = json.loads(good[1][1])
            document["unexpected"] = True
            write_archive(archive, [good[0], (good[1][0], json.dumps(document).encode(), good[1][2])])
            with self.assertRaisesRegex(ValueError, "source manifest fields are invalid"):
                VERIFY.verify(str(archive))

            content = b"source\n"
            document = {
                "schema": "zingg-duckdb-source-archive-1",
                "files": [
                    {"path": "src/file.txt", "sha256": hashlib.sha256(content).hexdigest()},
                    {"path": "SRC/FILE.TXT", "sha256": hashlib.sha256(content).hexdigest()},
                ],
            }
            write_archive(archive, [
                ("zingg-duckdb/src/file.txt", content, 0o100644),
                ("zingg-duckdb/SRC/FILE.TXT", content, 0o100644),
                ("zingg-duckdb/SOURCE-MANIFEST.json", json.dumps(document).encode(), 0o100644),
            ])
            with self.assertRaisesRegex(ValueError, "colliding source manifest path"):
                VERIFY.verify(str(archive))

    def test_rejects_hash_mismatch_and_multiple_manifests(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            archive = Path(directory) / "invalid.zip"
            entries = valid_entries()
            entries[0] = (entries[0][0], b"tampered", 0o100644)
            write_archive(archive, entries)
            with self.assertRaisesRegex(ValueError, "source hash mismatch"):
                VERIFY.verify(str(archive))

            entries = valid_entries() + [("other/SOURCE-MANIFEST.json", b"{}", 0o100644)]
            write_archive(archive, entries)
            with self.assertRaisesRegex(ValueError, "must occur exactly once"):
                VERIFY.verify(str(archive))


if __name__ == "__main__":
    unittest.main()
