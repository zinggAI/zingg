---
description: from pairs to clusters
---

# The graph algorithm

After the similarity model scores every candidate pair, a graph algorithm groups them into complete clusters using transitive closure.

If Record A matches Record B, and Record B matches Record C, the algorithm concludes that A, B, and C all represent the same entity and groups them into a single cluster, even if A and C were never directly compared.

This is what turns a list of scored pairs into a usable identity graph. Every resolved entity becomes a node. Every cluster is an entity resolved across all its representations.

In Community, clusters are assigned a `Z Cluster` that is non-persistent between runs. In Enterprise, each cluster is assigned a persistent `Zingg ID` (GUID) that remains stable across runs, incremental updates, and model changes.

{% hint style="success" icon="right-long" %}
**Read more**:

* [Identity Graph](identity-graph.md)
* [Z Cluster ID vs Zingg ID](z-cluster-and-zingg-id.md)
* [Zingg ID](/broken/pages/9QpDFW20AMt0UJ4cEW6b)
{% endhint %}
