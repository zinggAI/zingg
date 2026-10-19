# Legacy model import boundary

Spark 3.5.5 model importers run as separate processes. The normal DuckDB worker never loads Java-serialized Spark objects. `IsolatedImporterProcess` passes canonical input/output paths and explicit size/network limits through environment variables, applies a wall-clock timeout, and forcefully terminates an overrun. The importer must emit a neutral `model-format` directory with a manifest, checksum, source version, and provenance.

## Zingg 0.7 blocking-tree source contract

The upstream Spark implementation persists the blocking tree as a binary row created from `Tree<Canopy<Row>>` by `Util.convertObjectIntoByteArray`. The tree is written through the Spark pipe layer as a single binary column and is not a frequency-table format. The isolated Spark importer must therefore deserialize this object only inside the legacy process, extract the tree nodes, field context, hash-function identity, canopy hash and child relationships, and emit those values into the neutral `model-format` payload. The DuckDB worker must consume only that neutral payload and must never load `zingg.common.core.block.Tree`, `Canopy`, Spark `Row`, or the serialized bytes.

The pinned upstream build was verified from the 0.7.0 source checkout with `-DskipTests`; it produces `zingg-common-client-0.7.0.jar` and `zingg-common-core-0.7.0.jar`. These artifacts belong exclusively to the isolated Spark importer classpath. The importer entry point is `zingg.spark.core.util.SparkBlockingTreeUtil`, whose `getBlockingTree(byte[])` path delegates to `Util.revertObjectFromByteArray`; the adapter must invoke that path in the child process, enforce the configured class allowlist, and serialize only the neutral tree nodes to the output directory.

The upstream reference is pinned to Zingg `0.7.0`, Spark `3.5.5`, and Scala `2.12`; importer changes must preserve that boundary and record the upstream source version in `ImportProvenance`.

`packaging/invoke-legacy-import.ps1` is the operational launcher for that external importer. It validates the Spark launcher and directories, exports the bounded importer contract, disables network access through `ZINGG_IMPORT_NETWORK=disabled`, and fails on a non-zero Spark exit code. The normal worker remains independent of Spark.
