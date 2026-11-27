"""Deterministic and portable source archive creator tests."""
from __future__ import annotations

import hashlib
import importlib.util
import json
import os
import shutil
import subprocess
import sys
import tempfile
import unittest
import zipfile
from pathlib import Path
from unittest.mock import patch


PACKAGING = Path(__file__).resolve().parents[1]


def load_module(name: str, path: Path):
    spec = importlib.util.spec_from_file_location(name, path)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


CREATE = load_module("create_source_archive", PACKAGING / "create_source_archive.py")
VERIFY = load_module("verify_source_archive", PACKAGING / "verify_source_archive.py")


def make_source(root: Path) -> Path:
    (root / "src").mkdir(parents=True)
    (root / "README.md").write_bytes(b"source tree\n")
    (root / "mvnw").write_bytes(b"#!/bin/sh\nexit 0\n")
    (root / "src" / "driver.sh").write_bytes(b"#!/bin/sh\necho ok\n")
    for directory in (".git", "target", "build", "dist", "dist-current", "__pycache__", ".pytest_cache"):
        ignored = root / directory
        ignored.mkdir()
        (ignored / "generated.bin").write_bytes(b"must not be included")
    return root


class CreateSourceArchiveTest(unittest.TestCase):
    def test_creation_is_byte_reproducible_and_uses_canonical_layout(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            base = Path(directory)
            root = make_source(base / "repo")
            first = CREATE.create(str(root), str(base / "first.zip"))
            os.chmod(root / "README.md", 0o600)
            os.utime(root / "README.md", (1_700_000_000, 1_700_000_000))
            second = CREATE.create(str(root), str(base / "second.zip"))
            self.assertEqual(hashlib.sha256(first.read_bytes()).digest(), hashlib.sha256(second.read_bytes()).digest())
            VERIFY.verify(str(first))
            with zipfile.ZipFile(first) as archive:
                names = archive.namelist()
                self.assertEqual(names, sorted(names[:-1]) + ["zingg-duckdb/SOURCE-MANIFEST.json"])
                self.assertIn("zingg-duckdb/README.md", names)
                self.assertNotIn("README.md", names)
                self.assertFalse(any("generated.bin" in name for name in names))
                document = json.loads(archive.read("zingg-duckdb/SOURCE-MANIFEST.json"))
                self.assertEqual(CREATE.SCHEMA, document["schema"])
                self.assertEqual("zingg-0.7.0-duckdb-1.5.6.0", document["compatibilityProfile"])
                readme = archive.getinfo("zingg-duckdb/README.md")
                self.assertEqual((readme.external_attr >> 16) & 0o777, 0o644)
                launcher = archive.getinfo("zingg-duckdb/mvnw")
                self.assertEqual((launcher.external_attr >> 16) & 0o777, 0o755)

    def test_rejects_output_inside_nonexcluded_source_directory(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = make_source(Path(directory) / "repo")
            output = root / "source.zip"
            with self.assertRaisesRegex(ValueError, "under an excluded dist directory"):
                CREATE.create(str(root), str(output))
            output = root / "dist" / "source.zip"
            created = CREATE.create(str(root), str(output))
            VERIFY.verify(str(created))

    def test_relative_output_is_resolved_from_source_root(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            base = Path(directory)
            root = make_source(base / "repo")
            previous = Path.cwd()
            try:
                os.chdir(base)
                created = CREATE.create(str(root), "dist/generated.zip")
            finally:
                os.chdir(previous)
            self.assertEqual(root.resolve() / "dist" / "generated.zip", created)
            VERIFY.verify(str(created))

    def test_failed_creation_does_not_replace_existing_archive(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            base = Path(directory)
            root = make_source(base / "repo")
            output = base / "source.zip"
            output.write_bytes(b"keep old artifact")
            link = root / "unsafe-link"
            try:
                link.symlink_to(root / "README.md")
            except (OSError, NotImplementedError) as error:
                self.skipTest(f"symlink creation unavailable: {error}")
            with self.assertRaisesRegex(ValueError, "symbolic link"):
                CREATE.create(str(root), str(output))
            self.assertEqual(b"keep old artifact", output.read_bytes())
            self.assertEqual([], list(base.glob(".zingg-source-*.zip")))

    def test_atomic_publish_failure_preserves_existing_archive_and_cleans_stage(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            base = Path(directory)
            root = make_source(base / "repo")
            output = base / "source.zip"
            previous = b"preserve prior source archive"
            output.write_bytes(previous)
            with patch.object(CREATE.os, "replace", side_effect=OSError("injected atomic publish failure")):
                with self.assertRaisesRegex(OSError, "injected atomic publish failure"):
                    CREATE.create(str(root), str(output))
            self.assertEqual(previous, output.read_bytes())
            self.assertEqual([], list(base.glob(".zingg-source-*.zip")))

    def test_platform_wrapper_emits_verifiable_archive(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            base = Path(directory)
            root = make_source(base / "repo")
            if os.name == "nt":
                powershell = shutil.which("pwsh") or shutil.which("powershell")
                if not powershell:
                    self.skipTest("PowerShell is unavailable")
                output = base / "windows.zip"
                subprocess.run(
                    [powershell, "-NoProfile", "-File", str(PACKAGING / "create-source-archive.ps1"),
                     "-Root", str(root), "-Output", str(output)],
                    check=True, capture_output=True, text=True,
                )
            else:
                output = base / "posix.zip"
                subprocess.run(
                    ["sh", str(PACKAGING / "create-source-archive.sh"), str(root), str(output)],
                    check=True, capture_output=True, text=True,
                )
            VERIFY.verify(str(output))

    def test_powershell_and_posix_writer_bytes_match(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            base = Path(directory)
            root = make_source(base / "repo")
            powershell = shutil.which("pwsh")
            shell = shutil.which("sh")
            if not powershell or not shell:
                self.skipTest("PowerShell Core is unavailable for cross-platform writer comparison")
            windows_output = base / "powershell.zip"
            posix_output = base / "posix.zip"
            subprocess.run(
                [shell, str(PACKAGING / "create-source-archive.sh"), str(root), str(posix_output)],
                check=True, capture_output=True, text=True,
            )
            subprocess.run(
                [powershell, "-NoProfile", "-File", str(PACKAGING / "create-source-archive.ps1"),
                 "-Root", str(root), "-Output", str(windows_output)],
                check=True, capture_output=True, text=True,
            )
            self.assertEqual(windows_output.read_bytes(), posix_output.read_bytes())
            VERIFY.verify(str(windows_output))


if __name__ == "__main__":
    unittest.main()
