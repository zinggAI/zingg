# Runtime architecture

`DuckRuntime` owns the database instance. Each `ZinggJob` owns one duplicated JDBC connection. Every `Frame` carries that owner identity. TEMP cache relations, Arrow registrations, SQL statements, and cancellation handles remain on that job connection. The Python process is a control plane; bulk data is represented by files/Arrow and never crosses the boundary object-by-object.

Every frame exposes a read-only `explain()` operation that delegates to DuckDB `EXPLAIN` without executing the data-producing query. Callers can persist the returned plan alongside runtime diagnostics as the input to later profiling and regression-threshold tooling.

`CompatibilityRuntime` owns the immutable compatibility profile, phase registry, hash registry, and similarity registry. Function names are allowlisted and registry instances are scoped to the runtime, preventing accidental cross-profile or process-global semantic changes.

`CompatibilityRuntime` selects an immutable compatibility profile at construction time; jobs cannot silently change Zingg/DuckDB semantic rules during execution.

Phase orchestration is represented by `PhaseRegistry` and `PhaseExecutor`; an unregistered phase fails explicitly, and native implementations can be registered without changing the worker protocol or frame ownership model.
Arrow IPC files (`.arrow`/`.feather`) are ingested through the pinned Arrow 19.0.0 Java reader into job-local DuckDB TEMP tables. Scalar boolean, integer, floating-point, date, timestamp, and string fields are supported; complex Arrow fields fail explicitly until a compatible mapping is defined.
