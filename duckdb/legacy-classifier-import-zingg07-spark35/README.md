# Legacy classifier importer

This isolated process boundary is reserved for importing supported Zingg v0.7.0 Spark 3.5.5 classifier artifacts and emitting neutral model metadata. `ClassifierImporterLauncher` applies the same timeout, size, path, and non-zero-exit safeguards as the blocking importer. Unsupported estimator classes fail closed with a structured diagnostic.
