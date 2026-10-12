import importlib.util
import hashlib
import math
from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[2]
SPEC = importlib.util.spec_from_file_location(
    "benchmark_report", ROOT / "packaging" / "verify-benchmark-report.py"
)
REPORT = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
SPEC.loader.exec_module(REPORT)


def _stats(samples):
    mean = sum(samples) / len(samples)
    return {
        "minNs": min(samples),
        "maxNs": max(samples),
        "meanNs": round(mean, 3),
        "populationStdDevNs": round(math.sqrt(sum((x - mean) ** 2 for x in samples) / len(samples)), 3),
    }


def sample_report():
    size, repetitions, file_bytes = 10, 3, 100
    jdbc = [1000, 3000, 2000]
    appender = [100, 300, 200]
    write = [50, 60, 70]
    return {
        "benchmark": "zingg-duckdb-arrow-ingress",
        "size": size,
        "repetitions": repetitions,
        "warmupRuns": 1,
        "fileBytes": file_bytes,
        "writeMedianNs": 60,
        "ingestMedianNs": 2000,
        "ingestRowsPerSecond": 5_000_000,
        "ingestBytesPerSecond": 50_000_000,
        "appenderMedianNs": 200,
        "appenderRowsPerSecond": 50_000_000,
        "appenderSpeedup": 10.0,
        "writeSamplesNs": write,
        "jdbcIngestSamplesNs": jdbc,
        "appenderIngestSamplesNs": appender,
        "writeStats": _stats(write),
        "jdbcIngestStats": _stats(jdbc),
        "appenderIngestStats": _stats(appender),
        "resourceSamplingEnabled": False,
        "resourceSamplingIntervalMs": 0,
        "jdbcMaxObservedRssBytes": [-1] * repetitions,
        "jdbcMaxObservedSpillBytes": [-1] * repetitions,
        "jdbcResourceSampleCounts": [0] * repetitions,
        "appenderMaxObservedRssBytes": [-1] * repetitions,
        "appenderMaxObservedSpillBytes": [-1] * repetitions,
        "appenderResourceSampleCounts": [0] * repetitions,
        "processRssBeforeBenchmarkBytes": 100,
        "processRssAfterBenchmarkBytes": 200,
        "spillDirectoryBytesBefore": 0,
        "spillDirectoryBytesAfter": 0,
    }


