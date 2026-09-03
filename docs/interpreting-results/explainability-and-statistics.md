---
description: >-
  Understand how a specific entity cluster was formed, which records matched,
  which did not, explore the cluster insights.
tags:
  - ent
  - tag: enterprise-only
    primary: true
---

# 📊 Explainability and Statistics

{% hint style="info" icon="right-long" %}
Enterprise only.
{% endhint %}

Once you have run a match or `runIncremental` phase, you can use the `explain` phase to look under the hood of any cluster. Provide a Zingg ID, and Zingg shows you exactly how that cluster formed, which record pairs were matched probabilistically, and which records did not directly match each other.

This is useful for model validation, governance, explaining results to business stakeholders, human-in-the-loop reviews with domain experts, and building confidence in production pipelines.

{% hint style="success" icon="right-long" %}
`explain` currently covers probabilistic matches only. Deterministic matches are not included\
in explain output at this time. Clusters formed primarily through deterministic matching rules may return empty results.

If you get empty results, verify that the Zingg ID you are querying exists in your match output before running explain.
{% endhint %}

Explain answers "why did these specific records end up together?" for one cluster at a time. If you instead want aggregate, run-level visibility , how many clusters formed, how dense they are, how much of the matching came from deterministic rules versus probabilistic scoring, and whether clusters are stable across incremental runs , that's what **output statistics** provides, at three levels: a run-wide **Summary**, per-**Cluster** detail, and per-**Record** detail.

{% hint style="success" icon="right-long" %}
**Read more**:

* For the step-by-step explain phase task with code - [Explain a specific cluster](explain-a-specific-cluster.md)
* For record-level and per-cluster match-quality metrics - [Output statistics](output-statistics.md)
{% endhint %}
