---
description: >-
  Understand how a specific entity cluster was formed, which records matched,
  which did not, and why.
tags:
  - ent
---

# Explain Matches

{% hint style="info" icon="right-long" %}
Enterprise only. The explain phase is not available in Community.
{% endhint %}

Once you have run a match or `runIncremental` phase, you can use the `explainOutput` phase to look under the hood of any cluster. Provide a Zingg ID, and Zingg shows you exactly how that cluster formed, which record pairs were matched probabilistically, their individual scores, and which records did not directly match each other.

This is useful for model validation, governance, explaining results to business stakeholders, human-in-the-loop reviews with domain experts, and building confidence in production pipelines.

{% hint style="success" icon="right-long" %}
`explainOutput` currently covers probabilistic matches only. Deterministic matches are not included\
in explain output at this time. Clusters formed primarily through deterministic matching rules may return empty results.

If you get empty results, verify that the Zingg ID you are querying exists in your match output before running explain.
{% endhint %}

{% hint style="success" icon="right-long" %}
**Read more**:

* For the step-by-step explain phase task with code - [Explain a specific cluster](explain-a-specific-cluster.md)&#x20;
* For output statistics - [Output statistics](output-statistics.md)
{% endhint %}

