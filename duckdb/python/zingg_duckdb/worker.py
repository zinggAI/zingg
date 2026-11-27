from __future__ import annotations
from .client import WorkerClient, WorkerRequest

def _payload(*fields: str) -> str:
    import base64
    encoded = [base64.urlsafe_b64encode(value.encode("utf-8")).decode("ascii").rstrip("=") for value in fields]
    return "v1." + str(len(encoded)) + "." + ".".join(encoded)

def _decode_payload(payload: str) -> list[str]:
    import base64
    import binascii
    parts = payload.split(".")
    if len(parts) < 2 or parts[0] != "v1":
        raise ValueError("invalid worker payload")
    try:
        count = int(parts[1])
        if count < 0 or count != len(parts) - 2:
            raise ValueError("invalid worker payload")
        return [base64.b64decode(value + "=" * (-len(value) % 4), altchars=b"-_", validate=True)
                .decode("utf-8") for value in parts[2:]]
    except (ValueError, UnicodeDecodeError, binascii.Error) as error:
        raise ValueError("invalid worker payload") from error

class DuckWorker:
    """Small typed facade over the line-oriented worker protocol."""
    def __init__(self, command: list[str] | None = None, database: str = ":memory:", **worker_options):
        self.client = WorkerClient(command, database, **worker_options)
    def ping(self) -> str:
        return self.client.request("ping")
    def request_async(self, operation: str, payload: str = "") -> WorkerRequest:
        """Submit a raw worker operation without blocking the calling thread."""
        return self.client.submit(operation + ((" " + payload) if payload else ""))
    def cancel_request(self, request_id: str) -> str:
        """Cancel only the active operation identified by its WorkerRequest ID."""
        return self.client.cancel_request(request_id)
    def status(self) -> dict[str, str]:
        values = self.client.request("status").split(";")
        return dict(item.split("=", 1) for item in values if "=" in item)
    def explain(self, sql: str) -> str:
        return self.client.explain(sql)
    def count(self, sql: str) -> int:
        return int(self.client.request("count " + sql))
    def run(self, phase: str, input_path: str | list[str], output_path: str, predicate: str = "TRUE",
            classifier_model: str | None = None, *, id_column: str | None = None,
            blocking_column: str | None = None, score_expression: str = "1.0",
            threshold: float = 0.5) -> int:
        inputs = input_path if isinstance(input_path, list) else [input_path]
        configured = classifier_model is not None or id_column is not None or blocking_column is not None
        if configured and (not id_column or not blocking_column):
            raise ValueError("configured matching requires id_column and blocking_column")
        fields = [phase, output_path, predicate, classifier_model or ""]
        if configured:
            fields.extend((id_column or "", blocking_column or "", score_expression, str(threshold)))
        fields.extend(inputs)
        payload = _payload(*fields)
        return int(self.client.request(("run_v2 " if configured else "run ") + payload))
    def train(self, input_path: str | list[str], artifact_path: str, blocking_expression: str,
              blocking_column: str, max_rows: int = 1_000_000) -> str:
        inputs = input_path if isinstance(input_path, list) else [input_path]
        payload = _payload("", artifact_path, blocking_expression, blocking_column, str(max_rows), *inputs)
        return self.client.request("train_blocking " + payload)
    def train_classifier(self, input_path: str | list[str], artifact_path: str, label_column: str,
                         features: list[str], max_rows: int = 1_000_000, iterations: int = 200,
                         learning_rate: float = 0.05, l2: float = 1e-4,
                         profile: str = "zingg-0.7.0") -> str:
        inputs = input_path if isinstance(input_path, list) else [input_path]
        payload = _payload("", artifact_path, label_column, ",".join(features), str(max_rows),
                           str(iterations), str(learning_rate), str(l2), profile, *inputs)
        return self.client.request("train_classifier " + payload)
    def train_from_labels(self, input_path: str | list[str], artifact_path: str,
                          id_column: str, blocking_column: str, features: list[str],
                          label_column: str = "z_label", max_rows: int = 1_000_000,
                          iterations: int = 200, learning_rate: float = 0.05,
                          l2: float = 1e-4, profile: str = "zingg-0.7.0") -> str:
        """Train candidate-pair classifier from accepted decisions in the worker label store."""
        inputs = input_path if isinstance(input_path, list) else [input_path]
        payload = _payload(artifact_path, id_column, blocking_column, ",".join(features),
                           label_column, str(max_rows), str(iterations), str(learning_rate),
                           str(l2), profile, *inputs)
        return self.client.request("train_from_labels " + payload)
    def apply_labels(self, schema_version: str, idempotency_key: str,
                     decisions: list[tuple[str, str, str, str]]) -> dict:
        """Apply (left_id, right_id, MATCH|NON_MATCH|UNKNOWN, source) decisions."""
        fields = [schema_version, idempotency_key]
        for left, right, decision, source in decisions:
            fields.extend((left, right, decision, source))
        values = _decode_payload(self.client.request("apply_labels " + _payload(*fields)))
        if len(values) < 3 or len(values) != 3 + 4 * int(values[2]):
            raise ValueError("invalid apply_labels response")
        return {"applied": int(values[0]), "replay": values[1] == "true",
                "rejections": [tuple(values[i:i+4]) for i in range(3, len(values), 4)]}
    def enqueue_pending_label(self, schema_version: str, idempotency_key: str,
                              left_id: str, right_id: str, left_payload: str | None = None,
                              right_payload: str | None = None) -> bool:
        """Queue one pair durably; return True if this producer key was already applied."""
        fields = (schema_version, idempotency_key, left_id, right_id,
                  str(left_payload is not None).lower(), left_payload or "",
                  str(right_payload is not None).lower(), right_payload or "")
        values = _decode_payload(self.client.request("enqueue_pending_label " + _payload(*fields)))
        if values not in (["true"], ["false"]):
            raise ValueError("invalid enqueue_pending_label response")
        return values[0] == "true"
    def get_pending_labels(self, schema_version: str, idempotency_key: str,
                           limit: int = 10) -> dict:
        """Retrieve an idempotent batch from the durable pending-label queue."""
        if not 1 <= limit <= 10:
            raise ValueError("pending-label limit must be 1..10")
        values = _decode_payload(self.client.request("get_pending_labels " +
                                                     _payload(schema_version, idempotency_key, str(limit))))
        if len(values) < 2 or values[0] not in ("true", "false"):
            raise ValueError("invalid get_pending_labels response")
        count = int(values[1])
        if count < 0 or count > limit or len(values) != 2 + 6 * count:
            raise ValueError("invalid get_pending_labels response")
        labels = []
        for i in range(2, len(values), 6):
            if values[i + 2] not in ("true", "false") or values[i + 4] not in ("true", "false"):
                raise ValueError("invalid pending-label presence flag")
            labels.append((values[i], values[i + 1],
                           values[i + 3] if values[i + 2] == "true" else None,
                           values[i + 5] if values[i + 4] == "true" else None))
        return {"labels": labels, "has_more": values[0] == "true"}
    def close(self) -> None:
        try:
            self.client.request("shutdown")
        finally:
            self.client.close()
    def cancel(self) -> None:
        """Abort the entire worker process and any active request."""
        self.client.cancel()
    def __enter__(self): return self
    def __exit__(self, *_): self.close()
