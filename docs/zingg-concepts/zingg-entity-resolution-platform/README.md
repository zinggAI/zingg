# Zingg Entity Resolution Platform

Zingg is an ML-powered entity resolution engine built to run where your data already lives directly on your warehouse or lakehouse, with no data movement and no rules to write or maintain.

Three capabilities work together to handle the full problem:

| Warehouse-native execution | Probabilistic + deterministic matching | Persistent identity graph |
|---|---|---|
| Zingg runs inside Databricks, Microsoft Fabric, Snowflake, GCP Dataproc, AWS Glue, and AWS EMR. Your data never leaves your environment. No ETL pipelines. No external APIs. There is no separate infrastructure to operate. The same model that runs on 100,000 records scales to hundreds of millions using your existing Spark or Snowflake compute, without any architectural changes. | Probabilistic matching is Zingg's default and available in the Community Edition. The ML model learns from your labeled pairs; 30 to 50 examples are enough to build a model calibrated to your specific data and scores every candidate pair on multiple field-level features. It handles typos, abbreviations, missing values, and format variations automatically. Deterministic matching (Enterprise) adds hard rules for trusted identifiers. When two records share the same SSN, tax ID, or email, Zingg treats them as the same entity without consulting the ML model. Both approaches run in a single flow, finding all possible matches. → [Deterministic vs Probabilistic Matching](deterministic-vs-probabilistic-matching.md) | Every resolved entity receives a `Zingg ID`, a globally unique, persistent GUID assigned in Enterprise that remains stable across runs, incremental updates, and model changes. Community produces a `Z Cluster` which is unique. Matching records share the same `Z_Cluster` that is reassigned each run. Enterprise produces a `Zingg ID` you can store in downstream systems with confidence. The identity graph grows incrementally. New records are matched to existing clusters without rerunning on your full dataset. → [Identity Graph](../identity-graph.md) → [Z Cluster and Zingg ID](../z-cluster-and-zingg-id.md) |

This is the combination that makes enterprise-scale entity resolution computationally feasible and practically maintainable.

{% hint style="success" icon="right-long" %}
**Read more**:

* [Deterministic vs Probabilistic Matching](deterministic-vs-probabilistic-matching.md)
* [How Zingg Learns](../how-zingg-learns/)
* [Zingg Models](../how-zingg-learns/zingg-models/) (blocking + similarity)
{% endhint %}
