---
description: >-
  Every column Zingg adds to your match output, how scores are computed, and how
  to decide which clusters need human review.
---

# Interpret Output Scores

Every Zingg match or link output contains your original input fields plus a set of columns added by Zingg. These columns tell you which records have been resolved into the same entity, how confident the model is about each match, and (in Enterprise) provide a persistent identifier you can store downstream.

This page defines every Zingg output column and shows how to use the scores to decide what to do with each cluster

### Output columns - quick reference

| Column | Available in | What it tells you | Jump to detail |
|--------|--------------|-------------------|----------------|
| `Z_CLUSTER` | All editions | Which records resolved to the same entity | [Z_CLUSTER →](interpret-output-scores.md#z_cluster) |
| `Z_MINSCORE` | All editions | Weakest match confidence in the cluster | [Z_MINSCORE →](interpret-output-scores.md#z_minscore) |
| `Z_MAXSCORE` | All editions | Strongest match confidence in the cluster | [Z_MAXSCORE →](interpret-output-scores.md#z_maxscore) |
| `ZINGG_ID` | Enterprise only | Persistent, globally unique entity GUID | [ZINGG_ID →](interpret-output-scores.md#zingg_id-enterprise) |
| `Z_SCORE` | Enterprise only | Per-record-pair model confidence score | [Z_SCORE →](interpret-output-scores.md#z_score-enterprise) |

### How Zingg computes scores

For each field - `fname`, `lname`, `email`, and so on, Zingg computes multiple features and feeds them to a classifier. These features are different ways to compare strings: character-level differences, string length differences, positional weighting, and common-typo awareness.

No individual feature is perfect, but the classifier finds the best-fit curve across all features and produces a final score. Key behaviors to understand:

* The shorter string pair `ABCD`/`ABCE` will be less similar than `ABCDEF`/`ABCEEF` - length matters.
* Common typos (for example, `m` instead of `n`) are penalized less severely than unusual character swaps.
* Differences in the middle of a string are penalized more than prefix or suffix differences.
* The threshold is automatically optimized. You may see scores below the conventional 0.5; this is intentional. Zingg optimizes for both accuracy and recall, not just high scores.

### Output columns - all editions

#### **`Z_CLUSTER`**

The most important column. All records sharing the same `Z_CLUSTER` value have been resolved to the same real-world entity. Group by `Z_CLUSTER`to collapse duplicates into a single golden record or link records across systems.

In the Community version, `Z_CLUSTER` is non-persistent. It is reassigned fresh each time the match job runs. The same records may receive a different `Z_CLUSTER` on\
the next run.

In the Enterprise version, `Z_CLUSTER` is replaced by the persistent `Zingg ID`. See `Zingg ID` in the next section.

{% hint style="success" icon="right-long" %}
**Read more**: [Z Cluster ID and Zingg ID](../zingg-concepts/z-cluster-and-zingg-id.md)
{% endhint %}

#### `Z_MINSCORE`

The lowest similarity score between any two records in the cluster. Indicates the confidence of the weakest link in the cluster. A cluster where `Z_MINSCORE` is `0` or very low means at least two records in that cluster matched weakly. Flag these for manual review.

#### `Z_MAXSCORE`

The highest similarity score between any two records in the cluster. Indicates the strongest match within the cluster. A high `Z_MAXSCORE` with a low `Z_MINSCORE` means the cluster\
has both strong and weak matches - worth inspecting.

#### `ZINGG_ID` (Enterprise)

Globally unique, persistent identifier for each resolved entity. It does not change between runs, including incremental runs. Safe to store and reference in downstream systems. Replaces `Z_CLUSTER` in Enterprise output.

{% hint style="success" icon="right-long" %}
**Read more**: For the full Zingg ID concept — [Z Cluster and Zingg ID](../zingg-concepts/z-cluster-and-zingg-id.md)
{% endhint %}

#### `Z_SCORE` (Enterprise)

The model confidence score is provided for each record pair. Higher values closer to 1.0 indicate a stronger likelihood that the records are a true match. Used alongside `Z_MINSCORE` and `Z_MAXSCORE` for threshold-based automated decisions.

#### `Z_SOURCE` (Enterprise)

Appears only in the link phase output. Identifies which source dataset each record came from. Use `Z_SOURCE` to trace each resolved record back to its origin system after linking two datasets together.

{% hint style="success" icon="right-long" %}
**Read more**: For the link phase - [Link across datasets](../running-zingg/link-across-datasets.md)
{% endhint %}

### Reading match output in code

Reading match output is the same in Community and Enterprise; only the cluster column name differs. Community produces `Z_CLUSTER`. Enterprise produces `ZINGG_ID`. Replace the column name in your code accordingly.

{% tabs %}
{% tab title="Community" %}
```python
from pyspark.sql.functions import col, count, avg

output = spark.read.csv("/tmp/zinggOutput", header=True)

output.groupBy("Z_CLUSTER") \
  .agg(
    count("*").alias("records"),
    avg(col("Z_MINSCORE").cast("double")).alias("avg_min"),
    avg(col("Z_MAXSCORE").cast("double")).alias("avg_max")
  ) \
  .orderBy("avg_min") \
  .show()
```
{% endtab %}

{% tab title="Enterprise" %}
```python
from pyspark.sql.functions import col, count, avg

output = spark.read.csv("/tmp/zinggOutput", header=True)

output.groupBy("ZINGG_ID") \
  .agg(
    count("*").alias("records"),
    avg(col("Z_MINSCORE").cast("double")).alias("avg_min"),
    avg(col("Z_MAXSCORE").cast("double")).alias("avg_max")
  ) \
  .orderBy("avg_min") \
  .show()
```
{% endtab %}

{% tab title="Enterprise Snowflake" %}
{% hint style="info" %}
**Coming soon** — Enterprise Snowflake guidance for interpreting output scores is in progress and will be published here when available.
{% endhint %}
{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
Reading match output is the same in Community and Enterprise - only the cluster column name differs. Community produces `Z_CLUSTER`. Enterprise produces `ZINGG_ID`. Replace the column name in your code accordingly.
{% endhint %}

{% hint style="success" icon="right-long" %}
Ordering clusters by `avg_min` ascending puts the weakest clusters at the top, those are the ones worth reviewing for false positives first.

**Read more**:

* [Interpret Output Scores](interpret-output-scores.md) - `Z_MINSCORE` and `Z_MAXSCORE` explained
* [Label Training Pairs](../running-zingg/create-training-data/label-training-pairs.md) - adding training data for missed patterns
* [Verify blocking](../running-zingg/create-training-data/verify-blocking.md)
{% endhint %}

A small number of targeted labels for the specific pattern being missed is more effective than a large general labeling run.

Run `findTrainingData` and look for pairs similar to the ones being missed. Label them as `match`. The similarity model needs to see this pattern in the training data to learn it. If the variation that\
causes the mismatch like a specific abbreviation pattern, a missing field, or a transliteration not represented in your labeled pairs; the model has no basis for scoring it above the threshold.

#### Add match labels for the missed pattern

If `verifyBlocking` confirms the pair is reaching the similarity model but still not matching, proceed to the training data fix below.

Run `verifyBlocking`. If the missed pair is not being blocked together, the similarity model is not the cause. Address the blocking issue first by referring to the [Blocking Model](../zingg-concepts/how-zingg-learns/zingg-models/blocking-model.md) page.

#### Confirm it as a similarity problem, not a blocking problem

A false negative is a pair of records that represent the same entity but were not placed in the same cluster. The similarity model scored them below the match threshold or the blocking model never allowed them to be compared.

### Diagnosing false negatives matches that were missed

<details>

<summary><strong>If false positives are widespread, not isolated</strong></summary>

If false positives affect many clusters rather than a specific pattern, check your field match types before adding more training data.

Fields that should use `EXACT` but are set to `FUZZY` are the most common cause\
of widespread false positives. Date of birth, SSN, national ID, and tax IDs should always use `EXACT`. Fuzzy tolerance on these fields allows records with different values to score above the match threshold.

**Also check**: Are there any fields that should be `DONT_USE` contributing to the match?\
scores? Internal IDs and sequence numbers that happen to appear similar across records can inflate match scores incorrectly.

</details>

<details>

<summary><strong>How to identify the cause</strong></summary>

Look at the false positive cluster in your output. Ask:

Do the records share high values on some fields but clearly differ on fields that should be discriminating? For example, can you have the same first name and city but different dates of birth?

This scenario is almost always a training data issue. The model has not seen enough non-match-labeled pairs that look similar in some fields but differ in the discriminating ones. It has learned that similarity in those shared fields is enough for a match because you have not shown it the counter-examples.

**The fix**: Run `findTrainingData` again and find pairs that look like the false positive, similar on the misleading fields, different on the discriminating ones and label them as No Match. The similarity model will learn to use the discriminating fields correctly.

</details>

A false positive is a cluster that contains records representing different real-world entities. They look similar enough that the model merged them, but they should not be in the same cluster.

### Diagnosing false positives records that should not be together

{% hint style="success" icon="right-long" %}
Before debugging the similarity model, confirm the missed pairs are actually reaching it. If two records are in different blocks, the similarity model never evaluates them regardless of its accuracy.
{% endhint %}

How to inspect the similarity model, diagnose false positives and false negatives, and improve accuracy through targeted retraining.\
\
The similarity model scores every candidate pair that the blocking model passes through. If your results contain records incorrectly merged into the same cluster (false positives) or matching records that were missed (false negatives), the similarity model is where to investigate after first confirming that blocking is not the cause.

### Using scores to decide what to do with clusters

<details>

<summary><strong>How do I set thresholds for automated decisions?</strong></summary>

Use `Z_MINSCORE` to gate automation. Clusters with high `Z_MINSCORE` are confident across every pair - these can flow directly to automated golden record creation. Clusters with low `Z_MINSCORE` have at least one weakly matched pair and should go to manual review.

A common starting pattern:

* `Z_MINSCORE` above 0.8 → auto-merge into golden record
* `Z_MINSCORE` between 0.4 and 0.8 → human review queue
* `Z_MINSCORE` below 0.4 → flag as suspect, often a false positive

Tune these cutoffs based on your tolerance for false positives vs. missed reviews. The threshold values are not absolute - Zingg's automatic threshold optimization means scores below 0.5 can still be valid matches in your data.

</details>

<details>

<summary><strong>My cluster has records I don't think should be together. What do I do?</strong></summary>

Two possible causes:

**Training data gap** - the similarity model has not seen enough non-match labels for pairs that look similar but represent different entities. Add `findTrainingData` runs and label more No Match pairs that resemble the false positive.

**Field match type mismatch** - fields that should use `EXACT` are set to `FUZZY`. Date of birth, SSN, national ID, and tax IDs should always use `EXACT`. Fuzzy tolerance on these fields lets records with different identifier values match each other

</details>

<details>

<summary><strong>What does a score of 0 mean?</strong></summary>

A `Z_MINSCORE` of `0` does not mean the match is wrong. It means that at least two records in the cluster matched transitively - they were connected through a chain of\
intermediate matches rather than directly.

For example, Record A matches B with a score of 0.8. Record B matches C with a score of 0.7. All three go into the same cluster even if A and C have a low direct score. The cluster `Z_MINSCORE` will reflect the weakest direct pair comparison.

Review clusters with `Z_MINSCORE` of `0` manually to confirm the full cluster is correct.

</details>

{% hint style="success" icon="right-long" %}
**Read more**:

* Z Cluster and Zingg ID - [Z Cluster ID and Zingg ID](../zingg-concepts/z-cluster-and-zingg-id.md)
* Explaining how a cluster formed - [Explainability and Statistics](explainability-and-statistics.md)
* Improving accuracy when results are wrong - [Improve accuracy](../tuning/improve-accuracy/)
{% endhint %}
