# Remediation audit — 2026-09-27

> **Status refresh — 2026-09-29:** This audit's original findings and ordering remain useful, but the repository has since closed additional local work: durable label-decision replay/recovery probes, Arrow floating-point precision and post-commit rollback regressions, a 93/93 ZFrame-to-selected-Spark-fixture evidence mapping, shaded-runtime SPDX reconciliation, runtime dependency legal-resource preservation, jlink JRE provenance/legal inventory, a working Linux x64 GraalVM Native Image prototype with a native/JVM train-reload-MATCH smoke, and shared Windows/POSIX bundle/provenance validation. Fresh Windows and WSL bundles pass both packaged verification front ends; parity testing uncovered and fixed helper files omitted by the POSIX assembly. Both provenance wrappers emit identical byte-level documents for identical inputs, each platform bundle binds to its own worker JAR, and when the sibling source ZIP exists both verifiers validate it non-extractively and cross-check every declared source record. The current plan remains **37/53 (69.8%)** by tracked build-plan checkbox count. Do not count the prototype as Native Image qualification or release closure. See the appended current-state remediation checklist below and dated evidence in `docs/validation-status.md`.

## Scope and measurement

Sources inspected: the current working tree; `build-plan.md`; `test-plan.md`; `docs/validation-status.md`; `docs/external-inputs-request.md`; package scripts and CI workflow; key phase, model, frame, and Arrow implementations; current Surefire reports; and the available original specification `zingg-duckdb-20260921-1458.md`. The later audit attachment `zingg-duckdb-20260923-0902.md` is absent from its previously named path, so its original wording could only be checked through the repository's audit addendum. Reattach it for a line-by-line reconciliation.

The current checklist records **37/53 = 69.8%** (recounted from the current `build-plan.md` checkboxes on 2026-09-28). This is a count of checklist entries, **not** a percentage of code, tests, semantic parity, or release readiness. Many entries combine multiple obligations, and some previously checked entries still need independent clean-runner evidence. A green Maven build proves only the covered Java tests on the runner used. Historical dated entries below retain the counts reported at the time and must not be interpreted as the current total.

### Current follow-up — 2026-09-28 Arrow ingress failure cleanup

The offline Arrow IPC writer publishes through a sibling temporary file and atomically replaces the requested output after serialization and output-budget validation; tests also confirm no destination is created for an unsupported nested JDBC type. A reproduced ingress failure was different: a two-row Arrow input under a one-row budget left its newly created per-job table in place until job close. `ArrowFileSupport.read` now attempts immediate `DROP TABLE IF EXISTS` on any post-create failure, preserving a cleanup failure as suppressed on the original exception. `DuckFrameMetadataTest` proves the table is absent from `information_schema.tables` before job close. It passed 13/13 on Windows and WSL; full Windows Maven verify and Python 11/11 passed. This closes only this local rollback defect; vectorized-ingress performance, nested-type policy, later-batch faults, cancellation/kill coverage, and hosted platforms remain open. Current checklist: **37/53 (69.8%)**.

### Current integration delta (late 2026-09-27)

**Portable capsule reproducibility check.** The initial Windows/WSL JARs contained the same 134 entry names and identical `ZFrame.class` bytes, but five resources differed through CRLF/LF and an environment-specific `Built-By` line. A shared Python canonicalizer now fixes those fields and ZIP metadata. Independent full bootstraps from the pinned upstream commit on Windows and WSL both produced SHA-256 `a289903fae2ed9ff3a0a3360fed3c80232d70fce848df27e2c6a6ee8406c4b5d`; both passed the 93-signature inventory. They publish outside Maven's cache. The preexisting WSL cache still has only 92 methods and must not be used. Hosted clean-runner execution, source/dependency provenance and maintainer approval remain open; local byte equivalence alone is not release approval.

The worker now has an opt-in persistent label store, `apply_labels`, `enqueue_pending_label`, `get_pending_labels`, model-aware `run_v2`, and `train_from_labels`, which restores accepted decisions and derives pair labels. The freshly packaged worker passed a **three-process** smoke on Windows and WSL: process A queued pairs and applied positive/negative decisions; process B recovered them, verified producer/delivery replay and decision conflict, and trained a native classifier; process C replayed the first delivery, consumed the next pair, loaded the artifact, and produced two MATCH rows. `runtime-worker -am package` and full Windows Maven verify passed, and `python -m pytest -q` passed 9 tests. This is a small native-path smoke, **not** strict Zingg v0.7 parity, canonical LINK output, failure-matrix qualification, or a full differential fixture.

Immediate local closure sequence:

1. Keep `tools/verify-worker-label-restart.py` passing on Windows and WSL with three distinct PIDs and recovered positive/negative decisions. It now asserts no artifact on a no-label failure, model type/checksum, the exact `(1,3)` and `(2,3)` pair IDs, required score/prediction columns, finite thresholded scores, and positive predictions. Extend this to approved upstream score goldens, full output schema/order and repeated-run determinism; the native smoke alone does not prove them.
2. Keep `tools/verify-worker-failure-matrix.py` passing on Windows and WSL. It now exercises no labels, one class, `UNKNOWN` only, unmatched/duplicate IDs, missing feature/block schema, malformed training payload, corrupted snapshot/model, output path, and output-byte limits. It asserts no published model/output, process reuse after recoverable errors, and startup rejection of a corrupt label snapshot. Extend this to cancellation at each phase, partial-directory inventory, larger/malicious payload classes, approved phase goldens, and hosted-runner evidence.
3. The opt-in provider now persists pending-label queue state, producer keys, and idempotent delivery receipts in a versioned, checksummed snapshot. The worker and Python facade expose enqueue/dequeue; a three-process Windows/WSL smoke proves replay and FIFO consumption. Focused tests cover v1/v2 migration and two-reader same-JVM concurrency. A separate four-worker Windows/WSL probe now proves 20 distinct producer and delivery operations with no lost/duplicate pair and a simultaneous same-key replay. Remaining: sustained multi-process load, kill precisely inside snapshot replacement, and delivery lifecycle/retention limits. The default provider remains in-memory.
4. The Windows/WSL kill probe sends producer/delivery requests, kills workers without reading replies, and recovers one pair per key. Package-private snapshot observers support injected exceptions and test-only child-JVM `Runtime.halt(73)` after temporary-file creation, file force, and atomic move. Startup holds the store lock while removing only regular orphan `.labels-*.partial` files. The snapshot serializer now enforces its byte cap while writing; focused tests reject oversized UTF fields without publishing a snapshot, and the worker smoke rejects a 10,001-unit payload while staying reusable. A 10-repeat test reproduced transient Windows `AccessDeniedException` during atomic replacement (2 failures); replacement now retries that exception up to five times with short backoff, retaining the atomic-only fail-closed policy. The repeated suite passed on Windows (one symlink-related skip) and WSL. Symlink rejection ran under WSL; Windows could not create a test symlink. Remaining: sustained multi-process contention, receipt compaction, Windows symlink/permission evidence, power-loss/directory-sync policy, and independent large-payload stress.
5. Bring the default worker, Python facade, pipeline phase contract, and documentation into one supported end-to-end flow. Do not count the opt-in path as default durability or the native classifier as released Zingg v0.7 parity.

