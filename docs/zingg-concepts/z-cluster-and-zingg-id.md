---
description: >-
  The key difference between Community and Enterprise output, and why it matters
  for every system that consumes Zingg's results.
---

# Z Cluster and Zingg ID

Zingg writes a cluster identifier into your match output so every record belonging to the same resolved entity carries the same value. Community uses `Z_CLUSTER`, a non-persistent identifier. Enterprise uses `Zingg_ID`, a persistent globally unique identifier. This page covers the difference between the two, the lifecycle behaviour, and how to use Zingg ID in your downstream systems.

### `Z_Cluster` - Community

When Zingg Community resolves a set of records into a cluster, it assigns a `Z Cluster` to every record in that cluster. All records in the same cluster share the same `Z Cluster` value in the output. This is how you identify which records resolved to the same entity.

`Z Cluster` is non-persistent. Each time you run the match job, Zingg rebuilds the identity graph from scratch and assigns new cluster identifiers. The same cluster of records may receive a completely different value on the next run.

This is expected behavior for Community, but it means you cannot store `Z Cluster` in downstream systems and expect it to remain valid across runs.

<details>

<summary><strong>What breaks downstream if <code>Z_Cluster</code> changes between runs?</strong></summary>

Any system that stores `Z_Cluster` as a customer or entity identifier will break when the match job re-runs.

Common failure scenarios:

* A CRM storing `Z_Cluster` as the unified customer key - after re-run, the key no longer exists or resolves to a different cluster
* An analytics dashboard segmenting by cluster - segment membership changes unpredictably
* An AI model trained on `Z_Cluster`-based features - features become invalid after each re-run
* A compliance system linking transactions to a cluster identifier - audit trail breaks

If any downstream system needs a stable entity identifier to reference across time, `Z_Cluster` is not suitable. This is the core use case for Zingg Enterprise and the `Zingg_ID`.

</details>

{% hint style="warning" icon="right-long" %}
Need stable entity IDs across runs? [Talk to us about Enterprise](https://www.zingg.ai/company/contact/contact).
{% endhint %}

### `Zingg_ID` - Enterprise

Zingg Enterprise assigns a globally unique, persistent GUID - the `Zingg ID` , to each resolved entity. Once assigned, the `Zingg ID` is stable across incremental runs. New records that match an existing cluster inherit its `Zingg ID`, and cluster merges are handled automatically.

However, if cluster membership changes significantly between runs. For example, a cluster splits due to new data or updated training, the `Zingg ID` may be reassigned. Downstream systems should treat the `Zingg ID` as stable within a deployment lifecycle, not as an indefinitely immutable identifier.

#### **Lifecycle behaviour**

<table><thead><tr><th width="306.1484375">Scenario</th><th>Zingg_ID behaviour</th></tr></thead><tbody><tr><td>New record matches an existing cluster</td><td>Inherits the cluster's existing <code>Zingg_ID</code></td></tr><tr><td>New record does not match any existing cluster</td><td>Receives a new <code>Zingg_ID</code></td></tr><tr><td>Cluster merges with another in an incremental run</td><td>Merge is handled automatically</td></tr><tr><td>Cluster splits due to new data or updated training</td><td><code>Zingg_ID</code> may be reassigned</td></tr><tr><td>Human-approved cluster decisions from previous runs</td><td>Preserved, not overridden</td></tr><tr><td>Model is retrained</td><td>Use <a href="../running-zingg/reassign-zingg-id.md">Reassign <code>Zingg_ID</code> </a>to carry existing IDs to the new model</td></tr></tbody></table>

### The `Zingg_ID` column in your output

In your match or incremental output, the Zingg ID appears as a column named `Zingg_ID` alongside your input fields. Every record in the same resolved cluster shares the same `Zingg_ID` value.

Use this column to:

* Join Zingg output back to your source tables
* Build a golden record by grouping on `Zingg_ID` and selecting the best field values across each group
* Track the same entity across incremental runs
* Pass a stable entity reference to downstream CRM, analytics, or compliance systems

{% hint style="success" icon="right-long" %}
**Read more:**

* [Interpret Output Scores](../interpreting-results/interpret-output-scores.md) - for the full reference of every Zingg output column
* [Identity Graph](identity-graph.md) - how Zingg's resolved entities form a graph structure
* [Run Incremental Matching](../running-zingg/run-incremental-matching.md) - how Zingg IDs persist when new records arrive
* [Reassign Zingg ID](../running-zingg/reassign-zingg-id.md) - preserve Zingg IDs across model retraining or platform migration
* [Concept Glossary](concept-glossary.md) - every Zingg term in one place
{% endhint %}
