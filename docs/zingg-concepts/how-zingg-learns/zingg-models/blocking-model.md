---
description: >-
  How to inspect the blocking model, understand its coverage, and fix missed
  matches caused by blocking.
---

# Blocking Model

The blocking model is the first filter in every Zingg run. It decides which record pairs the similarity model ever sees. If it places two matching records in different buckets, those records will never be compared, and the match will be missed regardless of how well everything else is configured.

If your results are missing matches you expect to see, start here.

### **What `verifyBlocking` output contains**

The `verifyBlocking` phase produces two output directories under `zinggDir/modelId/blocks/timestamp/`:

| Output path    | What it contains                                                                                                                                         |
| -------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `counts`       | Record count per block. Shows how many records are in each block. Very large blocks indicate poor blocking specificity.                                  |
| `blockSamples` | The top 10% of records associated with each block. Use this to understand which records are being grouped together and whether the groupings make sense. |

{% hint style="success" icon="right-long" %}
**Read more**: To run the `verifyBlocking` phase and inspect output coverage→ [Verify Blocking](../../../running-zingg/verify-blocking.md)
{% endhint %}

### Why blocking coverage fails and how to fix it

Poor blocking coverage means matching pairs are being placed in different buckets and never compared. Four causes account for most cases:

<details>

<summary><strong>Skewed or limited training samples</strong></summary>

The blocking model learns heuristics from your labeled training data. If your training data is skewed; for example, all your labeled pairs are from a single state, city, or category the blocking model learns blocking rules that are too narrow for the full dataset.

**Fix**: Run `findTrainingData` again and label pairs that represent the full distribution of your data. If your dataset has records from 50 states, your training data should include\
pairs from multiple states not just the largest.

</details>

<details>

<summary><strong>Manually added training samples overriding learned rules</strong></summary>

If you supplement your training data with manually created pairs rather than pairs selected by `findTrainingData` those pairs, it may teach the blocking model rules that are too specific to those exact examples. The blocking model needs representative pairs selected by Zingg's own candidate selection logic, not hand-curated ones.

**Fix**: Use `findTrainingData` to generate candidate pairs. Add manually created training data only to supplement coverage gaps, and ensure they are diverse enough that the blocking model can generalize from them.

</details>

<details>

<summary><strong>Too few training examples</strong></summary>

30 to 50 labeled `match` pairs is a good starting point for the similarity model. But the blocking model may need more examples, especially on large datasets with many field variations; before it learns blocking rules that are generic enough to cover the full problem space.

**Fix**: Run `findTrainingData` and `label` additional round. Pay particular attention to `match` pairs that cover different field value patterns different name formats, address abbreviations, and missing field combinations.

</details>

<details>

<summary><strong>Non-differentiating columns in the schema</strong></summary>

If your field definitions include columns that are the same or nearly the same across a large proportion of your records, for example, a country code that is "US" for 99%\
of records those columns produce very large blocks. Large blocks slow down matching and indicate that the blocking model is not creating useful partitions.

**Fix**: Mark non-differentiating columns as `DONT_USE` in your field definitions. `DONT_USE` fields are excluded from both blocking and similarity; they appear in output but do not\
influence matching.

</details>

### If `verifyBlocking` shows low coverage, what to do in order

1. Add more labeled training pairs of the type being missed. The blocking model learns from the same training data as the similarity model. Focus on pairs that represent the variation patterns missing from your current training set.
2. Review your field match types. Fields marked `DONT_US`E are excluded from blocking as well as similarity. If a field has a strong identity signal - a consistent identifier that appears across matching records - consider changing it from `DONT_USE` to `FUZZY`.
3. Check whether missed pairs share a common characteristic. If all missed pairs have empty values in a key field, empty fields cannot contribute to blocking. Either remove that field from your blocking config or improve data completeness upstream.
4. If standard blocking consistently misses a specific pattern in your data, consider custom blocking functions.&#x20;

{% hint style="success" icon="right-long" %}
**Read more**:

* [Label Training Pairs](../../../running-zingg/label-training-pairs.md) - how to add more training data
* [Configure Zingg](../../../running-zingg/configure-zingg.md) - changing field match types
* [Custom Blocking and Similarity](../../../tuning/custom-blocking-and-similarity.md) Functions&#x20;
{% endhint %}