Use the following rule for every row below: close it only when implementation, negative and positive tests, and a dated evidence artifact all cover the full claim. Record command, environment, versions, result, owner, and links in `docs/validation-status.md`. Preserve existing user changes in the dirty worktree; make a reviewed baseline commit before release qualification.

## Critical corrections found in the current implementation

**ZFrame status reconciliation (2026-09-28).** The item below records the audit-time snapshot and has since been superseded by repository work: the canonical 93-signature inventory and `docs/zframe-v07-adapter-matrix.json` now verify **93 mapped, 0 conditional, 0 unsupported, 0 unmapped** (`python tools/generate-zframe-adapter-matrix.py --check` passes). `as(String)` is mapped and tested for aliases/self-joins. Both sampling overloads, including with-replacement Poisson multiplicity, now have an adapter route; exact contract tests and Spark 3.5.5 statistical differential fixtures pass on Windows and WSL2. The remaining gate is exhaustive behavior/overload parity and approved upstream fixtures, not basic routing. Keep the dated point 3 below as historical audit context, not current disposition.

1. **LINK has a native graph output path, but not verified Zingg v0.7 parity.** `LocalPhaseExecutors.registerNativeLink` accepts already-scored pairs (`z_zid`, `z_z_zid`, `z_score`), canonicalizes undirected duplicate/reversed pairs, skips self-links, computes connected-component entity summaries, emits zero scores for implied pairs, and uses the injected compatibility clock for cluster IDs. Its output shape is explicitly `z_zid,z_minScore,z_maxScore,z_cluster`. Output is batch-inserted as typed rows into a run-schema-owned table, cleaned on job close, with partial-table cleanup on insertion failure; one-million row/entity hard caps and configured row/collect budgets bound local materialization. Pair keys now use a typed ordered tuple rather than delimiter concatenation; regression coverage proves embedded-NUL IDs cannot merge distinct pairs. Tests cover transitive/disconnected components, duplicate/reversed edges, text/control-character IDs, empty input/schema, missing columns, NULL IDs, non-finite scores, configured row budgets, oversized-input rejection and cleanup on Windows/WSL. Approved upstream schema/order/score/ID goldens, rollback/cancellation, and full pipeline FIND→labels→TRAIN→MATCH→LINK remain open; do not equate this native contract with canonical LINK parity.
2. **Persisted workflow is partly repaired, but not yet full Zingg parity.** `PersistedMatchWorkflow` derives binary training labels from accepted pair decisions, trains a native classifier, loads it into matching, and rejects the backend histogram as a classifier. Focused tests cover label-dependent predictions, unmatched decisions, and corrupt classifier checksums. An opt-in, atomic label snapshot is exposed through the worker; the three-process Windows/WSL smoke now proves pending-pair producer/delivery replay, recovered-decision training, and later MATCH scoring. The default remains in-memory. Remaining: approved score/schema goldens, canonical FIND_TRAINING_DATA and LINK outputs, strict upstream classifier/blocking parity, rollback/cancellation/kill cases, and full release-scope durability.
3. **The ZFrame coverage number measures a different interface.** `FrameCapabilityReport` reflects the project's `Frame`, not `zingg.common.client.ZFrame<D,R,C>`. A hash-pinned `javap` inventory records all 93 upstream abstract overloads. The current profile-isolated dynamic adapter routes all 93 signatures; the checked-in generated matrix records 93 `MAPPED_NOT_DIFFERENTIAL`, 0 conditional, 0 unsupported, and 0 unmapped. Exact-contract runtime tests pass on Windows and WSL, including expression joins across two frame scopes. This proves routing, not semantic parity: full per-overload differential coverage and the complete adapter gate remain open.
4. **Native TRAIN now has local artifact acceptance, but still is not Zingg TRAIN parity.** `LocalPhaseExecutors.registerNativeTraining` writes a backend histogram and returns `input.cache()`. The expanded `ZinggPipelineTest` now executes TRAIN, verifies output rows, reloads the artifact, and checks its explicit backend-histogram type and payload on Windows/WSL. Remaining: trained-model reload/scoring for a classifier, failure cleanup, label influence, and approved Zingg blocking-tree/classifier differential behavior. Do not count the histogram as either upstream artifact.
5. **The direct Spark ML importer is fixture-specific.** `SparkMlClassifierImporter` hard-codes stage directory IDs, one Parquet filename, 18 columns, degree 3, 1329 features, and threshold 0.4. It proves that one checked-in fixture can be converted; it is not a general v0.7 artifact importer. Define and test the supported format envelope, discover stages/files safely, and reject every unsupported variation before claiming general support.
6. **SBOM completeness and release scanning are not proven.** The POSIX generator can include Maven's JSON tree, whereas the PowerShell generator lists locks/modules; neither excerpt proves one-to-one coverage of shaded contents. Some external licenses are `NOASSERTION`. The security workflow is opt-in and requires an NVD credential. A structural SPDX verifier and a resolved dependency tree do not equal license/CVE review.
7. **Documentation has drifted.** The prior validation header was dated 2026-09-24 despite later entries; it stated 6/6 fixture tests while the current test class/report shows five. The compatibility profile described degree-grouped polynomial ordering even though the Spark differential exposed a different ordering. Those claims have been corrected in this audit; perform the same consistency check across README, packaging notes, and release evidence.

