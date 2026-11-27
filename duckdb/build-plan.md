# Zingg DuckDB — Build Plan

## 1. Purpose and authority

This document is the implementation deliverable requested by the user. The attached report `zingg-duckdb-20260921-1458.md` is treated as technical source material: its architecture, compatibility observations, constraints, open questions, and test gates are inputs to this plan, not additional user instructions. Where the report is uncertain, implementation must prove the behavior experimentally before declaring the profile complete.

### Current execution boundary

The implementation-only phase is complete enough to begin validation. Testing is now authorized: subsequent work may create and execute unit, contract, integration, differential, resource, security, and packaging tests from `test-plan.md`. Validation evidence, rather than compilation or packaging success alone, is required before closing the remaining gates.

## 2. Target outcome

Deliver a standalone DuckDB backend for Zingg v0.7.0 behavior with:

- a pure DuckDB relational engine;
- a narrow Zingg compatibility layer;
- a one-JDBC-connection-per-job execution model for TEMP-backed frames;
- imported v0.7.0 blocking-tree and classifier models;
- native DuckDB matching and graph/output parity;
- Arrow/file data-plane ingestion and egress;
- a Python subprocess API that does not require a separately installed Java runtime;
- immutable compatibility profiles, reproducible builds, security controls, and cross-platform packages.

## 3. Non-goals for v0.1

- replacing Spark inside the legacy model importers;
- transparent concurrent JDBC statements within one job;
- automatic extension installation;
- Java serialization for new artifacts;
- Py4J or JPype as the bulk-data path;
- exact equality of wall-clock-prefixed cluster tokens across independent runs without a test clock;
- implementing every unreleased upstream Zingg change.

## 4. Required repository layout

Create these modules and enforce the dependency direction:

```text
engine-api/
engine-duckdb/
model-format/
protocol/
compat-zingg07-runtime/
runtime-worker/
python/
legacy-blocking-import-zingg07-spark35/
legacy-classifier-import-zingg07-spark35/
reference-spark35-tests/
fixtures/
packaging/
docs/
benchmarks/
```

`engine-api`, `engine-duckdb`, `model-format`, and `protocol` must not import Spark, GraphFrames, Scala, Py4J, JPype, or `zingg.*`. Spark is allowed only in legacy importers and reference tests. Every module gets its own unit/integration test source set and explicit dependency-lock metadata.

## 5. Architecture and contracts

### 5.1 Job and connection ownership

Implement `DuckRuntime`, `DuckJobContext`, `JobHandle`, and `DuckFrame`.

1. Open one root/pinning connection per runtime/database instance.
2. Create exactly one execution connection per `DuckJob` in the initial mode.
3. Distinguish connection-local state from worker-shared state. TEMP tables, TEMP views, registered Arrow streams, statements, and cancellation handles are connection-local. Reusable frames that may cross connections must be materialized as ordinary tables/views in a run-private `_zingg_run_<runId>` schema.
4. Store an opaque owner token in every frame and materialization.
5. Reject joins, unions, excepts, and other binary operations across different owners before SQL generation.
6. Serialize jobs per worker initially; expose a scheduler limit before enabling multiple jobs.
7. Close statements, Arrow resources, TEMP objects, job connections, and root connections in deterministic `try/finally` order.
8. Track relation scope explicitly (`QUERY_ONLY`, `CONNECTION_LOCAL`, `WORKER_SHARED`, `PERSISTED`) and refuse to move connection-local handles across connections.
9. Reserve ordinary per-job schemas for reusable state and serialize catalog mutations through the scheduler.

### 5.2 Relational core

Implement `DuckRelation`, `DuckSchema`, `DuckExpr`, `DuckRow`, `MaterializedRef`, and the `ZFrame<D,R,C>` adapter. The minimum operation set is:

`select`, `selectExpr` (allowlisted), `filter`, `withColumn`, `drop`, `rename`, `join`, `union`, `unionByName`, `except`, `distinct`, `groupBy`, aggregations, `sort`, `limit`, `split`, `explode`, `repartition`, `coalesce`, `cache`, `count`, `collect`, schema inspection, and output writing.

Implement typed AST nodes rather than concatenating user SQL. Quote identifiers, bind literals, validate operators, and reject arbitrary function/class names.

### 5.3 Expression/literal coercion

Mirror Spark `functions.lit` behavior:

- primitive/string/date/timestamp values become literals;
- `DuckExpr` values remain expressions;
- `withColumn("x", frame.col("id"))` copies a column reference;
- nulls retain an explicit logical type where required;
- unsupported values fail with a typed diagnostic.

### 5.4 Cache semantics

Implement eager CTAS materialization into a connection-local TEMP table only for strictly connection-affine scratch. `cache()`/checkpoint-like operations that can outlive one statement or be consumed by another execution connection must promote the relation into the run-private worker-shared schema, with schema capture, reference counting, idempotent release, and cleanup on failure. Do not execute a connection-local handle on another connection.

### 5.5 Zingg context compatibility

Implement the `Context<S,D,R,C,T>` composition root with DuckDB equivalents for configuration, pipes, frame factory, output writers, model readers, clock, and logger. Keep Zingg common-client/core dependencies isolated in `compat-zingg07-runtime`.

### 5.6 Pipe and input semantics

Implement `DuckPipeReader` with explicit modes:

- MATCH: add `z_source` per input before `unionByName(..., allowMissingColumns=true)`;
- FIND_TRAINING_DATA: positional union matching the released overload;
- assign row IDs only after all pipes have been combined;
- preserve source order and failure behavior;
- return cached frame according to phase contract.

### 5.7 Clock and identifiers

Implement `CompatibilityClock`. Production uses epoch milliseconds. Tests inject a fixed clock. Preserve the distinction between internal graph IDs and external timestamp-prefixed cluster tokens.

### 5.8 Set and row-operation details

Implement Spark-compatible behavior for `filterInCond` as a join where multiplicity matters, `split`/`explode` projection behavior, positional versus by-name unions, duplicate handling, nulls, type coercion, and asymmetrical link-output deduplication.

### 5.9 Compatibility capsule and upstream provenance

Generate a reproducible `compat-zingg-0_7` capsule from the exact Zingg v0.7.0 tag rather than depending on an unavailable or mixed Spark assembly. The capsule must:

- capture the upstream commit/tag, source manifest, SHA-256 hashes, license and notice files;
- keep Scala 2.12 leakage isolated from the pure engine;
- fail the build if Spark, GraphFrames, Py4J, JPype, or `zingg.*` enters a pure-engine runtime graph;
- record every local patch and regenerate the capsule deterministically;
- provide a compatibility adapter per released Zingg line instead of binding to unreleased branches or pull requests.

### 5.10 Labels and remote application boundary

Define an internal language-neutral `LabelDecisionProvider`/`ApplyLabels` protocol for pending-label retrieval and label application. The v0.7 adapter must emulate the semantics without depending on unreleased upstream label APIs. Include request IDs, schema/version fields, idempotency keys, rejection diagnostics, and a bounded batch contract. Bind directly to a future upstream label interface only after it lands in a tagged release.

## 6. Implementation phases and exit criteria

### Phase 0 — proof spikes

Build the exact DuckDB JDBC 1.5.5.1 dependency. Prove TEMP visibility, duplicate-connection behavior, run-schema visibility, UDF catalog scope, Arrow registration lifecycle, cancellation, query timeout/progress, and Java 21 runtime loading. Record whether every required registration is connection-local or instance-visible. Exit only with executable probes checked into `reference-spark35-tests`/`engine-duckdb` and CI results.

### Phase 1 — neutral contracts and relational core

Create module skeletons, dependency rules, AST, schema/value model, connection ownership, frame operations, cache, SQL renderer, diagnostics, and resource lifecycle. Exit when all unit tests and cross-owner rejection tests pass.

### Phase 2 — pipe I/O and Arrow

Implement file readers/writers, Arrow registration-to-CTAS ingress, Arrow egress, source tagging, phase-specific union, post-union IDs, schema validation, and issue-#713 regression coverage.

### Phase 3 — hashes and similarities

Implement the complete required function registry. Preserve Java UTF-16, signed 32-bit `String.hashCode`, `Math.round`, null, Unicode, numeric, and floating-point semantics. Use Java UDFs only where SQL cannot prove parity; register and verify each function on the job connection.

### Phase 4 — blocking tree

Implement the released training behavior, including known double-sampling and signed 32-bit blocking-key behavior. Add serialization-free native model format and deterministic test fixtures.

### Phase 5A — blocking migration

Build a separate Spark 3.5.5 legacy importer. Enforce class allowlist, object-size/depth/reference limits, timeout, isolated process, symlink/path checks, non-zero-exit handling, and provenance. Reject output equal to or nested under the input using both normalized and real input paths; reject output paths containing symlink components before creating the destination or starting the child. Read the released `Tree<Canopy<Row>>` blocking artifact only inside that process, extract field context/hash identity/tree edges, and convert to a versioned neutral model format. Add an upstream fixture manifest and reject unknown serialized model types. These lexical/existing-component checks do not eliminate a hostile concurrent path replacement race.

