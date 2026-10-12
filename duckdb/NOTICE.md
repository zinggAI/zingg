# Notices

This project is a standalone compatibility implementation and records upstream runtime inputs explicitly.

- DuckDB JDBC 1.5.6.0: MIT license.
- Apache Maven and Maven Wrapper: Apache License 2.0.
- Zingg compatibility target: Zingg v0.7.0; consult the upstream distribution for its AGPL-3.0 notices before redistributing compatibility modules.
- Spark 3.5.5 is reference/importer-only and is not part of the normal DuckDB worker runtime.

Release packaging must regenerate a complete transitive SBOM and include the exact JDK vendor/build and native library notices used for that release.