## P0 — restore truthful behavior and prove the supported workflow

### P0.1 Phase dispatch and output contracts

- Define a typed input/config/output contract for each of FIND_TRAINING_DATA, labeling, TRAIN, MATCH, and LINK. Include artifact paths, profile, clock/seed, limits, and output schema; reject missing configuration before I/O.
- Keep explicit MATCH scoring and registry execution mutually exclusive. The former MATCH/LINK shared fallback and pipeline post-filter have been removed, with focused regression tests; retain this as a regression gate.
- Make unsupported phases fail before publishing output; assert no partial file or orphan table remains. Return a stable error with phase and missing capability.
- Publish completed phase outputs only with `ATOMIC_MOVE`; if the target filesystem cannot provide it, fail closed rather than silently replacing non-atomically. Regression coverage must preserve any previous output and clean the unpublished temporary file.
- Add command-level tests for each supported phase and one explicit failure for each unsupported combination. Compare schemas and contents, not only row counts or file existence.
- LINK command acceptance now runs registered native graph closure through the full pipe/output pipeline. A regression discovered that generic row-ID assignment overwrote the left endpoint `z_zid`; `ZinggJob.read` now preserves phase-provided endpoint IDs and uses union-by-name for LINK inputs, with a two-file fixture whose pair headers have different orders. The fixture asserts all three component members, cluster ID, implied-pair zero minima, output header, and output count; upstream Zingg LINK parity is still blocked on approved phase goldens.
- Exit: LINK cannot be mistaken for MATCH, MATCH is scored once, and each phase produces its documented schema.

### P0.2 Real persisted training and matching

- Specify whether v0.7 TRAIN means imported Spark artifacts, native learning, or both under distinct model types and profiles. Keep backend histogram separate from strict Zingg artifacts.
- Make FIND_TRAINING_DATA produce actual candidate/label input, stable IDs, source columns, ordering, and bounds. Verify duplicate and empty inputs.
- Feed nonempty positive/negative/ambiguous labels into training; persist label state with idempotency and conflict handling, then exercise restart recovery.
- Persist blocking/classifier artifacts with model version, feature order, normalization, coefficients, threshold, and checksums. Create a new runtime/process, load them, score candidate pairs, and prove predictions depend on model bytes.
- Pair identity must be structural rather than delimiter-concatenated. NUL-bearing IDs exposed collisions in the in-memory provider and workflow decision index; both now use typed `(leftId,rightId)` keys, and SQL label predicates encode NUL with `chr(0)`. Tests cover the collision pair through snapshot recovery and real binary classifier label derivation on Windows/WSL. This closes only the local pair-key correctness case; full cross-process and upstream phase goldens remain required.
- Add atomic publication, corrupted/incompatible model rejection, cancellation, rollback, and repeated-run determinism tests. Never use a plain predicate filter as evidence of model-based MATCH.
- Exit: a clean `findTrainingData → labels → train → process restart → match → link` run produces canonical outputs and verifiable artifacts; a modified/corrupt model causes the expected result/error.

### P0.3 Exact Zingg adapter and method matrix

- Keep the locally reconciled Windows/WSL canonical capsule hash stable in hosted clean-runner builds; capture both manifests, toolchain versions, source/dependency provenance, licenses and a reviewed approval. Do not substitute either unpinned Maven cache artifact or claim hosted reproducibility from local WSL alone.
- A profile-isolated `ZFrameDuckAdapter` now compiles against the canonical capsule; `engine-*` remains free of Scala/Zingg dependencies. The current pinned matrix routes 93/93 exact overloads, with no conditional, unsupported, or unmapped dispositions; the matrix explicitly labels all 93 as `MAPPED_NOT_DIFFERENTIAL` until their behavior is proven. Local compile/runtime tests passed on Windows and WSL. Continue adding overload-specific positive/negative behavior tests, preserve exact owner/resource rules, and obtain hosted bootstrap evidence before release qualification.
- Reflect **upstream `ZFrame`**, `Context`, `DSUtil`, `PipeUtilBase`, graph/model/pipe interfaces into versioned method inventories. `ZFrame` has a pinned 93-signature inventory, drift verifier, and machine-generated 93-row adapter matrix checked by CI. Expand inventory and reviewed mappings to the other contracts; require approved differential evidence and successful hosted CI before claiming the full gate.
- Inventory every upstream `IMatchType`, hash, and similarity function. For each one record expected normalization, null/empty/Unicode handling, accepted input types, output range, released quirks, local implementation, and reference fixture. Keep the profile registry and package manifest in sync.
- Implement each required operation or raise a tested capability error: select/expressions, joins, unions, except/intersect, deduplication, grouping, sorting, sample/partitioning, split/explode, row/schema access, collection, cache, and expression/literal coercion.
- Build a method-by-method differential table: input and output schema, values, nullability, order, multiplicity, coercion, error behavior, and owner/connection scope. Use small Spark/Zingg golden fixtures first, then property tests with fixed seeds.
- Exit: every upstream method has a traceable implementation or explicit unsupported result; supported operations match reference outputs and no forbidden runtime dependency leaks.

### P0.4 Approved reference data and model parity

