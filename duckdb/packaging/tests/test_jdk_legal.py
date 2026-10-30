from __future__ import annotations

import importlib.util
import tempfile
import unittest
from pathlib import Path


SCRIPT = Path(__file__).resolve().parents[1] / "jdk_legal.py"
SPEC = importlib.util.spec_from_file_location("jdk_legal", SCRIPT)
assert SPEC is not None and SPEC.loader is not None
JDK_LEGAL = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(JDK_LEGAL)


class JdkLegalInventoryTests(unittest.TestCase):
    def _fixture(self, root: Path) -> tuple[Path, Path]:
        jdk = root / "jdk"
        runtime = root / "runtime"
        (jdk / "bin").mkdir(parents=True)
        (jdk / "release").write_text(
            'JAVA_VERSION="21.0.12.1"\nIMPLEMENTOR="Example Vendor"\n'
            'IMPLEMENTOR_VERSION="Example-42"\nOS_ARCH="x86_64"\n', encoding="utf-8")
        (runtime / "bin").mkdir(parents=True)
        (runtime / "bin" / "java").write_bytes(b"java executable fixture")
        (runtime / "legal" / "java.base").mkdir(parents=True)
        (runtime / "legal" / "java.base" / "LICENSE").write_text("vendor license\n", encoding="utf-8")
        (runtime / "legal" / "java.base" / "ADDITIONAL_LICENSE_INFO").write_text("additional terms\n", encoding="utf-8")
        return jdk, runtime

    def test_records_vendor_version_executable_and_exact_legal_files(self):
        with tempfile.TemporaryDirectory() as temporary:
            jdk, runtime = self._fixture(Path(temporary))
            provenance, manifest = JDK_LEGAL.collect(jdk, runtime, "java")
            checked_provenance, checked_manifest = JDK_LEGAL.verify(runtime)
            self.assertEqual("Example Vendor", provenance["javaVendor"])
            self.assertEqual("21.0.12.1", provenance["javaVersion"])
            self.assertEqual(2, manifest["resourceCount"])
            self.assertEqual(provenance, checked_provenance)
            self.assertEqual(manifest, checked_manifest)

    def test_verifier_rejects_legal_tampering_unlisted_files_and_executable_changes(self):
        with tempfile.TemporaryDirectory() as temporary:
            jdk, runtime = self._fixture(Path(temporary))
            JDK_LEGAL.collect(jdk, runtime, "java")
            legal_file = runtime / "legal" / "java.base" / "LICENSE"
            legal_file.write_text("tampered", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "hash/size mismatch"):
                JDK_LEGAL.verify(runtime)
            legal_file.write_text("vendor license\n", encoding="utf-8")
            (runtime / "legal" / "extra.txt").write_text("extra", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "inventory mismatch"):
                JDK_LEGAL.verify(runtime)
            (runtime / "legal" / "extra.txt").unlink()
            (runtime / "bin" / "java").write_bytes(b"changed executable")
            with self.assertRaisesRegex(ValueError, "executable does not match"):
                JDK_LEGAL.verify(runtime)

    def test_accepts_internal_legal_symlinks_and_rejects_escaping_links(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            jdk, runtime = self._fixture(root)
            link = runtime / "legal" / "java.logging" / "LICENSE"
            link.parent.mkdir()
            try:
                link.symlink_to("../java.base/LICENSE")
            except (OSError, NotImplementedError) as error:
                self.skipTest(f"symlink creation unavailable: {error}")
            provenance, manifest = JDK_LEGAL.collect(jdk, runtime, "java")
            _, checked = JDK_LEGAL.verify(runtime)
            record = next(entry for entry in manifest["resources"] if entry["path"] == "legal/java.logging/LICENSE")
            self.assertEqual("symlink", record["type"])
            self.assertEqual("../java.base/LICENSE", record["linkTarget"])
            self.assertEqual(checked, manifest)
            self.assertEqual("Example Vendor", provenance["javaVendor"])

            (root / "outside-license").write_text("outside", encoding="utf-8")
            link.unlink()
            link.symlink_to("../../../outside-license")
            with self.assertRaisesRegex(ValueError, "escapes legal"):
                JDK_LEGAL.collect(jdk, runtime, "java")

    def test_rejects_unknown_vendor_and_missing_legal_tree(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            jdk, runtime = self._fixture(root)
            (jdk / "release").write_text('JAVA_VERSION="21.0.12.1"\n', encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "IMPLEMENTOR"):
                JDK_LEGAL.collect(jdk, runtime, "java")
            (jdk / "release").write_text('JAVA_VERSION="21.0.12.1"\nIMPLEMENTOR="Vendor"\n', encoding="utf-8")
            for path in (runtime / "legal").rglob("*"):
                if path.is_file():
                    path.unlink()
            (runtime / "legal" / "java.base").rmdir()
            (runtime / "legal").rmdir()
            with self.assertRaisesRegex(ValueError, "missing its legal/"):
                JDK_LEGAL.collect(jdk, runtime, "java")


if __name__ == "__main__":
    unittest.main()
