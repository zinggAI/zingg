from __future__ import annotations

import importlib.util
import tempfile
import unittest
import zipfile
from pathlib import Path


SCRIPT = Path(__file__).resolve().parents[1] / "dependency_licenses.py"
SPEC = importlib.util.spec_from_file_location("dependency_licenses", SCRIPT)
assert SPEC is not None and SPEC.loader is not None
LICENSES = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(LICENSES)


class DependencyLicenseInventoryTests(unittest.TestCase):
    def test_collects_original_legal_resources_verbatim_and_verifies_exact_inventory(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            staged = root / "staged"
            jar_path = staged / "org" / "example" / "lib" / "1.2" / "lib-1.2.jar"
            jar_path.parent.mkdir(parents=True)
            with zipfile.ZipFile(jar_path, "w") as jar:
                jar.writestr("META-INF/LICENSE", "License bytes\n")
                jar.writestr("META-INF/NOTICE", "Notice bytes\n")
                jar.writestr("META-INF/licenses/third-party/COPYING", "Copying bytes\n")
                jar.writestr("com/example/NotALicense.class", b"class")
                jar.writestr("../LICENSE", "unsafe")
            output = root / "bundle" / "licenses" / "third-party"

            document = LICENSES.collect(staged, output)
            verified = LICENSES.verify(output)
            self.assertEqual(3, document["resourceCount"])
            self.assertEqual(verified, document)
            self.assertEqual({"License bytes\n", "Notice bytes\n", "Copying bytes\n"},
                             {path.read_text(encoding="utf-8") for path in output.rglob("*")
                              if path.is_file() and path.name != LICENSES.INDEX})

    def test_verification_detects_tampering_and_unindexed_files(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            staged = root / "staged"
            staged.mkdir()
            with zipfile.ZipFile(staged / "dep.jar", "w") as jar:
                jar.writestr("META-INF/LICENSE.txt", "original")
            output = root / "licenses"
            LICENSES.collect(staged, output)
            resource = next(path for path in output.rglob("*") if path.is_file() and path.name != LICENSES.INDEX)
            resource.write_text("tampered", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "hash/size mismatch"):
                LICENSES.verify(output)
            resource.write_text("original", encoding="utf-8")
            (output / "unindexed.txt").write_text("extra", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "exactly match"):
                LICENSES.verify(output)

    def test_rejects_missing_dependencies_and_output_overwrite(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            with self.assertRaisesRegex(ValueError, "no staged runtime"):
                LICENSES.collect(root / "missing", root / "out")
            staged = root / "staged"
            staged.mkdir()
            with zipfile.ZipFile(staged / "dep.jar", "w") as jar:
                jar.writestr("META-INF/LICENSE", "license")
            output = root / "out"
            output.mkdir()
            (output / "user-data.txt").write_text("keep", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "non-empty output"):
                LICENSES.collect(staged, output)
            self.assertEqual("keep", (output / "user-data.txt").read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
