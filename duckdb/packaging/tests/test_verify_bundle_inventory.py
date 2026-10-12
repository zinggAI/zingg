"""Cross-platform checksum inventory verifier adversarial tests."""
from __future__ import annotations

import hashlib
import importlib.util
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch


SCRIPT = Path(__file__).resolve().parents[1] / "verify_bundle_inventory.py"
SPEC = importlib.util.spec_from_file_location("verify_bundle_inventory", SCRIPT)
assert SPEC is not None and SPEC.loader is not None
VERIFY = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(VERIFY)
WRITER_SCRIPT = Path(__file__).resolve().parents[1] / "write_bundle_manifest.py"
WRITER_SPEC = importlib.util.spec_from_file_location("write_bundle_manifest", WRITER_SCRIPT)
assert WRITER_SPEC is not None and WRITER_SPEC.loader is not None
WRITER = importlib.util.module_from_spec(WRITER_SPEC)
WRITER_SPEC.loader.exec_module(WRITER)


def make_bundle(root: Path) -> Path:
    bundle = root / "bundle"
    (bundle / "worker").mkdir(parents=True)
    worker = b"worker bytes\n"
    readme = b"bundle\n"
    (bundle / "worker" / "worker.jar").write_bytes(worker)
    (bundle / "README.md").write_bytes(readme)
    (bundle / "SHA256SUMS").write_text(
        f"{hashlib.sha256(worker).hexdigest()}  worker/worker.jar\n"
        f"{hashlib.sha256(readme).hexdigest()}  README.md\n",
        encoding="utf-8",
    )
    return bundle


