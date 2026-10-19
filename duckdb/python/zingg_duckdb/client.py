from __future__ import annotations

from concurrent.futures import Future
import re
import subprocess
import threading
import uuid
from pathlib import Path


class WorkerRequest:
    """A correlated in-flight worker operation that can be cancelled selectively."""

    def __init__(self, client: WorkerClient, request_id: str, future: Future[str]):
        self.request_id = request_id
        self._client = client
        self._future = future

    def result(self, timeout: float | None = None) -> str:
        return self._future.result(timeout)

    def cancel(self) -> str:
        return self._client.cancel_request(self.request_id)


class WorkerClient:
    MAX_LINE_CHARS = 8 * 1024 * 1024
    OPERATION = re.compile(r"^[A-Za-z0-9_.-]+$")

    def __init__(self, command: list[str] | None = None, database: str = ":memory:", *, input_root: str | None = None, output_root: str | None = None, labels_root: str | None = None, temp_directory: str | None = None, threads: int | None = None, memory_bytes: int | None = None, max_temp_bytes: int | None = None, max_jobs: int | None = None, max_rows: int | None = None, max_input_bytes: int | None = None, max_spill_bytes: int | None = None, max_collect_bytes: int | None = None, max_model_bytes: int | None = None, max_output_bytes: int | None = None, offline: bool = True, allowed_extensions: list[str] | None = None, unsafe_debug_sql: bool = False, request_timeout: float | None = 300.0, cancel_grace: float = 5.0, kill_on_cancel_failure: bool = True):
        if request_timeout is not None and request_timeout <= 0:
            raise ValueError("request_timeout must be positive or None")
        if cancel_grace <= 0:
            raise ValueError("cancel_grace must be positive")
        self.request_timeout = request_timeout
        self.cancel_grace = cancel_grace
        self.kill_on_cancel_failure = kill_on_cancel_failure
        if command is None:
            root = Path(__file__).resolve().parents[2]
            bundled = root / "runtime" / "java" / "bin" / ("java.exe" if __import__("os").name == "nt" else "java")
            candidates = [
                root / "worker" / "runtime-worker-0.1.0-SNAPSHOT.jar",
                root / "runtime-worker" / "target" / "runtime-worker-0.1.0-SNAPSHOT.jar",
            ]
            jar = next((candidate for candidate in candidates if candidate.exists()), candidates[0])
            if not jar.exists(): raise FileNotFoundError(f"runtime worker jar not found; checked: {candidates}")
            if bundled.exists():
                java = str(bundled)
            elif (root / "worker").is_dir():
                raise FileNotFoundError("bundled Java 21 runtime is missing from distribution: runtime/java")
            else:
                java = "java"
            url = self._jdbc_url(database)
            self._command = [java, "-jar", str(jar), "--url", url]
            if not offline: self._command.append("--online")
            if unsafe_debug_sql: self._command.append("--unsafe-debug-sql")
            for extension in allowed_extensions or []: self._command.extend(("--allow-extension", extension))
            for flag, value in (("--input-root", input_root), ("--output-root", output_root), ("--labels-root", labels_root), ("--temp-directory", temp_directory), ("--threads", threads), ("--memory-bytes", memory_bytes), ("--max-temp-bytes", max_temp_bytes), ("--max-jobs", max_jobs), ("--max-rows", max_rows), ("--max-input-bytes", max_input_bytes), ("--max-spill-bytes", max_spill_bytes), ("--max-collect-bytes", max_collect_bytes), ("--max-model-bytes", max_model_bytes), ("--max-output-bytes", max_output_bytes)):
                if value is not None: self._command.extend((flag, str(value)))
        else:
            self._command = command
        # Inherit stderr instead of creating a pipe that nobody drains. A full
        # child-process stderr pipe can otherwise stall the worker indefinitely.
        self._process = subprocess.Popen(self._command, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=None, text=True, bufsize=1)
        self._initialize_dispatch()

    @staticmethod
    def _jdbc_url(database: str) -> str:
        if database == ":memory:": return "jdbc:duckdb:"
        if database.startswith("jdbc:duckdb:"): return database
        return "jdbc:duckdb:" + database

    def _initialize_dispatch(self) -> None:
        if not hasattr(self, "_pending_lock"):
            self._pending_lock = threading.Lock()
            self._write_lock = threading.Lock()
            self._reader_start_lock = threading.Lock()
            self._pending: dict[str, Future[str]] = {}
            self._reader_thread: threading.Thread | None = None

    def _ensure_reader(self) -> None:
        self._initialize_dispatch()
        with self._reader_start_lock:
            if self._reader_thread is None:
                self._reader_thread = threading.Thread(target=self._read_responses,
                                                       name="zingg-worker-responses", daemon=True)
                self._reader_thread.start()

    def submit(self, line: str, *, request_id: str | None = None) -> WorkerRequest:
        if self._process.stdin is None or self._process.stdout is None:
            raise RuntimeError("worker pipes unavailable")
        if self._process.poll() is not None:
            raise RuntimeError("worker exited unexpectedly")
        operation, separator, payload = line.partition(" ")
        if not self.OPERATION.fullmatch(operation):
            raise ValueError("invalid worker operation")
        if not separator: payload = ""
        request_id = request_id or uuid.uuid4().hex
        if not self.OPERATION.fullmatch(request_id):
            raise ValueError("invalid worker request id")
        escape = lambda value: value.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n")
        message = "\t".join(escape(value) for value in (request_id, operation, payload))
        if len(message) > self.MAX_LINE_CHARS:
            raise ValueError("worker request exceeds 8 MiB protocol limit")

        self._initialize_dispatch()
        future: Future[str] = Future()
        with self._pending_lock:
            if request_id in self._pending:
                raise ValueError("worker request id is already in flight")
            self._pending[request_id] = future
        self._ensure_reader()
        try:
            with self._write_lock:
                self._process.stdin.write(message + "\n")
                self._process.stdin.flush()
        except Exception as error:
            with self._pending_lock:
                self._pending.pop(request_id, None)
            future.set_exception(RuntimeError("cannot write request to worker"))
            raise RuntimeError("cannot write request to worker") from error
        return WorkerRequest(self, request_id, future)

    def request(self, line: str) -> str:
        request = self.submit(line)
        timeout = getattr(self, "request_timeout", 300.0)
        try:
            return request.result(timeout)
        except TimeoutError as error:
            try:
                acknowledgement = self._cancel_request_scoped(request.request_id)
            except Exception as cancellation_error:
                if self.kill_on_cancel_failure:
                    self.cancel()
                    raise TimeoutError(
                        f"worker request {request.request_id} exceeded timeout of {timeout} seconds; "
                        "request cancellation failed; worker terminated"
                    ) from cancellation_error
                raise TimeoutError(
                    f"worker request {request.request_id} exceeded timeout of {timeout} seconds; "
                    "request cancellation failed and worker was preserved"
                ) from cancellation_error
            if acknowledgement not in ("cancel_requested", "not_active"):
                if self.kill_on_cancel_failure:
                    self.cancel()
                    raise TimeoutError(
                        f"worker request {request.request_id} exceeded timeout of {timeout} seconds; "
                        f"unexpected cancellation acknowledgement {acknowledgement!r}; worker terminated"
                    ) from error
                raise TimeoutError(
                    f"worker request {request.request_id} exceeded timeout of {timeout} seconds; "
                    f"unexpected cancellation acknowledgement {acknowledgement!r}; worker was preserved"
                ) from error
            raise TimeoutError(
                f"worker request {request.request_id} exceeded timeout of {timeout} seconds; "
                "request cancelled; worker preserved"
            ) from error

    def _cancel_request_scoped(self, request_id: str) -> str:
        """Cancel one request without routing a timeout back through request()."""
        control = self.submit("cancel " + request_id)
        return control.result(self.cancel_grace)

    def cancel_request(self, request_id: str) -> str:
        if not self.OPERATION.fullmatch(request_id):
            raise ValueError("invalid worker request id")
        try:
            return self._cancel_request_scoped(request_id)
        except TimeoutError as error:
            if self.kill_on_cancel_failure:
                self.cancel()
                raise TimeoutError(
                    f"cancellation acknowledgement for {request_id} timed out; worker terminated"
                ) from error
            raise

    def _read_responses(self) -> None:
        failure: RuntimeError | None = None
        try:
            while True:
                response = self._process.stdout.readline(self.MAX_LINE_CHARS + 2)
                if not response:
                    raise RuntimeError("worker exited unexpectedly")
                if len(response) > self.MAX_LINE_CHARS + 1 or not response.endswith("\n"):
                    raise RuntimeError("worker response exceeds 8 MiB protocol limit or is unterminated")
                fields = response[:-1].split("\t", 2)
                if len(fields) != 3:
                    raise RuntimeError("invalid worker response")
                response_id, status, payload = fields
                with self._pending_lock:
                    future = self._pending.pop(response_id, None)
                if future is None:
                    raise RuntimeError(f"worker response correlation mismatch: unexpected id {response_id}")
                if status == "error":
                    future.set_exception(RuntimeError(self._unescape(payload)))
                elif status == "ok":
                    future.set_result(self._unescape(payload))
                else:
                    raise RuntimeError("invalid worker response status")
        except RuntimeError as error:
            failure = error
        except Exception as error:
            failure = RuntimeError("worker response reader failed")
            failure.__cause__ = error
        finally:
            self._fail_pending(failure or RuntimeError("worker response stream closed"))
            if (failure is not None and self._process.poll() is None
                    and callable(getattr(self._process, "terminate", None))):
                self.cancel()

    def _fail_pending(self, error: RuntimeError) -> None:
        with self._pending_lock:
            pending = list(self._pending.values())
            self._pending.clear()
        for future in pending:
            if not future.done(): future.set_exception(error)

    def status(self) -> dict[str, str]:
        values = self.request("status").split(";")
        return dict(item.split("=", 1) for item in values if "=" in item)

    def explain(self, sql: str) -> str:
        return self.request("explain " + sql)

    @staticmethod
    def _unescape(value: str) -> str:
        out: list[str] = []
        escaped = False
        for char in value:
            if escaped:
                out.append({"n": "\n", "t": "\t", "\\": "\\"}.get(char, "\\" + char))
                escaped = False
            elif char == "\\":
                escaped = True
            else:
                out.append(char)
        if escaped: out.append("\\")
        return "".join(out)

    def close(self) -> None:
        try:
            self.cancel()
        finally:
            for stream in (self._process.stdin, self._process.stdout, self._process.stderr):
                if stream is not None and not stream.closed:
                    stream.close()

    def cancel(self) -> None:
        """Abort the worker process, including any in-flight request."""
        if self._process.poll() is None:
            self._process.terminate()
            try:
                self._process.wait(timeout=5)
            except subprocess.TimeoutExpired:
                self._process.kill()
                self._process.wait(timeout=5)

    def __enter__(self): return self
    def __exit__(self, *_): self.close()
