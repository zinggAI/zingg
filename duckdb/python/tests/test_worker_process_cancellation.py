import os
import shutil
import subprocess
import tempfile
import time
import unittest
from pathlib import Path

from zingg_duckdb.worker import DuckWorker


ROOT = Path(__file__).resolve().parents[2]
WORKER_JAR = ROOT / "runtime-worker" / "target" / "runtime-worker-0.1.0-SNAPSHOT.jar"
JAVA = os.environ.get("ZINGG_TEST_JAVA") or shutil.which("java")
WORKER_JAR = Path(os.environ.get("ZINGG_TEST_WORKER_JAR", WORKER_JAR))
FIXTURE_CLASSES = Path(os.environ.get(
    "ZINGG_TEST_CLASSES", ROOT / "runtime-worker" / "target" / "test-classes"))


@unittest.skipUnless(JAVA and WORKER_JAR.is_file(),
                     "requires Java and the packaged runtime-worker JAR")
class PackagedWorkerCancellationTest(unittest.TestCase):
    def test_request_timeout_cancels_packaged_request_and_worker_recovers(self):
        command = [JAVA, "-jar", str(WORKER_JAR), "--unsafe-debug-sql"]
        worker = DuckWorker(command=command, request_timeout=30, cancel_grace=2)
        process = worker.client._process
        try:
            self.assertEqual("pong", worker.ping(), "warm the packaged worker before applying timeout")
            worker.client.request_timeout = 0.05
            started = time.monotonic()
            with self.assertRaisesRegex(TimeoutError, "request cancelled; worker preserved"):
                worker.client.request("count SELECT sum(i) FROM range(1000000000000) t(i)")
            self.assertLess(time.monotonic() - started, 8,
                            "request-scoped timeout must return promptly")
            self.assertIsNone(process.poll(), "a healthy worker must survive request-scoped cancellation")
            self.assertEqual("pong", worker.ping(),
                             "the same worker must accept a request after scoped cancellation")
        finally:
            worker.close()

    def test_active_request_is_cancelled_and_worker_accepts_next_request(self):
        worker = DuckWorker(command=[JAVA, "-jar", str(WORKER_JAR), "--unsafe-debug-sql"])
        try:
            active = worker.request_async("count", "SELECT sum(i) FROM range(1000000000000) t(i)")
            self.assertEqual("cancel_requested", active.cancel())
            with self.assertRaisesRegex(RuntimeError, "count failed|operation cancelled"):
                active.result(timeout=10)
            self.assertEqual("pong", worker.ping(),
                             "selective cancellation must not terminate the worker process")
        finally:
            worker.client.close()

    def test_native_classifier_training_can_be_cancelled_through_worker_protocol(self):
        from zingg_duckdb.worker import _payload

        with tempfile.TemporaryDirectory(prefix="zingg-train-cancel-") as temporary:
            directory = Path(temporary)
            source = directory / "training.csv"
            artifact = directory / "cancelled-model"
            with source.open("w", encoding="utf-8", newline="") as stream:
                stream.write("feature,label\n")
                for value in range(2_000):
                    stream.write(f"{value},{value % 2}\n")

            worker = DuckWorker(command=[JAVA, "-jar", str(WORKER_JAR)])
            try:
                self.assertEqual("pong", worker.ping(), "start the worker before measuring training")
                fields = ["", str(artifact), "label", "feature", "2000", "1000000",
                          "0.05", "0.0001", "cancel-test", str(source)]
                active = worker.request_async("train_classifier", _payload(*fields))
                # CSV import and initial frame setup should finish well before
                # the million-iteration fit; keep the request active long
                # enough to exercise cancellation during the CPU-heavy command.
                time.sleep(2)
                self.assertEqual("cancel_requested", active.cancel())
                with self.assertRaisesRegex(RuntimeError, "operation cancelled"):
                    active.result(timeout=20)
                self.assertFalse(artifact.exists(), "cancelled TRAIN must not publish a model")
                self.assertEqual([], list(directory.glob(".model-artifact-*")),
                                 "cancelled TRAIN must remove artifact stage/backup directories")
                self.assertEqual("pong", worker.ping(),
                                 "worker must remain usable after cancelling native TRAIN")
            finally:
                worker.client.close()

    @unittest.skipUnless((FIXTURE_CLASSES / "io/zingg/duckdb/worker/ArrowCancellationFixture.class").is_file(),
                         "requires the Arrow cancellation fixture test class")
    def test_run_v2_arrow_ingestion_cancellation_via_worker_facade(self):
        from zingg_duckdb.worker import _payload

        with tempfile.TemporaryDirectory(prefix="zingg-arrow-cancel-") as temporary:
            directory = Path(temporary)
            arrow = directory / "large.arrow"
            output = directory / "cancelled.csv"
            generator = subprocess.run(
                [JAVA, "--add-opens=java.base/java.nio=ALL-UNNAMED",
                 "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED", "-cp",
                 os.pathsep.join((str(WORKER_JAR), str(FIXTURE_CLASSES))),
                 "io.zingg.duckdb.worker.ArrowCancellationFixture", str(arrow)],
                capture_output=True, text=True, timeout=60)
            self.assertEqual(0, generator.returncode, generator.stderr)
            self.assertGreater(arrow.stat().st_size, 1_000_000)

            worker = DuckWorker(command=[JAVA, "-jar", str(WORKER_JAR)])
            try:
                fields = ["MATCH", str(output), "TRUE", "", "z_zid", "value", "1.0", "0.5", str(arrow)]
                active = worker.request_async("run_v2", _payload(*fields))
                time.sleep(0.1)
                self.assertEqual("cancel_requested", active.cancel())
                with self.assertRaisesRegex(RuntimeError, "operation cancelled|Arrow IPC ingestion failed"):
                    active.result(timeout=30)
                self.assertFalse(output.exists(), "cancelled run must not publish output")
                self.assertEqual([], list(directory.glob(".cancelled.csv.partial-*")),
                                 "cancelled run must remove atomic-output staging files")
                self.assertEqual("pong", worker.ping(), "worker must remain available after request cancellation")
            finally:
                worker.client.close()

    def test_cancel_racing_durable_label_write_is_idempotently_retryable(self):
        from zingg_duckdb.worker import _decode_payload, _payload

        with tempfile.TemporaryDirectory(prefix="zingg-label-cancel-") as temporary:
            labels_root = Path(temporary) / "labels"
            worker = DuckWorker(command=[JAVA, "-jar", str(WORKER_JAR),
                                         "--labels-root", str(labels_root)])
            expected = set()
            completed_before_cancel = 0
            try:
                for index in range(24):
                    producer_key = f"cancel-race-{index}"
                    left, right = f"left-{index}", f"right-{index}"
                    expected.add((left, right, None, None))
                    fields = ("v1", producer_key, left, right, "false", "", "false", "")
                    active = worker.request_async("enqueue_pending_label", _payload(*fields))
                    first_result = None
                    if index % 4 == 0:
                        # Force the commit-won branch as well as the immediate
                        # cancel race; cancellation after completion is explicit.
                        first_result = _decode_payload(active.result(timeout=10))
                        completed_before_cancel += 1
                        self.assertEqual(["false"], first_result)
                        self.assertEqual("not_active", active.cancel())
                    else:
                        acknowledgement = active.cancel()
                        self.assertIn(acknowledgement, ("cancel_requested", "not_active"))
                    try:
                        if first_result is None:
                            first_result = _decode_payload(active.result(timeout=10))
                        self.assertIn(first_result, (["false"], ["true"]))
                    except RuntimeError as failure:
                        self.assertIn("operation cancelled", str(failure))

                    # Whether the original operation committed or was cancelled
                    # before starting, retrying its producer key is safe.
                    worker.enqueue_pending_label("v1", producer_key, left, right)
                    self.assertTrue(worker.enqueue_pending_label("v1", producer_key, left, right),
                                    "a repeated producer key must be reported as an idempotent replay")

                self.assertEqual(6, completed_before_cancel,
                                 "the test must exercise commits that complete before cancellation arrives")
                observed = []
                page = 0
                while True:
                    batch = worker.get_pending_labels("v1", f"cancel-race-read-{page}", 10)
                    observed.extend(batch["labels"])
                    if not batch["has_more"]:
                        break
                    page += 1
                    self.assertLess(page, 10, "pending-label pagination did not terminate")
                self.assertEqual(24, len(observed), "cancel/retry races must not duplicate durable labels")
                self.assertEqual(expected, set(observed), "all labels must survive cancel/retry races")
                self.assertEqual("pong", worker.ping())
            finally:
                worker.client.close()


if __name__ == "__main__":
    unittest.main()