- Complete EXT-01–EXT-04 intake: source commit/URL, file hashes including CRC sidecars, license/notice, producer Spark version, source records, model/label output, owner approval, and a sanitized golden dataset. The original fixture identifies Spark 3.0.1; keep the Spark 3.5.5 oracle explicitly labeled as a compatibility experiment.
- Reconcile the blocking-tree binary before claiming import: its stream names `zingg.block.*`, `zingg.client.*`, and `zingg.hash.*`, but the pinned source uses `zingg.common.*`; the isolated importer safely rejects it on Windows/WSL. Obtain maintainer confirmation of the serialized compatibility contract or a matching approved artifact, then cover field context and tree edges. Classifier coverage separately checks feature order, polynomial basis, margins, probabilities, threshold, predicted class, and unsupported-stage diagnostics.
- Replace hard-coded fixture path parsing in the direct importer with validated metadata/stage discovery inside the isolated import boundary, or explicitly document and gate it as single-fixture support.
- Cover corrupt/truncated/oversized/symlinked artifacts, unknown versions/classes, non-finite coefficients, dimension mismatch, and path escape. Preserve size/time/memory constraints in the child process.
- Add golden vectors for each supported MatchType and registered function, with nulls, empty input, case, punctuation, Unicode, malformed numbers, and collisions. Compare to the released Zingg/Spark behavior with explicit tolerances; version intentional corrections under a new compatibility profile.
- Exit: signed-off fixture manifest, Spark/Zingg golden outputs for all phases, zero unexplained differences under stated numeric tolerances, and a bounded importer with stable rejection messages.

## P1 — data plane, packaging, and release evidence

### P1.1 Arrow and relation lifecycle

- Confirm the default Arrow egress uses only JDBC + Arrow IPC with network/extensions disabled. Validate stream/file framing, schema metadata, batch boundaries, empty relations, nulls, binary, decimal precision/scale, dates, timestamps, and cancellation.
- Define list/struct/map support from the target contract. Implement each supported nested type end to end; explicitly reject unsupported types before publishing output.
- Benchmark JDBC row inserts against vectorized Arrow registration plus immediate CTAS and a temporary-file path. Choose the winner using throughput, RSS, spill, and cleanup evidence; prevent Arrow registration from outliving its connection.
- Run malformed/truncated IPC, row/byte limit, repeated-reader, mid-write kill, and no-partial-output tests under Windows and Linux.
- Exit: independent Arrow reader validates output, ingress is qualified by measurements, and lifecycle resources are absent after success/failure.

### P1.2 Resource, failure, and performance gates

- Run budget edges at exact limit and one unit over for memory, input/output bytes, collect rows/bytes, temp disk, and concurrent jobs. Observe actual DuckDB settings, RSS, spill bytes, and error category.
- Cancel or kill at schema creation, candidate generation, TRAIN/model write, Arrow ingest/egress, MATCH scoring, LINK graph, and output publication. Verify permit/connection/table/file cleanup and successful reuse after restart.
- Benchmark baseline and regression thresholds for read, training, matching, linking, Arrow, startup, RSS, spill, output size, and the 301-node graph case on pinned hardware/version profiles. Preserve raw samples and statistical method.
- Exit: reproducible stress/benchmark logs with explicit thresholds, clean process/resource inventories, and no unexplained failures.

### P1.3 Self-contained installs and platform matrix

- Choose and document the distribution unit: platform wheel, self-contained archive, or explicit external runtime. The current source wheel is Python control plane only; do not advertise it as a bundled worker/JRE.
- On *clean* Windows x86_64, Linux amd64/arm64, and macOS x86_64/arm64 runners, build a private Java 21 runtime and bundle the worker, launchers, model import tools, licenses, hashes, and provenance. Confirm architecture/OS of every binary.
- Install offline with system Java and `JAVA_HOME` unavailable. Exercise Python discovery, ping/status/run/shutdown, spaces/Unicode/read-only paths, wrong architecture, missing/corrupt JRE/JAR, upgrades, reinstall, and rollback.
- Distinguish local Windows and WSL smoke evidence from clean native Linux/macOS/ARM evidence. Preserve runner image, JDK vendor/patch, exact package hash, command transcript, and exit codes.
- Exit: every supported platform has an independently reproducible clean-install transcript and no-system-Java smoke.

### P1.4 Native Image decision

- Obtain a pinned GraalVM Java 21 Native Image toolchain in a disposable runner. Generate tracing/reachability metadata for DuckDB JNI, Arrow, JSON, reflection, services, and model loading; build with no fallback.
- Run the same protocol, model, Arrow, cancellation, cleanup, and negative tests as the JVM worker. Compare startup, RSS, binary size, cold/warm latency, throughput, and diagnostics.
- Decide go/no-go using explicit support criteria; if no-go, record the supported JVM distribution contract and remove Native Image from mandatory release claims only through a reviewed scope decision.
- Exit: successful native binary plus comparison report, or an approved evidence-based scope decision.

### P1.5 SBOM, security, signing, and rollback

- Generate the same transitive inventory on Windows and POSIX; compare Maven resolved graph, shaded JAR contents, packaged JRE modules, importers, and Python package. Add SPDX identifiers, checksums, relationships, source URLs, license evidence, and notices; resolve `NOASSERTION` or record reviewed exceptions.
- Run a current CVE scan with a working database/API key (the prior OWASP attempt produced no usable report). Triage findings by affected packaged component, reachable use, remediation, and approved waiver. Make the release scan mandatory, archive JSON and human review.
- Pin JDK vendor/build, record quarterly security-update ownership and rebuild policy. Verify source archive reproduction on a second clean runner and compare hashes.
- Sign artifacts/SBOM/provenance/checksums with timestamped release identity; verify on every target platform. Build and restore a rollback package, then re-run protocol/model smoke on the restored version.
- Exit: complete reviewed SBOM, CVE/license sign-off, reproducibility report, verified signatures, and exercised rollback.

### P1.6 Hosted CI and documentation closure

- Run the configured OS/architecture and Python matrix on hosted runners. Compile-only lanes are evidence of compilation only; add relevant tests and clean-install gates to their release qualification jobs. Do not count YAML parsing as a hosted run.
- Make exact-contract, approved fixture differential, model import, lifecycle, resource, Arrow, package, security, and benchmark gates blocking for the claimed release scope. Keep the DuckDB 2.0 canary informational until a published compatible artifact exists.
- Update README, `docs/compatibility-profiles.md`, package notes, `build-plan.md`, `test-plan.md`, and status page to describe the same supported phases, model types, Spark version, Arrow types, and package layout. Add a requirements→test→CI→evidence index with owner/date/hash.
- Exit: green hosted checks on all required platforms, reviewed compatibility decision, and a release evidence index with no missing mandatory item.

## External decisions and inputs

