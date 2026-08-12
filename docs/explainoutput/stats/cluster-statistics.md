# Cluster Statistics

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

### Cluster Stats

* `z_cluster_count = n`: Number of records in the cluster (cluster size).
* `z_cluster_edges`: Total number of  deterministic and probabilistic pairs among records in the cluster. This shows all the pairwise connections formed within the cluster.
* `z_cluster_deterministic_edges`: Total number of deterministic edges among records in the cluster as [configured through the determinstic rules](../../deterministicMatching.md).
* `z_cluster_centrality = z_cluster_edges / (nC2)`: Edge density of connections in the cluster. Values near 1 → dense clusters (almost all pairs connected); lower values → sparse clusters.
* `z_cluster_determinism = z_cluster_deterministic_edges / (nC2)`: Proportion of connectivity driven by deterministic rules. Higher means more rule-driven cluster cohesion.

Note:

* `nC2` (combinations of n records taken 2 at a time) equals `n*(n-1)/2`.

#### Tip

* Watch determinism ratio: Rising `z_cluster_determinism` over iterations usually means your rules are improving.

[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to https://www.zingg.ai/product/zingg-entity-resolution-compare-versions for individual tier features.
