---
description: >-
  Compare the accuracy of two trained Zingg models before deploying to
  production.
tags:
  - ent
  - tag: enterprise-only
    primary: true
---

# Compare Model Results

{% hint style="info" icon="right-long" %}
**Enterprise** **Plus** only. Comparing model results is available in Zingg Enterprise Plus (ZinggES).\
Not available in Community, Enterprise Lite, or Enterprise.
{% endhint %}

When you build a new model or update an existing one, you need to know whether the new model actually performs better than the old one before switching production workloads. Comparing model results lets you run both models on the same dataset and evaluate their outputs side by side.

This is part of the Zingg model workflow: build → tweak → run → interpret → compare → save. Comparing model results is the step between interpreting and saving - it gives you confidence that the model you are saving is better than the one it is replacing.

### When to use Compare model results

Use this when you have:

Retrained an existing model with more labeled data and want to confirm the updated model outperforms the original.

* Changed field match types; for example, switching a field from `FUZZY` to `EXACT` or adding a new field and want to validate the impact before promoting the change.
* Run an A/B test between two modeling strategies and need a quantitative basis for choosing one over the other.
* Changed blocking strategy settings and want to measure the effect on cluster quality before committing to production

{% hint style="success" icon="right-long" %}
**Read more:**

* Model workflow context - [Run the match phase](run-the-match-phase.md)
* Reassign Zingg ID when promoting a new model - [Reassign Zingg ID](reassign-zingg-id.md)
{% endhint %}

#### How Compare Model Results works

Compare model results uses the `diff` phase to analyze both model outputs and identify exactly which records and clusters were affected by your changes.

The `diff` phase compares two configurations:

* **Original configuration:** your baseline model (e.g., with `FUZZY` matching)
* **New configuration:** your updated model (e.g., with `EXACT` matching or new deterministic rules)

The `diff` phase then identifies:

* Records that moved between clusters due to model changes
* New clusters created in the updated model
* Merged or split clusters between the two models

{% tabs %}
{% tab title="Enterprise" %}
### Python

```python
from zingg.client import*
from zingg.pipes import*
from zinggEC.enterprise.common.EArguments import*
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggES.enterprise.spark.ESparkClient import*
from zinggEC.enterprise.common.TransformedOutputArguments import*
from zinggEC.enterprise.common.EClientOptions import*
```

#### Step 1: Define ORIGINAL baseline config

```python
originalArgs = EArguments()
#... your original field definitions...
originalArgs.setModelId("100")
originalArgs.setZinggDir("./models")
```

#### Step 2: Define NEW/UPDATED Config

```python
newArgs = EArguments()
#... your updated field definitions...
newArgs.setModelId("200")
newArgs.setZinggDir("./models")
```

#### Step 3: Create TransformedOutputArguments

```python
diffArgs = TransformedOutputArguments()
diffArgs.setParentArgs(newArgs)
diffArgs.setOriginalArgs(originalArgs)

diffOutputPipe = ECsvPipe("diffOutput", "/tmp/zinggDiff")
diffOutputPipe.setHeader("true")
diffArgs.setTransformedOutputPath(diffOutputPipe)
```

#### Step 4: Execute diff

```python
diffOptions = EClientOptions([
    EClientOptions.PHASE,
    "diff"
])
zinggDiff = EZingg(diffArgs, diffOptions)
zinggDiff.initAndExecute()
```

### CLI

```bash
./scripts/zingg.sh \
  --phase diff \
  --conf examples/febrl/sparkIncremental/configdiff.json \
  --compareTo examples/febrl/configBaseline.json \
  --properties-file config/zingg.conf
```
{% endtab %}

{% tab title="Enterprise for Snowfalke" %}
_**CHECK WITH SONAL - Enterprise Snowflake content for this topic to be provivded by Sonal**_
{% endtab %}
{% endtabs %}

### Understand why use Diff with use cases

* **Field type changes:** When you switch a field from `FUZZY` to `EXACT` or add a new field, run `diff` before deploying. See exactly which clusters changed and decide whether the new model behaves better for your data.
* **Validating model improvements:** After retraining with more labeled data, use `diff` to confirm the updated model outperforms the original. Don't assume more labels always improve results—verify.
* **A/B testing modeling strategies:** Run two different configurations on the same dataset and compare their outputs with `diff`. Choose the strategy with the better cluster quality based on evidence, not intuition.
* **Blocking strategy changes:** Changed `setBlockingModel()` from `DEFAULT` to `WIDER`? Run `diff` to measure the effect on cluster quality before committing to production.
* **Regression detection:** Ensure that changes made to improve one part of the model have not degraded match quality on other parts of your data.
* **Cluster evolution tracking:** Understand how clusters merge, split, or change as your model evolves. Especially useful when your input data has changed significantly between model versions.