| Input | Current evidence | Required action/owner | Unblocks |
|---|---|---|---|
| Zingg contract authority | A pinned capsule and exact-symbol compile test exist locally; maintainer approval and complete functional mapping do not | Compatibility maintainer: approve commit, binary/hash/license and required phase/API scope | Adapter and strict parity claim |
| Blocking/classifier source fixtures | Candidate upstream model and local Spark 3.5.5 oracle exist; original model metadata says Spark 3.0.1 | Compatibility maintainer: approve provenance and provide representative source records/expected outputs | Full import/score differential |
| Phase goldens | No complete approved reference outputs for labels, TRAIN, MATCH, LINK | Zingg owner: provide sanitised inputs/outputs and tolerance/order rules | Phase acceptance suite |
| Integration boundary | Standalone is provisional in `docs/compatibility-decision.md` | Zingg maintainer: confirm standalone vs `home/duckdb`, supported phase set, and release ownership | Adapter packaging/PR target |
| Legacy Spark policy | Importer uses isolated external Spark prerequisite; distribution contract remains undecided | Maintainer/release owner: choose supported Spark version, install mode, and license obligations | Importer release package |
| Hosted environment | Workflow is configured; local tests do not prove hosted macOS/ARM execution | Release owner: enable runners, credentials, signing identity, and NVD/security data | Platform and release gates |

## Recommended execution order

1. Keep P0.1 dispatch, atomic publication, MATCH/LINK routing, and multi-file LINK regressions green; remaining phase output parity belongs to P0.4 because it requires approved upstream goldens.
2. Extend P0.2's native worker/workflow path with current-process recovery, cancellation/kill-at-each-phase and full failure cleanup; then align outputs with approved Zingg v0.7 semantics.
3. Continue P0.3 with overload-specific differential evidence and exact-contract negative cases using the canonical capsule; routing the 93 signatures is complete, while exhaustive semantics and hosted bootstrap evidence are not.
4. Generate P0.4 phase goldens and run differential suites; keep Spark 3.0.1 metadata distinct from the Spark 3.5.5 experiment.
5. Qualify P1.1 Arrow and P1.2 stress/performance; then P1.3 package and P1.4 Native Image.
6. Complete P1.5 security/signing/rollback and P1.6 hosted matrix; close external decisions and publish the evidence index.

The next local engineering actions are the remaining P0.2 recovery/failure cases and P0.3 semantic gaps; external fixture approval and hosted qualification must proceed in parallel and cannot be inferred from local green tests.

## Current-state delta and exhaustive remaining task checklist — 2026-09-29

This section supersedes the execution order above where newer evidence has closed a local subtask. It does not supersede external approval requirements. “Done locally” means the described implementation/test evidence exists in this checkout; it does not imply upstream approval, hosted CI, legal approval, or release readiness.

### A. Immediate P0 — make the compatibility claim truthful and close the workflow

1. **Freeze the target contract (external dependency EXT-01/EXT-05).** Obtain the maintainer's written decision on standalone repository versus `home/duckdb`, supported Zingg phases and release ownership, exact v0.7 source/API authority, and whether the adapter targets only v0.7.0. Record commit/tag, binary hashes, licenses/notices and decision date in the compatibility profile. Do not claim strict compatibility before this is approved.
2. **Complete authoritative API inventory.** Extend the existing 93-signature `ZFrame` inventory to the exact generic and phase contracts used by `Context`, `DSUtil`, `GraphUtil`, `ModelUtil`, `PipeUtilBase`, readers/writers, model interfaces and phase executors. For every method, record mapping, unsupported behavior, overload-specific semantics, test reference and source hash. Make drift fail CI.
3. **Complete phase orchestration.** Audit dispatch and routing for FIND_TRAINING_DATA, label retrieval/application, TRAIN, MATCH and LINK. Remove or explicitly reject every identity/backend-histogram/non-scoring fallback; ensure every command has typed input validation, phase-specific output schema, deterministic diagnostics and no accidental phase aliasing.
4. **Prove one persisted end-to-end lifecycle.** Run FIND → pending labels → apply labels → TRAIN → worker shutdown/restart → MATCH using the persisted trained model as the scorer. Verify model provenance, exact schemas/rows, scores, pairs, clusters, source IDs and publication paths against approved goldens; repeat with fresh processes and deterministic input. Existing local classifier training/reload and durable label replay probes are components, not closure of the canonical workflow.
5. **Finish failure recovery and atomicity.** Exercise worker kill/cancel/timeout at each phase and each model/output publication boundary; prove replay/idempotency after lost acknowledgements, no double labels, no stale locks, and complete orphan/temp cleanup. Extend beyond tested snapshot boundaries to sustained contention and documented durability guarantees; avoid claiming power-loss/fsync semantics unless specifically tested.
6. **Close phase output contracts.** For FIND, labels, TRAIN, MATCH and LINK, compare full row multisets, schema/order/nullability, duplicate behavior, identifiers, score precision and error behavior. Test one and multiple input files, empty/single-row inputs, duplicate direct/transitive links, asymmetric link output, output overwrite/append policy, partial writes and atomic final publication.

**Exit gate:** approved target/API boundary plus green canonical restart workflow and complete phase-output differentials, with no scoring or orchestration fallback hidden behind success-shaped output.

### B. P0 — real upstream model and semantic compatibility evidence (external EXT-02/03/04)

