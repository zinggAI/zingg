---
description: >-
  Generate readable documentation of your labeled training data to inspect
  quality before committing to the train phase.
---

# Generate Model Documentation

{% hint style="success" icon="right-long" %}
Run this BETWEEN `label` and `train`. Do not skip - it lets you inspect training data quality before the model is built.
{% endhint %}

The `generateDocs` phase produces readable documentation of the records you have marked during the label phase, including both matches and non-matches. Use this to review training data quality, understand what Zingg has learned, and share results with subject matter experts before training begins.

The documentation is written to the `zinggDir/modelId` folder and can be viewed in a browser.

### Run generateDocs

{% tabs %}
{% tab title="Community" %}
### **Python**&#x20;

```python
options = ClientOptions([ ClientOptions.PHASE, "generateDocs" ]) zingg =
    Zingg(args, options) zingg.initAndExecute()
```

### **CLI**&#x20;

```bash
./ scripts /
    zingg.sh-- phase generateDocs-- conf<location to conf.json> --showConcise =
    true
```
{% endtab %}

{% tab title="Enterprise" %}
### **Python**

{% hint style="info" icon="right-long" %}
Enterprise uses `EZingg` instead of `Zingg`.
{% endhint %}

```python
options = ClientOptions([ ClientOptions.PHASE, "generateDocs" ]) zingg =
    EZingg(args, options) zingg.initAndExecute()
```

### CLI

```bash
./ scripts /
    zingg.sh-- phase generateDocs-- conf<location to conf.json> --showConcise =
    true
```
{% endtab %}

{% tab title="Enterprise Snowflake" %}
**CHECK WITH SONAL ABOUT THIS TOPIC - NEEDS ENTIRELY DIFFERENT SET OF CONTENT TO BE DISCUSSED LATER.**
{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
The generated documentation is viewable in a browser. It shows all pairs you labelled as matches and non-matches in a readable format. Share this with subject matter experts to validate training data quality before running `train`.

[Build and Save the Model ](build-and-save-the-model.md)→ run `train` once you are satisfied with the training data quality.
{% endhint %}