### Phase 5B — classifier migration/scoring

Import supported v0.7.0 classifier artifacts through the isolated Spark process, validate feature metadata, implement DuckDB scoring, and record unsupported model types explicitly.

For every model artifact, serialize `manifest.json` and `provenance.json` as valid UTF-8 JSON using complete JSON string escaping for control characters; never hand-escape only quotes and backslashes. Strictly validate object shape, duplicate/unknown keys, field types, Unicode and checksums on load. Keep staged publication fail-closed and verify the final manifest/checksum after publication.

Local publication mechanics are now hardened in `ModelArtifactWriter`: atomic moves only, rollback to the previous artifact when staged publication or final integrity verification fails, final manifest/payload/provenance verification before success, and non-destructive warning/recovery behavior if deleting the old backup fails. A request-scoped cancellation predicate is checked before publication and after each atomic rename; cancellation after moving the old model or publishing the new tree rolls back and restores the prior artifact. Native classifier and histogram writers pass the active worker cancellation token. Deterministic injected-cancellation tests cover both rename boundaries. `ModelReader` now opens manifest, payload, and provenance with `NOFOLLOW_LINKS`, applies byte limits from the opened channel's size, reads from the same handle, caps each metadata file at 64 MiB (and within the caller's total limit), and strictly decodes UTF-8. Injected failure tests cover key publication boundaries on Windows and WSL; this does not claim crash/power-loss durability, race-free parent directories, or upstream model-semantic validity.

Both classifier import routes now enforce their supported three-stage Spark pipeline order and exact feature-column wiring before model publication. The isolated Spark 3.5.5 importer also validates binary coefficient dimensions/finite values, uses strict JSON escaping, rejects output equal to/nested under input using normalized and real paths, and rejects symlink output components before destination creation. The checked-in v0.7 fixture importer reads each bounded metadata file through a single `NOFOLLOW_LINKS` channel and snapshots the bounded Parquet file through one no-follow handle into a private temporary file before DuckDB reads it; the staged Parquet is removed after import. Neither implementation broadens the declared fixture envelope or removes the external provenance/parity gate. Path checks remain vulnerable to hostile concurrent substitution.

### Phase 6 — graph and output parity

Implement connected components, direct edges, implied transitive pairs, zero-score propagation, min/max aggregation, external cluster IDs, and LinkOutputBuilder asymmetry. Keep the straightforward O(k²) algorithm until equivalence is proven.

### Phase 7 — imported-model MATCH

Wire configuration, input reading, blocking, candidate generation, classifier scoring, graph/link output, persistence, metrics, cancellation, and CLI/API diagnostics. This is the first production milestone.

### Phase 8 — native TRAIN

Implement sampling, blocking-tree training, feature generation, classifier training, model validation, artifact writing, and reproducibility metadata. Treat fresh model byte equality as non-goal unless all randomness is controlled.

### Phase 9 — Python and runtime packaging

Implement JSON request/response protocol, subprocess worker, Arrow/file handoff, structured errors, cancellation, log separation, project-bundled Java 21 `jlink` runtime, pinned `jdk4py` Java-21 line, platform launchers, and no-system-Java checks. In parallel, prototype a GraalVM Native Image worker using DuckDB JDBC 1.5.5.1 tracing/reachability metadata; compare JVM/native behavior, startup, RSS, artifact size, reflection coverage, and model loading before making native packaging a default.

### Phase 10 — hardening and release

Complete security, license/SBOM, dependency and CVE review, performance/resource budgets, platform packages, upgrade/migration docs, compatibility-profile manifest, release signatures, rollback package, and final gate review. Include clean-install/offline-install gates, source/provenance attestations, signed `SHA256SUMS`, and explicit JDK/Native Image/vendor security-update policy.

## 7. Build and dependency controls

- Pin Zingg v0.7.0, DuckDB JDBC 1.5.5.1, Spark 3.5.5, Scala ABI, Arrow, JDK vendor/build, Python versions, and native artifacts.
- Rebuild common client/core artifacts reproducibly instead of using the mixed Spark assembly as the normal dependency boundary.
- Verify no Spark classes enter pure-engine runtime jars.
- Generate SBOMs and license notices for every distributable.
- Use reproducible timestamps/order where the artifact format permits.
- Run API probes against the exact pinned DuckDB artifact, not current online documentation.
- Generate and verify `upstream-manifest.json` for the compatibility capsule; fail on source drift or unreviewed patches.
- Maintain a dependency ban report for pure-engine jars and inspect shaded jars/classes, not only Maven declarations.
- Set both DuckDB `memory_limit` and `max_temp_directory_size`; measure process RSS separately because buffer-manager memory is not total process memory.
- Keep one DuckDB database instance per worker; cap concurrent jobs and avoid multiplying native thread pools through independent instances.
- Maintain JVM-bundled and Native Image dependency locks separately, including reachability metadata and platform-specific native inputs.

## 8. Security and operational requirements

Canonicalize configured paths; reject traversal and remote schemes by default; bind literals; allowlist expressions; disable auto-install; pin extension hashes; redact secrets; cap memory, spill, rows, depth, references, and wall time; isolate legacy Java deserialization; sign packages; track JDK security updates.

Additional operational checks:

- verify that registered Arrow streams are never reused across connection owners without CTAS promotion;
- verify TEMP cleanup, run-schema cleanup, and failure cleanup after cancellation, process kill, timeout, and partial output;
- capture structured progress/error envelopes with request IDs and never mix diagnostics into the data channel;
- redact paths, credentials, model contents, and environment secrets from logs;
- enforce offline mode by failing closed when an extension, dependency, or runtime download is requested;
- monitor process RSS, spill bytes, open files, native threads, and worker child processes;
- validate Windows launcher prerequisites, macOS signing/notarization requirements, and Linux shared-library dependencies on clean images.

## 9. Deliverables

Source modules, generated compatibility capsule, upstream manifest and patches, unit/integration/reference tests, fixtures, imported-model examples, worker and Python package, JVM and Native Image packaging tracks, platform archives, SBOM/notices, compatibility manifest, signed provenance, benchmark reports, migration guide, operations runbook, and this plan plus the companion test plan.

## 10. Definition of done

All mandatory test gates pass on Tier-A platforms; imported MATCH works with real v0.7.0 blocking and classifier fixtures; the generated compatibility capsule is reproducible and Spark-free at runtime; no-system-Java JVM packaging works; the Native Image decision is evidence-backed; relation scope/lifecycle cleanup is proven across owner and duplicate connections; resource, RSS, spill, cancellation, and process-kill behavior is documented; security/license review is signed off; performance and memory budgets are documented; release artifacts are reproducible, signed, and traceable to pinned inputs.

## 11. Additional implementation and verification backlog

The following backlog is added from the 2026-09-22 architecture review. These are build-plan tasks, not instructions embedded in the source report; each item requires implementation evidence before being marked complete.

### P0 — architecture proof and first workflow

