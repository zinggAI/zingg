"""POSIX Native Image wrapper contract tests using a controlled fake toolchain."""
from __future__ import annotations

import hashlib
import json
import os
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path
import importlib.util


PACKAGING = Path(__file__).resolve().parents[1]
WRAPPER = PACKAGING / "build-native-image.sh"
SHELL = shutil.which("sh")
PREPARE = PACKAGING / "prepare_native_image_config.py"
SPEC = importlib.util.spec_from_file_location("prepare_native_image_config", PREPARE)
assert SPEC is not None and SPEC.loader is not None
CONFIG_BUILDER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(CONFIG_BUILDER)


class BuildNativeImageTests(unittest.TestCase):
    def _repo(self, root: Path) -> Path:
        packaging = root / "packaging"
        config = root / "runtime-worker/src/main/resources/META-INF/native-image/io.zingg.duckdb/runtime-worker"
        config.mkdir(parents=True)
        (config / "reflect-config.json").write_text("[]\n", encoding="utf-8")
        (config / "resource-config.json").write_text(
            json.dumps({"resources": {"includes": [{"pattern": ".*\\.(json|sql)$"}]}}),
            encoding="utf-8",
        )
        jar = root / "runtime-worker/target/runtime-worker-0.1.0-SNAPSHOT.jar"
        jar.parent.mkdir(parents=True)
        jar.write_bytes(b"fake worker jar")
        packaging.mkdir()
        shutil.copy2(WRAPPER, packaging / WRAPPER.name)
        shutil.copy2(PREPARE, packaging / PREPARE.name)
        return packaging / WRAPPER.name

    def test_builds_with_required_flags_and_writes_verified_sha256(self) -> None:
        if SHELL is None:
            self.skipTest("POSIX sh is unavailable")
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            script = self._repo(root / "repo")
            tools = root / "tools"
            tools.mkdir()
            log = root / "native-image-args.json"
            fake = tools / "native-image"
            fake.write_text(
                "#!/usr/bin/env python3\n"
                "import json, pathlib, sys\n"
                f"pathlib.Path({str(log)!r}).write_text(json.dumps(sys.argv[1:]))\n"
                "pathlib.Path(sys.argv[-1]).write_bytes(b'native executable')\n",
                encoding="utf-8",
            )
            fake.chmod(0o755)
            fake_java = tools / "java"
            fake_java.write_text(
                "#!/usr/bin/env sh\n"
                "echo 'openjdk version \"22.0.2\"' >&2\n",
                encoding="utf-8",
            )
            fake_java.chmod(0o755)
            output = root / "dist"
            env = os.environ.copy()
            env["PATH"] = str(tools) + os.pathsep + env.get("PATH", "")
            result = subprocess.run(
                [SHELL, str(script), "native-image", str(output), "worker-test"],
                env=env, capture_output=True, text=True,
            )
            self.assertEqual(0, result.returncode, result.stderr)
            args = json.loads(log.read_text(encoding="utf-8"))
            self.assertIn("--no-fallback", args)
            self.assertIn("--enable-url-protocols=http,https", args)
            self.assertIn("--initialize-at-run-time=org.duckdb,java.sql.SQLException,java.sql.DriverManager", args)
            self.assertIn("-H:+UnlockExperimentalVMOptions", args)
            self.assertTrue(any(value.startswith("-H:ConfigurationFileDirectories=") for value in args))
            self.assertEqual("-jar", args[-3])
            artifact = output / "worker-test"
            manifest = json.loads((output / "worker-test.sha256.json").read_text(encoding="utf-8"))
            self.assertEqual(hashlib.sha256(artifact.read_bytes()).hexdigest(), manifest["sha256"])

    def test_rejects_jdk21_for_native_image(self) -> None:
        if SHELL is None:
            self.skipTest("POSIX sh is unavailable")
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            script = self._repo(root / "repo")
            tools = root / "tools"
            tools.mkdir()
            fake = tools / "native-image"
            fake.write_text("#!/usr/bin/env sh\nexit 99\n", encoding="utf-8")
            fake.chmod(0o755)
            fake_java = tools / "java"
            fake_java.write_text(
                "#!/usr/bin/env sh\n"
                "echo 'openjdk version \"21.0.12\"' >&2\n",
                encoding="utf-8",
            )
            fake_java.chmod(0o755)
            env = os.environ.copy()
            env["PATH"] = str(tools) + os.pathsep + env.get("PATH", "")
            result = subprocess.run(
                [SHELL, str(script), "native-image", str(root / "dist")],
                env=env, capture_output=True, text=True,
            )
            self.assertEqual(2, result.returncode)
            self.assertIn("JDK 22+ is required", result.stderr)
            self.assertFalse((root / "dist").exists())

    def test_missing_native_image_fails_with_actionable_prerequisite(self) -> None:
        if SHELL is None:
            self.skipTest("POSIX sh is unavailable")
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            script = self._repo(root / "repo")
            env = os.environ.copy()
            env["PATH"] = "/usr/bin:/bin"
            result = subprocess.run(
                [SHELL, str(script), "native-image-definitely-not-installed", str(root / "dist")],
                env=env, capture_output=True, text=True,
            )
            self.assertEqual(2, result.returncode)
            self.assertIn("GraalVM Native Image prerequisite is missing", result.stderr)
            self.assertFalse((root / "dist").exists())

    def test_rejects_unsafe_binary_name_before_invoking_tool(self) -> None:
        if SHELL is None:
            self.skipTest("POSIX sh is unavailable")
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            script = self._repo(root / "repo")
            result = subprocess.run(
                [SHELL, str(script), "missing-native-image", str(root / "dist"), "../escape"],
                capture_output=True, text=True,
            )
            self.assertEqual(2, result.returncode)
            self.assertIn("binary name must contain only", result.stderr)
            self.assertFalse((root / "dist").exists())

    def test_build_config_includes_only_the_host_duckdb_library(self) -> None:
        config = PACKAGING.parent / "runtime-worker/src/main/resources/META-INF/native-image/io.zingg.duckdb/runtime-worker/resource-config.json"
        document = json.loads(config.read_text(encoding="utf-8"))
        patterns = [entry["pattern"] for entry in document["resources"]["includes"]]
        self.assertFalse(any("libduckdb_java" in pattern for pattern in patterns))
        with tempfile.TemporaryDirectory() as temporary:
            output = Path(temporary) / "config"
            CONFIG_BUILDER.prepare(config.parent, output, "Linux", "x86_64")
            generated = json.loads((output / "resource-config.json").read_text(encoding="utf-8"))
            native_patterns = [entry["pattern"] for entry in generated["resources"]["includes"]
                               if "libduckdb_java" in entry["pattern"]]
            self.assertEqual([".*libduckdb_java\\.so_linux_amd64$"], native_patterns)
            self.assertTrue((output / "jni-config.json").is_file())

    def test_selects_supported_library_for_each_platform_and_rejects_unknown_targets(self) -> None:
        cases = {
            ("Linux", "x86_64"): "libduckdb_java.so_linux_amd64",
            ("Linux", "aarch64"): "libduckdb_java.so_linux_arm64",
            ("Darwin", "arm64"): "libduckdb_java.so_osx_universal",
            ("Windows", "AMD64"): "libduckdb_java.so_windows_amd64",
        }
        for target, library in cases.items():
            with self.subTest(target=target):
                self.assertEqual(library, CONFIG_BUILDER.duckdb_library(*target))
        with self.assertRaisesRegex(ValueError, "unsupported Native Image target"):
            CONFIG_BUILDER.duckdb_library("Linux", "mips")

    def test_jni_metadata_registers_duckdb_callbacks_and_data_types(self) -> None:
        config = PACKAGING.parent / "runtime-worker/src/main/resources/META-INF/native-image/io.zingg.duckdb/runtime-worker/jni-config.json"
        entries = json.loads(config.read_text(encoding="utf-8"))
        by_name = {entry["name"]: entry for entry in entries}
        self.assertIn("execute", {method["name"] for method in by_name["org.duckdb.DuckDBScalarFunctionWrapper"]["methods"]})
        self.assertIn("executeFunction", {method["name"] for method in by_name["org.duckdb.DuckDBTableFunctionWrapper"]["methods"]})
        self.assertIn("org.duckdb.DuckDBVector", by_name)


if __name__ == "__main__":
    unittest.main()
