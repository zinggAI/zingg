---
description: >-
  Run the match phase to find duplicate records within a single dataset using
  your trained Zingg model.
---

# Run the match phase

The `match` phase runs AFTER `train`. It applies the trained Zingg model to your full dataset and groups records that represent the same real-world entity into clusters.

Use `match` when you want to find duplicates within a single dataset. If you need to match records across two separate datasets, use the link phase instead - both are equal operations using the same trained model.

### Output Fields

Every record in the match output contains your original input fields plus three columns added by Zingg:

* `Z_CLUSTER` - unique identifier shared by all records in the same cluster. Records with the same `Z_CLUSTER` represent the same real-world entity. In Enterprise, this is the persistent\
  Zingg ID.
* `Z_MINSCORE` - the lowest similarity score between any two records in that cluster. Indicates the confidence of the weakest link in the cluster.
* `Z_MAXSCORE` - the highest similarity score between any two records in that cluster. Indicates the strongest match within the cluster.

{% hint style="success" icon="right-long" %}
**Read more:**

* Scores are explained in detail - [Interpreting output scores](../interpreting-results/interpret-output-scores.md)
* For the link phase (across two datasets) - [Link across datasets](link-across-datasets.md)
{% endhint %}

{% tabs %}
{% tab title="Community" %}
### Python

```python
options = ClientOptions([
    ClientOptions.PHASE,
    "match"
])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

### CLI

```bash
./zingg.sh --phase match --conf config.json
```

### Read and View Output

```python
output = spark.read.csv(
    "/tmp/febrlOutput",
    header = True
)
display(output)
```

{% hint style="info" icon="right-long" %}
Matching records share the same `Z_CLUSTER` value.

`Z_MINSCORE` and `Z_MAXSCORE` show match confidence within the cluster.
{% endhint %}
{% endtab %}

{% tab title="Enterprise" %}
### Python

```python
options = ClientOptions([
    ClientOptions.PHASE,
    "match"
])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

### CLI

```bash
./zingg.sh --phase match --conf config.json
```

### Read and View Output

```python
# Read match output
output = spark.read.csv(
    "/tmp/febrlOutput",
    header=True
)
display(output)
```

{% hint style="info" icon="right-long" %}
Enterprise output includes `Zingg ID` (persistent across runs) instead of `Z_CLUSTER`, plus deterministic match flag and Match Statistics. `Zingg ID` is stable across all subsequent incremental runs.
{% endhint %}
{% endtab %}

{% tab title="Enterprise Snowflake" %}
{% hint style="info" icon="right-long" %}
Enterprise only. Zingg on Snowflake uses Snowpark and does not require a Spark cluster.
{% endhint %}

_**CHECK WITH SONAL - Enterprise Snowflake content for this topic to be provivded by Sonal**_
{% endtab %}
{% endtabs %}

<details>

<summary><strong>How do I interpret Z_MINSCORE and Z_MAXSCORE?</strong></summary>

`Z_MINSCORE` and `Z_MAXSCORE` are the confidence range for a cluster.

* `Z_MINSCORE` is the lowest similarity score between any two records in the cluster. A very low `Z_MINSCORE` means some records in the cluster matched weakly—worth reviewing manually.
* `Z_MAXSCORE` is the highest similarity score between any pair in the cluster. The threshold is automatically optimized by Zingg so you do not need to tune a cut-off manually. You may see records with scores below the conventional 0.5; this behaviour is intentional as Zingg optimizes for both accuracy and recall.

**Recommended approach**: Keep clusters whose value `Z_MINSCORE` is 0 for manual inspection. Keep the cluster size above 4 or 5 for closer review. The exact threshold depends on how accurate you determine your results and how much manual control you want.

</details>
