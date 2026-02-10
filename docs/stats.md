---
title: match statistics
parent: Step By Step Guide
nav_order: 17
description: Reassign the ZINGG IDs for clusters from the original production model
---

# Match Statistics

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

Entity resolution is more insightful when you can observe how clusters form, connect, and evolve across runs. Currently, the Zingg output consists of the original data along with the Zingg ID and the match probabilities. If we could surface information about the linkages we found among records within a cluster, we could help users with matching internals and anomalies. When our users are dealing with millions of records, finding the needle in the haystack is critical.

This information about changing clusters would be a good first step to observing the entity resolution pipeline. While running Zingg incrementally, Match Statistics exposes how cluster numbers change as records get inserted and updated into the identity graph. If the number of clusters changes disproportionately to the number of records updated or added, an alert could be triggered.
Match Statistics surfaces those insights by writing structured metrics for every match or incremental run, so you can:

- See how dense or sparse your clusters are
- Understand how much of a cluster is explained by deterministic rules vs. probabilistic links
- Identify highly central records (connectors) and outliers
- Track how clusters change across runs (growth, splits, merges, reassignments)

If you’ve ever asked “how deterministic rules are performing?” or “did my latest incremental run improve cluster quality?”, Match Statistics is your answer.

---

## What gets written

Zingg writes statistics to the stats directory whenever you run phases like match or incremental. The output currently comprises three types:

- SUMMARY: High-level run summary
- CLUSTER: One row per cluster with connectivity metrics
- RECORD: One row per record with its connectivity metrics within its cluster

---

## Configuring stats output

You can configure stats output either via JSON configuration or programmatically (e.g., Python).

Important: Please ensure the path/name contains the placeholder “_$ZINGG_DYNAMIC_STAT_NAME_”.

### JSON configuration example

```json name=zingg-stats-output-config.json
{
  "outputStats" : {
       "name":"stats", 
       "format":"csv", 
       "props": {
          "location": "/tmp/zinggStats_$ZINGG_DYNAMIC_STAT_NAME",
          "delimiter": ",",
          "header":true
       }
    }
}
```

### Python configuration example

```
from zingg import ECsvPipe

statsOutputPipe = ECsvPipe("stats", "/tmp/febrlStats_$ZINGG_DYNAMIC_STAT_NAME")
statsOutputPipe.setHeader("true")
```

---

## How to execute

### Run match/incremental with statistics enabled
- Ensure `outputStats` is configured (JSON or Python pipe).
- Execute your standard `match` phase/ `incremental` phase.
- Zingg writes stats for this run under the configured location with timestamp.

---
## Metrics and terminology

### Cluster Stats

- `z_cluster_count = n`: Number of records in the cluster (cluster size).
- `z_cluster_edges`: Total number of edges (deterministic + probabilistic) among records in the cluster; i.e., all pairwise connections formed within the cluster.
- `z_cluster_deterministic_edges`: Total number of deterministic edges among records in the cluster; i.e., rule-based matches.
- `z_cluster_centrality = z_cluster_edges / (nC2)`: Edge density of connections in the cluster. Values near 1 → dense clusters (almost all pairs connected); lower values → sparse clusters.
- `z_cluster_determinism = z_cluster_deterministic_edges / (nC2)`: Proportion of connectivity driven by deterministic rules. Higher means more rule-driven cluster cohesion.

Note:
- `nC2` (combinations of n records taken 2 at a time) equals `n*(n-1)/2`.

### Record Stats

- `z_record_deterministic_edges`: Number of records this record matches deterministically.
- `z_cluster_count = n`: Number of records in the cluster that this record is part of.
- `z_record_edges`: Number of other records it matches (deterministic + probabilistic).
- `z_record_centrality = z_record_edges / (n − 1)`: How central the record is within its cluster.
- `z_record_determinism = z_record_deterministic_edges / (n − 1)`: Proportion of the record’s connectivity explained by deterministic rules.

---


## Tips, prompts, and best practices
- Watch determinism ratio: Rising `z_cluster_determinism` over iterations usually means your rules are improving.
- Audit connectors: A few records with very high `z_record_centrality` can sometimes falsely bridge clusters so validate them.

---

[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to [Zingg Entity Resolution Compare Versions](https://www.zingg.ai/product/zingg-entity-resolution-compare-versions) for individual tier features.