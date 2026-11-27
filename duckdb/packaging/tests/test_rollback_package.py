"""Safety and reproducibility tests for rollback archives."""
from __future__ import annotations

import hashlib
import importlib.util
import os
import shutil
import subprocess
import tempfile
import unittest
import zipfile
from pathlib import Path


SCRIPT = Path(__file__).resolve().parents[1] / "rollback_package.py"
SPEC = importlib.util.spec_from_file_location("rollback_package", SCRIPT)
assert SPEC is not None and SPEC.loader is not None
ROLLBACK = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(ROLLBACK)


def make_bundle(root: Path, *, unsafe_path: bool = False, windows_paths: bool = False) -> Path:
    bundle = root / "zingg-test-0.1"
    (bundle / "bin").mkdir(parents=True)
    payload = b"#!/bin/sh\necho worker\n"
    (bundle / "bin" / "worker.sh").write_bytes(payload)
    (bundle / "README.md").write_text("test bundle\n", encoding="utf-8")
    relative = "../outside" if unsafe_path else ("bin\\worker.sh" if windows_paths else "bin/worker.sh")
    digest = hashlib.sha256(payload).hexdigest()
    readme_digest = hashlib.sha256((bundle / "README.md").read_bytes()).hexdigest()
    (bundle / "SHA256SUMS").write_text(
        f"{digest}  {relative}\n{readme_digest}  README.md\n", encoding="utf-8"
    )
    return bundle


