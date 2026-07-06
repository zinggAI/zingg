---
description: >-
  Every column Zingg adds to your match output, how scores are computed, and how
  to decide which clusters need human review.
---

# Interpret Output Scores

Every Zingg match or link output contains your original input fields plus a set of columns added by Zingg. These columns tell you which records have been resolved into the same entity, how confident the model is about each match, and (in Enterprise) provide a persistent identifier you can store downstream.

This page defines every Zingg output column and shows how to use the scores to decide what to do with each cluster

### Output columns - quick reference

<table><thead><tr><th valign="top">Column</th><th valign="top">Available in</th><th valign="top">What it tells you</th><th valign="top">Jump to detail</th></tr></thead><tbody><tr><td valign="top"><code>Z_CLUSTER</code></td><td valign="top">All editions</td><td valign="top">Which records resolved to the same entity</td><td valign="top"><a href="interpret-output-scores.md#z_cluster">Z_CLUSTER →</a></td></tr><tr><td valign="top"><code>Z_MINSCORE</code></td><td valign="top">All editions</td><td valign="top">Weakest match confidence in the cluster</td><td valign="top"><a href="interpret-output-scores.md#z_minscore">Z_MINSCORE →</a></td></tr><tr><td valign="top"><code>Z_MAXSCORE</code></td><td valign="top">All editions</td><td valign="top">Strongest match confidence in the cluster</td><td valign="top"><a href="interpret-output-scores.md#z_maxscore">Z_MAXSCORE →</a></td></tr><tr><td valign="top"><code>ZINGG_ID</code></td><td valign="top">Enterprise only</td><td valign="top">Persistent, globally unique entity GUID</td><td valign="top"><a href="interpret-output-scores.md#zingg_id-enterprise">ZINGG_ID →</a></td></tr><tr><td valign="top"><code>Z_SCORE</code></td><td valign="top">Enterprise only</td><td valign="top">Per-record-pair model confidence score</td><td valign="top"><a href="interpret-output-scores.md#z_score-enterprise">Z_SCORE →</a></td></tr></tbody></table>

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
**Read more**: For the full Zingg ID concept - [Zingg ID](/broken/pages/9QpDFW20AMt0UJ4cEW6b)
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
**CONTENT FOR THIS SECTION TO BE GIVEN BY SONAL LATER**
{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
Reading match output is the same in Community and Enterprise - only the cluster column name differs. Community produces `Z_CLUSTER`. Enterprise produces `ZINGG_ID`. Replace the column name in your code accordingly.
{% endhint %}

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
* Explaining how a cluster formed - [Explain matches](explain-matches.md)
* Improving accuracy when results are wrong - [Improve accuracy](../tuning/improve-accuracy/)
{% endhint %}