- [x] Implement `RelationScope` and a `DuckRelationHandle`; reject cross-connection use of `CONNECTION_LOCAL` handles.
- [x] Add owner-connection, duplicate-connection, TEMP, run-schema, UDF, Arrow-registration, cancellation, timeout, and cleanup probes against DuckDB JDBC 1.5.5.1; the recorded lifecycle probe completed successfully on the pinned JDBC runtime.
- [x] Replace TEMP-only reusable cache semantics with run-private ordinary tables/views where reuse can cross a connection.
- [x] Build the exact v0.7 compatibility capsule outside the upstream reactor; emit source hashes, notices, patches, and dependency provenance.
- [x] Add a pure-engine dependency gate that inspects resolved and shaded runtime contents for Spark/GraphFrames/Scala/`zingg.*` leakage.
- [ ] Execute a deterministic `findTrainingData -> applyLabels -> trainMatch -> restart -> match` workflow **using the persisted model for scoring**. The native-classifier path derives positive/negative labels from accepted pair IDs, trains, reloads, and proves label-dependent predictions. A three-runtime regression persists labels in `PersistentLabelDecisionProvider`, closes runtime 1, restores them and trains the classifier in runtime 2, then loads that artifact and scores MATCH inputs in runtime 3; `PersistedMatchWorkflowTest` passes 7/7 on Windows and WSL2. A separate three-process worker smoke on Windows/WSL proves opt-in pending-pair producer/delivery replay, applied-label replay, training from recovered decisions, and later classifier scoring. Separate Windows/WSL probes cover four-process queue contention and lost-acknowledgement recovery after worker kill; focused child-JVM hard-stop tests cover three atomic snapshot boundaries and orphan-temp cleanup. Latest hardening opens the lock and snapshot files with `NOFOLLOW_LINKS`; snapshot size validation and content read now share one channel. A deterministic post-construction symlink replacement regression verifies both targets cannot be followed and an outside sentinel remains untouched. Newly created POSIX lock and snapshot files are explicitly mode `0600`, and pre-existing files with broader POSIX permissions are rejected on open; `PersistentLabelDecisionProviderTest` now includes both permission regressions and passes 34 tests on Windows (4 environment skips) and 34/34 on WSL2. Windows files continue to rely on inherited directory ACLs; no explicit private ACL policy is implemented. Sustained load, power-loss policy, and per-syscall interruption remain open. The full gate remains open for default-worker durability, approved pair/score/schema goldens, canonical candidate/graph outputs, upstream v0.7 model parity, and broader failure recovery; the backend histogram remains non-scoring.
- Coverage increment (2026-09-29): `PersistedMatchWorkflowTest` now exercises classifier train → same-workflow `restart()` (closing/recreating its job) → reread inputs → MATCH with the reloaded persisted classifier, then separately retains new-runtime `restartFromModel` prediction checks. The focused dependency reactor passed on Windows and WSL2. This verifies both local restart APIs, not the multi-process default-provider or canonical Zingg phase gate above.
- Coverage increment within this open persisted-workflow gate: replaced delimiter-concatenated pair keys in both in-memory label storage and `PersistedMatchWorkflow` with structural `(leftId,rightId)` keys. The SQL label predicate now renders embedded NUL as `chr(0)` segments. Windows/WSL regressions distinguish two NUL-bearing pair IDs through acceptance, persisted snapshot/reopen and actual positive/negative classifier label derivation. Default-provider durability and upstream phase parity remain open.
- Additional recovery-parser hardening (2026-09-29): snapshot decoding now rejects noncanonical boolean bytes outside `{0,1}` for pending-payload-presence and delivery `hasMore` fields, duplicate/blank delivery or enqueue receipt keys, and duplicate applied-label idempotency keys across ZLD1/ZLD2/ZLD3—even when snapshot checksums are valid. Crafted version-specific regressions recompute valid checksums around these malformed states; Windows/WSL verification passes. This does not close the broader recovery/durability or upstream-parity gate.

The current snapshot writer also retries transient Windows `AccessDeniedException` during atomic replacement (bounded five attempts; atomic-only, fail-closed). Ten repeated two-instance contention iterations now pass on Windows and WSL. This improves local robustness but does not close the workflow gate: hosted Windows behavior, sustained multi-process contention, power-loss durability, default persistence, and phase/model parity remain open.

### P0 — model and labeling compatibility

- [ ] Resolve EXT-02 provenance and authority before claiming that the checked-in blocking model is a v0.7 artifact. The exact `zingg.block` bytes are also present in upstream v0.3.4 test resources (tag commit `297c7906e73afe7d8d86ee46681b152d212699f6`, blob `68b9faeae91e31ce979aee5a51d1ea35ba691331`) and were carried into the pinned v0.7 test tree; this alone does not prove producer version, maintainer authority, or v0.7 compatibility. The stream declares historical `zingg.block.*`/`zingg.client.*`/`zingg.hash.*` names, while pinned source defines `zingg.common.*`; the strict isolated importer rejects it without output on Windows/WSL. Tree UID `-7879348438713840442` differs from pinned 0.7.0 UID `69916775799935088`, ruling out simple class-name aliasing. Required sequence: obtain maintainer decision on target contract; obtain a compatible approved artifact or approved historical source plus field-by-field migration specification; record source records and expected neutral tree nodes, field context, hash identity and edges; implement within the existing isolated/filter boundary; verify approved goldens on Windows and WSL; only then close this task. Do not weaken the deserialization filter or patch descriptors speculatively. See [external input handoff](docs/external-inputs-request.md) and [fixture provenance audit](docs/upstream-fixture-candidate.md).
- [x] Import the pinned v0.7.0 Spark ML classifier fixture and verify vector assembler ordering, polynomial expansion, logistic margin/probability, thresholding, and unsupported-stage diagnostics. Stage directories and Parquet part-file names are discovered by strict class/paramMap metadata and bounded unique-file search (not hard-coded Spark UIDs); duplicate stages/data files, fractional polynomial degrees, malformed JSON/UTF-8, and wrong field types fail closed without publishing a model. Parquet vectors require exact dimensions, one classifier row, and finite fully parsed values; every directory component beneath the fixture root is checked without following symlinks. This remains fixture-envelope support, not a general Spark model importer: root layout, three supported stage classes, feature/degree/threshold/shape constraints are intentionally fixed. The fixture is compared to a Spark 3.5.5 WSL oracle; historical Spark 3.0.1 re-execution and approved broad v0.7 goldens remain separate gates.
- [x] Implement `GetPendingLabels`/`ApplyLabels` protocol semantics with bounded batches, idempotency, request correlation, and structured rejections.
- [x] Add model round-trip manifests, checksum verification, and explicit unknown-version failures; version migration rules remain intentionally unsupported until a migration contract exists.

### P1 — resource, correctness, and platform gates

- [x] Set and observe `memory_limit` plus `max_temp_directory_size`; measure RSS independently and enforce documented budgets.
- [x] Add forced-spill, cancellation, timeout, process-kill, partial-output, and orphan-cleanup gates.
- [ ] Add JVM-worker clean installs with no system Java on Linux, macOS, and Windows. Local Windows evidence now includes package/SBOM/checksum verification, source-archive verification, and the `.cmd` worker ping/shutdown with a restricted PATH and no `JAVA_HOME`; WSL evidence includes native bundle assembly, shell syntax, packaging tests, bundle/source-archive verification, and the portable no-system-Java smoke. PowerShell and POSIX bundle verification now share `verify_package.py` for required inventory, source/worker provenance, runtime/profile/policy structure, Java 21 declaration, Python-cache exclusion, and JRE executable/version/legal metadata; cross-wrapper bundle acceptance is covered locally. Native hosted Linux/macOS/ARM clean-install evidence, independent clean-install fixtures, and the complete Python-client matrix remain outstanding. The portable POSIX no-system-Java verifier is kept separate from Linux-only RSS/process-group probes.
- [ ] Build and qualify a GraalVM Native Image worker with tracing/reachability metadata; compare outputs, model loading, startup, RSS, size, and diagnostics against JVM worker. Windows PowerShell and POSIX wrappers now use shared target-specific metadata preparation; JNI/reflection metadata was captured with GraalVM's tracing agent and reviewed into the worker resources. An actual no-fallback Linux x64 image built with Oracle GraalVM 21.0.12+7.1 (download SHA-256 `b007ff64c425f85bbe0e686107044fba6ca5054a7e89271a473767f546aaddc1`). The version-4 comparator requires recorded Native Image toolchain provenance, captures Java runtime/OS/architecture/launch flags and artifact hashes, refuses to retain results if paired workers differ in runtime limits/configuration, and measures deterministic MATCH inputs at 64/128/256 rows with full per-pair output parity. WSL2 Linux x64 report `benchmarks/native-image-scaling-2026-09-29-wsl.json`: one warmup plus five measured pairs passed protocol, SQL count, classifier train/reload, all MATCH rows/hashes, and worker configuration parity. Median MATCH throughput was 47,934/34,857 output rows/s at 64 rows, 169,727/113,261 at 128, and 423,829/311,499 at 256 (native/JVM); median latencies were 20.7/28.5 ms, 23.8/35.6 ms and 38.4/52.2 ms, respectively. At 256 inputs this produced 16,256 output pairs; post-MATCH median RSS was 134.7/120.1 MB. The separate version-3 startup report records 0.830/1.687 s median startup-to-ping and 110.5/91.8 MB artifact sizes. These are repeatable local micro-workload diagnostics, not representative capacity budgets or platform qualification. Still require broad protocol/Arrow/model/failure/security tests, hosted Linux reproduction, Windows/macOS/ARM target builds as supported, artifact SBOM/signing/security review (GraalVM reports Java deserialization), and an explicit release support decision before closing the gate.
- [x] Add Linux amd64/arm64, macOS x86_64/arm64, and Windows x86_64 release jobs, including offline installation and launcher prerequisites.
- [ ] Add signed provenance attestations, source archive verification, SBOM/license review, rollback package, and JDK security-update tracking. Rollback creation/verification is now shared across PowerShell and POSIX packaging, bound to the verified checksum inventory, with local unit/wrapper evidence. Native wrapper subprocess tests now exercise create/verify with space-containing paths and checksum-failure propagation without partial output on Windows and POSIX. The shared rollback verifier now rejects unlisted ZIP directory entries, duplicate JSON keys, unknown/missing manifest fields, and invalid manifest field types as well as unlisted files, maintaining exact member inventory and manifest shape. Source ZIP verification now shares a non-extracting Python validator across Windows/POSIX; it rejects unsafe/duplicate/symlink/special/encrypted/directory entries, duplicate JSON keys, unknown manifest fields, malformed entry shapes and case/Unicode-colliding paths; it requires exact manifest inventory/hashes and caps entry count, total uncompressed bytes, and manifest size. Its canonical creator also has injected atomic-publication-failure coverage proving an existing archive remains intact and staged ZIPs are cleaned. Package checksum generation and verification now reject Windows-reserved/invalid/trailing-dot/trailing-space path components and case/Unicode-normalization collisions so POSIX-built bundles remain portable to Windows; injected pre-publication and atomic-replace failures prove the prior `SHA256SUMS` remains byte-identical and staging files are removed. Source provenance generation now uses one canonical Python implementation across platforms, including root files, discovered module POM/lock files, and the worker; the structural verifier rejects malformed/unsafe/duplicate records and binds worker provenance to the actual packaged JAR. Same-input provenance output is byte-identical on Windows/WSL. When the sibling source ZIP is available, both wrappers now validate it without extraction and compare every declared source digest against its `SOURCE-MANIFEST.json`; standalone bundles without that sibling can only receive structural/worker-hash validation. The executable signer/validator remains only one part of this open release-control gate; signed attestations, independent license review, production JDK update ownership, and hosted clean-runner release qualification remain outstanding.
- Package inventory enforcement is also unified: both packagers use `write_bundle_manifest.py`; PowerShell and POSIX verification delegate to `verify_bundle_inventory.py`, which rejects traversal/ADS/ambiguous and case-colliding paths, malformed/duplicate hashes, missing or unlisted files, digest mismatches, external symlinks and symlink directories while supporting safe internal JRE symlinks. Fresh local Windows and WSL bundles (the WSL build includes a jlink JRE) pass inventory, package and rollback verification; this does not replace hosted clean-install/signature/security qualification.

