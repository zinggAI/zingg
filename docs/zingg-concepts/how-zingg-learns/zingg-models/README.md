---
description: >-
  The two models Zingg builds during training - what each one does, why both are
  necessary, and how they work together to make entity resolution scalable and
  accurate.
---

# Zingg Models

When you run the train phase, Zingg builds two separate machine learning models from your labeled pairs. They solve different parts of the entity resolution problem; one handles scale, the other handles accuracy.

Understanding what each model does gives you a clear framework for diagnosing problems, tuning performance, and knowing which part of the pipeline to adjust when results are not what\
you expect.

###





###

{% hint style="success" icon="right-long" %}
* Run [Incremental Matching](../../../running-zingg/run-incremental-matching.md) - how the graph grows over time
{% endhint %}

### Where models are saved

<table><thead><tr><th width="205.12890625" valign="top">Model</th><th valign="top">Path</th></tr></thead><tbody><tr><td valign="top">Blocking model</td><td valign="top"><code>zinggDir/modelId/model/block/</code></td></tr><tr><td valign="top">Similarity model</td><td valign="top"><code>zinggDir/modelId/model/zingg.block</code></td></tr><tr><td valign="top">Both models</td><td valign="top">Written during the train phase. Present before any match, link,<br>or incremental run can proceed.</td></tr></tbody></table>

Use the same `modelId` across all subsequent phases - `match`, `link`, and `runIncremental` to apply both models to your full dataset.

If you retrain with a new model, use `Compare Model Results (diff phase)` in Enterprise to benchmark the new model against the current one before deploying. Zingg shows you exactly which clusters changed, merged, or split between the two models.

{% hint style="success" icon="right-long" %}
**Read more**:

* [Compare Model Results](../../../running-zingg/compare-model-results.md) - benchmark two models before deploying
* [Reassign Zingg ID](../../../running-zingg/reassign-zingg-id.md) - carry existing IDs to a new model
{% endhint %}
