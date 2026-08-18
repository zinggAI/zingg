---
description: Where Zingg runs, which data it resolves
---

# Platform Infrastructure vs Data Platform

Two things that are easy to conflate but mean different things in Zingg's context. Platform infrastructure is where computation happens, such as the Databricks/Fabric/EMR/Glue/Dataproc  Spark service or Snowflake compute that processes your data. A data platform is where your data lives - Snowflake tables, Databricks Lakehouse and Unity Catalog, OneLake, BigQuery, Redshift, or a file store like S3.

These are independent: you can have your data in Snowflake but run Zingg's computation on Spark. Zingg connects to the data platform and runs computations on whichever engine you configure.

{% hint style="success" icon="right-long" %}
**Read more:** [Connect data](../connect-your-data/pipes-and-data-connections.md) | [Install Zingg](../running-zingg/install-zingg.md)
{% endhint %}