### P2 — compatibility and ecosystem backlog

- [ ] Maintain a MatchType/function inventory and a complete ZFrame operation matrix, including null/type coercion, ordering, sampling, explode/split, and duplicate semantics. `docs/semantic-inventory.md` now enumerates all 11 MatchTypes reflectively exposed by the pinned capsule, explicitly marks upstream feature generators not implemented locally, documents all registered hash/similarity names and observed NULL/normalization quirks, and indexes every ZFrame method family against the 93-signature routing/coverage files. Registry-name assertions and an opt-in pinned-capsule MatchType probe guard inventory drift. This documentation/consistency slice is implemented; complete per-overload semantic proof and approved feature goldens remain required to close the task.
- [x] Add graph-clustering parity fixtures for connected components, implied pairs, zero-score propagation, min/max aggregation, cluster IDs, and LinkOutput asymmetry; GraphOutputTest and CompatibilityFunctionsTest cover the full listed fixture set.
- [x] Add DuckDB next-patch canary and DuckDB 2.0 prequalification lanes without making snapshots release blockers.
- [x] Add extension-backed connectors only behind explicit offline/allowlist feature flags; `ConnectorPolicy` and regression tests enforce offline denial, normalized allowlists, and invalid-name rejection.
- [ ] Establish and enforce explain/profiling-backed performance regression thresholds for runtime, memory/RSS, spill, and output size. Benchmark history, smoke validation, and local diagnostic Arrow/native-image reports exist; Linux CI validates clean repeated timing samples separately from 50 ms sampled RSS/spill diagnostics, but has no agreed fixture-specific budgets or enforced regression thresholds. Resource values are best-effort maxima, not guaranteed peaks; RSS is process-wide/cumulative and monitoring runs can perturb their own timings, so those runs are not used as clean timing evidence. CPU counters, larger capacity cases, vectorized C Data/CTAS comparison, and independent-runner reproduction remain open. Current 2026-09-30 10k-row results in `benchmarks/README.md` and the JSON report are local only, not acceptance evidence.
- [x] Prepare narrowly scoped upstream compatibility RFCs/issues only after a backend failure demonstrates the required abstraction; `docs/upstream-rfc-arrow-egress.md` records the reproduced offline Arrow COPY failure and scoped request.

## Audit addendum — 2026-09-23 specification reconciliation

The attached repository audit is authoritative for the next implementation pass. Its requirements are grouped below so implementation tasks remain traceable to executable evidence.

### Completed during this pass

- [x] Release job permits when job-schema creation fails.
- [x] Always close job JDBC connections even when table/schema cleanup reports an error.
- [x] Discover authoritative SQL-frame columns lazily so `sql(...).select(...)`, `withColumn`, `drop`, and `rename` compose correctly.
- [x] Materialize cache plans once, then enforce row budgets against the materialized relation.
- [x] Validate Python response correlation IDs and escalate worker termination to kill after timeout.
- [x] Correct Unix CI manifest assertions and make JRE verification/metadata platform-aware.
- [x] Correct generated project/module SBOM license declarations to AGPL-3.0-only.
- [x] Add regression tests for lazy frame metadata and cache composition.

### P0 implementation gates

- [ ] Add and qualify `adapter-zingg07` against the exact Zingg v0.7 generic contracts while keeping `engine-*` Spark-free. The opt-in `ZFrameDuckAdapter` now routes all 93 signatures, including both sample overloads; this is routing/implementation status, not release semantic parity. With-replacement sampling implements per-source-row Poisson(λ=fraction) multiplicities for Spark's supported fraction range [0,1], with one materialized random variate per source row and a bounded inverse-CDF lookup. `as(String)` creates a distinct relation identity, preserves logical column names, and passes alias self-join equivalence against Spark 3.5.5 on Windows/WSL. Empty `select(C...)`/`select(List<C>)` projections preserve row multiplicity using a private physical sentinel while exposing zero columns/schema/row values; selected Spark differential and engine composition tests cover this relation. CSV/Arrow exports reject zero-column frames before publishing output, with engine tests verifying no file is created. Exact-contract tests cover representative relational paths, row accessors, schema rendering, sample overload/bounds/empty/endpoints; the reference module differentially covers aggregation/null behavior, selection/projection/drop/rename, joins, set/union operations, deduplication, sorting, field metadata and `fieldIndex` (including Spark's last-index behavior for duplicate names and missing-field errors), schema names/types, `limit`, boundary/statistical no-replacement sampling, and Poisson replacement count/occupancy/duplicate behavior for float and double against Spark, plus `coalesce`, all four `repartition` overloads and selected `split`/`explode` cases. The canonical capsule and exact-profile suites were revalidated on WSL: 93/93 signature inventory, 93/93 matrix, exact contract 3/3, contract reflection 2/2, and Spark differential 1/1. Windows and WSL2 tests pass for the selected fixture set. DuckDB JDBC nullability remains conservatively wider than Spark in some plans. Per-overload differential goldens, exact nullability parity, hosted execution, packaging policy, and complete lifecycle/semantic qualification remain open.
- [ ] Close the executable **upstream Zingg v0.7 `ZFrame`** method-coverage gate. `tools/verify-zframe-inventory.py` hash-verifies all 93 signatures, and `tools/generate-zframe-adapter-matrix.py --check` verifies dispatch and evidence links. As of 2026-09-30, 47 overloads are `PER_OVERLOAD_SPARK_DIFFERENTIAL` (including `not(C)`, `or(C,C)`, and `gt(C,C)`); each has a dedicated Spark test method, direct result/output assertion, and Javac-attributed overload resolution. The other 46 signatures remain `OVERLOAD_SELECTED_SEMANTICS_UNPROVEN`; their calls resolve exactly within shared selected-operation fixtures, but assertions are not attributable to each overload. The complete current differential class plus resolver passed on Windows (**48/48 + 1/1**, zero failures/errors/skips); focused `gt(C,C)` differential plus resolver had previously passed Windows, WSL, and Docker (**2/2 each**). Remaining: add independent/structurally linked assertions for the other 46 overloads, expand edge cases, obtain approved Zingg v0.7 goldens, and collect full current-suite Linux/Docker and actual hosted runner evidence. Run platform reactors serially or in isolated target trees because this checkout's platform runs share Maven outputs.
- Coverage increment within this still-open gate: Spark differentials separately invoke `joinOnCol(ZFrame,String)` (USING join), `joinOnCol(ZFrame,C)` (expression join), `join(ZFrame,String)` (implicit right-side `z_<key>`), `join(ZFrame,String,String,String)` (two keys with explicit `left_outer`), and now `drop(String...)` distinct from the string/column drop routes. Fixtures compare output schema and full row multiplicities; join fixtures include duplicate matches and outer unmatched rows. Exact-contract routing alone is not considered parity proof.
- [x] Give neutral predicate partitioning the explicit `partitionByPredicate` API name while retaining `split(Expression)` as a compatibility alias; the Zingg-shaped string-splitting overload remains distinct.
- [ ] Replace identity phase executors with explicit supported/unsupported behavior and complete FIND_TRAINING_DATA, labeling, TRAIN, MATCH, and LINK orchestration. FIND_TRAINING_DATA and explicit MATCH are locally registered; native TRAIN has an executed artifact/output acceptance test but emits only `BACKEND_BLOCKING_HISTOGRAM`. The experimental native classifier trainer rejects inputs above `maxRows` instead of truncating labels, rejects duplicate or label-leaking predictors and one-class data, verifies the count/collect row set did not change, and returns defensive coefficient copies; trainer-result and persisted-artifact-config immutability tests pass 5/5 on Windows and WSL, with the full Windows module passing 70/70 applicable tests (2 skipped) immediately before the final immutability assertion was added. Native LINK consumes scored pairs (`z_zid`, `z_z_zid`, `z_score`), computes deterministic connected components/entity min/max scores, propagates zero for implied transitive pairs, and writes `z_zid,z_minScore,z_maxScore,z_cluster`. Output entities are inserted with bounded prepared-statement batches into a typed job-owned table, with run-schema cleanup. Hard caps reject more than one million input edges or output entities before result publication; configured row/collect limits remain enforced. Windows/WSL acceptance covers duplicate/reversed edges, text IDs, disconnected components, empty input, missing columns, NULL rejection, oversized input and lifecycle cleanup. Pipeline publication no longer downgrades to a non-atomic replacement when `ATOMIC_MOVE` is unsupported; a deterministic regression checks fail-closed behavior and preserves the previous destination and temp source. Durable labeling is opt-in, and canonical Zingg blocking/classifier TRAIN and LINK output parity remain open pending approved v0.7 fixtures.
- Coverage increment within this still-open phase gate: added full `ZinggPipeline` native LINK acceptance across two input files with different column orders. This exposed and fixed `z_zid` being overwritten by generic generated row IDs and LINK inputs being unioned positionally; LINK now preserves supplied endpoints and unions by name. Windows and WSL checks compare final entity rows, cluster, min/max scores, output schema, and count. This is local native-contract evidence, not approved Zingg v0.7 phase parity.
- [x] Repair ordinary file-ingress metadata and implement deterministic Zingg pair projection with `z_`-prefixed right-side fields.
- [x] Separate backend-native blocking histograms from strict `zingg07-blocking-tree-v1` model types; validation of a real upstream tree remains an external gate.
- [x] Implement strict v0.7 similarity vectors and a persisted quirk registry, including Jaccard and released Jaro/Jaro-Winkler behavior; registry vectors and the Jaro-Winkler delegation are covered by `SimilarityRegistryTest`.
- [x] Validate the checked-in candidate Spark ML classifier envelope, preserve vector ordering, and make imported thresholds authoritative; the fixture is imported and scored against a Spark 3.5.5 WSL oracle. The importer now also validates exact ordered PipelineModel stage UIDs and feature-column wiring, rejecting an extra stage or disconnected columns before artifact publication. This is not approval of the fixture's v0.7 provenance or a general Spark model importer.
- [x] Remove the unused legacy worker path with arbitrary SQL; the active worker keeps diagnostics behind an explicit unsafe-debug mode.
- [x] Replace delimiter-based nested worker payloads with structured versioned messages.
- [x] Make Python installation discover a worker and private Java runtime on at least one supported platform; packaged Windows Python-client and no-system-Java launcher smoke both pass.

