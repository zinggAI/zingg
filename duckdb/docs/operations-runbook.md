# Operations runbook

## Start

Launch `runtime-worker` with a JDBC URL, configured input/output roots, a bounded job count, and explicit resource limits, including `--max-rows`, `--max-temp-bytes`, `--max-spill-bytes`, `--max-collect-bytes`, and `--max-model-bytes`. Keep `--max-jobs 1` until concurrent-job behavior is deliberately enabled. Bundle `runtime/java` for installations that do not provide a system Java 21.

## Data safety

Configure `--input-root` and `--output-root` for every service deployment. Do not enable extension auto-installation. Keep legacy importers out of the worker process and provide an allowlist and finite import limits.

## Diagnostics

The worker emits protocol responses on stdout and importer diagnostics on inherited stderr. Correlate responses by request id. A failed request does not imply that the worker is safe to reuse if the underlying job was cancelled; restart the worker after an unrecoverable connection or native-library error.

Use the read-only `status` operation during startup and incident triage to record the effective DuckDB limits and JVM heap counters. Keep an independent process/RSS measurement in production evidence because heap counters do not include all native allocations.

## Shutdown and cleanup

Send `shutdown`, wait for the response, then close the process pipes. Job-local TEMP objects are released when the job closes. Remove abandoned output artifacts only after checking the associated package hash and provenance.

When a worker is forcibly terminated while using a persistent DuckDB file, normal job `finally`/`close()` cleanup cannot run. On the next `DuckRuntime` open, the engine drops stale schemas in its reserved `_zingg_run_*` namespace, which removes partially imported Arrow tables and other job-local objects. Active run schemas in the same JVM are tracked and are not removed when another runtime opens. Treat `_zingg_run_*` as an internal reserved namespace; do not store user-owned schemas there. Process-wide cancellation still does not roll back already committed output files or other durable side effects, and cleanup occurs on database reopen rather than at the instant of process termination.

## Native Image prerequisite

Native Image remains an experimental, separate deliverable. The historical Linux x64 executable built with Oracle GraalVM 21.0.12+7.1 is retained only as non-supporting diagnostic evidence; current build wrappers fail closed on JDK 21 and require JDK 22+ because of DuckDB's Native Image compatibility requirement. A future release candidate must provision and pin a JDK 22+ GraalVM toolchain, reproduce a relocated artifact, review the Native Image deserialization warning and complete security/license checks, then run broader workload/performance qualification before enabling or distributing it. The normal bundled-Java 21 JVM distribution remains the supported path.

## Implemented diagnostic entry points

Run the JDBC lifecycle probe from the worker dependency classpath or shaded runtime:

```text
java -cp runtime-worker-0.1.0-SNAPSHOT.jar io.zingg.duckdb.engine.JdbcLifecycleProbe jdbc:duckdb:
```

The command emits JSON covering connection ownership, TEMP visibility, run-schema cleanup, macro/UDF scope, Arrow API presence, cancellation, and timeout configuration. Store the JSON with the build and compatibility profile.

Use these worker controls explicitly in deployments:

```text
--memory-bytes <bytes> --max-temp-bytes <bytes> --max-spill-bytes <bytes> --max-collect-bytes <bytes>
--max-output-bytes <bytes> --max-rows <rows> --max-model-bytes <bytes> --max-jobs <count>
--offline
```

Network-backed extensions require `--online` and one or more `--allow-extension <name>` options. The default is offline with an empty allowlist.

Package launchers are located at `bin/zingg-duckdb.cmd`, `bin/zingg-duckdb.ps1`, and `bin/zingg-duckdb.sh`; all require the bundled Java 21 runtime in `runtime/java`.
