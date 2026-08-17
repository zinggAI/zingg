---
description: >-
  Run the match phase to find duplicate records within a single dataset using
  your trained Zingg model.
---

# Run the match phase

The `match` phase runs AFTER `train`. It applies the trained Zingg model to your full dataset and groups records that represent the same real-world entity into clusters.

Use `match` when you want to find duplicates within a single dataset. If you need to match records across two separate datasets, use the link phase instead - both are equal operations using the same trained model.

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
./scripts/zingg.sh --phase match --conf config.json
```

### Read and View Output

```python
output = spark.read.csv("path-to-output-directory",header = True)
display(output)
```

{% hint style="info" icon="right-long" %}
Matching records share the same `Z_CLUSTER` value.
{% endhint %}
{% endtab %}

{% tab title="Enterprise" %}
### Python

```python
options = ClientOptions([ClientOptions.PHASE,"match"])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

### CLI

```bash
./scripts/zingg.sh --phase match --conf config.json
```

### Read and View Output

```python
# Read match output
output = spark.read.csv("path-to-output-directory",header=True)
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

### CLI

```bash
./scripts/zingg.sh --phase match --conf config.json \
--properties-file <location to snowflake.properties>
```

{% hint style="info" icon="right-long" %}
Enterprise output includes `Zingg ID` (persistent across runs) instead of `Z_CLUSTER`, plus deterministic match flag and Match Statistics. `Zingg ID` is stable across all subsequent incremental runs.
{% endhint %}
{% endtab %}
{% endtabs %}