### P1 correctness, performance, and release gates

- [ ] Complete Arrow qualification: offline IPC egress is implemented and focused ingress correctness covers budgets/rollback (including multi-record-batch budget and injected post-commit failures after partial JDBC/Appender ingestion), timestamp units/timezones, unsigned maxima, signed integer widths/boundaries, and preservation of FLOAT32 versus FLOAT64 through JDBC and Appender ingress, IPC egress, and re-ingress. Arrow HALF/SINGLE map to DuckDB FLOAT and DOUBLE maps to DOUBLE; IPC egress emits Float4Vector versus Float8Vector according to the DuckDB JDBC type. A regression now decodes raw Arrow Float16 bit patterns (normal/subnormal values, signed zero, largest finite value, infinity, NaN, and NULL) to their corresponding float values; HALF necessarily widens to DuckDB FLOAT/SINGLE because DuckDB has no 16-bit floating storage type. Arrow ingress now opens one `NOFOLLOW_LINKS` file channel for both IPC magic detection and parsing; a symlink-to-external-sentinel regression verifies rejection, no table publication, and unchanged target bytes. Injected `InterruptedException` after a committed batch preserves the interrupt flag and removes partial JDBC/Appender tables; a malformed-second-batch fixture proves actual decoder failure after the first batch commits rolls both paths back. A second fixture uses well-framed Arrow IPC with a raw DECIMAL(5,2) payload outside the declared precision; it proves an actual `SQLException` from each DuckDB JDBC/Appender insertion path after a prior batch commit, with partial-table and registration cleanup. A hard child-JVM termination regression kills Arrow ingestion after the first committed batch and proves runtime reopen removes the stale table/schema; a companion test protects live schemas when another runtime starts. The full `engine-api` + `engine-duckdb` reactor now passes 65 tests on native-filesystem WSL and 65 tests/0 failures/1 Windows symlink-capability skip. Packaged-worker `run_v2` cancellation during multi-batch Arrow ingestion now also passes through Python on Windows/WSL and in the networkless Linux Docker package integration; this does not close the separate durable/output mutation and process-recovery cancellation contracts. Pipe ingestion uses DuckDB's row-wise Appender based on a focused Windows/WSL comparison showing lower local import time than prepared JDBC batches at 1k/10k rows; preserve the prepared-statement path for differential/fallback use. Vectorized C Data/CTAS comparison, RSS/spill measurement, independent-runner reproduction, and Tier-A release qualification remain open. Keep this gate open until the candidate path proves full type/limit parity and a repeatable resource-aware performance improvement.
- [x] Replace the generic spill budget with typed input/output/collect/temp-directory budgets; collect and spill limits map to explicit runtime enforcement/settings.
- [x] Add byte-bounded collection and typed collect limits; optimize repeated graph closure edge scans and retain the 301-node chain regression benchmark.
- [x] Add deterministic local evidence for concurrent-job slot enforcement and run-schema/materialized-cache cleanup; startup now drops stale reserved `_zingg_run_*` schemas after process death while preserving active same-JVM schemas. A child-JVM Arrow import hard-kill regression proves recovery removes the committed partial table/schema on database reopen on Windows and native-filesystem WSL. Pipeline output is serialized per canonical destination using a retained sibling OS lock sidecar; an exact-key local lock registry removes idle entries instead of colliding unrelated destinations on fixed stripes, while cooperating processes use the OS lock. Retry removes only matching direct-child regular staging files under that lock before atomic publication. A child JVM halted immediately before rename proves the old output survives and a subsequent run removes the orphan and publishes a complete result. Coordinated tests prove same-target cross-process and same-JVM alias writers serialize, while two destinations with the same basename in different directories publish concurrently. A lock-path directory causes real `FileChannel.open` failure; the old result remains intact/no staging is created, and a later retry succeeds. Atomic-move failure likewise preserves prior output, removes staging, and permits a subsequent successful retry. Symlink lock sidecars are rejected and orphan cleanup does not follow/delete symlink staging entries. `ZinggPipelineTest` passes 25 tests with no failures on Windows (3 symlink-capability skips) and WSL (25/25, no skips). Power-loss durability, native `FileLock.lock()` backend failures, arbitrary unrelated orphan artifacts, non-cooperating writers, network-filesystem lock semantics, and hosted crash-recovery qualification remain open.
- [x] Add and exercise the POSIX packaging entry point, including WSL path conversion and bundled-Java package verification; native Linux/macOS artifact execution and cross-architecture release evidence remain open.
- [x] Add forced-spill, low-resource, cancellation, process-kill, orphan-cleanup, and concurrent-job tests. Persistent-label recovery now also tests hard child-JVM termination at every snapshot write stage for both producer enqueue and consumer delivery, and repeated concurrent delivery across provider instances. New startup-cleanup regressions verify multiple orphan staging files are removed without changing the committed snapshot, unrelated/nested partial-like files are preserved, and a symlink matching the orphan pattern is neither followed nor deleted. Focused suite passed Windows (36 tests, 0 failures/errors, 5 capability skips) and WSL (36/36, no skips); filesystem power-loss durability and broader process-crash matrices remain distinct open gates.
- [ ] Complete and approve the transitive SBOM/license/security release gate. Windows and POSIX packaging collect Maven JSON trees across reactor modules and use one SPDX 2.3 generator with dependency relationships/scopes, embedded shaded-JAR `pom.properties` components and structural verification; local end-to-end package, source archive, bundle verification, and rollback assembly/verification pass on Windows and WSL. The latest native WSL package emitted SPDX with 205 packages / 239 relationships (the Maven graph alone had 204 / 223); one embedded `org.jctools:jctools-core:4.0.5` coordinate is absent from those trees and remains explicitly marked for review because Netty relocates its classes while retaining its Maven metadata. Packaging stages all 20 runtime dependency JARs, preserves 21 verbatim license/notice/copyright resources under a hash-indexed bundle directory, and verifies their exact inventory/hash. 195 package license declarations remain `NOASSERTION` pending authoritative review. A mandatory pinned OSV workflow now builds the native Linux release package and scans its generated SPDX SBOM on push, PR, and weekly schedule, failing on findings or scan failure; actual hosted report/results and component-level triage remain pending. The prior OWASP Dependency-Check remains supplemental/opt-in pending NVD credentials. Still establish complete provenance/content/license inventory (including JRE, separate legacy artifacts, and metadata-only components), assess additional legal resources, resolve every shipped license, and complete independent license/CVE review before release. Shade still reports overlapping internal META-INF resources and module descriptors; preserved notices and an automated scan do not constitute legal/security approval.
- SBOM-to-scanner coverage invariant: `verify_spdx.py` now rejects any non-product dependency lacking exactly one well-formed package-manager PURL before the bundle can pass verification and the SBOM can be uploaded to OSV; Windows/WSL negative tests cover missing, malformed, and duplicate PURLs. Hosted OSV execution and advisory triage remain open.
- [ ] Run clean-install/no-system-Java validation on Linux, macOS, and Windows. Local evidence now includes Windows package/JRE/smoke and Ubuntu/WSL package/JRE/smoke both from the shared NTFS checkout, plus a fresh Ubuntu/WSL checkout copy under `/home` on the native Linux filesystem: all nine Maven modules build, the native Linux package and rollback verify, and its launcher passes with system Java removed from `PATH`. Native-filesystem test evidence: default Maven reactor `BUILD SUCCESS` (compat runtime 104 tests, 0 failures, 2 opt-in contract skips), Python client 15 passed/7 subtests, and packaging suite 89 tests/0 failures/1 platform skip. This closes only the local Ubuntu-native-filesystem portion; clean macOS and hosted/native runner matrix evidence remains required before checking this cross-platform gate.
- Container qualification addendum: `packaging/verify-docker-package.sh` runs the bundled Linux worker inside `ubuntu:24.04` with networking disabled, a read-only root and bundle mount, executable temporary `/tmp` for DuckDB JNI, and a PATH without system Java; it then runs the packaged-JRE/shaded-JAR Python process cancellation suite inside a separate networkless `python:3.12-slim` container with read-only bundle/source mounts. The Linux offline-package CI lane invokes it after assembling/verifying the bundled-JRE package. A fresh current-tree native-WSL package passed both local containers on Docker Desktop `linux/amd64`, including **2/2** packaged worker process tests. Workflow regression and full packaging suites pass on Windows and WSL; hosted Linux execution remains necessary before production matrix acceptance.

