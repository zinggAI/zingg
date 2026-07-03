---
description: >-
  Run the link phase to match records  across two separate datasets using  your
  trained Zingg model.
---

# Link across Datasets

{% hint style="success" icon="right-long" %}
`Link` and `Match` are two EQUAL operations. Both use the same trained model. Neither is a subset of the other.

* `Link` - match records across two datasets. Each record from the first source is matched with all records from the remaining sources.
* `Match` - find duplicates within one dataset.
{% endhint %}

The link phase is used when you have two datasets that are individually duplicate-free but need to be matched against each other. Common use cases include reference data mastering, data enrichment, and linking records from different source systems.

Link uses the same trained model as match. The output structure is identical to match output, with one additional column: `z_source`, which identifies which source dataset each record came from.

### Output Fields

Link output contains all input fields plus four Zingg-generated columns:

* `Z_CLUSTER` - unique identifier shared by all records resolved as the same entity across both datasets. In Enterprise this is the persistent Zingg ID.
* `Z_MINSCORE` - lowest similarity score within the cluster.
* `Z_MAXSCORE` - highest similarity score within the cluster.
* `Z_SOURCE` - the source dataset each record came from. Use this to trace each resolved record back to its origin system.

{% tabs %}
{% tab title="Community Python" %}
### Python

```python
options = ClientOptions([
    ClientOptions.PHASE,
    "link"
])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

### CLI

```bash
./ zingg.sh-- phase link-- conf config.json
```

{% hint style="info" icon="right-long" %}
The link config file needs two data sources defined. Sample config for link available at:\
`github.com/zinggAI/zingg/blob/main/examples/febrl/configLink.json`
{% endhint %}

### Read and View Output

```python
output = spark.read.csv(
    "/tmp/febrlLinkOutput",
    header = True
)
display(output)
```
{% endtab %}

{% tab title="Enterprise Python" %}
### Python

```python
options = ClientOptions([
    ClientOptions.PHASE,
    "link"
])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

### CLI

```bash
./zingg.sh --phase link --conf config.json
```

### Read and View Output

```python
# Read link output
output = spark.read.csv(
    "/tmp/febrlLinkOutput",
    header=True
)
display(output)
```

{% hint style="info" icon="right-long" %}
Enterprise link output includes Zingg ID instead of `Z_CLUSTER`, plus the `Z_SOURCE` column identifying the source dataset of each record.
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

<summary><strong>When should I use <code>link</code> instead of <code>match</code>?</strong></summary>

Use match when your goal is to find duplicates within a single dataset. For example, identifying that two customer records in the same CRM represent the same person.

Use link when you have two separate datasets that are individually duplicate-free but need to be matched against each other. For example:

* Matching a supplier list from one system against a vendor master from another
* Enriching a customer list with data from a reference dataset
* Linking records from two different source systems before consolidation

Both match and link use the same trained model. They are equal operations, not a hierarchy. You do not need separate training for link.

</details>
