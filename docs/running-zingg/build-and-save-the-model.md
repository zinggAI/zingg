---
description: >-
  Run the training phase to build Zingg's matching model from your labeled
  training data and save it for reuse.
---

# Build and Save the Model

The training phase builds up the Zingg models using the training data from your label sessions and writes them to `zinggDir/modelId` as specified in your config.

Once saved, reuse the same `modelId` in all subsequent phases - `match`, `link`, and `runIncremental` to apply this trained model to your data.

{% hint style="success" icon="right-long" %}
Model saved to: `zinggDir/modelId`

Use the same `modelId` when running `match`, `link`, or `runIncremental` to apply this trained model.
{% endhint %}

{% tabs %}
{% tab title="Community" %}
### Python

```python
options = ClientOptions([
    ClientOptions.PHASE,
    "train"
])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

### CLI

```bash
./ zingg.sh-- phase train-- conf config.json
```
{% endtab %}

{% tab title="Enterprise" %}
### Python

```python
options = ClientOptions([
    ClientOptions.PHASE,
    "train"
])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

### CLI

```bash
./zingg.sh --phase train --conf config.json
```

Enterprise adds blocking model configuration via `args.setBlockingModel()`. Set this in Configure Zingg before running `train`.

Available options:

* `DEFAULT` - standard blocking strategy, suitable for most datasets
* `WIDER` - casts a wider blocking net; use if known matching pairs are being missed
{% endtab %}

{% tab title="Enterprise Snowflake" %}
{% hint style="info" icon="right-long" %}
Enterprise only. Zingg on Snowflake uses Snowpark and does not require a Spark cluster.
{% endhint %}

_**CHECK WITH SONAL ABOUT THE ENTIRE SNOWFLAKE RELATED CONTENT**_
{% endtab %}
{% endtabs %}
