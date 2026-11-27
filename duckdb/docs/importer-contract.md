# Importer contract

Legacy importer modules are invoked through `LegacyImporterSpec`. The spec pins an importer name/version, launcher path, allowed class names, and resource limits. `BlockingImporterLauncher` validates the profile before delegating to `IsolatedImporterProcess`. The launcher emits a neutral model-format directory and never exposes Spark classes to `runtime-worker`.

The blocking bundle provides `io.zingg.duckdb.legacy.SparkBlockingTreeImporterMain` for Spark 3.5.5. It reads the Parquet artifact, extracts the serialized binary tree column, and delegates to `LegacyBlockingTreeImporterMain`, which applies Java serialization filters and emits neutral `Tree`/`Canopy` node metadata. Its Spark dependency is `provided` and is not included in the worker artifact.

The classifier bundle provides `io.zingg.duckdb.legacy.SparkClassifierImporterMain`. It loads a Spark `CrossValidatorModel`, requires the released `VectorAssembler` → degree-3 `PolynomialExpansion` → `LogisticRegressionModel` stage shape, validates coefficient and threshold values, and emits the neutral classifier envelope.

For either entry point, invoke `packaging/invoke-legacy-import.ps1` with `-SparkSubmit`, `-MainClass`, `-InputDirectory`, and `-OutputDirectory`; pass the matching JAR through `-Classpath` and keep Spark 3.5.5 on the external Spark distribution. The wrapper supplies `ZINGG_IMPORT_*` limits, disables network access by policy, rejects symbolic-link inputs, and requires the output directory to remain outside the input tree.

Before process creation, the launcher path must resolve to a regular local file. Input and output directories are canonicalized and may not be equal or nested, isolating output from legacy input bytes. Allowlist entries are validated as fully qualified Java class names, and the child receives limits plus `ZINGG_IMPORT_NETWORK=disabled` through its environment.
