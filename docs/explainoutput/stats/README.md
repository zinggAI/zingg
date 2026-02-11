---
title: match statistics
parent: Step By Step Guide
nav_order: 17
description: Under the hoods of the matching process
---

# Output Statistics

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

The Output Statistics surface information about the linkages Zingg found among records within a cluster. While running Zingg incrementally, Output Statistics exposes how cluster numbers change as records get inserted and updated into the identity graph. If the number of clusters changes disproportionately to the number of records updated or added, an alert could be triggered. Match Statistics surfaces those insights by writing structured metrics for every match or incremental run, so you can:

* See how dense or sparse your clusters are
* Understand how much of a cluster is explained by deterministic rules vs. probabilistic links
* Identify highly central records (connectors) and outliers
* Track how clusters change across runs (growth, splits, merges, reassignments)

If you’ve ever asked “how deterministic rules are performing?” or “did my latest incremental run improve cluster quality?”, Output Statistics is your answer.

***

## What gets written

Zingg writes statistics to the stats directory whenever you run phases like match or incremental. The output comprises of three types:

* SUMMARY: High-level run summary
* CLUSTER: One row per cluster with cluster level matching metrics
* RECORD: One row per record with its matching metrics within its cluster



[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to [Zingg Entity Resolution Compare Versions](https://www.zingg.ai/product/zingg-entity-resolution-compare-versions) for individual tier features.
