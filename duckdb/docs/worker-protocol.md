# Worker protocol

The Python client enforces an 8 MiB maximum on both outbound request lines and inbound response lines. Responses must be newline-terminated and carry the exact request correlation ID; oversized or unterminated responses close the worker client before failing. Versioned Python payload decoding validates the field count, URL-safe Base64 alphabet, and UTF-8 before exposing fields.

The Java worker enforces the same line limit during input accumulation, not after an unbounded `readLine()`. It drains an overlong request through its line ending, emits a correlated-format error response with an empty ID (the ID cannot be trusted before decoding), then continues serving subsequent requests. The exact-limit, over-limit recovery, and post-error `ping` behavior are covered by `ConfiguredWorkerServerTest`.

The worker is line-oriented. Each request is three escaped tab-separated fields:

```text
request-id<TAB>operation<TAB>payload
```

Supported operations:

- `ping` → `pong`;
- `count` → executes a read-only count SQL expression on a job connection;
- `run` → legacy `PayloadCodec` v1 fields `phase, output, predicate, classifierModel, input+`; returns output row count. A classifier path without matcher fields is rejected, so use `run_v2` for model-backed MATCH;
- `run_v2` → `PayloadCodec` v1 fields `phase, output, predicate, classifierModel, idColumn, blockingColumn, scoreExpression, threshold, input+`. It selects the explicit MATCH candidate/classifier scorer; a classifier model requires both ID and blocking columns. The Python `DuckWorker.run(...)` chooses `run_v2` when matcher arguments or a classifier artifact are supplied.
- `train_blocking` → `PayloadCodec` v1 fields `profile, output, blockingExpression, blockingColumn, maxRows, input...` and returns the artifact path;
- `train_classifier` → `PayloadCodec` v1 fields `profile, output, labelColumn, featureColumnsCsv, maxRows, iterations, learningRate, l2, modelProfile, input...` and returns the artifact path. Features must be finite numeric columns and the label must be boolean or numeric `0/1`. The operation name is explicit; payload length is never used to choose the implementation.
- `apply_labels` → `PayloadCodec` v1 fields `schemaVersion, idempotencyKey, (leftId, rightId, MATCH|NON_MATCH|UNKNOWN, source)*`. The request ID comes from the line envelope. The response is `PayloadCodec` v1 fields `applied, replay, rejectionCount, (leftId, rightId, code, message)*`. Conflicting decisions are rejected; exact retries replay and changed-payload retries fail.
- `enqueue_pending_label` → `PayloadCodec` v1 fields `schemaVersion, producerKey, leftId, rightId, leftPresent, leftPayload, rightPresent, rightPayload`. Presence flags are literal `true` or `false`, allowing empty strings to differ from nulls. The response is one `replay` boolean. It requires `--labels-root`; producer keys are persisted and retries cannot duplicate a pair. A reused key with changed content fails. Each payload field is limited to 10,000 UTF-16 code units so Java's snapshot encoding remains bounded.
- `get_pending_labels` → `PayloadCodec` v1 fields `schemaVersion, deliveryKey, limit` (1–10); response fields `hasMore, count, (leftId, rightId, leftPresent, leftPayload, rightPresent, rightPayload)*`. It requires `--labels-root`. Exact retries with the same key/schema/limit replay the same batch after restart; changing the schema or limit fails. Delivery removes those pairs from the pending FIFO and saves the receipt atomically before returning. The batch cap keeps even legacy snapshot fields below the worker's line-size limit.
- `cancel` → the raw request ID of an in-flight operation in the envelope payload. It returns `cancel_requested` when the target is active/queued, or `not_active` if it has already finished. The target request still receives its own correlated `error` response after cancellation unwinds and cleans up; the worker remains available for later requests.
- `train_from_labels` → `PayloadCodec` v1 fields `artifact, idColumn, blockingColumn, featureCsv, labelColumn, maxRows, iterations, learningRate, l2, profile, input+`. It requires previously accepted positive and negative decisions in the configured provider, generates bounded candidate pairs, derives the binary label column, and writes a native classifier artifact. It does not create a released Zingg v0.7 blocking tree or LINK output.
- `shutdown` → closes the worker.

## Arrow IPC ingress type contract

`run`/`run_v2` read Arrow IPC file or stream inputs through the same ingress layer. The current scalar mappings are:

