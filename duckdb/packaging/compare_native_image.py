"""Compare a Linux Native Image worker with the JVM worker on protocol and MATCH behavior."""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
import os
import platform
import shutil
import statistics
import subprocess
import sys
import tempfile
import time
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "python"))
from zingg_duckdb.worker import DuckWorker  # noqa: E402


WORKER_CONFIGURATION_FIELDS = (
    "memory_limit", "max_temp_directory_size", "threads", "max_rows",
    "max_collect_bytes", "max_output_bytes", "max_spill_bytes",
    "offline_mode", "unsafe_debug_sql", "allowed_extensions",
)


def _write_csv(path: Path, header: list[str], rows: list[tuple[Any, ...]]) -> None:
    with path.open("w", encoding="utf-8", newline="") as stream:
        writer = csv.writer(stream, lineterminator="\n")
        writer.writerow(header)
        writer.writerows(rows)


def _write_match_records(path: Path, row_count: int) -> None:
    _write_csv(path, ["id", "block", "feature"],
               [(f"R{index:06d}", "benchmark", index % 2) for index in range(row_count)])


def _run_worker(name: str, command: list[str], native_binary: Path | None,
                work: Path, match_sizes: tuple[int, ...]) -> dict[str, Any]:
    work.mkdir(parents=True)
    training = work / "training.csv"
    records = work / "records.csv"
    artifact = work / "classifier"
    output = work / "matches.csv"
    _write_csv(training, ["feature", "label"], [(0, 0), (0.1, 0), (0.9, 1), (1, 1)])
    _write_csv(records, ["id", "block", "feature"], [("A", "g", 0), ("B", "g", 1), ("C", "g", 0)])

    start = time.perf_counter()
    worker = DuckWorker(command=command)
    try:
        ping = worker.ping()
        startup_seconds = time.perf_counter() - start
        status = worker.status()
        count_single = worker.count("SELECT 42")
        count_two = worker.count("SELECT 42 UNION ALL SELECT 43")
        model_path = worker.train_classifier(
            str(training), str(artifact), "label", ["feature"],
            iterations=500, learning_rate=0.2, l2=0.001,
        )
        output_rows = worker.run(
            "MATCH", str(records), str(output), classifier_model=model_path,
            id_column="id", blocking_column="block", threshold=0.5,
        )
        with output.open(encoding="utf-8", newline="") as stream:
            matches = list(csv.DictReader(stream))
        if count_single != 1 or count_two != 2:
            raise AssertionError(f"{name} SQL row-count smoke failed: {(count_single, count_two)}")
        if ping != "pong" or output_rows != len(matches) or not matches:
            raise AssertionError(f"{name} protocol/model MATCH smoke failed")
        workloads: dict[str, dict[str, Any]] = {}
        status_after_match: dict[str, str] = status
        for size in match_sizes:
            _write_match_records(records, size)
            start_match = time.perf_counter()
            rows_written = worker.run(
                "MATCH", str(records), str(output), classifier_model=model_path,
                id_column="id", blocking_column="block", threshold=0.5,
            )
            match_seconds = time.perf_counter() - start_match
            with output.open(encoding="utf-8", newline="") as stream:
                workload_matches = list(csv.DictReader(stream))
            if rows_written != len(workload_matches) or rows_written <= 0 or match_seconds <= 0:
                raise AssertionError(
                    f"{name} MATCH scaling workload failed for {size} input rows: "
                    f"reported={rows_written}, observed={len(workload_matches)}"
                )
            for match in workload_matches:
                try:
                    score = float(match["z_score"])
                    prediction = int(match["z_prediction"])
                except (KeyError, ValueError) as error:
                    raise AssertionError(f"{name} MATCH output lacks score/prediction: {match}") from error
                if prediction != 1 or score < 0.5:
                    raise AssertionError(f"{name} MATCH output contains a below-threshold row: {match}")
            status_after = worker.status()
            status_after_match = status_after
            input_bytes = records.stat().st_size
            output_bytes = output.stat().st_size
            workloads[str(size)] = {
                "inputRows": size,
                "outputRows": rows_written,
                "inputBytes": input_bytes,
                "outputBytes": output_bytes,
                "matchLatencySeconds": round(match_seconds, 6),
                "inputRowsPerSecond": round(size / match_seconds, 3),
                "outputRowsPerSecond": round(rows_written / match_seconds, 3),
                "processResidentBytesAfterMatch": int(status_after["process_resident_bytes"]),
                "tempDirectoryBytesAfterMatch": int(status_after["temp_directory_used_bytes"]),
                "matchOutputSha256": hashlib.sha256(output.read_bytes()).hexdigest(),
                "matches": workload_matches,
            }
        return {
            "startupToPingSeconds": round(startup_seconds, 6),
            "ping": ping,
            "queryRowCounts": [count_single, count_two],
            "status": status,
            "processResidentBytesAtStartup": int(status["process_resident_bytes"]),
            "processResidentBytesAfterMatch": int(status_after_match["process_resident_bytes"]),
            "modelReloadMatchRows": output_rows,
            "matches": matches,
            "matchWorkloadsBySize": workloads,
            "artifactBytes": native_binary.stat().st_size if native_binary else None,
        }
    finally:
        worker.close()