### External evidence gates

- [ ] Obtain real Zingg v0.7 blocking/classifier artifacts and Spark differential fixtures.
- [ ] Confirm the intended integration boundary: standalone repository versus main Zingg `home/duckdb` module.
- [ ] Execute the full CI matrix on hosted Unix runners after executable-mode repair; the workflow now enforces `mvnw` executable mode and uses `setup-java@v5`. Local audit fixed an orphan `PY` command in the ZFrame Bash step, moved provenance output outside the checksum-sealed bundle, and changed Unix assembly to invoke `package-native.sh` directly (since `package.sh` can dispatch to PowerShell when installed). WSL2 native package+JRE, SBOM/source/rollback verification, no-system-Java smoke, regression tests, YAML parsing, and Windows/WSL packaging suites pass locally. Still require actual hosted Windows/Linux/macOS/ARM execution, artifact upload/hash verification, and review of runner logs before closing this gate.
- [ ] Decide the distribution contract for legacy Spark importers and any external Spark prerequisite.

## 12. Detailed remediation plan for remaining requirements

The [2026-09-27 remediation audit](docs/remediation-audit-2026-09-27.md) expands the remaining gates into prioritized implementation, test, external-input, and release tasks. It also records two reopened checklist items and the immediate LINK/MATCH routing defect.

This is the execution plan for every unchecked requirement. A task is complete only when its implementation, test, and evidence exit criteria are all satisfied.

### R0 — freeze and obtain the compatibility boundary

1. Decide and record whether this repository remains standalone or becomes `home/duckdb`; freeze Zingg `v0.7.0`, DuckDB/JDBC, Java, Python, Spark-importer, artifact-layout, and support-policy decisions in [docs/compatibility-decision.md](docs/compatibility-decision.md). The current file records a provisional standalone boundary and the required maintainer confirmations.
2. Obtain the exact Zingg generic contracts, real blocking trees, Spark ML classifiers, labeled fixtures, and Spark reference outputs using the intake checklist in [docs/external-inputs-request.md](docs/external-inputs-request.md). Hash every input, record license/provenance, and keep fixtures outside pure runtime dependencies.
3. Create `adapter-zingg07` and a reproducible generic-contract capsule containing the exact ZFrame, Context, DSUtil, GraphUtil, ModelUtil, PipeUtilBase, model, and phase interfaces. Compile it against pinned inputs while keeping `engine-*` Spark-free.
4. Generate a reflection/API method inventory and capability matrix. CI must fail on missing methods, undocumented unsupported behavior, or accidental Spark/Scala/GraphFrames leakage.

Exit evidence: approved boundary decision, capsule hashes/notices/patches, adapter compilation, purity report, generated coverage report, and clean-checkout API tests.

### R1 — implement and prove the complete workflow

1. Freeze stable row IDs, field ordering, null/type coercion, duplicate, ordering, sampling, and seed semantics.
2. Implement `FIND_TRAINING_DATA` with real blocking candidates, persisted IDs, bounded output, and source-column preservation.
3. Integrate bounded `GetPendingLabels`/`ApplyLabels` with ordering, correlation, idempotency, conflict handling, and restart-safe state.
4. Implement `TRAIN`/`trainMatch`: import or train the supported model, persist profile/checksum/vector order/threshold, and reject unsupported stages with stable diagnostics.
5. Implement restart from persisted state without recomputation or corruption.
6. Implement `MATCH` using strict vectors, model-owned threshold, deterministic predictions, pair schema, and bounded output.
7. Implement `LINK` and verify connected components, implied pairs, score aggregation, cluster IDs, and LinkOutput asymmetry.
8. Add a command-level acceptance test that runs `findTrainingData → applyLabels → trainMatch → restart → match → link` twice and compares canonical outputs, manifests, and checksums.

Exit evidence: persisted-model workflow succeeds after restart, repeated runs are deterministic, differential outputs match, and unsupported phases fail explicitly.

### R2 — strict model and similarity parity

1. Import real Spark ML classifiers and verify VectorAssembler order, PolynomialExpansion basis/degree, coefficients, intercept, margin, probability, threshold, and unsupported-stage diagnostics.
2. Persist a versioned strict quirk registry, including released Jaccard null/empty/case/punctuation behavior and Jaro-Winkler delegation to Jaro; put corrected behavior in a separate profile.
3. Generate golden vectors for every supported MatchType, nulls, empty values, Unicode, normalization, coercion, and malformed input; compare against Spark/SecondString with defined tolerances.
4. Fuzz model preflight for missing fields, non-finite values, wrong vector lengths, unknown stages, unsupported degrees, and threshold conflicts.

Exit evidence: real classifier differential suite, golden-vector report, persisted quirk manifest, threshold-authority test, and zero unexplained prediction differences.

### R3 — Arrow and relational data plane

1. Replace default `COPY ... FORMAT ARROW` egress with offline-safe JDBC result reading plus Arrow Java IPC writing; define nested-type support and close/error behavior.
2. Benchmark row-batch ingress, registered Arrow plus immediate CTAS, and temporary Arrow/Parquet scan; choose the safe path and materialize reusable data immediately.
3. Add round trips for primitive/null/timestamp/decimal/binary/list/struct/empty/large-batch/malformed IPC cases.
4. Prove connection scope: Arrow registrations cannot outlive their connection; run-schema tables are qualified and cleaned.

Exit evidence: independent Arrow IPC reader validates output offline, ingress benchmark report exists, and default mode never installs extensions.

### R4 — resource, failure, and concurrency gates

