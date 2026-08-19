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

Models are saved within the customer environment at a location of their choice.&#x20;



{% hint style="success" icon="right-long" %}
* [Compare Model Results](../../../running-zingg/compare-model-results.md) - benchmark two models before deploying
* [Reassign Zingg ID](../../../running-zingg/reassign-zingg-id.md) - carry existing IDs to a new model
{% endhint %}