def _assert_parity(native_result: dict[str, Any], jvm_result: dict[str, Any]) -> None:
    comparable = ("ping", "queryRowCounts", "modelReloadMatchRows", "matches")
    differences = {field: {"native": native_result[field], "jvm": jvm_result[field]}
                   for field in comparable if native_result[field] != jvm_result[field]}
    if differences:
        raise AssertionError(f"Native Image and JVM behavior differs: {json.dumps(differences, sort_keys=True)}")

    native_status = native_result.get("status", {})
    jvm_status = jvm_result.get("status", {})
    missing = {
        runtime: sorted(set(WORKER_CONFIGURATION_FIELDS) - set(status))
        for runtime, status in (("nativeImage", native_status), ("jvm", jvm_status))
    }
    missing = {runtime: fields for runtime, fields in missing.items() if fields}
    if missing:
        raise AssertionError(f"worker configuration fields are missing: {json.dumps(missing, sort_keys=True)}")
    config_differences = {
        field: {"nativeImage": native_status[field], "jvm": jvm_status[field]}
        for field in WORKER_CONFIGURATION_FIELDS if native_status[field] != jvm_status[field]
    }
    if config_differences:
        raise AssertionError(
            "Native Image and JVM resource/configuration settings differ: "
            + json.dumps(config_differences, sort_keys=True)
        )
    native_workloads = native_result.get("matchWorkloadsBySize", {})
    jvm_workloads = jvm_result.get("matchWorkloadsBySize", {})
    if set(native_workloads) != set(jvm_workloads):
        raise AssertionError("Native Image and JVM MATCH workload sizes differ")
    for size in native_workloads:
        native_rows = native_workloads[size]
        jvm_rows = jvm_workloads[size]
        if native_rows["matches"] != jvm_rows["matches"]:
            raise AssertionError(f"Native Image and JVM complete MATCH rows differ at input size {size}")
        for field in ("inputRows", "outputRows", "inputBytes", "outputBytes", "matchOutputSha256"):
            if native_rows[field] != jvm_rows[field]:
                raise AssertionError(
                    f"Native Image and JVM MATCH {field} differs at input size {size}"
                )


def _compact_run(result: dict[str, Any]) -> dict[str, Any]:
    compact = dict(result)
    compact_workloads = {}
    for size, workload in result["matchWorkloadsBySize"].items():
        compact_workload = dict(workload)
        compact_workload.pop("matches", None)
        compact_workloads[size] = compact_workload
    compact["matchWorkloadsBySize"] = compact_workloads
    return compact


def _runtime_version(java: str) -> tuple[str, str]:
    executable = shutil.which(java)
    if executable is None and Path(java).is_file():
        executable = str(Path(java).resolve())
    if executable is None:
        raise ValueError(f"Java executable is unavailable: {java}")
    result = subprocess.run([executable, "--version"], capture_output=True, text=True, timeout=20)
    version = (result.stdout + result.stderr).strip()
    if result.returncode != 0 or not version:
        raise ValueError(f"cannot capture Java runtime version from {executable}: {version}")
    return executable, version


def _summary(samples: list[dict[str, Any]], field: str, nested: str | None = None) -> dict[str, Any]:
    path = nested.split(".") if nested else []
    values = []
    for sample in samples:
        value: Any = sample
        for key in path:
            value = value[key]
        values.append(float(value[field]))
    return {
        "samples": len(values),
        "median": statistics.median(values),
        "min": min(values),
        "max": max(values),
        "populationStdDev": statistics.pstdev(values),
    }


