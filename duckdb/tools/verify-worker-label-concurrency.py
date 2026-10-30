"""Exercise one durable pending-label store from distinct concurrent worker processes."""
from __future__ import annotations

import sys
import tempfile
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
from threading import Barrier

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "python"))
from zingg_duckdb.worker import DuckWorker  # noqa: E402


def run_group(command: list[str], operation: str, workers: int, per_worker: int):
    barrier = Barrier(workers, timeout=120)

    def run(index: int):
        with DuckWorker(command=command) as worker:
            pid = worker.client._process.pid
            assert worker.ping() == "pong"
            barrier.wait()
            values = []
            for item in range(per_worker):
                key = f"{operation}-{index}-{item}"
                if operation == "enqueue":
                    left = f"left-{index}-{item}"
                    assert not worker.enqueue_pending_label("v1", key, left, f"right-{index}-{item}")
                    values.append(left)
                else:
                    batch = worker.get_pending_labels("v1", key, 1)
                    assert len(batch["labels"]) == 1, batch
                    values.append(batch["labels"][0][0])
            return pid, values

    with ThreadPoolExecutor(max_workers=workers) as pool:
        results = list(pool.map(run, range(workers)))
    pids = [pid for pid, _ in results]
    assert len(set(pids)) == workers, pids
    return [value for _, values in results for value in values]


def verify_same_key_race(command: list[str]) -> None:
    with DuckWorker(command=command) as worker:
        assert not worker.enqueue_pending_label("v1", "race-producer", "race-left", "race-right")
    barrier = Barrier(2, timeout=120)

    def retrieve(_: int):
        with DuckWorker(command=command) as worker:
            assert worker.ping() == "pong"
            barrier.wait()
            return worker.client._process.pid, worker.get_pending_labels("v1", "shared-delivery", 1)

    with ThreadPoolExecutor(max_workers=2) as pool:
        results = list(pool.map(retrieve, range(2)))
    assert results[0][0] != results[1][0], results
    expected = {"labels": [("race-left", "race-right", None, None)], "has_more": False}
    assert results[0][1] == results[1][1] == expected, results
    with DuckWorker(command=command) as worker:
        assert worker.get_pending_labels("v1", "after-race", 1)["labels"] == []


def main() -> None:
    if len(sys.argv) not in (2, 3):
        raise SystemExit("usage: verify-worker-label-concurrency.py <worker-jar> [java]")
    jar = Path(sys.argv[1]).resolve(strict=True)
    java = sys.argv[2] if len(sys.argv) == 3 else "java"
    with tempfile.TemporaryDirectory(prefix="zingg-label-concurrent-") as directory:
        command = [java, "-jar", str(jar), "--labels-root", str(Path(directory) / "labels")]
        expected = run_group(command, "enqueue", 4, 5)
        assert len(expected) == len(set(expected)) == 20, expected
        delivered = run_group(command, "delivery", 4, 5)
        assert len(delivered) == len(set(delivered)) == 20, delivered
        assert set(delivered) == set(expected), (expected, delivered)
        verify_same_key_race(command)
    print("WORKER_LABEL_CONCURRENCY_SUCCESS")


if __name__ == "__main__":
    main()