1. Force spill with low memory and a controlled temp directory; capture settings, spill bytes, RSS, time, and cleanup.
2. Test input/output/collect/temp budgets at exact limits and one byte over; errors must identify the typed category.
3. Request-selective cancellation is now implemented: the Java reader handles `cancel <request-id>` concurrently with a single-threaded, bounded (64 queued) operation executor; request tokens are scoped into opened jobs; JDBC statements receive `Statement.cancel()`, Arrow ingestion checks cancellation between rows/batches, and cleanup drops partial tables even after the token is cancelled. Native classifier training computes full-batch gradients in DuckDB rather than retaining boxed training rows in Java; DuckDB collection preserves cancellation identity when a cancelled query returns a JDBC exception. Cancellation-aware atomic model publication remains in place. Regression coverage includes SQL aggregate gradients and cancellation during a deliberately non-terminating million-iteration fit, with no model or staging debris. The same CPU-fit cancellation passes through a real `train` request against the packaged shaded worker on Windows, WSL and Docker; the Python cancellation-process suite passes **4/4** in all three environments. Python `submit`/`DuckWorker.request_async` return correlated handles with `result` and `cancel`; synchronous requests default to a configurable 300-second deadline and terminate the child on timeout; stderr is inherited to avoid unread-pipe deadlock. Windows and WSL tests cover active SQL cancellation, worker reuse, Arrow Appender/JDBC partial-table cleanup, client response multiplexing, and a real Python→shaded-JAR cancel→ping round-trip. `ConfiguredWorkerServerTest` verifies queue overflow, 32 completion/cancel races, and cancellation during `run_v2` Arrow IPC ingestion with no partial table/output; the five worker-server tests pass on Windows and WSL. The generated multi-batch `run_v2` Arrow cancellation also passes through Python against the bundled package on Windows/WSL and inside a networkless Docker container (Ubuntu 24.04 and Python 3.12-slim). A packaged-worker race test also performs 24 cancel-versus-durable-label-write attempts, retries each idempotency key, and verifies all 24 queued pairs persist exactly once on Windows, WSL and Docker. Remaining R4 work: cover other model/durable/output mutation phases and cleanup failures; broaden Future lifecycle/race/load testing beyond these regressions; and verify idempotent recovery after a process crash. Cancellation does not imply rollback of already-published external or durable side effects.
   Follow-up evidence: `ConfiguredWorkerServerTest` now also runs 32 completion-versus-cancel races and checks unique/present response IDs, only documented acknowledgement outcomes, terminal result consistency, and clean worker shutdown. Queue saturation with 64 queued requests, one overflow, and a still-readable cancel control is covered separately. The five worker-server tests pass on Windows and WSL2; packaged-JAR Arrow cancellation and durable-label cancel/retry idempotency are covered by Python integration on Windows/WSL and in Docker. Broader Future lifecycle, other durable/model/output mutation phases and crash-recovery race cases stay open.
4. Test timeout escalation from graceful termination to forced kill, including exit code, partial-output policy, and orphan-process checks.
5. Kill the worker during schema creation, cache, Arrow ingest, model write, and output write; restart and verify no stale schemas, tables, locks, or temp files.
6. Stress concurrent jobs with schema creation/drop, writes, UDFs, cancellation, spill, and duplicate connections.

Exit evidence: deterministic stress logs, process-tree snapshots, temp inventories, RSS/spill measurements, bounded timings, and clean restart proof.

### R5 — Python distribution and clean-install matrix

1. Choose the distribution contract: platform wheels with worker/JRE, self-contained archives, or explicit external-runtime mode. Do not advertise a source-only wheel as self-contained.
2. Build Windows x86_64, Linux amd64/arm64, and macOS x86_64/arm64 artifacts with launchers, worker, private JRE, manifests, hashes, licenses, and rollback metadata.
3. In clean environments with no system Java, install offline and run Python discovery, ping/status/run/shutdown; prove system Java is never used.
4. Test upgrades, reinstall, read-only paths, spaces/non-ASCII paths, missing/corrupt runtime, bad checksums, blocked network, and wrong architecture.

Exit evidence: per-platform transcripts, hashes, offline dependency inventories, launcher logs, and positive/negative smoke results.

### R6 — Native Image and release security

1. Build a GraalVM Native Image worker with reachability metadata for JDBC, DuckDB natives, Jackson, Arrow, services, reflection, and model loading.
2. Compare JVM/native outputs, startup, RSS, size, cold/warm latency, model loading, shutdown, and diagnostics; make a documented go/no-go decision.
3. Generate a complete transitive SBOM from Maven resolution and shaded contents; reconcile licenses, notices, vulnerabilities, provenance, and AGPL obligations.
4. Add reproducible source archives, signed provenance attestations, checksum verification, rollback restore tests, and JDK security-update tracking.

Exit evidence: JVM/native report, reproducible artifacts, complete SPDX/CycloneDX inventory, security/license sign-off, signatures, and verified rollback.

### R7 — hosted release qualification

1. Run the full matrix on Linux amd64/arm64, macOS x86_64/arm64, and Windows x86_64 with Python 3.10–3.14 where supported.
2. Run pinned, next-patch, and non-blocking DuckDB 2.0 canary lanes; preserve compatibility reports without silently changing the release pin.
3. Execute all unit, differential, stress, package, offline-install, SBOM, signing, and benchmark gates on clean runners.
4. Publish a release evidence index linking every requirement to logs, hashes, reports, owner, date, and accepted limitations.

Execution order is R0 → R1/R2 → R3/R4 → R5/R6 → R7. External blockers must record owner, requested input, date, workaround, and exit condition; they must never be marked complete because local unit tests pass.

## Audit reconciliation: `zingg_code_audit_2026-09-24.md`

The attached audit inspected an earlier repository revision and also includes findings for upstream Zingg, a historical Spark Connect branch, GraphFrames, and SecondString. Only the twelve `ZD-*` findings concern this repository. The findings below were rechecked against the current checkout on 2026-09-30; external-repository recommendations are not treated as instructions to modify those projects.

| Finding | Current disposition | Remaining evidence / follow-up |
|---|---|---|
| ZD-001 job constructor ownership leak | Fixed in `DuckRuntime.openJob`: constructor failures remove the active-schema marker, close the duplicated JDBC connection, and release the permit, including unchecked failures. | A read-only database regression forces job-schema creation to fail twice and proves the sole job slot is reusable. Direct JDBC duplicate-close instrumentation remains unavailable; hosted lifecycle/connection diagnostics remain useful evidence. |
| ZD-002 offline/external access | Engine startup now disables external access and automatic extension install/load in offline mode, disallows community extensions, installs/loads only explicit online allowlist entries, then locks DuckDB configuration. Effective flags are checked after open so JDBC URL parameters cannot silently override policy. Local filesystem access remains available through DuckDB's allowed root directory; worker `PathPolicy` remains the path boundary when roots are configured. | Expand tests to exercise every packaged file-import/export route and prove no outbound network attempt under offline mode on clean Windows/Linux environments; review global-instance reuse on hosted runners. |
| ZD-003 graph clique/quartic scoring | `entityScores` walks sparse adjacency and computes zero-aware per-entity extrema without clique expansion. `transitiveEdges` now pre-counts closure pairs, rejects before result allocation above a default 1,000,000-pair budget, supports caller-supplied budgets and cancellation, and has graph-shape/boundary tests. | Obtain explicit product/compatibility approval for the new default cap and larger-budget behavior. Run representative path/star/dense/giant-component resource benchmarks and platform evidence; its accepted failure mode is now explicit but capacity behavior is not yet qualified. |
| ZD-004 unordered silent training truncation | Already fixed before this audit: `maxRows` rejects oversized input rather than silently selecting an unordered prefix. | Preserve this as a regression and verify boundary `maxRows` and `maxRows+1` through the worker API. |
| ZD-005 JVM-resident classifier fitting | Native classifier fitting snapshots the bounded input once inside DuckDB, keeps it relational, and computes each full-batch gradient with SQL aggregates; only the small gradient vector crosses to Java. | Measure 10k/100k/1M rows at varied widths, cancellation latency, RSS, and spill; record numeric tolerances and establish supported workload budgets. |
| ZD-006 Python stderr/request hangs | Worker stderr is inherited (no undrained pipe); synchronous requests now have a configurable 300-second default deadline, terminate a wedged worker on timeout, and allow `request_timeout=None` for intentionally unbounded jobs. | Exercise timeout behavior through the packaged worker on Windows, WSL and Docker; document restart/recovery for timed-out workers. |
| ZD-007 Jaccard mismatch | Tokenizer now follows SecondString's letter-run/digit-run/whitespace/punctuation token boundaries and default-locale case folding; blank/null inputs retain the project's explicit safe score of 1 rather than legacy `NaN`. | Run the exact pinned Zingg/SecondString differential corpus, especially Turkish locale, Unicode, repeated/hash-collision tokens, and explicitly approve the blank-value deviation. |
| ZD-008 post-facto output byte limit | Still open. Atomic temporary-file publication prevents an oversized artifact from replacing the destination, but DuckDB can consume disk before the post-write size check. | Use OS/container/filesystem quotas or a bounded streaming writer; measure peak disk, not only final file size, and test output and spill budgets separately. |
| ZD-009 POSIX Maven wrapper mode | Fixed in current HEAD (`mvnw` is executable); CI now checks the wrapper mode and uses current action versions. | Hosted clean Linux/macOS/ARM jobs still must execute successfully; local Windows/WSL success is not hosted qualification. |
| ZD-010 malformed logistic SQL | The current scorer's logistic expression parses; a regression now compares SQL `z_score` against `LinearClassifier.scoreVector()` and verifies thresholded prediction. | Extend from a synthetic one-feature model to the committed imported Spark classifier and the complete MATCH/LINK worker path. |
| ZD-011 stale frame schema | `explode` now declares the `UNNEST` output column explicitly and invalidates stale schema metadata; aggregate/select-expression metadata discovery and join fallback are covered by composability checks. | Broaden differential coverage to all schema-changing overloads, duplicate column names, zero-column joins, and alias edge cases. |
| ZD-012 model threshold precedence | Current scorer uses the imported classifier threshold; importer metadata and semantic inventory agree. Matcher regression deliberately uses a conflicting `MatchConfig.threshold` to prove model-owned precedence. | Preserve real Spark threshold differential evidence at a non-default threshold such as 0.73 and document any explicit override separately. |

