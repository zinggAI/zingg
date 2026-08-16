# Spark Connect Compatibility Report

## Scope

This change prepares Zingg for Spark Connect on Spark 3.5 and Spark 4.x. The Spark 4 line is split into explicit Maven profiles so each Spark minor release uses its own binary-compatible Scala and protobuf versions.

## Maven profiles

| Profile | Spark | Scala | Java target | Protobuf |
|---|---:|---:|---:|---:|
| `spark-3.5` | 3.5.5 | 2.12.10 | 11 | 3.23.4 |
| `spark-4.0` | 4.0.3 | 2.13.16 | 17 | 4.29.3 |
| `spark-4.1` | 4.1.3 | 2.13.17 | 17 | 4.33.0 |
| `spark-4.2` | 4.2.0 | 2.13.18 | 17 | 4.33.5 |

GraphFrames uses `graphframes-spark3_2.12` for Spark 3.5 and `graphframes-spark4_2.13` for Spark 4.x.

## Source changes

- Spark Connect proto and server-plugin modules are included in the root reactor.
- The relation and command plugins support both the Spark 3.5 and Spark 4 plugin method signatures.
- Scala 2.13 collection conversions were updated in the Spark client and core code.
- The Python wrapper now uses the correct `ZinggOptions` package and option names.
- Python Spark session creation disables adaptive-plan rendering that can exhaust the driver heap on large Zingg blocking plans.
- CSV pipes expose both `location` and the legacy `path` property for compatibility.
- Python CSV pipes convert boolean properties to Java strings, and the Febrl test uses a separate labelled schema with CSV headers.

## Verification completed

- Spark Connect/Spark compilation passed for profiles `spark-3.5`, `spark-4.0`, `spark-4.1`, and `spark-4.2`.
- Full Scala Spark tests passed with zero failures:
  - Spark 3.5: 342 tests.
  - Spark 4.0: 341 tests.
  - Spark 4.1: 342 tests.
  - Spark 4.2: 342 tests.
- Full PySpark Febrl coverage passed on Spark 3.5.5, Spark 4.1.3, and Spark 4.2.0 with the matching Java runtime and GraphFrames artifact.
- The complete `test_init_and_execute` workflow passed on Spark 4.1.3 and Spark 4.2.0 with Java 17, including SecondString and GraphFrames on the runtime classpath.
- `testArgs.py` plus `testFebrl.py` passed as grouped runs:
  - Spark 3.5.5: 64 passed, 1 skipped.
  - Spark 4.1.3: 64 passed, 1 skipped.
  - Spark 4.2.0: 64 passed, 1 skipped.
- The Spark 4.2 environment-sensitive group passed (`12 passed`) after restoring `DATABRICKS_CONNECT` after its Databricks-specific test.

## Remaining verification

Spark 4.0 has full Scala verification and profile compilation; PySpark runtime coverage was performed on Spark 4.1 and 4.2, which exercise the shared Spark 4 / Scala 2.13 code path.
