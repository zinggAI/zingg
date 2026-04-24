---
description: Record level matching explanation
---

# Record Statistics

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

### Record Stats

* `z_record_deterministic_edges`: Number of records this record matches deterministically.
* `z_cluster_count = n`: Number of records in the cluster that this record is part of.
* `z_record_edges`: Total number of other records with which this record matches deterministically and probabilistically.
* `z_record_centrality = z_record_edges / (n - 1)`: How central the record is within its cluster.
* `z_record_determinism = z_record_deterministic_edges / (n - 1)`: Proportion of the record’s connectivity explained by deterministic rules.

#### Tip

* Audit connectors: A few records with very high `z_record_centrality` can sometimes falsely bridge clusters so validate them.

[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to https://www.zingg.ai/product/zingg-entity-resolution-compare-versions for individual tier features.