Audit-only findings for upstream `zinggAI/zingg`, its historical `revert-1356-spark-connect` branch, `zingg-native`, GraphFrames, and standalone SecondString are outside this repository's implementation scope. They remain external references or dependency-contract evidence, not local completion criteria unless separately requested.

## Audit reconciliation: `zingg_code_audit_2026-10-09.md`

The 2026-10-09 report was treated as evidence, not as repository instructions. Its findings were filtered to the current `zingg-duckdb` checkout; recommendations for upstream Zingg, Spark Connect, GraphFrames, SecondString, and other repositories were excluded. The following implementation work covers the local findings that were actionable in this checkout.

| Finding | Current disposition | Remaining evidence / follow-up |
|---|---|---|
| ZD-016 replay cost | Applied label journals now compact after the configured 1 MiB threshold, bounding normal replay growth while preserving the committed state. | Run long-lived and power-loss stress cases; compaction is not a substitute for filesystem durability proof. |
| ZD-021 corrupted journal header | A complete malformed header now fails closed without truncating the valid prefix; an incomplete final record remains treated as an uncommitted tail. | Expand corruption/fault injection across every record boundary and hosted filesystems. |
| ZD-022 separate spill cap | `SpillByteGuard` tracks active statements against the configured `maxSpillBytes` cap and cancels all active statements when the shared directory budget is exceeded. | Validate more DuckDB spill plans and process-level disk behavior; the guard is not an OS quota. |
| ZD-023 shared spill accounting | Active jobs sharing a canonical temp directory use one in-process aggregate guard, while DuckDB's `max_temp_directory_size` remains the cross-process/hard-limit mechanism. | Qualify multiple workers/processes and host-native filesystem quotas. |
| ZD-024 directory durability barrier | POSIX snapshot publication forces the label directory before truncating the journal; unsupported Windows directory-force providers retain the existing safe fallback. | Obtain power-loss and filesystem-provider evidence on supported release platforms. |
| ZD-025 null/sentinel collision | Blocking histograms use a typed null key, so SQL `NULL` and the literal `__NULL__` remain distinct; serialized histogram entries carry explicit nullness. | Preserve the regression through the packaged worker and broader blocking-key types. |
| ZD-026 cancellation-blocking output lock | JVM and OS output-lock waits poll with cancellation checks and clean their lock registries on interruption; a waiting-pipeline regression covers the contract. | Exercise cancellation at every output mutation phase and across native filesystems/process crashes. |
| ZD-027 POSIX executable bit | The current checkout already records the executable Maven-wrapper mode and local packaging checks cover wrapper execution. | Hosted Linux/macOS/ARM packaging and SBOM workflows remain release gates. |
| ZD-028 red CI matrix | Local module, compatibility, Python, and packaging suites are the actionable local checks; hosted red workflow history is not represented as green merely because local tests pass. | Re-run and resolve the full hosted matrix, dependency-security/OSV scan, SBOM/license review, and clean-platform qualification. |
| ZD-029 model publication gap | `ModelPublicationLock` serializes cooperating readers and writers across JVMs for the complete backup/stage/rename/verify/rollback window; a barrier regression proves readers never observe the intermediate missing root. | Qualify non-cooperating writers, network filesystems, power loss, and hosted platform behavior. |

### Implementation status and release gates

The code-level corrections above are implemented and covered by local regression tests. The build is not considered fully release-qualified until the remaining hosted CI/security, platform packaging, OS-quota, crash-recovery, and power-loss evidence is collected. Those are explicit external or qualification gates, not unresolved instructions silently imported from the report.

## 13. Execution addendum for the 2026-10-12 reports

The two supplied reports are evidence only. Their upstream-only findings (`ZN-016`, `GF-006`, `SS-014`–`SS-017`, `ZG-043`) are not local implementation tasks. The local scope is the seven `ZD-2026-10-*` findings in the 2026-10-12 review plus `ZD-016`, `ZD-021`–`ZD-029` carried forward by the 2026-10-09 audit.

| Finding(s) | Build action | Status after this execution | Exit evidence / residual gate |
|---|---|---|---|
| `ZD-2026-10-01`, `ZD-006` | Make Python timeouts request-scoped: issue correlated `cancel`, wait for bounded acknowledgement, preserve a healthy worker, and recycle only when scoped cancellation fails. | Implemented in `WorkerClient`; `cancel_grace` and `kill_on_cancel_failure` are configurable. | Python selective-timeout and silent-worker escalation tests pass; packaged-worker run is required on each hosted platform. |
| `ZD-2026-10-02` | Keep Java 21 for JVM bundles; make experimental Native Image wrappers require JDK 22+. | Implemented in POSIX/PowerShell wrappers and documentation. | JDK-21 rejection regression added; a real JDK-22+ relocated build remains a qualification gate. |
| `ZD-2026-10-03` | Close `DuckJob` lifecycle before accepting materializations; synchronize registration/snapshot cleanup and clean a table if registration races with close. | Implemented and compiled; existing ownership/resource suites pass. | Add/retain hosted close-vs-registration stress evidence; network filesystems remain out of contract. |
| `ZD-2026-10-04`, `ZD-022`, `ZD-023` | Apply an in-process shared spill growth guard while documenting that DuckDB settings are not OS quotas. | Implemented (`SpillByteGuard`) and documented. | Resource-budget tests pass; disk-full, cgroup/OOM, and multi-process quota tests remain platform gates. |
| `ZD-2026-10-05` | Run the 93-signature inventory and existing Spark differential gate; do not claim unproven overloads as parity. | Inventory is complete; current evidence is 64/93 dedicated differentials and 29 selected-but-unproven. | Finish or explicitly scope the remaining 29 and approve phase goldens before a broad v0.7 parity claim. |
| `ZD-2026-10-06` | Reconcile packaging documentation with the actual five-target CI topology. | Implemented: `packaging/README.md` now separates compile, full-test, package, and release evidence tiers and includes the support matrix. | Hosted green runs, artifact upload/hash review, and release sign-off remain open. |
| `ZD-2026-10-07` | Preserve JDBC/Appender benchmark evidence and run the current harness; keep `registerArrowStream`→CTAS as an explicit unmeasured spike. | Current implementation still uses JDBC/Appender; no C Data implementation is silently advertised. | Identical-fixture C Data/CTAS experiment and independent-runner thresholds remain open. |
| `ZD-016`, `ZD-021`, `ZD-024` | Bound journal replay, fail closed on malformed headers, force POSIX directory publication before journal truncation. | Implemented in the existing local changes. | Fault injection, power-loss, and hosted filesystem evidence remain open. |
| `ZD-025` | Preserve SQL `NULL` separately from the literal `__NULL__` in blocking histograms and model JSON. | Implemented in `NativeTrainingService`; regression coverage is present. | Packaged-worker and broader blocking-type matrix remain open. |
| `ZD-026` | Make JVM/OS output-lock waits cancellation-aware and clean lock registries. | Implemented in existing local changes. | Exercise every output mutation boundary and crash recovery. |
| `ZD-027`, `ZD-028` | Preserve executable wrapper mode and treat red hosted/security CI as an explicit release blocker. | Local scripts and static checks are present; no hosted green/security conclusion is inferred. | Hosted full matrix, dependency scan, SBOM/license review, and clean-platform qualification remain mandatory. |
| `ZD-029` | Serialize model readers and the complete backup/stage/verify/rollback publication window with a shared OS/JVM lock. | Implemented in `ModelPublicationLock`; reader/writer barrier tests are in the current local changes. | Qualify non-cooperating readers/writers, network filesystems, and power loss. |

Build execution order: compile changed modules; run focused regressions; run the full Maven, Python, packaging, protocol, model-publication, resource, contract/inventory and offline-package suites; run the current Arrow/JDBC/Appender benchmark; then record unresolved external/platform gates. A green local run is evidence for the tested host only and does not close hosted or provenance gates.
