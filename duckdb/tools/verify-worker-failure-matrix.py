"""Command-level negative training and matching cases for the persistent worker."""
from __future__ import annotations

import shutil
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "python"))
from zingg_duckdb.worker import DuckWorker  # noqa: E402

RECORDS = "id,block,feature\n1,a,1\n2,a,-1\n3,a,1\n"
VALID_LABELS = [("1", "2", "NON_MATCH", "negative"), ("1", "3", "MATCH", "positive")]


def must_fail(call, contains: str | None = None) -> None:
    try:
        call()
    except RuntimeError as error:
        if contains is not None:
            assert contains.lower() in str(error).lower(), error
    else:
        raise AssertionError("worker operation unexpectedly succeeded")


def train_failure(root: Path, command_prefix: list[str], name: str,
                  decisions: list[tuple[str, str, str, str]], records_text: str = RECORDS,
                  features: list[str] | None = None, expected: str | None = None) -> None:
    directory = root / name
    directory.mkdir()
    records = directory / "records.csv"
    records.write_text(records_text, encoding="utf-8")
    model = directory / "model"
    command = [*command_prefix, "--labels-root", str(directory / "labels")]
    with DuckWorker(command=command) as worker:
        if decisions:
            worker.apply_labels("v1", name + "-key", decisions)
        must_fail(lambda: worker.train_from_labels(str(records), str(model), "id", "block",
                                                  features or ["z_feature"]), expected)
        assert not model.exists(), f"{name}: failed training published an artifact"
        assert worker.ping() == "pong", f"{name}: worker could not be reused"
        must_fail(lambda: worker.client.request("train_from_labels malformed"), "payload")
        assert worker.ping() == "pong", f"{name}: malformed payload killed worker"


def model_failures(root: Path, command_prefix: list[str]) -> None:
    directory = root / "model-failures"
    directory.mkdir()
    records = directory / "records.csv"
    records.write_text(RECORDS, encoding="utf-8")
    model = directory / "good-model"
    command = [*command_prefix, "--labels-root", str(directory / "labels")]
    with DuckWorker(command=command) as worker:
        assert worker.apply_labels("v1", "good-key", VALID_LABELS)["applied"] == 2
        assert worker.train_from_labels(str(records), str(model), "id", "block", ["z_feature"]) == str(model)
    corrupt = directory / "corrupt-model"
    shutil.copytree(model, corrupt)
    payload = corrupt / "model.bin"
    bytes_ = bytearray(payload.read_bytes())
    bytes_[-1] ^= 1
    payload.write_bytes(bytes_)
    bad_output = directory / "corrupt-output.csv"
    with DuckWorker(command=command) as worker:
        must_fail(lambda: worker.run("MATCH", str(records), str(bad_output),
                                     classifier_model=str(corrupt), id_column="id",
                                     blocking_column="block"), "checksum")
        assert not bad_output.exists(), "corrupt model published output"
        assert worker.ping() == "pong"

    allowed = directory / "allowed-output"
    allowed.mkdir()
    outside = directory / "outside.csv"
    bounded = [*command_prefix, "--output-root", str(allowed), "--max-output-bytes", "1"]
    with DuckWorker(command=bounded) as worker:
        must_fail(lambda: worker.run("MATCH", str(records), str(outside),
                                     classifier_model=str(model), id_column="id",
                                     blocking_column="block"))
        assert not outside.exists(), "path-policy rejection published output"
        assert worker.ping() == "pong"
        limited = allowed / "too-large.csv"
        must_fail(lambda: worker.run("MATCH", str(records), str(limited),
                                     classifier_model=str(model), id_column="id",
                                     blocking_column="block"))
        assert not limited.exists(), "output-size rejection published output"
        assert worker.ping() == "pong"


def corrupt_snapshot_failure(root: Path, command_prefix: list[str]) -> None:
    directory = root / "corrupt-label-snapshot"
    directory.mkdir()
    label_root = directory / "labels"
    command = [*command_prefix, "--labels-root", str(label_root)]
    with DuckWorker(command=command) as worker:
        assert worker.apply_labels("v1", "seed", [VALID_LABELS[0]])["applied"] == 1
    snapshot = label_root / "labels.bin"
    bytes_ = bytearray(snapshot.read_bytes())
    bytes_[-1] ^= 1
    snapshot.write_bytes(bytes_)
    worker = DuckWorker(command=command)
    try:
        must_fail(worker.ping, "worker exited unexpectedly")
    finally:
        worker.client.close()
    assert snapshot.read_bytes() == bytes_, "failed startup modified the corrupt snapshot"


def main() -> None:
    if len(sys.argv) not in (2, 3):
        raise SystemExit("usage: verify-worker-failure-matrix.py <worker-jar> [java]")
    jar = Path(sys.argv[1]).resolve(strict=True)
    java = sys.argv[2] if len(sys.argv) == 3 else "java"
    with tempfile.TemporaryDirectory(prefix="zingg-failure-matrix-") as directory:
        root = Path(directory)
        command_prefix = [java, "-jar", str(jar)]
        train_failure(root, command_prefix, "no-labels", [], expected="no applied labels")
        train_failure(root, command_prefix, "one-class", [VALID_LABELS[1]], expected="positive and negative")
        train_failure(root, command_prefix, "unknown-only", [("1", "2", "UNKNOWN", "human")],
                      expected="no applied labels")
        train_failure(root, command_prefix, "unmatched-id",
                      [("1", "999", "NON_MATCH", "human"), VALID_LABELS[1]], expected="do not match")
        train_failure(root, command_prefix, "duplicate-id", VALID_LABELS,
                      records_text="id,block,feature\n1,a,1\n2,a,-1\n2,a,-1\n3,a,1\n")
        train_failure(root, command_prefix, "missing-feature", VALID_LABELS, features=["missing-feature"],
                      expected="feature column is not present")
        train_failure(root, command_prefix, "missing-block", VALID_LABELS,
                      records_text="id,feature\n1,1\n2,-1\n3,1\n")
        model_failures(root, command_prefix)
        corrupt_snapshot_failure(root, command_prefix)
    print("WORKER_FAILURE_MATRIX_SUCCESS")


if __name__ == "__main__":
    main()
