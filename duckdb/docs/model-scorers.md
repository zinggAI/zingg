# Model scorer registry

`ModelScorerRegistry` is the neutral runtime selection point. It contains no Spark dependencies. Importers map supported Zingg/Spark estimators to native scorer type names and persist those names in the native model manifest; unsupported types fail before entering the worker.

The first native classifier payload is UTF-8 JSON with `kind: "linear-classifier"`, ordered `features`, numeric `weights`, `intercept`, and boolean `logistic`. The registry validates the classifier model type, parses the payload, and rejects missing or dimensionally inconsistent metadata before constructing a scorer.