| Arrow logical type | DuckDB staging type / conversion |
| --- | --- |
| Boolean | `BOOLEAN` |
| Signed 8/16/32/64-bit integer | `TINYINT` / `SMALLINT` / `INTEGER` / `BIGINT` |
| Unsigned 8/16/32-bit integer | `BIGINT`, with unsigned values widened before insertion |
| Unsigned 64-bit integer | `DECIMAL(20, 0)` |
| Float16 / float32 | `FLOAT` (Float16 is expanded to binary32) |
| Float64 | `DOUBLE` |
| Decimal | matching `DECIMAL(precision, scale)` |
| Date32 (epoch days) / Date64 (epoch milliseconds) | `DATE`; Date64's midnight date representation is normalized to the SQL date value |
| Timestamp without timezone | `TIMESTAMP`; input is normalized to UTC wall time and truncated to DuckDB microsecond precision |
| Timestamp with timezone | `TIMESTAMPTZ`; instants are normalized to UTC and truncated to microsecond precision |
| Binary | `BLOB` |
| UTF-8 / string / large string | `VARCHAR` |

Types without an explicit mapping (including Arrow `TIME` and nested `LIST`/`STRUCT` schemas) fail with an `unsupported Arrow field type` error before a staging table is registered. For `TIME`, all valid Arrow unit/width encodings are tested: SECOND/32, MILLISECOND/32, MICROSECOND/64, and NANOSECOND/64. Rejection is verified for stream/file framing and JDBC/Appender. Duplicate field names, including case-only differences (DuckDB identifiers are case-insensitive), fail with an explicit `duplicate Arrow field name (case-insensitive)` error before staging. Schema-only streams and files, as well as streams and files containing an explicit zero-row record batch, preserve their schema and import zero rows through both JDBC and Appender. Direct scalar Date32/Date64 values are covered in both framings, including pre-epoch dates; a timezone-aware scalar timestamp with positive and pre-epoch instants is also covered through stream and file. Scalar dictionary-encoded columns resolve their IPC dictionary values before staging; string, signed integer, boolean, decimal, date, half-precision float, binary, and timezone-aware timestamp dictionaries are tested through JDBC and Appender, as are null dictionary values, null indices, and rejection/cleanup for out-of-range indices. Binary checks include empty and non-ASCII byte sequences. Row budgets are checked at each record-batch boundary before its rows are decoded/imported; a later over-budget dictionary batch fails and removes the partially populated table through both routes. Dictionary timestamp evidence includes positive and pre-epoch instants, but not all time units/timezone edge cases. Dictionary coverage for other value types and the complete Arrow type matrix are not yet qualified; unsupported types must not be assumed to coerce successfully.

Malformed IPC fails closed: arbitrary non-IPC bytes are rejected, a truncated later stream batch removes the table after rolling back already imported rows, and an Arrow file with a truncated footer fails during reader initialization before a staging table is created. These malformed-input checks run through both JDBC and Appender ingress. This does not replace coverage of every malformed FlatBuffer/body/footer corruption pattern.

Launch configuration:

```text
java -jar runtime-worker.jar \
  --url jdbc:duckdb: \
  --threads 8 \
  --memory-bytes 4294967296 \
  --temp-directory /var/lib/zingg/spill \
  --max-temp-bytes 10737418240 \
  --max-jobs 1 \
  --max-rows 1000000 \
  --max-input-bytes 10737418240 \
  --max-spill-bytes 10737418240 \
  --max-collect-bytes 268435456 \
  --max-model-bytes 268435456 \
  --labels-root /data/labels \
  --input-root /data/input \
  --output-root /data/output
```

`--max-input-bytes` limits the aggregate size, in bytes, of all input files in one phase request. The worker reports the configured value in `status`; `0` means unlimited. Input paths and regular-file/symlink policy are checked before the cumulative size is charged, and the full request's sizes are preflighted before any file is imported. When the limit is nonzero, inputs are copied into a private directory under DuckDB's configured temporary directory; copying is byte-budgeted across the whole request and DuckDB reads only those copies. The private directory is removed after result materialization or on failure. This freezes the bytes consumed by DuckDB after copying, but metadata checks and copy-time size enforcement cannot rule out a hostile change-and-restore race while an original file is being copied (which could yield a mixed-version snapshot). With the limit set to `0`, the worker reads source files directly and uses size/modification-time/file-identity checks; this is change detection, not an immutable snapshot or an OS-enforced read quota. Do not concurrently modify inputs when a strict adversarial guarantee is required.

The worker emits structured tab-separated responses and keeps diagnostics out of stdout. Requests execute in order on one worker thread, while the reader handles `cancel` control messages concurrently. At most 65 normal operations may be active/queued (one executing plus a bounded queue of 64); excess requests fail with `worker request queue is full`. SQL predicates are allowlisted by `SqlSafety`; input and output paths are constrained by `PathPolicy` when roots are configured.

