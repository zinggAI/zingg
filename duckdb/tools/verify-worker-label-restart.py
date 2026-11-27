"""Exercise recovered-decision training and scoring across three worker processes."""
from __future__ import annotations

import csv
import hashlib
import json
import math
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "python"))
from zingg_duckdb.worker import DuckWorker  # noqa: E402


def main() -> None:
    if len(sys.argv) not in (2, 3):
        raise SystemExit("usage: verify-worker-label-restart.py <worker-jar> [java]")
    jar = Path(sys.argv[1]).resolve(strict=True)
    java = sys.argv[2] if len(sys.argv) == 3 else "java"
    with tempfile.TemporaryDirectory(prefix="zingg-label-restart-") as directory:
        labels = Path(directory) / "labels"
        records = Path(directory) / "records.csv"
        model = Path(directory) / "classifier"
        matches = Path(directory) / "matches.csv"
        records.write_text("id,block,feature\n1,a,1\n2,a,-1\n3,a,1\n", encoding="utf-8")
        command = [java, "-jar", str(jar), "--labels-root", str(labels)]
        decisions = [("1", "2", "NON_MATCH", "smoke"), ("1", "3", "MATCH", "smoke")]
        with DuckWorker(command=command) as worker:
            first_pid = worker.client._process.pid
            try:
                worker.enqueue_pending_label("v1", "oversized", "x", "y", "x" * 10_001)
            except RuntimeError as error:
                assert "size limit" in str(error), error
            else:
                raise AssertionError("oversized pending payload unexpectedly succeeded")
            assert worker.ping() == "pong", "worker was not reusable after invalid payload"
            assert not (labels / "labels.bin").exists(), "invalid payload published a snapshot"
            assert not worker.enqueue_pending_label("v1", "producer-1", "1", "2", "", None)
            assert not worker.enqueue_pending_label("v1", "producer-2", "1", "3", None, "right")
            try:
                worker.train_from_labels(str(records), str(model), "id", "block", ["z_feature"])
            except RuntimeError as error:
                assert "no applied labels" in str(error), error
            else:
                raise AssertionError("training without applied labels unexpectedly succeeded")
            assert not model.exists(), "failed training published an artifact"
            first = worker.apply_labels("v1", "stable-key", decisions)
            assert first == {"applied": 2, "replay": False, "rejections": []}, first
        with DuckWorker(command=command) as worker:
            second_pid = worker.client._process.pid
            assert second_pid != first_pid
            assert worker.enqueue_pending_label("v1", "producer-1", "1", "2", "", None)
            try:
                worker.enqueue_pending_label("v1", "producer-1", "1", "999", "", None)
            except RuntimeError as error:
                assert "idempotency key" in str(error), error
            else:
                raise AssertionError("changed producer payload unexpectedly replayed")
            first_batch = worker.get_pending_labels("v1", "consumer-1", 1)
            assert first_batch == {"labels": [("1", "2", "", None)], "has_more": True}, first_batch
            replay = worker.apply_labels("v1", "stable-key", decisions)
            assert replay == {"applied": 2, "replay": True, "rejections": []}, replay
            conflict = worker.apply_labels("v1", "second-key",
                                           [("1", "2", "MATCH", "smoke")])
            assert conflict["applied"] == 0 and conflict["rejections"][0][2] == "CONFLICT", conflict
            assert worker.train_from_labels(str(records), str(model), "id", "block", ["z_feature"]) == str(model)
            manifest = json.loads((model / "manifest.json").read_text(encoding="utf-8"))
            assert manifest["modelType"] == "CLASSIFIER", manifest
            assert manifest["sha256"] == hashlib.sha256((model / "model.bin").read_bytes()).hexdigest()
        with DuckWorker(command=command) as worker:
            assert worker.client._process.pid not in (first_pid, second_pid)
            assert worker.get_pending_labels("v1", "consumer-1", 1) == first_batch
            second_batch = worker.get_pending_labels("v1", "consumer-2", 1)
            assert second_batch == {"labels": [("1", "3", None, "right")], "has_more": False}, second_batch
            try:
                worker.get_pending_labels("v1", "consumer-1", 2)
            except RuntimeError as error:
                assert "idempotency key" in str(error), error
            else:
                raise AssertionError("changed pending-label request unexpectedly replayed")
            assert worker.run("MATCH", str(records), str(matches), classifier_model=str(model),
                              id_column="id", blocking_column="block") == 2
            with matches.open(encoding="utf-8", newline="") as stream:
                reader = csv.DictReader(stream)
                assert reader.fieldnames is not None
                assert {"id", "z_id", "z_score", "z_prediction"}.issubset(reader.fieldnames)
                rows = list(reader)
            assert {(row["id"], row["z_id"]) for row in rows} == {("1", "3"), ("2", "3")}, rows
            scores = [float(row["z_score"]) for row in rows]
            assert all(0.5 < score <= 1.0 and math.isfinite(score) for score in scores), scores
            assert math.isclose(scores[0], scores[1], rel_tol=0, abs_tol=1e-12), scores
            assert all(int(row["z_prediction"]) == 1 for row in rows), rows
    print("WORKER_LABEL_RESTART_SUCCESS")


if __name__ == "__main__":
    main()