class BenchmarkReportTest(unittest.TestCase):
    def test_accepts_consistent_timing_only_report(self):
        REPORT.validate_arrow_report(sample_report(), resource_sampling=False)

    def test_rejects_median_not_derived_from_samples(self):
        report = sample_report()
        report["ingestMedianNs"] = 1999
        with self.assertRaisesRegex(ValueError, "does not match raw samples"):
            REPORT.validate_arrow_report(report)

    def test_rejects_inconsistent_throughput_and_speedup(self):
        report = sample_report()
        report["ingestRowsPerSecond"] += 1
        with self.assertRaisesRegex(ValueError, "ingestRowsPerSecond"):
            REPORT.validate_arrow_report(report)
        report = sample_report()
        report["appenderSpeedup"] = 9.0
        with self.assertRaisesRegex(ValueError, "appenderSpeedup"):
            REPORT.validate_arrow_report(report)

    def test_rejects_statistics_that_do_not_match_samples(self):
        report = sample_report()
        report["jdbcIngestStats"]["populationStdDevNs"] = 0
        with self.assertRaisesRegex(ValueError, "populationStdDevNs does not match"):
            REPORT.validate_arrow_report(report)

    def test_rejects_wrong_sample_count_and_boolean_as_integer(self):
        report = sample_report()
        report["writeSamplesNs"] = [10, 20]
        with self.assertRaisesRegex(ValueError, "writeSamplesNs"):
            REPORT.validate_arrow_report(report)
        report = sample_report()
        report["size"] = True
        with self.assertRaisesRegex(ValueError, "size must be an integer"):
            REPORT.validate_arrow_report(report)

    def test_validates_resource_sample_vectors_when_enabled(self):
        report = sample_report()
        report["resourceSamplingEnabled"] = True
        report["resourceSamplingIntervalMs"] = 50
        for prefix in ("jdbc", "appender"):
            report[f"{prefix}MaxObservedRssBytes"] = [100, 110, 120]
            report[f"{prefix}MaxObservedSpillBytes"] = [0, 0, 12]
            report[f"{prefix}ResourceSampleCounts"] = [2, 3, 4]
        report["processRssBeforeBenchmarkBytes"] = 10
        report["processRssAfterBenchmarkBytes"] = 20
        REPORT.validate_arrow_report(report, resource_sampling=True)
        report["appenderResourceSampleCounts"][1] = 1
        with self.assertRaisesRegex(ValueError, "sample counts must be at least two"):
            REPORT.validate_arrow_report(report, resource_sampling=True)

    def test_accepts_consistent_classifier_training_report(self):
        REPORT.validate_classifier_training_report(classifier_report())

    def test_rejects_classifier_training_cardinality_or_configuration_drift(self):
        report = classifier_report()
        report["cases"][0]["trainingRows"][1] -= 1
        with self.assertRaisesRegex(ValueError, "full input cardinality"):
            REPORT.validate_classifier_training_report(report)
        report = classifier_report()
        report["cases"][0]["trainingConfig"] = "width=4;iterations=20;learningRate=0.05"
        with self.assertRaisesRegex(ValueError, "configuration does not match"):
            REPORT.validate_classifier_training_report(report)

    def test_rejects_classifier_training_without_learning_signal_or_valid_digest(self):
        report = classifier_report()
        report["cases"][0]["lossByMeasuredRun"][0] = math.log(2)
        with self.assertRaisesRegex(ValueError, "must beat"):
            REPORT.validate_classifier_training_report(report)
        report = classifier_report()
        report["cases"][0]["inputDigest"] = "0" * 16
        with self.assertRaisesRegex(ValueError, "inputDigest does not match"):
            REPORT.validate_classifier_training_report(report)
        report = classifier_report()
        report["cases"][0]["gradientPlanSha256"] = "0" * 64
        with self.assertRaisesRegex(ValueError, "gradientPlanSha256 does not match"):
            REPORT.validate_classifier_training_report(report)


def classifier_report():
    rows, width, repetitions, iterations = 10, 2, 2, 20
    identity = f"rows={rows};width={width};generator=deterministic-v1"
    config = "width=2;iterations=20;learningRate=0.05;l2=0.0001;threads=4;jdk=21;duckdb=1.5.5.1"
    gradient_plan = "UNGROUPED_AGGREGATE\nTABLE_SCAN"
    digest = lambda value: hashlib.sha256(value.encode("utf-8")).hexdigest()
    return {
        "benchmark": "zingg-duckdb-native-classifier-training",
        "schemaVersion": 2,
        "capturedAt": "2026-10-02T00:00:00Z",
        "javaVersion": "21",
        "os": "Linux",
        "architecture": "amd64",
        "sourceRevision": "test-revision",
        "worktreeState": "clean",
        "containerImage": "python:test",
        "availableProcessors": 4,
        "maxHeapBytes": 4096,
        "duckdbVersion": "1.5.5.1",
        "warmupRuns": 1,
        "repetitions": repetitions,
        "iterations": iterations,
        "cases": [{
            "rows": rows,
            "featureWidth": width,
            "inputGenerator": "deterministic-v1",
            "inputIdentity": identity,
            "inputDigest": digest(identity),
            "trainingConfig": config,
            "configurationDigest": digest(config),
            "gradientPlanCaptureStage": "excluded_warmup_iteration_0",
            "firstGradientPhysicalPlan": gradient_plan,
            "gradientPlanSha256": digest(gradient_plan),
            "trainingRows": [rows] * repetitions,
            "elapsedNs": [1000, 2000],
            "elapsedMedianNs": 2000,
            "rowsPerSecondMedian": 5_000_000,
            "warmupLoss": 0.68,
            "randomClassifierBaselineLoss": math.log(2),
            "lossByMeasuredRun": [0.67, 0.66],
            "artifactBytes": [100, 100],
            "rssBeforeBytes": [1000, 1000],
            "rssAfterBytes": [1200, 1200],
            "spillBeforeBytes": [0, 0],
            "spillAfterBytes": [0, 0],
        }],
    }


if __name__ == "__main__":
    unittest.main()