def compare(native: Path, jar: Path, java: str, output_report: Path | None,
            warmups: int = 1, repetitions: int = 5,
            native_image_toolchain_version: str | None = None,
            match_sizes: tuple[int, ...] = (64, 128, 256)) -> dict[str, Any]:
    if warmups < 0:
        raise ValueError("warmups must be non-negative")
    if repetitions < 2:
        raise ValueError("repetitions must be at least 2 for a comparative benchmark")
    native_image_toolchain_version = (
        native_image_toolchain_version or os.environ.get("GRAALVM_VERSION") or ""
    ).strip()
    if not native_image_toolchain_version:
        raise ValueError("Native Image toolchain version must be recorded (--native-image-toolchain-version or GRAALVM_VERSION)")
    if (not match_sizes or any(size < 3 for size in match_sizes)
            or tuple(sorted(set(match_sizes))) != match_sizes):
        raise ValueError("MATCH workload sizes must be unique, strictly increasing integers of at least 3")
    native = native.resolve(strict=True)
    jar = jar.resolve(strict=True)
    if not os.access(native, os.X_OK):
        raise ValueError(f"Native Image artifact is not executable: {native}")
    java_executable, java_version = _runtime_version(java)
    native_command = [str(native), "--unsafe-debug-sql"]
    jvm_command = [java_executable, "--add-opens=java.base/java.nio=ALL-UNNAMED",
                   "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED", "-jar", str(jar),
                   "--unsafe-debug-sql"]
    with tempfile.TemporaryDirectory(prefix="zingg-native-jvm-compare-") as temporary:
        root = Path(temporary)
        measured: list[dict[str, Any]] = []
        all_runs: list[dict[str, Any]] = []
        for index in range(warmups + repetitions):
            order = ("native", "jvm") if index % 2 == 0 else ("jvm", "native")
            results: dict[str, dict[str, Any]] = {}
            for runtime in order:
                command, artifact = ((native_command, native) if runtime == "native"
                                     else (jvm_command, None))
                results[runtime] = _run_worker(
                    runtime, command, artifact, root / f"run-{index:03d}-{runtime}", match_sizes)
            _assert_parity(results["native"], results["jvm"])
            run = {"index": index, "order": list(order),
                   "nativeImage": _compact_run(results["native"]),
                   "jvm": _compact_run({**results["jvm"], "artifactBytes": jar.stat().st_size})}
            all_runs.append(run)
            if index >= warmups:
                measured.append(run)

    metrics = ("startupToPingSeconds", "processResidentBytesAfterMatch", "artifactBytes")
    summary = {runtime: {metric: _summary(measured, metric, runtime)
                         for metric in metrics}
               for runtime in ("nativeImage", "jvm")}
    for runtime in ("nativeImage", "jvm"):
        summary[runtime]["matchWorkloads"] = {
            str(size): {
                metric: _summary(measured, metric, f"{runtime}.matchWorkloadsBySize.{size}")
                for metric in ("matchLatencySeconds", "inputRowsPerSecond", "outputRowsPerSecond",
                               "processResidentBytesAfterMatch", "tempDirectoryBytesAfterMatch")
            }
            for size in match_sizes
        }
    report = {
        "schema": "zingg-duckdb-native-image-comparison-4",
        "platform": platform.platform(),
        "environment": {
            "operatingSystem": platform.system(),
            "osRelease": platform.release(),
            "architecture": platform.machine(),
            "python": platform.python_version(),
            "javaExecutable": Path(java_executable).name,
            "javaRuntimeVersion": java_version,
            "nativeImageToolchainVersion": native_image_toolchain_version,
            "launchCommands": {
                "nativeImage": ["<native-image-worker>", "--unsafe-debug-sql"],
                "jvm": [Path(java_executable).name, *jvm_command[1:3], "-jar", "<worker-jar>",
                        "--unsafe-debug-sql"],
            },
            "logicalCpuCount": os.cpu_count(),
            "nativeSha256": hashlib.sha256(native.read_bytes()).hexdigest(),
            "workerJarSha256": hashlib.sha256(jar.read_bytes()).hexdigest(),
            "warmups": warmups,
            "measuredRepetitions": repetitions,
            "matchInputSizes": list(match_sizes),
            "measurementPolicy": "fresh worker process per sample; paired runtimes alternate launch order",
        },
        "summary": summary,
        "runs": all_runs,
        "functionalParity": "PASS",
    }
    if output_report:
        output_report.parent.mkdir(parents=True, exist_ok=True)
        output_report.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    return report


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("native", type=Path, help="built Native Image executable")
    parser.add_argument("--worker-jar", type=Path,
                        default=ROOT / "runtime-worker/target/runtime-worker-0.1.0-SNAPSHOT.jar")
    parser.add_argument("--java", default=os.environ.get("JAVA_BIN", "java"))
    parser.add_argument("--native-image-toolchain-version", default=os.environ.get("GRAALVM_VERSION"),
                        help="GraalVM/native-image version used to build the supplied executable")
    parser.add_argument("--report", type=Path, help="optional JSON comparison report")
    parser.add_argument("--warmups", type=int, default=1,
                        help="unmeasured paired runs to discard (default: 1)")
    parser.add_argument("--repetitions", type=int, default=5,
                        help="measured paired runs; minimum 2 (default: 5)")
    parser.add_argument("--match-sizes", type=int, nargs="+", default=[64, 128, 256],
                        help="increasing MATCH input row counts (default: 64 128 256)")
    args = parser.parse_args()
    report = compare(args.native, args.worker_jar, args.java, args.report,
                     args.warmups, args.repetitions, args.native_image_toolchain_version,
                     tuple(args.match_sizes))
    print("NATIVE_JVM_COMPARISON_SUCCESS " + json.dumps(report, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
