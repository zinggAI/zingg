"""Check durable producer replay after a worker is killed with its reply unread."""
from __future__ import annotations

import sys
import tempfile
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "python"))
from zingg_duckdb.worker import DuckWorker, _payload  # noqa: E402


def main() -> None:
    if len(sys.argv) not in (2, 3):
        raise SystemExit("usage: verify-worker-label-kill.py <worker-jar> [java]")
    jar = Path(sys.argv[1]).resolve(strict=True)
    java = sys.argv[2] if len(sys.argv) == 3 else "java"
    with tempfile.TemporaryDirectory(prefix="zingg-label-kill-") as directory:
        label_root = Path(directory) / "labels"
        command = [java, "-jar", str(jar), "--labels-root", str(label_root)]
        committed_before_kill = 0
        for index, delay in enumerate((0.0, 0.0, 0.02, 0.02, 0.25, 1.0)):
            key = f"kill-producer-{index}"
            left = f"kill-left-{index}"
            right = f"kill-right-{index}"
            worker = DuckWorker(command=command)
            try:
                assert worker.ping() == "pong"
                process = worker.client._process
                assert process.stdin is not None
                # Intentionally do not read the response: the caller cannot tell whether it committed.
                request = _payload("v1", key, left, right, "false", "", "false", "")
                process.stdin.write(f"kill-{index}\tenqueue_pending_label\t{request}\n")
                process.stdin.flush()
                if delay:
                    time.sleep(delay)
                process.kill()
                process.wait(timeout=10)
            finally:
                worker.client.close()

            with DuckWorker(command=command) as recovered:
                if recovered.enqueue_pending_label("v1", key, left, right):
                    committed_before_kill += 1
                batch = recovered.get_pending_labels("v1", f"kill-delivery-{index}", 1)
                assert batch == {"labels": [(left, right, None, None)], "has_more": False}, batch
                empty = recovered.get_pending_labels("v1", f"kill-empty-{index}", 1)
                assert empty["labels"] == [], empty
        assert committed_before_kill > 0, "no pre-kill commits were observed"

        persisted_delivery_before_kill = 0
        for index, delay in enumerate((0.0, 0.02, 1.0)):
            left = f"delivery-left-{index}"
            right = f"delivery-right-{index}"
            delivery_key = f"killed-delivery-{index}"
            with DuckWorker(command=command) as producer:
                assert not producer.enqueue_pending_label("v1", f"delivery-producer-{index}", left, right)
            worker = DuckWorker(command=command)
            try:
                assert worker.ping() == "pong"
                process = worker.client._process
                assert process.stdin is not None
                request = _payload("v1", delivery_key, "1")
                process.stdin.write(f"kill-delivery-{index}\tget_pending_labels\t{request}\n")
                process.stdin.flush()
                if delay:
                    time.sleep(delay)
                process.kill()
                process.wait(timeout=10)
            finally:
                worker.client.close()
            if delivery_key.encode("ascii") in (label_root / "labels.bin").read_bytes():
                persisted_delivery_before_kill += 1
            with DuckWorker(command=command) as recovered:
                batch = recovered.get_pending_labels("v1", delivery_key, 1)
                assert batch == {"labels": [(left, right, None, None)], "has_more": False}, batch
                assert recovered.get_pending_labels("v1", f"after-delivery-kill-{index}", 1)["labels"] == []
        assert persisted_delivery_before_kill > 0, "no pre-kill delivery receipts were observed"
    print("WORKER_LABEL_KILL_SUCCESS "
          f"producer_commits={committed_before_kill} delivery_receipts={persisted_delivery_before_kill}")


if __name__ == "__main__":
    main()