class BundleInventoryVerificationTest(unittest.TestCase):
    def test_accepts_exact_bundle_inventory(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            VERIFY.verify(str(make_bundle(Path(directory))))

    def test_rejects_unsafe_checksum_paths(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            bundle = make_bundle(Path(directory))
            digest = hashlib.sha256(b"outside").hexdigest()
            for path in ("../outside", "/outside", "C:/outside", "worker\\..\\outside", "worker//x", "./worker.jar"):
                with self.subTest(path=path):
                    (bundle / "SHA256SUMS").write_text(f"{digest}  {path}\n", encoding="utf-8")
                    with self.assertRaisesRegex(ValueError, "unsafe checksum path"):
                        VERIFY.verify(str(bundle))

    def test_accepts_windows_path_separators_and_rejects_case_collisions(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            bundle = make_bundle(Path(directory))
            lines = (bundle / "SHA256SUMS").read_text(encoding="utf-8").replace("worker/worker.jar", "worker\\worker.jar")
            (bundle / "SHA256SUMS").write_text(lines, encoding="utf-8")
            VERIFY.verify(str(bundle))
            worker_digest = hashlib.sha256((bundle / "worker" / "worker.jar").read_bytes()).hexdigest()
            readme_digest = hashlib.sha256((bundle / "README.md").read_bytes()).hexdigest()
            (bundle / "SHA256SUMS").write_text(
                f"{worker_digest}  worker/worker.jar\n{readme_digest}  README.md\n{readme_digest}  readme.md\n",
                encoding="utf-8",
            )
            with self.assertRaisesRegex(ValueError, "case-insensitive"):
                VERIFY.verify(str(bundle))

    def test_rejects_windows_nonportable_names_and_unicode_collisions(self) -> None:
        for name in ("CON", "aux.txt", "COM1.log", "trailing.", "trailing ", "bad?.txt"):
            with self.subTest(name=name), self.assertRaisesRegex(ValueError, "non-portable checksum path"):
                VERIFY._relative_path(name)
        self.assertEqual(VERIFY._portable_key("caf\u00e9.txt"),
                         VERIFY._portable_key("cafe\u0301.txt"))

        with tempfile.TemporaryDirectory() as directory:
            bundle = make_bundle(Path(directory))
            digest = hashlib.sha256(b"same bytes").hexdigest()
            (bundle / "SHA256SUMS").write_text(
                f"{digest}  caf\u00e9.txt\n{digest}  cafe\u0301.txt\n", encoding="utf-8"
            )
            with self.assertRaisesRegex(ValueError, "case-insensitive"):
                VERIFY.verify(str(bundle))

    def test_rejects_duplicate_entries_missing_files_and_unlisted_files(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            bundle = make_bundle(Path(directory))
            original = (bundle / "SHA256SUMS").read_text(encoding="utf-8")
            (bundle / "SHA256SUMS").write_text(original + original.splitlines()[0] + "\n", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "duplicate SHA256SUMS path"):
                VERIFY.verify(str(bundle))

            (bundle / "SHA256SUMS").write_text(original, encoding="utf-8")
            (bundle / "extra.txt").write_text("unlisted", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "inventory differs"):
                VERIFY.verify(str(bundle))

            (bundle / "extra.txt").unlink()
            (bundle / "worker" / "worker.jar").unlink()
            with self.assertRaisesRegex(ValueError, "inventory differs"):
                VERIFY.verify(str(bundle))

    def test_rejects_malformed_manifest_and_digest_mismatch(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            bundle = make_bundle(Path(directory))
            (bundle / "SHA256SUMS").write_text("bad manifest\n", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "invalid SHA256SUMS line"):
                VERIFY.verify(str(bundle))
            checksums = make_bundle(Path(directory) / "valid")
            original = (checksums / "SHA256SUMS").read_text(encoding="utf-8")
            worker_line, _ = original.splitlines()
            (bundle / "SHA256SUMS").write_text(worker_line + "\n" + "0" * 64 + "  README.md\n", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "checksum mismatch"):
                VERIFY.verify(str(bundle))

    def test_rejects_external_symlink_and_allows_declared_internal_file_link(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            bundle = make_bundle(root)
            outside = root / "outside"
            outside.write_bytes(b"outside")
            external_link = bundle / "outside-link"
            try:
                external_link.symlink_to(outside)
            except (OSError, NotImplementedError) as error:
                self.skipTest(f"symlink creation unavailable: {error}")
            (bundle / "SHA256SUMS").write_text(
                (bundle / "SHA256SUMS").read_text(encoding="utf-8")
                + f"{hashlib.sha256(outside.read_bytes()).hexdigest()}  outside-link\n",
                encoding="utf-8",
            )
            with self.assertRaisesRegex(ValueError, "unsafe symbolic link"):
                VERIFY.verify(str(bundle))

            external_link.unlink()
            worker_hash = hashlib.sha256((bundle / "worker" / "worker.jar").read_bytes()).hexdigest()
            readme_hash = hashlib.sha256((bundle / "README.md").read_bytes()).hexdigest()
            (bundle / "SHA256SUMS").write_text(
                f"{worker_hash}  worker/worker.jar\n{readme_hash}  README.md\n", encoding="utf-8"
            )
            link = bundle / "readme-link"
            link.symlink_to(bundle / "README.md")
            (bundle / "SHA256SUMS").write_text(
                (bundle / "SHA256SUMS").read_text(encoding="utf-8")
                + f"{readme_hash}  readme-link\n",
                encoding="utf-8",
            )
            VERIFY.verify(str(bundle))

    def test_manifest_writer_and_verifier_agree_on_safe_internal_symlinks(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            bundle = Path(directory) / "bundle"
            bundle.mkdir()
            (bundle / "real.txt").write_text("payload\n", encoding="utf-8")
            alias = bundle / "alias.txt"
            try:
                alias.symlink_to(bundle / "real.txt")
            except (OSError, NotImplementedError) as error:
                self.skipTest(f"symlink creation unavailable: {error}")
            WRITER.write(str(bundle))
            manifest = (bundle / "SHA256SUMS").read_text(encoding="utf-8")
            self.assertIn("alias.txt", manifest)
            self.assertIn("real.txt", manifest)
            VERIFY.verify(str(bundle))

    def test_manifest_writer_enforces_windows_name_rules(self) -> None:
        for name in ("CON", "aux.txt", "COM1.log", "trailing.", "trailing ", "bad?.txt"):
            with self.subTest(name=name), self.assertRaisesRegex(ValueError, "not portable to Windows"):
                WRITER._portable_key(name)
        self.assertEqual(WRITER._portable_key("caf\u00e9.txt"),
                         WRITER._portable_key("cafe\u0301.txt"))

    def test_manifest_writer_preserves_previous_inventory_on_publish_failures(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            bundle = Path(directory) / "bundle"
            bundle.mkdir()
            (bundle / "payload.txt").write_text("payload\n", encoding="utf-8")
            manifest = bundle / "SHA256SUMS"
            previous = b"previous manifest bytes\n"
            manifest.write_bytes(previous)

            with patch.object(WRITER, "_portable_key", side_effect=ValueError("injected path validation failure")):
                with self.assertRaisesRegex(ValueError, "injected path validation failure"):
                    WRITER.write(str(bundle))
            self.assertEqual(previous, manifest.read_bytes())
            self.assertEqual([], list(bundle.glob(".SHA256SUMS-*.tmp")))

            with patch.object(WRITER.os, "replace", side_effect=OSError("injected publish failure")):
                with self.assertRaisesRegex(OSError, "injected publish failure"):
                    WRITER.write(str(bundle))
            self.assertEqual(previous, manifest.read_bytes())
            self.assertEqual([], list(bundle.glob(".SHA256SUMS-*.tmp")))


if __name__ == "__main__":
    unittest.main()
