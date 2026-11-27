# Profiling and benchmark evidence

Profiling evidence is collected without changing the model or output contract. A capture consists of:

- `profile`: the active compatibility profile, currently `zingg-0.7.0-duckdb-1.5.6.0`; the released `1.5.5.1` profile remains available for immutable replay.
- `build`: package version, worker artifact SHA-256, Java version, and DuckDB JDBC version.
- `request`: operation name, phase, input manifest hashes, row limits, and relevant configuration.
- `runtime`: the response from the worker `status` operation, including effective DuckDB limits and JVM heap counters.
- `plan`: the read-only output of `explain`/`Frame.explain()` for each materialized query boundary.
- `measurements`: wall-clock duration, output byte count, and independently collected process RSS.

Capture files must be JSON, immutable after collection, and named with the compatibility profile plus an input/configuration digest. Comparisons are valid only when the profile, artifact hash, input manifest, and configuration digest match. RSS must be collected outside the JVM diagnostic counters; heap usage alone is not a substitute for process memory.

The implementation currently provides the `status` and `explain` primitives. Benchmark history, threshold policy, and regression gates remain a later validation phase and must not be inferred from compilation success.
