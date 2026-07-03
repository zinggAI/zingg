---
description: >-
  Track match quality and volume across three levels: summary, cluster, and
  record; using the Zingg stats output pipe.
tags:
  - ent
---

# Output Statistics

{% hint style="info" icon="right-long" %}
Enterprise only. Output statistics are generated when the stats output pipe is configured in [Configure Zingg](../running-zingg/configure-zingg.md).
{% endhint %}

Zingg Enterprise generates match statistics at three levels alongside the match output. If you have ever asked 'how are deterministic rules performing?' or 'did my latest incremental run improve cluster quality?'; Output Statistics is your answer.

Statistics reveal the connections that Zingg discovered among records within each cluster. During incremental runs, they show exactly how cluster counts change as records are inserted and updated. Specifically, statistics let you

* See how dense or sparse your clusters are
* Understand how much of a cluster is driven by deterministic rules vs. probabilistic matching
* Identify highly central records (connectors) and outliers
* Track how clusters change across runs: growth, splits, merges, reassignments

If the number of clusters changes disproportionately to the number of records updated or added, an alert could be triggered.

{% tabs %}
{% tab title="Enterprise" %}
### **Configure the stats output pipe**

The stats pipe is configured in Configure Zingg alongside your input and output pipes. The `$ZINGG_DYNAMIC_STAT_NAME` placeholder is replaced at runtime with `SUMMARY`, `CLUSTER`, or `RECORD` for the three statistics files.

### Python

```python
statsOutputPipe = ECsvPipe("stats", "/tmp/febrlStats_$ZINGG_DYNAMIC_STAT_NAME")
                      statsOutputPipe.setHeader("true")
                          args.setOutputStats(statsOutputPipe)
```

### JSON

```json
{
  "outputStats" : {
    "name" : "stats", "format" : "csv", "props" : {
      "location" : "/tmp/zinggStats_$ZINGG_DYNAMIC_STAT_NAME",
                   "delimiter" : ",",
                                 "header" : true
    }
  }
}
```

{% hint style="success" icon="right-long" %}
The `$ZINGG_DYNAMIC_STAT_NAME` placeholder is automatically substituted with the statistics type (`SUMMARY`, `CLUSTER`, or `RECORD`) and a timestamp. This ensures each phase run writes to a separate statistics file. If `outputStats` is not configured, Zingg will not write statistics but the run will proceed normally.
{% endhint %}
{% endtab %}

{% tab title="Enterprise Snowflake" %}
**CONTENT FOR THIS SECTION TO BE PROVIDED BY SONAL LATER**
{% endtab %}
{% endtabs %}

### Three statistics levels

{% tabs %}
{% tab title="Summary" %}
Summary statistics give a high-level view of the match run:

* Total records processed
* Total clusters formed
* Total matched pairs
* Average cluster size
* Distribution of match scores

### Summary statistics field names

<table><thead><tr><th width="244.69921875" valign="top">Field</th><th valign="top">Descriptio</th></tr></thead><tbody><tr><td valign="top"><code>z_num_records</code></td><td valign="top">Total number of records processed in this run</td></tr><tr><td valign="top"><code>z_num_clusters</code></td><td valign="top">Total number of clusters formed</td></tr><tr><td valign="top"><code>z_time_stamp</code></td><td valign="top">Timestamp of the match or incremental run</td></tr></tbody></table>

### Reading summary statistics

Summary stats are written to the path configured with `$ZINGG_DYNAMIC_STAT_NAME` substituted with '`SUMMARY`'.

```python
summary_path = (zinggDir + "/" + modelId + "/stats/SUMMARY_*")

    summary = spark.read.csv(summary_path, header = True) summary.show()
```
{% endtab %}

{% tab title="Cluster" %}
Cluster-level statistics give detail per resolved entity:

* Cluster ID (Zingg ID)
* Number of records in the cluster
* Minimum score within the cluster
* Maximum score within the cluster
* Whether the cluster changed since the last incremental run

### Cluster statistics field names

<table><thead><tr><th width="275.1640625" valign="top">Field</th><th valign="top">Description</th></tr></thead><tbody><tr><td valign="top"><code>z_cluster_count</code></td><td valign="top">Number of records in the cluster (cluster size)</td></tr><tr><td valign="top"><code>z_cluster_edges</code></td><td valign="top">Total number of deterministic and probabilistic pairs among all records in the cluster</td></tr><tr><td valign="top"><code>z_cluster_deterministic_edges</code></td><td valign="top">Number of edges explained by deterministic rules</td></tr><tr><td valign="top"><code>z_cluster_centrality</code></td><td valign="top">Edge density: <code>z_cluster_edges / (n*(n-1)/2)</code>. Values near 1 = dense cluster; lower = sparse</td></tr><tr><td valign="top"><code>z_cluster_determinism</code></td><td valign="top">Proportion of connectivity driven by deterministic rules: <code>z_cluster_deterministic_edges / (n*(n-1)/2)</code></td></tr></tbody></table>

### Reading cluster statistics

```python
cluster_path = (zinggDir + "/" + modelId + "/stats/CLUSTER_*")

    cluster_stats = spark.read.csv(cluster_path, header = True)
```

#### **Find large clusters with low density**&#x20;

```python
from pyspark.sql.functions import col cluster_stats
    .filter((col("z_cluster_count") > 5) & (col("z_cluster_centrality") < 0.3))
    .show()
```
{% endtab %}

{% tab title="Record" %}
Record-level statistics give detail per individual record:

* Record ID (your primary key)
* Zingg ID assigned
* Match score for this record within its cluster
* Whether this record is new (added in the latest incremental run) or existing

### Record statistics field names

<table><thead><tr><th width="270.16796875" valign="top">Field</th><th valign="top">Description</th></tr></thead><tbody><tr><td valign="top"><code>z_record_edges</code></td><td valign="top">Total number of other records this record matches (deterministically and probabilistically)</td></tr><tr><td valign="top"><code>z_record_deterministic_edges</code></td><td valign="top">Number of records this record matches deterministically</td></tr><tr><td valign="top"><code>z_cluster_count</code></td><td valign="top">Number of records in the cluster this record belongs to</td></tr><tr><td valign="top"><code>z_record_centrality</code></td><td valign="top">How central this record is within its cluster: <code>z_record_edges / (n-1)</code></td></tr><tr><td valign="top"><code>z_record_determinism</code></td><td valign="top">Proportion of this record's connectivity explained by deterministic rules: <code>z_record_deterministic_edges / (n-1)</code></td></tr></tbody></table>

### Reading record statistics

```python
record_path = (zinggDir + "/" + modelId + "/stats/RECORD_*")

    record_stats = spark.read
                       .csv(record_path, header = True)

#Find connector records(high centrality)
#that may be bridging unrelated clusters
                           from pyspark.sql.functions import col record_stats
                       .filter(col("z_record_centrality") > 0.8)
                       .orderBy("z_record_centrality", ascending = False)
                       .show()
```
{% endtab %}
{% endtabs %}









