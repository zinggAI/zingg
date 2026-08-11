---
description: >-
  The complete Zingg workflow from setup to match output is your mental model
  for the entire Zingg docs site.
---

# Step-by-Step Guide

Zingg works in phases. Each phase performs a specific task and produces an output that the next phase depends on. You do not need to run all phases every time. Once the model is trained, you run `match`, `link`, or `runIncremental` directly.

The phases below represent the complete workflow from first setup to production matching. This page describes the workflow you follow once Zingg is installed.

{% hint style="success" icon="right-long" %}
This page describes the workflow you follow once Zingg is installed. \
For installing Zingg on your platform → [Install Zingg](install-zingg.md).

New to entity resolution or want to understand the problem space before diving in? → E[ntity Resolution](../zingg-concepts/entity-resolution/)
{% endhint %}

{% stepper %}
{% step %}
### Connect your data

Configure input and output pipes to tell Zingg where your source data lives and where to write results. Zingg connects to the data where it lives - it does not move it.

{% hint style="success" icon="right-long" %}
**Read more**: [Content your Data](https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/connect-your-data)
{% endhint %}
{% endstep %}

{% step %}
### Configure Zingg

Define your field definitions, match types, model ID, partition count, and any optional Enterprise features (deterministic matching, primary key, pass through, standardization). The configuration drives every downstream phase.

{% hint style="success" icon="right-long" %}
**Read more**: [Content your Data](https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/connect-your-data)
{% endhint %}
{% endstep %}

{% step %}
### Find and Label Training Pairs

Run `findTrainingData` to generate candidate pairs, then label each pair as Match, No Match, or Uncertain. Zingg selects the most informative pairs, not random samples. Repeat the find-and-label cycle until all field types and data variation patterns in your schema are represented in your labeled data.

{% hint style="success" icon="right-long" %}
**Read more**:

* [Create Training Data](create-training-data.md)
* [Label Training Pairs](label-training-pairs.md)
{% endhint %}
{% endstep %}

{% step %}
### Verify Blocking

Run `verifyBlocking` to check what percentage of your known matching pairs are being blocked together correctly. Run after labelling and before committing to a full training run. If coverage is low, return to labelling.

{% hint style="success" icon="right-long" %}
**Read more**: [Verify Blocking](verify-blocking.md)
{% endhint %}
{% endstep %}

{% step %}
### Generate Model Documentation

Run `generateDocs` to produce a human-readable HTML report of your training data, including pairs labeled as matches and non-matches. Useful for sharing with subject-matter experts before training.

{% hint style="success" icon="right-long" %}
**Read more**: [Generate Model Documentation](generate-model-documentation.md)
{% endhint %}
{% endstep %}

{% step %}
### Build and Save the Model

Run `train` to build and save the blocking and similarity models from your labelled training data. Models are written to `zinggDir/modelId` and can be reused on new data without retraining.

{% hint style="success" icon="right-long" %}
**Read more**: [Build and Save the Model](build-and-save-the-model.md)
{% endhint %}
{% endstep %}

{% step %}
### Run the Match Phase or Link across Datasets

Apply the trained model to your data:

* Run `match` to find duplicates within a single dataset
* Run `link` to match records across two or more separate datasets

`match` and `link` are two equal operations using the same trained model. As long as your input columns and field types are not changing, you do not need to rebuild the model.

{% hint style="success" icon="right-long" %}
**Read more**:

* [Run the Match Phase](run-the-match-phase.md)
* [Link across Datasets](link-across-datasets.md)
{% endhint %}
{% endstep %}

{% step %}
### Interpret Output Scores

Review output scores to understand match quality. Use `Z_MINSCORE` and `Z_MAXSCORE` to set confidence thresholds for automated vs human-reviewed clusters.

{% hint style="success" icon="right-long" %}
**Read more**: [Interpret Output Scores](../interpreting-results/interpret-output-scores.md)
{% endhint %}
{% endstep %}

{% step %}
### Improve Accuracy (if needed)

If match results need improvement, return to find-and-label with focused training data for the patterns being missed, then retrain. Remove stopwords from fields like addresses and company names to improve blocking. Use custom blocking and similarity for specialized data patterns.

{% hint style="success" icon="right-long" %}
**Read more**: [Improve Accuracy](../tuning/improve-accuracy/)
{% endhint %}
{% endstep %}
{% endstepper %}

{% hint style="warning" icon="right-long" %}
Enterprise: 7 production-grade notebooks, one per phase, with step-isolated execution and\
full Python API support. [Talk to us about Enterprise](https://www.zingg.ai/company/contact/contact)
{% endhint %}

{% hint style="success" icon="right-long" %}
**Read more**:

* Understanding the concepts behind each phase - [Overview section](https://app.gitbook.com/o/kn0G4kXLdlfPagjso48S/s/4FvYw4VaCJcugJzWCiLX/) | [Concepts glossary](../zingg-concepts/concept-glossary.md)
* [Configure Zingg](configure-zingg.md) - full configuration reference
* [Interpret Output Scores](../interpreting-results/interpret-output-scores.md) - every output column and how to read scores
* [Zingg\_ID and Z\_Cluster](../zingg-concepts/z-cluster-and-zingg-id.md) - Community vs Enterprise identifiers in your output
{% endhint %}
