# Record Statistics

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

### Record Stats

* `z_record_deterministic_edges`: Number of records this record matches deterministically.
* `z_cluster_count = n`: Number of records in the cluster that this record is part of.
* `z_record_edges`: Number of other records it matches (deterministic + probabilistic).
* `z_record_centrality = z_record_edges / (n - 1)`: How central the record is within its cluster.
* `z_record_determinism = z_record_deterministic_edges / (n - 1)`: Proportion of the record’s connectivity explained by deterministic rules.

[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to https://www.zingg.ai/product/zingg-entity-resolution-compare-versions for individual tier features.
