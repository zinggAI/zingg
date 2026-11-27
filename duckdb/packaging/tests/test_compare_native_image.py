"""Tests for repeatable Native Image/JVM comparison reporting."""
from __future__ import annotations

import importlib.util
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch


MODULE_PATH = Path(__file__).resolve().parents[1] / "compare_native_image.py"
SPEC = importlib.util.spec_from_file_location("compare_native_image", MODULE_PATH)
assert SPEC is not None and SPEC.loader is not None
COMPARATOR = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(COMPARATOR)


class CompareNativeImageTests(unittest.TestCase):
    WORKER_STATUS = {
        "process_resident_bytes": "100",
        "memory_limit": "1 GiB",
        "max_temp_directory_size": "1 GiB",
        "threads": "2",
        "max_rows": "1000",
        "max_collect_bytes": "4096",
        "max_output_bytes": "4096",
        "max_spill_bytes": "4096",
        "offline_mode": "true",
        "unsafe_debug_sql": "true",
        "allowed_extensions": "",
    }

    def test_summary_reports_median_range_and_population_deviation(self) -> None:
        samples = [{"nativeImage": {"startupToPingSeconds": value}}
                   for value in (1.0, 2.0, 3.0)]
        summary = COMPARATOR._summary(samples, "startupToPingSeconds", "nativeImage")
        self.assertEqual(3, summary["samples"])
        self.assertEqual(2.0, summary["median"])
        self.assertEqual(1.0, summary["min"])
        self.assertEqual(3.0, summary["max"])
        self.assertAlmostEqual(0.81649658, summary["populationStdDev"])

    def test_comparison_runs_warmups_and_alternates_measured_order(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            native = root / "native-worker"
            jar = root / "worker.jar"
            native.write_bytes(b"native")
            native.chmod(0o755)
            jar.write_bytes(b"jar")
            calls: list[str] = []

            def run(name: str, command: list[str], native_path: Path | None,
                    work: Path, match_sizes: tuple[int, ...]) -> dict[str, object]:
                calls.append(name)
                return {
                    "startupToPingSeconds": float(len(calls)),
                    "ping": "pong",
                    "queryRowCounts": [1, 2],
                    "status": dict(self.WORKER_STATUS),
                    "processResidentBytesAtStartup": 100,
                    "processResidentBytesAfterMatch": 120,
                    "modelReloadMatchRows": 1,
                    "matches": [{"id": "same"}],
                    "matchWorkloadsBySize": {
                        str(size): {
                            "inputRows": size,
                            "outputRows": size // 2,
                            "inputBytes": size * 10,
                            "outputBytes": size * 20,
                            "matchLatencySeconds": 0.1 * size,
                            "inputRowsPerSecond": 10.0,
                            "outputRowsPerSecond": 5.0,
                            "processResidentBytesAfterMatch": 120,
                            "tempDirectoryBytesAfterMatch": 0,
                            "matchOutputSha256": f"{size:064x}",
                            "matches": [{"id": f"{size}-same"}],
                        }
                        for size in match_sizes
                    },
                    "artifactBytes": native_path.stat().st_size if native_path else None,
                }

            with patch.object(COMPARATOR, "_run_worker", side_effect=run), \
                    patch.object(COMPARATOR, "_runtime_version", return_value=("/usr/bin/java", "Java test runtime")):
                report = COMPARATOR.compare(native, jar, "java", None,
                                            warmups=1, repetitions=2,
                                            native_image_toolchain_version="GraalVM test toolchain")

        self.assertEqual(["native", "jvm", "jvm", "native", "native", "jvm"], calls)
        self.assertEqual("zingg-duckdb-native-image-comparison-4", report["schema"])
        self.assertEqual(3, len(report["runs"]))
        self.assertEqual(2, report["environment"]["measuredRepetitions"])
        self.assertEqual(2, report["summary"]["nativeImage"]["startupToPingSeconds"]["samples"])
        self.assertEqual("Java test runtime", report["environment"]["javaRuntimeVersion"])
        self.assertEqual("GraalVM test toolchain", report["environment"]["nativeImageToolchainVersion"])
        self.assertTrue(report["environment"]["architecture"])
        self.assertEqual([64, 128, 256], report["environment"]["matchInputSizes"])
        self.assertEqual(2, report["summary"]["nativeImage"]["matchWorkloads"]["128"]
                         ["inputRowsPerSecond"]["samples"])
        self.assertNotIn("matches", report["runs"][0]["nativeImage"]["matchWorkloadsBySize"]["64"])
        self.assertEqual("PASS", report["functionalParity"])

    def test_parity_rejects_different_resource_or_runtime_configuration(self) -> None:
        native = {
            "ping": "pong", "queryRowCounts": [1, 2], "modelReloadMatchRows": 1,
            "matches": [], "status": dict(self.WORKER_STATUS),
        }
        jvm = {**native, "status": {**self.WORKER_STATUS, "threads": "8"}}
        with self.assertRaisesRegex(AssertionError, "resource/configuration settings differ"):
            COMPARATOR._assert_parity(native, jvm)

    def test_parity_rejects_status_without_configuration_evidence(self) -> None:
        native = {
            "ping": "pong", "queryRowCounts": [1, 2], "modelReloadMatchRows": 1,
            "matches": [], "status": {"process_resident_bytes": "100"},
        }
        with self.assertRaisesRegex(AssertionError, "configuration fields are missing"):
            COMPARATOR._assert_parity(native, native)

    def test_parity_compares_every_row_in_each_scaling_workload(self) -> None:
        workload = {
            "inputRows": 8, "outputRows": 1, "inputBytes": 100, "outputBytes": 80,
            "matchOutputSha256": "a" * 64, "matches": [{"left": "a", "right": "b"}],
        }
        native = {
            "ping": "pong", "queryRowCounts": [1, 2], "modelReloadMatchRows": 1,
            "matches": [], "status": dict(self.WORKER_STATUS),
            "matchWorkloadsBySize": {"8": workload},
        }
        jvm = {**native, "matchWorkloadsBySize": {"8": {**workload, "matches": [{"left": "a", "right": "c"}]}}}
        with self.assertRaisesRegex(AssertionError, "complete MATCH rows differ"):
            COMPARATOR._assert_parity(native, jvm)

    def test_requires_multiple_measured_repetitions(self) -> None:
        with self.assertRaisesRegex(ValueError, "at least 2"):
            COMPARATOR.compare(Path("missing"), Path("missing"), "java", None,
                               repetitions=1)

    def test_requires_native_image_toolchain_provenance(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            native = Path(temporary) / "native"
            jar = Path(temporary) / "worker.jar"
            native.write_bytes(b"native")
            native.chmod(0o755)
            jar.write_bytes(b"jar")
            with patch.dict(COMPARATOR.os.environ, {}, clear=True):
                with self.assertRaisesRegex(ValueError, "toolchain version must be recorded"):
                    COMPARATOR.compare(native, jar, "java", None, repetitions=2)

    def test_requires_strictly_increasing_match_workload_sizes(self) -> None:
        for sizes in ((), (64, 64), (128, 64), (2, 64)):
            with self.subTest(sizes=sizes), self.assertRaisesRegex(ValueError, "workload sizes"):
                COMPARATOR.compare(Path("missing"), Path("missing"), "java", None,
                                   repetitions=2, native_image_toolchain_version="test",
                                   match_sizes=sizes)


if __name__ == "__main__":
    unittest.main()