7. **Resolve blocking-tree fixture provenance before changing the importer.** Obtain an authoritative compatible v0.7 artifact, or maintainer-approved historical artifact plus field-by-field migration contract. Capture source records and expected field context, hash identity, tree nodes/edges and predictions. The current candidate is byte-identical to v0.3.4 and has incompatible serialized class names/UID; do not weaken the deserialization filter or alias descriptors speculatively.
8. **Approve classifier fixture authority and scope.** Obtain maintainer sign-off on fixture producer/version/license and expected feature order, polynomial expansion, coefficients/intercept, threshold, margins and probabilities. Both local importer routes now enforce the exact ordered three-stage pipeline and feature-column wiring; the isolated Spark 3.5.5 route also checks coefficient dimension and emits escaped JSON. Negative tests prove extra/reordered stages and disconnected columns reject without modifying an existing output. This reduces silent mis-scoring risk but does not approve fixture provenance or prove general Spark model support; preserve the explicit envelope and unsupported-stage diagnostics.
9. **Build canonical Spark differential goldens.** For each phase and supported model, generate sanitized inputs/outputs from the approved Spark/Zingg version. Include nulls, Unicode, duplicates, empty data, skew, multiple pipes, IDs, pair shapes, label transitions, scores, connected components and order/tolerance policy. Record Spark 3.0.1 artifact metadata separately from the experimental Spark 3.5.5 oracle.
10. **Expand negative and edge contract coverage.** Complete per-overload ZFrame semantics (including exact `show` text), coercion, null propagation, duplicate column names, positional/by-name union, filter multiplicity, split/explode, sampling, hash overflow/UTF-16, locale/timezone, ordering, and unsupported API behavior. The 93/93 fixture routing report is selected coverage, not exhaustive equivalence.
11. **Validate model import security and determinism.** For every approved artifact, test size/depth/reference limits, class allowlist rejection, malformed/truncated/hostile streams, symlink/path escape, timeout/non-zero process exits, deterministic neutral-model output, checksum/provenance validation and no publish-on-failure on Windows and Linux.

**Exit gate:** signed-off fixture provenance and phase goldens; importer and scorer match those references; all supported methods pass semantic differential tests and unsupported methods fail explicitly.

### C. P1 — Arrow, data-plane completeness and resource correctness

12. **Finish Arrow type/shape matrix.** Add nested structs/lists/maps, decimals, dates/timestamps with timezone and unit boundaries, binary/strings, unsigned extrema, null-only/empty batches, zero-column rows, dictionary encoding and malformed IPC. Define/document any DuckDB type widening (notably FP16 → FLOAT) and assert schema plus complete value fidelity.
13. **Exercise ingress and egress failure lifecycle.** Inject consumer/producer errors, malformed streams, cancellation, worker kill, budget exhaustion and filesystem failures at each transfer stage; prove handles, temp relations/files, subprocesses and partial outputs are cleaned. Validate offline behavior with extension/network access unavailable.
14. **Qualify ingestion implementation choice.** Compare Appender, prepared JDBC batches and vectorized Arrow C Data/CTAS on the same datasets, schema and machine. Measure rows/s, latency, CPU, RSS, spill and cleanup; retain the alternative path unless improvement is repeatable and semantic parity is exact.
15. **Close relation ownership/lifecycle gaps.** Extend current local connection/TEMP/run-schema probes to repeated jobs, concurrent scheduling, failure/cancel/kill and all cached/materialized/Arrow handle scopes. Prove no cross-owner access before SQL and bounded resource counts after repeated cycles.
16. **Establish resource budgets empirically.** Define workload tiers and limits for RSS, DuckDB memory, spill bytes, temp disk, rows/collect bytes, files, threads, concurrent jobs and wall time. Include graceful limit errors, cleanup and recovery for each budget; thresholds must be measured on clean reference runners, not copied from one developer machine.

**Exit gate:** full declared Arrow/type matrix, offline round-trip, failure cleanup and resource budgets pass on required reference environments; selected ingestion path has repeatable evidence.

### D. P1 — benchmarks, performance and capacity

17. **Define benchmark protocol and acceptance thresholds.** Pin hardware/VM, OS, CPU quota, memory, storage, JDK/Graal version, DuckDB threads, dataset generator/seed, warmup, repetitions, confidence/variance and baseline commit. Separate cold start, warm query, end-to-end phase, data transfer and packaging measurements.
18. **Expand workload suite.** Cover small/medium/large input, row width, null density, duplicate rate, candidate-pair explosion, skewed/large connected components, model size, Arrow batch sizes, spill/no-spill, concurrent jobs and cancellation. Report throughput, p50/p95/p99 latency, CPU, RSS, spill/temp and output size.
19. **Repeat Arrow and Native Image comparisons.** Native Image now has a one-warmup/five-measured-pair WSL2 comparator with alternating launch order, exact output parity, raw samples and variance summaries (`benchmarks/native-image-repeated-2026-09-29-wsl.json`). Still run representative workloads on clean hosted target runners and complete Arrow Appender/JDBC/C Data comparisons with RSS/spill; the narrow native smoke is not a stable cross-machine performance claim.
20. **Set regression policy.** Establish statistically defensible per-workload thresholds, allowed variance, environment normalization, artifact retention and CI trend alarms. Do not gate on noisy microbenchmarks without a stable runner.

**Exit gate:** reviewed reproducible reports meet explicit performance/capacity targets, or deviations have approved scope/mitigation.

### E. P1 — Windows/Linux portability and self-contained distribution

21. **Run genuine hosted clean-install matrix.** Windows x64 and Linux x64 are locally exercised; obtain clean hosted evidence for Linux x64, Linux ARM64, macOS x64/ARM64 and Windows x64 where claimed. Test fresh checkout, restricted PATH/no JAVA_HOME, offline installation, spaces/non-ASCII paths, read-only directories, and supported Python versions.
22. **Qualify shell/PowerShell parity.** Run equivalent package, source archive, bundle verify, rollback, worker start/ping/shutdown, process-kill and no-system-Java workflows on native Windows PowerShell and Linux shells. Validate exit codes, quoting, signals, path conversion, permissions/executable bits and symlink behavior. WSL is valuable evidence but is not a substitute for hosted Linux/macOS.
23. **Verify Python wheel/sdist installs.** Build and install artifacts in isolated environments without repository imports; test worker discovery, private JRE discovery, subprocess lifecycle, protocol version mismatch, cancellation and all supported Python versions/OS targets.
24. **Decide the legacy importer install contract (EXT-05).** Maintainer/release owner must approve Spark version, external versus bundled Spark, supported platforms, license/notice obligations, security isolation and upgrade policy. Then build and test the chosen artifact; until then the importer is not a complete user-facing distribution.
25. **Validate all release outputs from clean artifacts.** Verify checksums and inventories for JVM bundle, optional jlink runtime, source archive, importer package, rollback and any native image; test offline clean install and rollback without access to the working tree or Maven cache.