class RollbackPackageTest(unittest.TestCase):
    def test_native_platform_wrappers_create_verify_and_propagate_failure(self) -> None:
        with tempfile.TemporaryDirectory(prefix="rollback wrapper ") as directory:
            root = Path(directory)
            bundle = make_bundle(root / "bundle inputs with spaces")
            output = root / "rollback output with spaces.zip"
            invalid_output = root / "failed output.zip"
            if os.name == "nt":
                powershell = shutil.which("pwsh") or shutil.which("powershell")
                if not powershell:
                    self.skipTest("PowerShell is unavailable")
                create = [powershell, "-NoProfile", "-File", str(SCRIPT.parent / "create-rollback-package.ps1"),
                          "-Bundle", str(bundle), "-Output", str(output)]
                verify = [powershell, "-NoProfile", "-File", str(SCRIPT.parent / "verify-rollback-package.ps1"),
                          "-Archive", str(output)]
            else:
                shell = shutil.which("sh")
                if not shell:
                    self.skipTest("POSIX sh is unavailable")
                create = [shell, str(SCRIPT.parent / "create-rollback-package.sh"), str(bundle), str(output)]
                verify = [shell, str(SCRIPT.parent / "verify-rollback-package.sh"), str(output)]

            subprocess.run(create, check=True, capture_output=True, text=True)
            subprocess.run(verify, check=True, capture_output=True, text=True)
            (bundle / "README.md").write_text("tampered after manifest generation\n", encoding="utf-8")
            failed_create = [*create[:-1], str(invalid_output)] if os.name == "nt" else [
                create[0], create[1], str(bundle), str(invalid_output)
            ]
            result = subprocess.run(failed_create, capture_output=True, text=True)
            self.assertNotEqual(0, result.returncode, result.stdout + result.stderr)
            self.assertFalse(invalid_output.exists())

    def test_archive_is_checksum_bound_verifiable_and_deterministic(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            bundle = make_bundle(root)
            first, second = root / "first.zip", root / "second.zip"
            ROLLBACK.create(str(bundle), str(first))
            ROLLBACK.create(str(bundle), str(second))
            ROLLBACK.verify(str(first))
            self.assertEqual(first.read_bytes(), second.read_bytes())
            with zipfile.ZipFile(first) as archive:
                manifest = ROLLBACK.json.loads(archive.read("ROLLBACK-MANIFEST.json"))
                self.assertEqual("zingg-duckdb-rollback-1", manifest["schema"])
                self.assertIn("bundle/bin/worker.sh", archive.namelist())

    def test_invalid_bundle_checksum_does_not_publish_archive(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            bundle = make_bundle(root)
            (bundle / "bin" / "worker.sh").write_text("modified\n", encoding="utf-8")
            output = root / "invalid.zip"
            with self.assertRaisesRegex(ValueError, "checksum mismatch"):
                ROLLBACK.create(str(bundle), str(output))
            self.assertFalse(output.exists())

    def test_rejects_unlisted_bundle_payload(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            bundle = make_bundle(root)
            (bundle / "unlisted.txt").write_text("not covered by SHA256SUMS\n", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "inventory differs"):
                ROLLBACK.create(str(bundle), str(root / "unlisted.zip"))

    def test_accepts_windows_style_checksum_paths(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            bundle = make_bundle(root, windows_paths=True)
            output = root / "windows-paths.zip"
            ROLLBACK.create(str(bundle), str(output))
            ROLLBACK.verify(str(output))

    def test_materializes_internal_bundle_symlinks_as_regular_archive_files(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            bundle = make_bundle(root)
            target = bundle / "README.md"
            link = bundle / "LICENSE.txt"
            try:
                link.symlink_to(target)
            except (OSError, NotImplementedError) as error:
                self.skipTest(f"symlink creation unavailable: {error}")
            digest = hashlib.sha256(target.read_bytes()).hexdigest()
            worker_digest = hashlib.sha256((bundle / "bin" / "worker.sh").read_bytes()).hexdigest()
            (bundle / "SHA256SUMS").write_text(
                f"{worker_digest}  bin/worker.sh\n{digest}  README.md\n{digest}  LICENSE.txt\n",
                encoding="utf-8",
            )
            output = root / "internal-link.zip"
            ROLLBACK.create(str(bundle), str(output))
            ROLLBACK.verify(str(output))
            with zipfile.ZipFile(output) as archive:
                entry = archive.getinfo("bundle/LICENSE.txt")
                mode = (entry.external_attr >> 16) & 0xFFFF
                self.assertFalse(ROLLBACK.stat.S_ISLNK(mode))
                self.assertEqual(target.read_bytes(), archive.read(entry))

    def test_rejects_bundle_symlink_that_escapes_bundle_root(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            bundle = make_bundle(root)
            outside = root / "outside.txt"
            outside.write_text("must not be packaged\n", encoding="utf-8")
            link = bundle / "outside.txt"
            try:
                link.symlink_to(outside)
            except (OSError, NotImplementedError) as error:
                self.skipTest(f"symlink creation unavailable: {error}")
            digest = hashlib.sha256(outside.read_bytes()).hexdigest()
            worker_digest = hashlib.sha256((bundle / "bin" / "worker.sh").read_bytes()).hexdigest()
            (bundle / "SHA256SUMS").write_text(
                f"{worker_digest}  bin/worker.sh\n{digest}  outside.txt\n",
                encoding="utf-8",
            )
            with self.assertRaisesRegex(ValueError, "missing or not regular"):
                ROLLBACK.create(str(bundle), str(root / "unsafe-link.zip"))

    def test_existing_archive_is_never_overwritten(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            bundle = make_bundle(root)
            output = root / "keep.zip"
            output.write_bytes(b"preserve existing user data")
            with self.assertRaises(FileExistsError):
                ROLLBACK.create(str(bundle), str(output))
            self.assertEqual(b"preserve existing user data", output.read_bytes())

    def test_rejects_traversal_paths_and_output_inside_bundle(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            unsafe = make_bundle(root, unsafe_path=True)
            with self.assertRaisesRegex(ValueError, "unsafe SHA256SUMS path"):
                ROLLBACK.create(str(unsafe), str(root / "unsafe.zip"))
            safe = make_bundle(root / "safe")
            with self.assertRaisesRegex(ValueError, "outside the bundle"):
                ROLLBACK.create(str(safe), str(safe / "rollback.zip"))

    def test_verifier_rejects_modified_archive_payload(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            bundle = make_bundle(root)
            good, damaged = root / "good.zip", root / "damaged.zip"
            ROLLBACK.create(str(bundle), str(good))
            with zipfile.ZipFile(good) as source, zipfile.ZipFile(damaged, "w") as target:
                for item in source.infolist():
                    data = source.read(item.filename)
                    if item.filename == "bundle/bin/worker.sh":
                        data = b"tampered\n"
                    target.writestr(item, data)
            with self.assertRaisesRegex(ValueError, "payload checksum mismatch"):
                ROLLBACK.verify(str(damaged))

    def test_verifier_rejects_unlisted_directory_entries(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            bundle = make_bundle(root)
            good, extra_directory = root / "good.zip", root / "extra-directory.zip"
            ROLLBACK.create(str(bundle), str(good))
            with zipfile.ZipFile(good) as source, zipfile.ZipFile(extra_directory, "w") as target:
                for item in source.infolist():
                    target.writestr(item, source.read(item.filename))
                target.writestr("bundle/unlisted/", b"")
            with self.assertRaisesRegex(ValueError, "unexpected rollback archive directory"):
                ROLLBACK.verify(str(extra_directory))

    def test_verifier_rejects_duplicate_and_unexpected_manifest_fields(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            bundle = make_bundle(root)
            good = root / "good.zip"
            ROLLBACK.create(str(bundle), str(good))
            for label, mutation, diagnostic in (
                ("duplicate", "duplicate", "duplicate rollback manifest key"),
                ("unknown", "unknown", "rollback manifest fields are invalid"),
                ("wrong-type", "wrong-type", "bundle directory must have a simple name"),
            ):
                malformed = root / f"{label}.zip"
                with zipfile.ZipFile(good) as source, zipfile.ZipFile(malformed, "w") as target:
                    for item in source.infolist():
                        data = source.read(item.filename)
                        if item.filename == ROLLBACK.MANIFEST_NAME:
                            manifest_text = data.decode("utf-8")
                            if mutation == "duplicate":
                                manifest_text = manifest_text[:-2] + ',\n  "schema": "overridden"\n}\n'
                            else:
                                manifest = ROLLBACK.json.loads(manifest_text)
                                if mutation == "unknown":
                                    manifest["unexpected"] = True
                                else:
                                    manifest["bundleName"] = []
                                manifest_text = ROLLBACK.json.dumps(manifest)
                            data = manifest_text.encode("utf-8")
                        target.writestr(item, data)
                with self.subTest(manifest=label), self.assertRaisesRegex(ValueError, diagnostic):
                    ROLLBACK.verify(str(malformed))


if __name__ == "__main__":
    unittest.main()
