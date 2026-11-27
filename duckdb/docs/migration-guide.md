# Migration guide

1. Export legacy Zingg 0.7.0 blocking or classifier artifacts into an isolated importer input directory.
2. Run the corresponding Spark 3.5.5 importer and write a neutral `zingg-0.1-native` artifact containing manifest, payload, checksum, and provenance.
3. Place data files under the configured input root and choose an output format by extension: CSV, Parquet, JSON, or Arrow.
4. Use the worker `run` operation for MATCH. Supply the optional classifier artifact when native linear scoring is required.
5. Compare output schemas and graph/link semantics during rollout. Timestamp-prefixed cluster IDs are intentionally not stable across independent production runs.

The normal worker does not load Spark, GraphFrames, Scala, Java serialization, Py4J, or JPype. Unsupported legacy model types must remain at the importer boundary and fail closed.