**Exit gate:** each claimed target has actual hosted build/test/install artifacts and clean-room no-system-Java evidence; platform scope and importer prerequisites are explicit.

### F. P1 — Native Image: prototype is not release qualification

26. **Pin and automate GraalVM build provenance.** Make version/vendor/checksum acquisition reproducible, record toolchain and target metadata, and build on clean hosted runners. Verify exact target JNI resource selection and no foreign platform libraries in each artifact.
27. **Complete reachability metadata.** Exercise all supported worker paths under tracing (protocol, SQL, all input formats, Arrow, train/import/load/match/link, errors, cancellation, logging). Review agent output and fail on unexplained missing reflection/JNI/resource entries; add regression tests for metadata drift.
28. **Test Native Image functional parity.** The comparator now verifies ping, basic SQL counts, native linear classifier train/reload/MATCH, and exact complete match rows in each of five measured WSL pairs. Extend coverage to the full protocol, formats/types, imported model classes, phases, limits, failures, shutdown, cancellation and diagnostics; then compare complete JVM/native results on hosted supported targets.
29. **Build per-target artifacts.** Qualify Linux x64/ARM64, macOS x64/ARM64 and Windows x64 (or explicitly narrow supported scope), including native DuckDB JNI loading, clean launch, packaging, code signing/notarization where required and platform-specific diagnostics.
30. **Resolve security/support decision.** Triage Graal's Java-deserialization warning, scan native binaries and embedded dependencies, produce target-specific SBOM/provenance, review licenses, vendor support/update policy and signing. Decide and document go/no-go; keep JVM distribution default until approved.

**Exit gate:** repeatable hosted builds, broad parity and security review, supportable update/signing plan, benchmark acceptance, and explicit release approval. Otherwise retain as experimental and excluded from normal bundles.

### G. P1 — supply chain, licenses, security and release governance

31. **Reconcile the complete shipped-component graph.** Identify exact contents across shaded worker, 20 staged runtime dependencies, bundled JRE, both legacy importers, Python package, Native Image, source archive and platform packages. Resolve the metadata-only JCTools coordinate and Shade overlap warnings by inspecting actual contents and provenance.
32. **Resolve all 195 `NOASSERTION` package licenses.** For every shipped component, record authoritative license expression/source, applicable notices, modifications/relocations and reviewer/approval. Preserve the current verbatim legal-resource inventory; it is evidence collection, not legal approval.
33. **Run independent security review.** Execute current dependency/native/container/source scans with usable vulnerability database access; triage reachability and exploitability, fix or obtain signed waivers, and archive machine-readable reports and human review. Include deserialization, subprocess boundaries, path/symlink handling, SQL/debug surfaces, secrets and offline guarantees.
34. **Approve JDK/Graal vendor lifecycle.** Select production vendor/build, update cadence, CVE response owner and rebuild deadline; document supported upgrades and repeatable verification.
35. **Sign and attest releases.** Generate signed checksums, SBOM and provenance/attestations with release identity and verify them independently on each target. Confirm timestamp/key rotation and artifact retention policy.
36. **Exercise rollback and recovery on release artifacts.** Install, upgrade, interrupt, roll back and re-run protocol/model smoke against packaged versions; verify signatures/checksums and preserve user data. Existing local rollback assembly/verification does not replace a clean-install upgrade/restore scenario.

**Exit gate:** complete reviewed SBOM/license/security artifacts, signed provenance, approved JDK lifecycle, clean release install and tested rollback.

### H. P1 — hosted CI and final documentation

37. **Make CI executable and authoritative.** Run hosted required OS/architecture/Python jobs; ensure semantic, model, Arrow, lifecycle, package, no-system-Java, security and benchmark suites run (not compile-only). Publish test logs, checksums, runner metadata and skipped-test reasons.
38. **Add requirements-to-evidence traceability.** Map each supported requirement to implementation symbol, test ID, CI job, artifact/hash, status, reviewer and date. Fail release qualification when a required row lacks evidence or relies only on a local/unapproved fixture.
39. **Reconcile every public claim.** Align README, compatibility profiles, package docs, operations guide, CLI help, build/test plans and release notes on phase/model/Arrow/platform scope, Spark prerequisites and experimental Native Image status. Clearly label unsupported behavior and decision owners.
40. **Final independent gate review.** Review all P0/P1 acceptance evidence, external approvals, skipped tests, known risks, rollback, security/legal and benchmarks; produce an explicit release go/no-go and residual-risk record.

### Dependencies, current blockers and execution order

| Blocker | Type | Current state | Next concrete action | Can local code proceed? |
|---|---|---|---|---|
| Exact integration/release boundary and API authority | Maintainer decision | Provisional; standalone vs `home/duckdb` unresolved | Obtain written decision and approved commit/API contract (EXT-01/05) | Yes, but strict compatibility claim remains blocked |
| Authoritative v0.7 blocking artifact | Maintainer fixture/contract | Candidate rejected by strict importer; historical v0.3.4 bytes/class IDs | Request compatible artifact or approved migration contract and goldens (EXT-02) | Generic importer hardening only; no speculative descriptor fixes |
| Approved classifier + phase output goldens | Maintainer data | Local candidate/oracle coverage is selected and unapproved | Obtain source records and expected outputs for all phases/models (EXT-03/04) | Yes, infrastructure and negative tests continue |
| Legacy Spark distribution policy | Release/compatibility decision | External Spark prerequisite exists; distribution contract open | Decide Spark version, bundle/install and license policy (EXT-05) | Yes, package mechanics can proceed |
| Hosted macOS/ARM/release environment | CI/release owner | Local Windows + WSL evidence; hosted jobs/credentials not demonstrated | Enable runners, secrets/signing and security data (EXT-06) | Local work yes; platform/release claim no |
| License/CVE/security approvals | Independent reviewers | 195 `NOASSERTION`; scans/overlaps need review | Assign reviewer, resolve inventory and run current scans | Packaging tooling yes; release approval no |
| Native Image release decision | Engineering/release | Linux x64 prototype; broad qualification incomplete | Run tasks 26–30 and approve supported scope or explicitly defer | JVM track proceeds independently |

