# Zingg Entity Resolution Platform

Zingg is an ML-powered entity resolution engine built to run where your data already lives directly on your warehouse or lakehouse, with no data movement and no rules to write or maintain.

Three capabilities work together to handle the full problem:

<table><thead><tr><th valign="top">Warehouse-native execution</th><th valign="top">Probabilistic + deterministic matching</th><th valign="top">Persistent identity graph</th></tr></thead><tbody><tr><td valign="top">Zingg runs inside Databricks, Microsoft<br>Fabric, Snowflake, GCP Dataproc, AWS Glue, and AWS EMR. Your data never<br>leaves your environment. No ETL pipelines. No external APIs. There is no separate infrastructure to operate. The same model that runs on 100,000 records scales to hundreds of millions using your existing Spark or Snowflake compute, without any architectural changes.</td><td valign="top"><p>Probabilistic matching is Zingg's default and available in the Community Edition. The ML model learns from your labeled pairs; 30 to 50 examples are enough to build a model calibrated<br>to your specific data and scores every candidate pair on multiple field-level features. It handles<br>typos, abbreviations, missing values, and format variations automatically.</p><p>Deterministic matching (Enterprise) adds hard rules for trusted identifiers.<br>When two records share the same SSN, tax ID, or email, Zingg treats them<br>as the same entity without consulting the ML model.<br><br>Both approaches run in a single flow, finding all possible matches.</p><p>→ <a href="deterministic-vs-probabilistic-matching.md">Deterministic vs Probabilistic<br>Matching</a></p></td><td valign="top"><p>Every resolved entity receives a <code>Zingg ID</code>, a globally unique, persistent GUID assigned in Enterprise that remains stable<br>across runs, incremental updates, and model changes.</p><p>Community produces a <code>Z Cluster</code><br>which is unique. Matching records share the same <code>Z_Cluster</code> that is reassigned each run.</p><p><br>Enterprise produces a <code>Zingg ID</code> you can store in downstream systems<br>with confidence.</p><p>The identity graph grows incrementally. New records are matched to existing clusters without rerunning on your full dataset.</p><p>→ <a href="../identity-graph.md">Identity Graph</a><br>→ <a href="../z-cluster-and-zingg-id.md">Z Cluster and Zingg ID</a></p></td></tr></tbody></table>

This is the combination that makes enterprise-scale entity resolution computationally feasible and practically maintainable.

{% hint style="success" icon="right-long" %}
**Read more**:

* [Deterministic vs Probabilistic Matching](deterministic-vs-probabilistic-matching.md)
* [How Zingg Learns](../how-zingg-learns/)
* [Zingg Models](../how-zingg-learns/zingg-models/) (blocking + similarity)
{% endhint %}
