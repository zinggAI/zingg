# Native classifier runtime

`LinearClassifier` is the initial neutral scorer contract for imported/native models. It stores an ordered feature list, weights, intercept, and optional logistic transform. Model importers are responsible for converting supported Spark estimators into this neutral representation; unsupported estimator classes fail closed at the legacy boundary.

`FeatureVectorizer` enforces the model feature order and makes missing-value behavior explicit (`FAIL` or `ZERO`) before scoring.

## Native training

`NativeClassifierTrainer` trains a binary logistic model directly from a `Frame`. The caller supplies the ordered numeric feature columns, a boolean/`0/1` label column, a maximum row count, iteration count, learning rate, and L2 penalty. Coefficients are validated for finiteness and serialized through `NativeClassifierArtifact`, so the output is immediately consumable by `ModelScorerRegistry` and the MATCH pipeline.

The Java composition root exposes this through `ZinggJob.trainClassifier(...)`; the worker and Python facade expose the same capability through the classifier form of `train` and `DuckWorker.train_classifier(...)`.