The Python facade accepts the same runtime options (`input_root`, `output_root`, `labels_root`, `temp_directory`, `threads`, `memory_bytes`, `max_temp_bytes`, `max_jobs`, `max_rows`, `max_input_bytes`, `max_spill_bytes`, `max_collect_bytes`, and `max_model_bytes`) and translates them to the worker flags. `temp_directory` / `--temp-directory` selects DuckDB's spill location. `max_temp_bytes` is passed to DuckDB as `max_temp_directory_size`, but must not be treated as an OS-enforced disk quota: stress probes observed overshoot beyond the setting. To bound container disk use, mount a quota-limited writable filesystem or size-limited tmpfs at the selected path (for example, `--tmpfs /zingg-spill:rw,nosuid,nodev,size=10g`) and pass `--temp-directory /zingg-spill`; use the equivalent quota-backed path/mount on non-Docker deployments. The package's Docker smoke mounts a 16 MiB tmpfs and confirms a large sort fails under a 1 GiB DuckDB temp setting after filling the mount, then verifies normal runtime close removes all spill entries. This qualifies one Linux/Docker workload only; hard-kill cleanup and concurrent-worker aggregate quotas remain open. The mount size includes all processes sharing it and should be sized with concurrency and filesystem overhead in mind. `DuckWorker.apply_labels(...)`, `enqueue_pending_label(...)`, and `get_pending_labels(...)` use versioned payloads. `--labels-root` opts into the atomic, checksummed label snapshot; without it applied labels remain in memory and pending-label worker operations are rejected. Snapshot v3 persists applied requests, pending pairs, delivery receipts, and producer keys; it reads v1/v2 snapshots and migrates on the next write. Startup takes the store lock before removing regular orphan `.labels-*.partial` files left by a killed process. Windows/WSL probes cover small concurrent-process and lost-response fixtures; test-only child JVMs halt at three snapshot stages. Sustained contention, power-loss durability, and receipt retention/compaction remain unqualified.

The corresponding Python methods are `DuckWorker.train(...)` for a blocking tree and `DuckWorker.train_classifier(...)` for a native logistic classifier. Classifier training is deterministic for a fixed input order and configuration, uses bounded batch gradient descent with L2 regularization, and writes the neutral `zingg-0.1-native` classifier artifact.

The read-only `status` operation returns the effective DuckDB settings (`memory_limit`, `max_temp_directory_size`, `temp_directory`, and `threads`), configured row/collect/output/spill budgets, and JVM heap counters. These diagnostics complement, but do not replace, independent process/RSS measurement. It is available as `DuckWorker.status()` and `WorkerClient.status()` in Python.

The read-only `explain <sql>` operation validates the query with the same read-only SQL policy as `count` and returns DuckDB's plan without executing the query. It is available as `DuckWorker.explain(sql)` and `WorkerClient.explain(sql)`.

`WorkerClient.submit(...)` and `DuckWorker.request_async(operation, payload)` return a `WorkerRequest` with its correlation ID, `result(timeout)`, and `cancel()`. The response-reader thread multiplexes out-of-order replies by request ID, so a cancellation acknowledgement can arrive before the target operation's error response. Java binds each request ID to a cancellation token; JDBC statements in the job are tracked and cancelled with `Statement.cancel()`, while Arrow Appender ingestion checks the token between rows/batches and drops partial materialization before replying. Cancellation is cooperative at those boundaries: it does not promise rollback of already committed external output or durable label effects. `cancel_request(request_id)` sends the same control request when the ID is managed separately.

CSV, Parquet, JSON, and Arrow direct exports check cancellation before writing and again before atomic publication; a cancellation observed before the commit boundary preserves an existing destination and removes format staging. The worker integration suite also cancels a `run_v2` Arrow export only after observing its staging file grow, and verifies correlated cancel/error responses, destination preservation, staging cleanup, and subsequent worker availability. Once atomic publication itself starts, a completed rename wins the cancellation race.

`WorkerClient.cancel()` remains an emergency process-wide termination path: it waits up to five seconds after graceful process termination, then escalates to a hard kill and waits again. Use `close()` when the client will not be reused; the request-selective operation should normally leave the worker alive. Calls that already completed durable side effects remain committed, so callers should use idempotency keys/recovery and must not infer transaction rollback from a cancellation response.

Synchronous Python `WorkerClient.request()` calls use a 300-second deadline by default. Set `request_timeout` to another positive number of seconds to change it, or to `None` for workloads that intentionally have no upper bound. On timeout the client first sends a correlated `cancel <request-id>` control message and waits up to `cancel_grace` seconds (5 seconds by default, allowing a cold packaged worker to start). A healthy worker is preserved and the exception reports `request cancelled; worker preserved`; unrelated in-flight work can continue. If the cancellation acknowledgement cannot be obtained, `kill_on_cancel_failure=True` (the default) terminates and reaps the worker, and the exception reports `request cancellation failed; worker terminated`. Set `kill_on_cancel_failure=False` when the caller owns an external recovery policy. Cancellation is not transactional: the operation may already have committed labels or published output before the timeout was observed. Do not blindly retry non-idempotent work; inspect/recover durable output as appropriate, and use idempotency keys for label mutations. `WorkerClient.submit(...).result(timeout=...)` remains available when callers need per-request waiting behavior.
