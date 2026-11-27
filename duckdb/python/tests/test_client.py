import unittest
import sys
import io
import threading
import tempfile
import time
from pathlib import Path
from unittest.mock import patch

from zingg_duckdb.client import WorkerClient


class WorkerClientHelpersTest(unittest.TestCase):
    def test_jdbc_url_normalization(self):
        self.assertEqual("jdbc:duckdb:", WorkerClient._jdbc_url(":memory:"))
        self.assertEqual("jdbc:duckdb:sample.db", WorkerClient._jdbc_url("sample.db"))
        self.assertEqual("jdbc:duckdb:sample.db", WorkerClient._jdbc_url("jdbc:duckdb:sample.db"))

    def test_unescape_round_trip(self):
        encoded = r"line1\nline2\tvalue\\tail"
        self.assertEqual("line1\nline2\tvalue\\tail", WorkerClient._unescape(encoded))

    def test_request_encoding_matches_java_protocol_golden(self):
        class FakeProcess:
            def __init__(self):
                self.stdin = io.StringIO()
                self.stdout = io.StringIO("golden\tok\tpong\n")
                self.stderr = io.StringIO()
            def poll(self): return None
        client = WorkerClient.__new__(WorkerClient)
        client._process = FakeProcess()
        payload = "slash\\tab\tline\nunicode-λ\rend"
        with patch("zingg_duckdb.client.uuid.uuid4") as uuid4:
            uuid4.return_value.hex = "golden"
            self.assertEqual("pong", client.request("ping " + payload))
        self.assertEqual("golden\tping\tslash\\\\tab\\tline\\nunicode-λ\rend\n",
                         client._process.stdin.getvalue())

    def test_worker_response_is_bounded_and_terminated(self):
        class BoundedOutput:
            def __init__(self, value): self.value = value; self.requested_size = None; self.closed = False
            def readline(self, size=-1):
                self.requested_size = size
                return self.value[:size]
            def close(self): self.closed = True
        class FakeProcess:
            def __init__(self, output):
                self.stdin = io.StringIO()
                self.stdout = BoundedOutput(output)
                self.stderr = io.StringIO()
                self.returncode = None
            def poll(self): return self.returncode
            def terminate(self): self.returncode = 0
            def wait(self, timeout=None): return self.returncode
            def kill(self): self.returncode = -9

        client = WorkerClient.__new__(WorkerClient)
        client._process = FakeProcess("x" * (WorkerClient.MAX_LINE_CHARS + 8))
        with self.assertRaisesRegex(RuntimeError, "8 MiB protocol limit"):
            client.request("ping")
        self.assertEqual(WorkerClient.MAX_LINE_CHARS + 2, client._process.stdout.requested_size)
        self.assertEqual(0, client._process.returncode)

        client = WorkerClient.__new__(WorkerClient)
        client._process = FakeProcess("response-without-newline")
        with self.assertRaisesRegex(RuntimeError, "unterminated"):
            client.request("ping")

    def test_worker_response_shape_correlation_and_error_status(self):
        class FakeProcess:
            def __init__(self, response):
                self.stdin = io.StringIO()
                self.stdout = io.StringIO(response)
                self.stderr = io.StringIO()
                self.returncode = None
            def poll(self): return self.returncode
            def terminate(self): self.returncode = 0
            def wait(self, timeout=None): return self.returncode
            def kill(self): self.returncode = -9

        for response, message in (("malformed\n", "invalid worker response"),
                                  ("other\tok\tvalue\n", "correlation mismatch")):
            client = WorkerClient.__new__(WorkerClient)
            client._process = FakeProcess(response)
            with self.subTest(message=message), self.assertRaisesRegex(RuntimeError, message):
                client.request("ping")
        client = WorkerClient.__new__(WorkerClient)
        client._process = FakeProcess("request\terror\tdenied\n")
        with patch("zingg_duckdb.client.uuid.uuid4") as uuid4:
            uuid4.return_value.hex = "request"
            with self.assertRaisesRegex(RuntimeError, "denied"):
                client.request("ping")

        prefix = "request\tok\t"
        payload = "x" * (WorkerClient.MAX_LINE_CHARS - len(prefix))
        client = WorkerClient.__new__(WorkerClient)
        client._process = FakeProcess(prefix + payload + "\n")
        with patch("zingg_duckdb.client.uuid.uuid4") as uuid4:
            uuid4.return_value.hex = "request"
            self.assertEqual(payload, client.request("ping"))

    def test_worker_payload_decoder_rejects_malformed_encoding(self):
        from zingg_duckdb.worker import _decode_payload
        for payload in ("v1.nope.", "v1.2.YQ", "v1.1.%%%", "v1.1._w", "v1.-1"):
            with self.subTest(payload=payload), self.assertRaises(ValueError):
                _decode_payload(payload)

    def test_operation_and_size_limits(self):
        client = WorkerClient(command=[sys.executable, "-c", ""])
        try:
            with self.assertRaises(ValueError):
                client.request("bad/op payload")
        finally:
            client.close()

    def test_cancel_terminates_worker_and_unblocks_an_inflight_request(self):
        with tempfile.TemporaryDirectory() as directory:
            ready = Path(directory) / "ready"
            script = "import pathlib,sys,time; sys.stdin.readline(); pathlib.Path(sys.argv[1]).write_text('ready'); time.sleep(60)"
            client = WorkerClient(command=[sys.executable, "-c", script, str(ready)])
            failures = []
            request = threading.Thread(
                target=lambda: self._capture_request_failure(client, failures), daemon=True)
            request.start()
            self._wait_for_file(ready)
            client.cancel()
            request.join(timeout=2)
            try:
                self.assertFalse(request.is_alive(), "terminating the worker must unblock stdout.readline")
                self.assertIsNotNone(client._process.poll())
                self.assertEqual(1, len(failures))
                self.assertIsInstance(failures[0], RuntimeError)
                self.assertIn("exited unexpectedly", str(failures[0]))
            finally:
                client.close()

    def test_request_timeout_terminates_silent_worker(self):
        script = "import sys,time; sys.stdin.readline(); time.sleep(60)"
        client = WorkerClient(command=[sys.executable, "-c", script], request_timeout=0.1,
                              cancel_grace=0.1)
        try:
            with self.assertRaisesRegex(TimeoutError, "request cancellation failed; worker terminated"):
                client.request("ping")
            self.assertIsNotNone(client._process.poll())
        finally:
            client.close()

    def test_request_timeout_cancels_only_timed_out_request_and_preserves_worker(self):
        with tempfile.TemporaryDirectory() as directory:
            ready = Path(directory) / "started"
            script = r'''
import sys, threading
from pathlib import Path
cancelled = {}
lock = threading.Lock()
write_lock = threading.Lock()
ready = Path(sys.argv[1])
def respond(request_id, status, payload):
    with write_lock:
        print(request_id + "\t" + status + "\t" + payload, flush=True)
def long_request(request_id):
    ready.write_text("started")
    while not cancelled.get(request_id):
        threading.Event().wait(0.01)
    respond(request_id, "error", "operation cancelled")
for line in sys.stdin:
    request_id, operation, payload = line.rstrip("\n").split("\t", 2)
    if operation == "long":
        with lock: cancelled[request_id] = False
        threading.Thread(target=long_request, args=(request_id,), daemon=True).start()
    elif operation == "cancel":
        with lock: cancelled[payload] = True
        respond(request_id, "ok", "cancel_requested")
    elif operation == "ping":
        respond(request_id, "ok", "pong")
'''
            client = WorkerClient(command=[sys.executable, "-c", script, str(ready)],
                                  request_timeout=0.05, cancel_grace=0.5)
            try:
                with self.assertRaisesRegex(TimeoutError, "request cancelled; worker preserved"):
                    client.request("long")
                self.assertEqual("pong", client.request("ping"))
                self.assertIsNone(client._process.poll())
            finally:
                client.close()

    def test_stderr_is_inherited_to_avoid_an_undrained_child_pipe(self):
        with patch("zingg_duckdb.client.subprocess.Popen") as popen:
            class FakeProcess:
                stdin = io.StringIO()
                stdout = io.StringIO()
                def poll(self): return 0
            popen.return_value = FakeProcess()
            WorkerClient(command=["worker"])
            self.assertIsNone(popen.call_args.kwargs["stderr"])

    @staticmethod
    def _wait_for_file(path):
        deadline = time.monotonic() + 5
        while time.monotonic() < deadline:
            if path.exists():
                return
            time.sleep(0.01)
        raise AssertionError(f"worker did not create readiness marker: {path}")

    @staticmethod
    def _capture_request_failure(client, failures):
        try:
            client.request("run_v2 payload")
        except Exception as error:
            failures.append(error)

    def test_cancel_escalates_to_kill_after_terminate_timeout(self):
        class FakeProcess:
            def __init__(self):
                self.returncode = None
                self.terminated = False
                self.killed = False
                self.wait_timeouts = []
            def poll(self): return self.returncode
            def terminate(self): self.terminated = True
            def wait(self, timeout=None):
                self.wait_timeouts.append(timeout)
                if self.killed:
                    self.returncode = -9
                    return self.returncode
                raise __import__("subprocess").TimeoutExpired("worker", timeout)
            def kill(self): self.killed = True

        client = WorkerClient.__new__(WorkerClient)
        client._process = FakeProcess()
        client.cancel()
        self.assertTrue(client._process.terminated)
        self.assertTrue(client._process.killed)
        self.assertEqual([5, 5], client._process.wait_timeouts)

    def test_request_handle_cancels_only_its_correlated_operation(self):
        with tempfile.TemporaryDirectory() as directory:
            ready = Path(directory) / "ready"
            self._test_request_handle_cancels_only_its_correlated_operation_with_marker(ready)

    def _test_request_handle_cancels_only_its_correlated_operation_with_marker(self, ready):
        script = r'''
import sys, threading
from pathlib import Path
cancelled = threading.Event()
write_lock = threading.Lock()
ready = Path(sys.argv[1])
def respond(request_id, status, payload):
    with write_lock:
        print(request_id + "\t" + status + "\t" + payload, flush=True)
def long_request(request_id):
    ready.write_text("started")
    cancelled.wait(10)
    respond(request_id, "error", "operation cancelled")
for line in sys.stdin:
    request_id, operation, payload = line.rstrip("\n").split("\t", 2)
    if operation == "long":
        threading.Thread(target=long_request, args=(request_id,), daemon=True).start()
    elif operation == "cancel":
        cancelled.set()
        respond(request_id, "ok", "cancel_requested")
    elif operation == "ping":
        respond(request_id, "ok", "pong")
'''
        client = WorkerClient(command=[sys.executable, "-c", script, str(ready)])
        try:
            active = client.submit("long")
            self._wait_for_file(ready)
            self.assertEqual("cancel_requested", active.cancel())
            with self.assertRaisesRegex(RuntimeError, "operation cancelled"):
                active.result(timeout=5)
            self.assertEqual("pong", client.request("ping"),
                             "a cancelled operation must not cancel the worker process")
            self.assertIsNone(client._process.poll())
        finally:
            client.close()

    def test_structured_worker_payload_preserves_delimiters(self):
        from zingg_duckdb.worker import _payload
        import base64
        encoded = _payload("/tmp/a|b;part.csv", "a || b", "C:\\data\\x.y")
        parts = encoded.split(".")
        values = [base64.urlsafe_b64decode(part + "=" * (-len(part) % 4)).decode("utf-8") for part in parts[2:]]
        self.assertEqual(["/tmp/a|b;part.csv", "a || b", "C:\\data\\x.y"], values)

    def test_collect_budget_is_forwarded_to_worker(self):
        class FakePipe:
            def close(self): pass
        class FakeProcess:
            stdin = FakePipe()
            stdout = FakePipe()
            stderr = FakePipe()
            def poll(self): return 0
        with patch("zingg_duckdb.client.subprocess.Popen", return_value=FakeProcess()) as popen:
            WorkerClient(command=["worker"], max_collect_bytes=123)
            self.assertEqual(["worker"], popen.call_args.args[0])

        # The option is part of the default-launch path; verify it without starting a process.
        with patch("zingg_duckdb.client.subprocess.Popen", return_value=FakeProcess()) as popen:
            with patch("zingg_duckdb.client.Path.exists", return_value=True):
                WorkerClient(max_collect_bytes=123)
            self.assertIn("--max-collect-bytes", popen.call_args.args[0])
            self.assertIn("123", popen.call_args.args[0])

    def test_input_byte_budget_is_forwarded_to_worker(self):
        class FakePipe:
            def close(self): pass
        class FakeProcess:
            stdin = FakePipe()
            stdout = FakePipe()
            stderr = FakePipe()
            def poll(self): return 0
        with patch("zingg_duckdb.client.subprocess.Popen", return_value=FakeProcess()) as popen:
            with patch("zingg_duckdb.client.Path.exists", return_value=True):
                WorkerClient(max_input_bytes=456)
        command = popen.call_args.args[0]
        index = command.index("--max-input-bytes")
        self.assertEqual("456", command[index + 1])

    def test_temp_directory_is_forwarded_to_worker(self):
        class FakePipe:
            def close(self): pass
        class FakeProcess:
            stdin = FakePipe()
            stdout = FakePipe()
            stderr = FakePipe()
            def poll(self): return 0

        with patch("zingg_duckdb.client.subprocess.Popen", return_value=FakeProcess()) as popen:
            with patch("zingg_duckdb.client.Path.exists", return_value=True):
                WorkerClient(temp_directory="/mnt/zingg-spill")
        command = popen.call_args.args[0]
        index = command.index("--temp-directory")
        self.assertEqual("/mnt/zingg-spill", command[index + 1])

    def test_labels_root_and_apply_labels_payload(self):
        from zingg_duckdb.worker import DuckWorker, _decode_payload, _payload

        class FakePipe:
            def close(self): pass
        class FakeProcess:
            stdin = FakePipe()
            stdout = FakePipe()
            stderr = FakePipe()
            def poll(self): return 0

        with patch("zingg_duckdb.client.subprocess.Popen", return_value=FakeProcess()) as popen:
            with patch("zingg_duckdb.client.Path.exists", return_value=True):
                WorkerClient(labels_root="labels")
            self.assertIn("--labels-root", popen.call_args.args[0])
            self.assertIn("labels", popen.call_args.args[0])

        worker = DuckWorker.__new__(DuckWorker)
        class FakeClient:
            def request(self, line):
                operation, payload = line.split(" ", 1)
                self.operation = operation
                self.fields = _decode_payload(payload)
                return _payload("1", "false", "0")
        worker.client = FakeClient()
        result = worker.apply_labels("v1", "key", [("a", "b", "MATCH", "human")])
        self.assertEqual("apply_labels", worker.client.operation)
        self.assertEqual(["v1", "key", "a", "b", "MATCH", "human"], worker.client.fields)
        self.assertEqual({"applied": 1, "replay": False, "rejections": []}, result)

    def test_configured_match_uses_v2_payload(self):
        from zingg_duckdb.worker import DuckWorker, _decode_payload

        worker = DuckWorker.__new__(DuckWorker)
        class FakeClient:
            def request(self, line):
                self.operation, payload = line.split(" ", 1)
                self.fields = _decode_payload(payload)
                return "2"
        worker.client = FakeClient()
        count = worker.run("MATCH", "records.csv", "matches.csv", classifier_model="model",
                           id_column="id", blocking_column="block", threshold=0.4)
        self.assertEqual(2, count)
        self.assertEqual("run_v2", worker.client.operation)
        self.assertEqual(["MATCH", "matches.csv", "TRUE", "model", "id", "block", "1.0", "0.4", "records.csv"],
                         worker.client.fields)
        with self.assertRaises(ValueError):
            worker.run("MATCH", "records.csv", "matches.csv", classifier_model="model")

    def test_train_from_labels_payload(self):
        from zingg_duckdb.worker import DuckWorker, _decode_payload

        worker = DuckWorker.__new__(DuckWorker)
        class FakeClient:
            def request(self, line):
                self.operation, payload = line.split(" ", 1)
                self.fields = _decode_payload(payload)
                return "artifact"
        worker.client = FakeClient()
        self.assertEqual("artifact", worker.train_from_labels("records.csv", "artifact", "id", "block", ["z_feature"]))
        self.assertEqual("train_from_labels", worker.client.operation)
        self.assertEqual(["artifact", "id", "block", "z_feature", "z_label", "1000000", "200", "0.05",
                          "0.0001", "zingg-0.7.0", "records.csv"], worker.client.fields)

    def test_pending_label_payloads_preserve_nullable_fields(self):
        from zingg_duckdb.worker import DuckWorker, _decode_payload, _payload

        worker = DuckWorker.__new__(DuckWorker)
        class FakeClient:
            def request(self, line):
                self.operation, payload = line.split(" ", 1)
                self.fields = _decode_payload(payload)
                if self.operation == "enqueue_pending_label":
                    return _payload("false")
                return _payload("true", "1", "a", "b", "true", "", "false", "")
        worker.client = FakeClient()
        self.assertFalse(worker.enqueue_pending_label("v1", "producer", "a", "b", "", None))
        self.assertEqual("enqueue_pending_label", worker.client.operation)
        self.assertEqual(["v1", "producer", "a", "b", "true", "", "false", ""], worker.client.fields)
        self.assertEqual({"labels": [("a", "b", "", None)], "has_more": True},
                         worker.get_pending_labels("v1", "consumer", 1))
        self.assertEqual("get_pending_labels", worker.client.operation)
        self.assertEqual(["v1", "consumer", "1"], worker.client.fields)
        with self.assertRaises(ValueError):
            worker.get_pending_labels("v1", "consumer", 11)


if __name__ == "__main__":
    unittest.main()
