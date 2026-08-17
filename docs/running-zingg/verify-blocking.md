---
description: >-
  Verify that your blocking model is grouping known matching pairs into the same
  block before training and after.
---

# Verify Blocking

The blocking model filters your dataset before any similarity comparisons happen. Records in different blocks are never compared. This is what makes Zingg scalable, but it also means that if the blocking model puts two matching records in different blocks, those records will never be matched no matter how well the similarity model is trained.

`verifyBlocking` tells you what percentage of your known matching pairs are being blocked together correctly. Run this test after configuring Zingg and again after training if you suspect missed matches.

{% hint style="success" icon="right-long" %}
Run `verifyBlocking` in two situations:

1. **Before labeling** - to confirm your blocking is covering expected matches before you invest in training data
2. **After training** - if your match results are missing pairs you know should be there
{% endhint %}

### Run the `verifyBlocking` phase

{% tabs %}
{% tab title="Community" %}
### Python

```python
options = ClientOptions([
    ClientOptions.PHASE,
    "verifyBlocking"
])
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
options = ClientOptions([
    ClientOptions.PHASE,
    "verifyBlocking"
])
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
3. **Try the `WIDER` blocking strategy** (Enterprise only) if large blocks suggest the tree is over-relying on one or two fields → [Blocking Strategies: DEFAULT vs WIDER](../tuning/blocking-strategy.md)

{% hint style="success" icon="right-long" %}
**Consider custom blocking functions** for specialised data patterns → [Custom Blocking and Similarity](../tuning/custom-blocking-and-similarity.md)

**Read more**:

* Blocking model concept and how it fits in the pipeline → [Blocking Model](../zingg-concepts/how-zingg-learns/zingg-models/blocking-model.md)
* Custom blocking functions for advanced tuning → [Custom Blocking and Similarity](../tuning/custom-blocking-and-similarity.md)
{% endhint %}
