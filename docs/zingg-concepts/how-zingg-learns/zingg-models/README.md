---
description: >-
  The two models Zingg builds during training - what each one does, why both are
  necessary, and how they work together to make entity resolution scalable and
  accurate.
---

# Zingg Models

When you run the train phase, Zingg builds two separate machine learning models from your labeled pairs. They solve different parts of the entity resolution problem; one handles scale, the other handles accuracy.

Understanding what each model does gives you a clear framework for diagnosing problems, tuning performance, and knowing which part of the pipeline to adjust when results are not what\
you expect.

### The blocking model - solving the scale problem

The fundamental challenge of entity resolution at scale is not accuracy. It is the size of the comparison space.

Comparing every record against every other record produces `N × (N-1) / 2` comparisons, a number that grows quadratically with dataset size. At one million records, that is 500 billion comparisons. At ten million records, it is 50 trillion. No system can evaluate 50 trillion pairs at production cadence.

The blocking model is Zingg's solution. Before any similarity comparison runs, the blocking model groups records into candidate buckets using field heuristics it learns from your training data. Only records within the same bucket are ever compared against each other. Records in different buckets are never compared, which means the blocking model is the first and most\
consequential filter in the pipeline.

Typical Zingg blocking reduces the comparison space to 0.05% to 1% of all possible pairs, without losing the pairs that matter.

The cost of overly aggressive blocking is that it leads to missed matches. If the blocking model places two matching records into different buckets, the similarity model will never compare them. That pair will not appear in your output regardless of how well the similarity model is\
trained.&#x20;

This is why blocking is the first thing to verify when matches you expect are missing.

{% hint style="success" icon="right-long" %}
The blocking model learns from your labeled training pairs, the same training data that teaches the\
similarity model. Better training data improves both models.

For diagnosing blocking coverage and concept details → [Blocking Model](blocking-model.md)
{% endhint %}

### The similarity model - solving the accuracy problem

The similarity model evaluates each candidate pair that the blocking model passes through and produces a similarity score: a number between 0 and 1 reflecting how likely it is that the two records represent the same real-world entity.

It is a classifier, not a rules engine. For each pair, Zingg computes multiple features per field; character-level differences, string lengths, common transpositions, and prefix and suffix overlaps, and combines them into a single prediction. The threshold between match and no-match is automatically optimized. You do not set it manually.

The similarity model learns from your labeled pairs. Match labels show it what a true match looks like in your specific data. Non-match labels show it, what different entities look like\
even when their field values are similar.

This is why label quality matters more than label quantity. A well-chosen set of 30 to 50 match pairs, covering the variation patterns in your schema produces a more accurate model than\
a large set of casually labeled pairs.

{% hint style="success" icon="right-long" %}
For diagnosing similarity model behaviour and concept details → [Similarity Model](similarity-model.md)
{% endhint %}

### The graph algorithm - from pairs to clusters

After the similarity model scores every candidate pair, a graph algorithm groups them into complete clusters using transitive closure.

If Record A matches Record B, and Record B matches Record C, the algorithm concludes that A, B, and C all represent the same entity and groups them into a single cluster, even if A and C were never directly compared.

This is what turns a list of scored pairs into a usable identity graph. Every resolved entity becomes a node. Every cluster is an entity resolved across all its representations.

In Community, clusters are assigned a `Z Cluster` that is non-persistent between runs. In Enterprise, each cluster is assigned a persistent `Zingg ID` (GUID) that remains stable across runs, incremental updates, and model changes.

{% hint style="success" icon="right-long" %}
**Read more**:

* [Identity Graph](../../identity-graph.md)
* [Z Cluster ID vs Zingg ID](../../z-cluster-and-zingg-id.md)
* [Zingg ID](/broken/pages/9QpDFW20AMt0UJ4cEW6b)
* Run [Incremental Matching](../../../running-zingg/run-incremental-matching.md) - how the graph grows over time
{% endhint %}

### Where models are saved

<table><thead><tr><th width="205.12890625" valign="top">Model</th><th valign="top">Path</th></tr></thead><tbody><tr><td valign="top">Blocking model</td><td valign="top"><code>zinggDir/modelId/model/block/</code></td></tr><tr><td valign="top">Similarity model</td><td valign="top"><code>zinggDir/modelId/model/zingg.block</code></td></tr><tr><td valign="top">Both models</td><td valign="top">Written during the train phase. Present before any match, link,<br>or incremental run can proceed.</td></tr></tbody></table>

Use the same `modelId` across all subsequent phases - `match`, `link`, and `runIncremental` to apply both models to your full dataset.

If you retrain with a new model, use `Compare Model Results (diff phase)` in Enterprise to benchmark the new model against the current one before deploying. Zingg shows you exactly which clusters changed, merged, or split between the two models.

{% hint style="success" icon="right-long" %}
**Read more**:

* [Compare Model Results](../../../running-zingg/compare-model-results.md) - benchmark two models before deploying
* [Reassign Zingg ID](../../../running-zingg/reassign-zingg-id.md) - carry existing IDs to a new model
{% endhint %}

