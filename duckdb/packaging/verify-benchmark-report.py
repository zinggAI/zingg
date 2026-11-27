#!/usr/bin/env python3
"""Validate internal consistency of BenchmarkMain Arrow-ingress JSON reports."""
from __future__ import annotations

import argparse
import hashlib
import json
import math
from pathlib import Path
from typing import Any


def _integer(report: dict[str, Any], key: str, *, minimum: int = 1) -> int:
    value = report.get(key)
    if isinstance(value, bool) or not isinstance(value, int) or value < minimum:
        raise ValueError(f"{key} must be an integer >= {minimum}; got {value!r}")
    return value


def _samples(report: dict[str, Any], key: str, repetitions: int) -> list[int]:
    values = report.get(key)
    if (not isinstance(values, list) or len(values) != repetitions
            or any(isinstance(value, bool) or not isinstance(value, int) or value <= 0
                   for value in values)):
        raise ValueError(f"{key} must contain {repetitions} positive integer samples; got {values!r}")
    return values


def _assert_close(report: dict[str, Any], key: str, expected: float, *, tolerance: float = 0.001) -> None:
    actual = report.get(key)
    if isinstance(actual, bool) or not isinstance(actual, (int, float)) or not math.isfinite(actual):
        raise ValueError(f"{key} must be a finite number; got {actual!r}")
    if not math.isclose(actual, expected, rel_tol=1e-12, abs_tol=tolerance):
        raise ValueError(f"{key} is inconsistent with samples: expected {expected}, got {actual}")


