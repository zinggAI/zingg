---
description: >-
  Verify that your blocking model is grouping known matching pairs into the same
  block before training and after.
tags:
  - tag: enterprise-only
    primary: true
---

# 🧪 Verify Blocking

The blocking model filters your dataset before any similarity comparisons happen. Records in different blocks are never compared. This is what makes Zingg scalable, but it also means that if the blocking model puts two matching records in different blocks, those records will never be matched no matter how well the similarity model is trained.

`verifyBlocking` tells you what percentage of your known matching pairs are being blocked together correctly. Run this test after configuring Zingg and again after training if you suspect missed matches.

How to inspect the blocking model, understand its coverage, and fix missed matches caused by blocking.

The blocking model is the first filter in every Zingg run. It decides which record pairs the similarity model ever sees. If it places two matching records in different buckets, those records will never be compared, and the match will be missed regardless of how well everything else is configured.

If your results are missing matches you expect to see, start here.

### **What `verifyBlocking` output contains**

The `verifyBlocking` phase produces two output directories under `zinggDir/modelId/blocks/timestamp/`:

| Output path    | What it contains                                                                                                                                         |
| -------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `counts`       | Record count per block. Shows how many records are in each block. Very large blocks indicate poor blocking specificity.                                  |
| `blockSamples` | The top 10% of records associated with each block. Use this to understand which records are being grouped together and whether the groupings make sense. |

{% hint style="success" icon="book-open" %}
**Read more**: To run the `verifyBlocking` phase and inspect output coverage→ [Verify Blocking](verify-blocking.md)
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
4. If standard blocking consistently misses a specific pattern in your data, consider custom blocking functions.

{% hint style="success" icon="book-open" %}
**Read more**:

* [Label Training Pairs](label-training-pairs.md) - how to add more training data
* [Configure Zingg](../configure-zingg.md) - changing field match types
* [Blocking Strategies: DEFAULT vs WIDER](../../tuning/blocking-strategy.md) - reorder candidate fields in the blocking tree (Enterprise only)
* [Blocking Model](../../zingg-concepts/how-zingg-learns/zingg-models/blocking-model.md) - define custom blocking functions
{% endhint %}

{% hint style="success" icon="lightbulb" %}
Run `verifyBlocking` in two situations:

1. **Before labeling** - to confirm your blocking is covering expected matches before you invest in training data
2. **After training** - if your match results are missing pairs you know should be there
{% endhint %}

### Run the `verifyBlocking` phase

{% tabs %}
{% tab title="Community" %}
### Python

```python
options = ClientOptions([ClientOptions.PHASE, "verifyBlocking"])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

### CLI

```bash
./scripts/zingg.sh --phase verifyBlocking --conf config.json
```
{% endtab %}

{% tab title="Enterprise" %}
### Python

```python
options = ClientOptions([ClientOptions.PHASE, "verifyBlocking"])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

### CLI

```bash
./scripts/zingg.sh --phase verifyBlocking --conf config.json
```
{% endtab %}

{% tab title="Enterprise Snowflake" %}
### CLI

```bash
./scripts/zingg.sh --phase verifyBlocking --conf config.json \
--properties-file <location to snowflake.properties>
```
{% endtab %}
{% endtabs %}

### **Reading the output**

`verifyBlocking` produces two output directories under `zinggDir/modelId/blocks/timestamp/`:

* `counts` - block size distribution
* `blockSamples` - sample records from each block

Read the output to inspect block coverage:

```python
verify_output = spark.read.parquet(
    f "{zinggDir}/{modelId}/verifyBlocking"
)
verify_output.show()
```

### **What to do if coverage is low**

If `verifyBlocking` shows that many known matching pairs are not being blocked together, try these in order:

1. **Add more labelled training pairs** of the type being missed. The blocking model learns from the same training data as the similarity model.
2. **Check your field match types.** Fields set to `DONT_USE` are excluded from blocking. If a key identity field is set to `DONT_USE`, matching pairs that differ on other fields may end up in different blocks.
3. **Try the `WIDER` blocking strategy** (Enterprise only) if large blocks suggest the tree is over-relying on one or two fields → [Blocking Strategies: DEFAULT vs WIDER](../../tuning/blocking-strategy.md)

{% hint style="success" icon="book-open" %}
**Consider custom blocking functions** for specialised data patterns → [Blocking Model](../../zingg-concepts/how-zingg-learns/zingg-models/blocking-model.md)

**Read more**:

* Blocking model concept and how it fits in the pipeline → [Blocking Model](../../zingg-concepts/how-zingg-learns/zingg-models/blocking-model.md)
* Custom blocking functions for advanced tuning → [Blocking Model](../../zingg-concepts/how-zingg-learns/zingg-models/blocking-model.md)
{% endhint %}
