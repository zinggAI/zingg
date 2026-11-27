# ZFrame capability matrix

This report is backed by `FrameCapabilityCoverageTest`, which fails when an API method is added without a public `DuckFrame` implementation. The test proves structural coverage; semantic parity still requires the differential fixtures listed in [validation-status.md](validation-status.md).

| Capability | DuckDB implementation | Validation state |
|---|---|---|
| Projection, expressions, filters | `select`, `selectExpr`, `filter` | Local unit coverage |
| Join and deterministic pair projection | `join`, `joinProjected` | Local pair-shape regression |
| Set operations | `union`, `except`, `distinct` | Structural coverage; differential parity pending |
| Ordering and limiting | `sort`, `limit` | Structural coverage; differential parity pending |
| Aggregation and repartitioning | `aggregate`, `repartition`, `coalesce` | Structural coverage; backend semantics pending |
| Explode and split | `explode`, predicate `split`, and `split(column, pattern, resultColumn)` | Both split contracts are structurally available; full Zingg differential parity pending |
| Materialization and collection | `cache`, `collect`, `count` | Local lifecycle/resource tests |
| File egress | CSV, JSON, Parquet, Arrow writers | Arrow offline-safe qualification pending |

Unsupported or not-yet-proven semantics remain explicitly marked instead of being presented as Zingg parity.