def _validate_timing(samples: list[int], report: dict[str, Any], prefix: str) -> int:
    ordered = sorted(samples)
    stats = report.get(f"{prefix}Stats")
    if not isinstance(stats, dict):
        raise ValueError(f"{prefix}Stats must be an object")
    if stats.get("minNs") != min(samples) or stats.get("maxNs") != max(samples):
        raise ValueError(f"{prefix}Stats min/max do not match raw samples")
    mean = sum(samples) / len(samples)
    stddev = math.sqrt(sum((sample - mean) ** 2 for sample in samples) / len(samples))
    for key, expected in (("meanNs", mean), ("populationStdDevNs", stddev)):
        actual = stats.get(key)
        if isinstance(actual, bool) or not isinstance(actual, (int, float)) or not math.isfinite(actual):
            raise ValueError(f"{prefix}Stats.{key} must be finite")
        if not math.isclose(actual, expected, rel_tol=1e-9, abs_tol=0.001):
            raise ValueError(f"{prefix}Stats.{key} does not match raw samples")
    median = ordered[len(ordered) // 2]  # BenchmarkMain uses the upper middle sample.
    median_key = {"write": "writeMedianNs", "jdbcIngest": "ingestMedianNs",
                  "appenderIngest": "appenderMedianNs"}[prefix]
    if report.get(median_key) != median:
        raise ValueError(f"{median_key} does not match raw samples")
    return median


def validate_arrow_report(report: dict[str, Any], *, resource_sampling: bool | None = None) -> None:
    if report.get("benchmark") != "zingg-duckdb-arrow-ingress":
        raise ValueError("benchmark must be zingg-duckdb-arrow-ingress")
    size = _integer(report, "size")
    repetitions = _integer(report, "repetitions")
    if _integer(report, "warmupRuns", minimum=0) != 1:
        raise ValueError("exactly one excluded warmup is required")
    file_bytes = _integer(report, "fileBytes")
    enabled = report.get("resourceSamplingEnabled")
    if not isinstance(enabled, bool):
        raise ValueError("resourceSamplingEnabled must be boolean")
    if resource_sampling is not None and enabled is not resource_sampling:
        raise ValueError(f"resource sampling must be {resource_sampling}")
    expected_interval = 50 if enabled else 0
    if report.get("resourceSamplingIntervalMs") != expected_interval:
        raise ValueError(f"resourceSamplingIntervalMs must be {expected_interval}")

    write_samples = _samples(report, "writeSamplesNs", repetitions)
    jdbc_samples = _samples(report, "jdbcIngestSamplesNs", repetitions)
    appender_samples = _samples(report, "appenderIngestSamplesNs", repetitions)
    _validate_timing(write_samples, report, "write")
    jdbc_median = _validate_timing(jdbc_samples, report, "jdbcIngest")
    appender_median = _validate_timing(appender_samples, report, "appenderIngest")
    for key, expected in (
        ("ingestRowsPerSecond", math.floor(size * 1_000_000_000 / jdbc_median + 0.5)),
        ("ingestBytesPerSecond", math.floor(file_bytes * 1_000_000_000 / jdbc_median + 0.5)),
        ("appenderRowsPerSecond", math.floor(size * 1_000_000_000 / appender_median + 0.5)),
    ):
        if _integer(report, key) != expected:
            raise ValueError(f"{key} does not match size/file bytes and measured median")
    _assert_close(report, "appenderSpeedup", jdbc_median / appender_median, tolerance=0.00051)

    for key in ("processRssBeforeBenchmarkBytes", "processRssAfterBenchmarkBytes",
                "spillDirectoryBytesBefore", "spillDirectoryBytesAfter"):
        _integer(report, key, minimum=0)
    if enabled and (report["processRssBeforeBenchmarkBytes"] == 0
                    or report["processRssAfterBenchmarkBytes"] == 0):
        raise ValueError("resource-sampling report must include positive process RSS endpoints")
    for prefix in ("jdbc", "appender"):
        rss = report.get(f"{prefix}MaxObservedRssBytes")
        spill = report.get(f"{prefix}MaxObservedSpillBytes")
        counts = report.get(f"{prefix}ResourceSampleCounts")
        if not all(isinstance(values, list) and len(values) == repetitions for values in (rss, spill, counts)):
            raise ValueError(f"{prefix} resource vectors must each contain {repetitions} entries")
        if enabled:
            if any(isinstance(value, bool) or not isinstance(value, int) or value <= 0 for value in rss):
                raise ValueError(f"{prefix} RSS samples must be positive integers")
            if any(isinstance(value, bool) or not isinstance(value, int) or value < 0 for value in spill):
                raise ValueError(f"{prefix} spill samples must be nonnegative integers")
            if any(isinstance(value, bool) or not isinstance(value, int) or value < 2 for value in counts):
                raise ValueError(f"{prefix} resource sample counts must be at least two")
        elif rss != [-1] * repetitions or spill != [-1] * repetitions or counts != [0] * repetitions:
            raise ValueError(f"{prefix} resource vectors must use disabled-sampling sentinels")


def validate_classifier_training_report(report: dict[str, Any]) -> None:
    if report.get("benchmark") != "zingg-duckdb-native-classifier-training" or report.get("schemaVersion") != 2:
        raise ValueError("unsupported classifier-training benchmark schema")
    for key in ("capturedAt", "javaVersion", "os", "architecture", "duckdbVersion",
                "sourceRevision", "worktreeState", "containerImage"):
        if not isinstance(report.get(key), str) or not report[key].strip():
            raise ValueError(f"{key} must be a non-empty string")
    _integer(report, "availableProcessors")
    _integer(report, "maxHeapBytes")
    repetitions = _integer(report, "repetitions")
    iterations = _integer(report, "iterations")
    if _integer(report, "warmupRuns", minimum=0) != 1:
        raise ValueError("exactly one excluded warmup is required")
    cases = report.get("cases")
    if not isinstance(cases, list) or not cases:
        raise ValueError("cases must be a non-empty array")
    baseline = math.log(2.0)
    seen: set[tuple[int, int]] = set()
    for index, case in enumerate(cases):
        if not isinstance(case, dict):
            raise ValueError(f"case {index} must be an object")
        rows = _integer(case, "rows", minimum=2)
        width = _integer(case, "featureWidth")
        if (rows, width) in seen:
            raise ValueError(f"case {index} duplicates a rows/featureWidth configuration")
        seen.add((rows, width))
        identity = f"rows={rows};width={width};generator=deterministic-v1"
        if case.get("inputGenerator") != "deterministic-v1" or case.get("inputIdentity") != identity:
            raise ValueError(f"case {index} input identity does not match rows and width")
        config = case.get("trainingConfig")
        if not isinstance(config, str) or not config.startswith(f"width={width};iterations={iterations};"):
            raise ValueError(f"case {index} training configuration does not match feature width/iterations")
        for key, value in (("inputDigest", identity), ("configurationDigest", config)):
            digest = case.get(key)
            expected = hashlib.sha256(value.encode("utf-8")).hexdigest()
            if digest != expected:
                raise ValueError(f"case {index} {key} does not match its identity")
        plan = case.get("firstGradientPhysicalPlan")
        if not isinstance(plan, str) or not plan.strip():
            raise ValueError(f"case {index} firstGradientPhysicalPlan must be non-empty")
        if case.get("gradientPlanCaptureStage") != "excluded_warmup_iteration_0":
            raise ValueError(f"case {index} physical plan must be captured outside measured training runs")
        if "AGGREGATE" not in plan.upper():
            raise ValueError(f"case {index} gradient plan does not show an aggregate operator")
        if case.get("gradientPlanSha256") != hashlib.sha256(plan.encode("utf-8")).hexdigest():
            raise ValueError(f"case {index} gradientPlanSha256 does not match the captured plan")
        if case.get("trainingRows") != [rows] * repetitions:
            raise ValueError(f"case {index} trainingRows must prove full input cardinality for every run")
        elapsed = _samples(case, "elapsedNs", repetitions)
        median = sorted(elapsed)[len(elapsed) // 2]
        if case.get("elapsedMedianNs") != median:
            raise ValueError(f"case {index} elapsedMedianNs does not match raw samples")
        expected_throughput = math.floor(rows * 1_000_000_000 / median + 0.5)
        if _integer(case, "rowsPerSecondMedian") != expected_throughput:
            raise ValueError(f"case {index} rowsPerSecondMedian is inconsistent with elapsed time")
        random_baseline = case.get("randomClassifierBaselineLoss")
        if isinstance(random_baseline, bool) or not isinstance(random_baseline, (int, float)) \
                or not math.isclose(random_baseline, baseline, rel_tol=1e-12, abs_tol=1e-12):
            raise ValueError(f"case {index} randomClassifierBaselineLoss must equal ln(2)")
        losses = case.get("lossByMeasuredRun")
        if not isinstance(losses, list) or len(losses) != repetitions:
            raise ValueError(f"case {index} lossByMeasuredRun must contain {repetitions} values")
        for key, value in [("warmupLoss", case.get("warmupLoss"))] + [
                (f"lossByMeasuredRun[{i}]", value) for i, value in enumerate(losses)]:
            if isinstance(value, bool) or not isinstance(value, (int, float)) or not math.isfinite(value):
                raise ValueError(f"case {index} {key} must be finite")
        if any(value >= baseline for value in losses):
            raise ValueError(f"case {index} measured loss must beat the random-classifier baseline")
        for key in ("rssBeforeBytes", "rssAfterBytes", "spillBeforeBytes", "spillAfterBytes", "artifactBytes"):
            minimum = 1 if key.startswith("rss") or key == "artifactBytes" else 0
            values = case.get(key)
            if not isinstance(values, list) or len(values) != repetitions \
                    or any(isinstance(value, bool) or not isinstance(value, int) or value < minimum for value in values):
                raise ValueError(f"case {index} {key} must contain {repetitions} valid measurements")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("report", type=Path)
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument("--timing-only", action="store_true")
    mode.add_argument("--resource-sampling", action="store_true")
    mode.add_argument("--classifier-training", action="store_true")
    args = parser.parse_args()
    report = json.loads(args.report.read_text(encoding="utf-8"))
    if not isinstance(report, dict):
        raise SystemExit("benchmark report must contain a JSON object")
    if args.classifier_training:
        validate_classifier_training_report(report)
    else:
        validate_arrow_report(report, resource_sampling=args.resource_sampling)
    marker = "CLASSIFIER_TRAINING_BENCHMARK_REPORT_VALID" if args.classifier_training else "ARROW_BENCHMARK_REPORT_VALID"
    print(f"{marker} {args.report}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