Recommended sequence: (1) request/resolve EXT-01–06 in parallel; (2) finish phase dispatch and recovery; (3) obtain approved fixtures and close model/semantic differentials; (4) finish Arrow and resource matrix; (5) define/run repeatable benchmarks; (6) qualify clean installs and hosted platform matrix; (7) complete Native Image go/no-go; (8) close SBOM/security/signing/rollback; (9) traceability and independent release review. External requests should be sent early because they gate the critical path and cannot be solved by local implementation alone.

### Progress accounting note

The headline **37/53 (69.8%)** is the repository's existing global build-plan checkbox accounting, not a weighted engineering estimate and not “69.8% release ready.” Several unchecked parent gates include substantial locally completed subwork; they remain open until the complete stated exit evidence is met. Conversely, the new 40-item remediation checklist decomposes blockers and is not a replacement denominator. Keep both measures distinct, and update the 37/53 count only when an existing checkbox's entire acceptance statement is satisfied with recorded evidence.

## Exact mapping of the 15 open build-plan checkboxes

This table maps every unchecked checkbox in `build-plan.md` to the detailed work above. Rows 2/11 and 4/10 overlap in implementation but remain distinct checklist claims; do not inflate real progress by counting the same local smoke twice.

| # | Open gate | Detailed section | Closure evidence |
|---|---|---|---|
| 1 | Full persisted find → labels → train → restart → match workflow | P0.1–P0.2 | Canonical phase outputs, model-owned scoring, deterministic rerun, fresh-process and failure tests; the small native smoke is insufficient |
| 2 | JVM worker clean installs without system Java | P1.3 | Independent clean Linux/macOS/Windows offline installs, manifests, hashes, launcher/Python smoke |
| 3 | GraalVM Native Image worker and comparison | P1.4 | No-fallback native build and JVM/native parity, RSS/startup/size report or approved scope decision |
| 4 | Signed provenance, archive, SBOM/license, rollback, JDK tracking | P1.5 | Verified signatures/attestations, reproducible archive, reviewed license/CVE report, exercised rollback |
| 5 | MatchType/function inventory and complete ZFrame operation matrix | P0.3 | Versioned inventory, null/coercion/ordering/duplicate semantics, differential tests |
| 6 | Exact-contract `adapter-zingg07` | P0.3 | Build against pinned upstream capsule on clean runner, functional adapter tests, no forbidden runtime deps |
| 7 | Upstream `ZFrame` method-coverage report | P0.3 | Reflection of upstream interface, every method mapped or explicitly rejected, executable CI gate |
| 8 | Complete phase executors and FIND/labels/TRAIN/MATCH/LINK | P0.1–P0.2 | Typed phase contracts, canonical outputs, positive/negative command tests, no identity fallback |
| 9 | Offline-safe Arrow egress and qualified ingress | P1.1 | Independent IPC round trip offline, type/null/empty/cancel/kill matrix, ingress throughput/RSS decision |
| 10 | Complete transitive SBOM and security/license review | P1.5 | Shaded/dependency/JRE/Python reconciliation, SPDX/license evidence, current CVE scan and sign-off |
| 11 | Clean-install/no-system-Java validation | P1.3 | Same platform matrix as row 2, with explicit offline/no-PATH/no-JAVA_HOME negative checks |
| 12 | Approved real Zingg artifacts and Spark differential fixtures | P0.4 | EXT-01–EXT-04 provenance, hashes, source records, Spark 3.0.1/3.5.5 distinction, owner sign-off |
| 13 | Standalone versus `home/duckdb` integration boundary | External decisions | Written maintainer decision, supported phase scope, package/PR target and ownership |
| 14 | Hosted Unix/full CI matrix | P1.6 | Actual green Linux/macOS/ARM/Windows/Python jobs with artifacts; YAML parsing alone is insufficient |
| 15 | Legacy Spark importer distribution policy | External decisions/P0.4 | Approved Spark version/install/license policy and isolated importer package/negative tests |
## Status refresh — 2026-09-29

- Local CI workflow audit fixed the stray `PY` shell command in the pinned ZFrame inventory step and prevents provenance generation from changing the already checksummed package directory.
- Added regression assertions and updated the validation/test plans. Local evidence: workflow YAML parses; focused workflow checks 2/2; packaging suite 64 tests passed on Windows and WSL2 with platform-dependent skips.
- No hosted CI run evidence is available in this workspace. Keep the hosted runner matrix, external compatibility approvals/fixtures, and release qualification tasks open; overall checklist remains 37/53 (69.8%).
- Further audit found the Ubuntu validation job still routed package assembly, verification, and provenance through PowerShell despite native POSIX scripts being available. The job now uses the POSIX entrypoints and a static regression protects that routing. Current local evidence: 3/3 workflow assertions and 65/65 packaging tests on Windows and WSL2 (platform skips documented in `docs/validation-status.md`). This does not replace hosted CI evidence.
- Tightened the previous finding: `package.sh` is a dispatcher that prefers PowerShell whenever available, including this WSL setup. CI now invokes `package-native.sh` directly for Unix. Confirmed the actual native Linux package flow on WSL2 with Java 21 `JAVA_HOME`: full nine-module compile, bundled JRE/legal inventory, 205-component SPDX, source ZIP and rollback verification, package verification, and no-system-Java launcher smoke all passed. Regression suite is 4/4 and packaging suite 66/66 (Windows 10 skips, WSL2 1 skip). Native macOS/ARM and hosted clean-runner execution remain pending; overall completion stays 37/53 (69.8%).
- Security refresh: the OWASP Dependency-Check job remains opt-in and NVD-key dependent, so added a separate mandatory OSV workflow on push, PR, and weekly schedule, pinned to immutable upstream commit `a345acffa64b0eaede81a3d9aae6141214d9c8fc`. It builds/verifies the native Linux package, uploads the exact SPDX SBOM, and scans that artifact; SPDX verification now requires exactly one valid PURL for every dependency package. The scan fails on findings/scan failures and retains SARIF. Both workflow files parse; workflow regression tests pass 5/5 and the full packaging suite passes 68/68 on Windows and WSL2 with platform skips recorded. No hosted OSV result has yet been produced, and license review/triage remain release gates.
