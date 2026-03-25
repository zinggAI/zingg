---
title: ExplainOutput
parent: Step By Step Guide
---

# Explanation of Matches

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

Entity resolution is more effective when you can observe how clusters form, connect, and evolve across runs. Once you have run a `match`  or `runIncremental` phase, you may like to look under the hood to understand how a cluster got formed and which records matched with each other and which did not. This insight helps with the following:

* Model Training and Validation: Quickly check if results match expectations.
* Traceability: See exactly how clusters are formed over time.
* Governance: Explain results to business stakeholders.
* Human-in-the-loop Reviews: Share pair-level evidence with domain experts.
* Operational Confidence: Build trust in production pipelines.

## Important Notes

The `explainOutput` phase currently focuses on **probabilistic matches** between records. Please note the following:

* **Deterministic matches** are not included in the explain output at this time. These matches are based on exact rules and can be reviewed separately through your match results.
* Some clusters may not generate explain output, particularly those formed primarily through deterministic matching rules.
* **Empty results** will occur if the specified `ZINGG_ID` does not exist in the cluster data. Please verify the cluster ID exists in your match output before running explain.
* For comprehensive cluster analysis, we recommend reviewing the primary match output alongside the explain output to get the complete picture of both deterministic and probabilistic relationships.

This focused approach allows the explain phase to efficiently highlight the machine learning model's decision-making process for probabilistic matches, which typically require the most scrutiny and validation.



[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to https://www.zingg.ai/product/zingg-entity-resolution-compare-versions for individual tier features.
