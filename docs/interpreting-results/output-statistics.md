---
description: >-
  Track match quality and volume across three levels: summary, cluster, and
  record; using the Zingg stats output pipe.
tags:
  - ent
  - tag: enterprise-only
    primary: true
---

# 📊 Output Statistics

{% hint style="info" icon="building" %}
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

#### Python

```python
statsOutputPipe = ECsvPipe("stats", "/tmp/febrlStats_$ZINGG_DYNAMIC_STAT_NAME")
statsOutputPipe.setHeader("true")
args.setOutputStats(statsOutputPipe)
```

#### JSON

```json
{
  "outputStats" : {
    "name" : "stats",
    "format" : "csv",
    "props" : {
      "location" : "/tmp/zinggStats_$ZINGG_DYNAMIC_STAT_NAME",
      "delimiter" : ",",
      "header" : true
    }
  }
}
```

{% hint style="success" icon="circle-info" %}
The `$ZINGG_DYNAMIC_STAT_NAME` placeholder is automatically substituted with the statistics type (`SUMMARY`, `CLUSTER`, or `RECORD`) and a timestamp. This ensures each phase run writes to a separate statistics file. If `outputStats` is not configured, Zingg will not write statistics but the run will proceed normally.
{% endhint %}
{% endtab %}

{% tab title="Enterprise Snowflake" %}
{% hint style="info" %}
**Coming soon** — Enterprise Snowflake guidance for output statistics is in progress and will be published here when available.
{% endhint %}
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

| Field | Description |
|-------|-------------|
| `z_num_records` | Total number of records processed in this run |
| `z_num_clusters` | Total number of clusters formed |
| `z_time_stamp` | Timestamp of the match or incremental run |

### Reading summary statistics

Summary stats are written to the path configured with `$ZINGG_DYNAMIC_STAT_NAME` substituted with '`SUMMARY`'.

```python
summary_path = (zinggDir + "/" + modelId + "/stats/SUMMARY_*")

summary = spark.read.csv(summary_path, header=True)
summary.show()
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

| Field | Description |
|-------|-------------|
| `z_cluster_count` | Number of records in the cluster (cluster size) |
| `z_cluster_edges` | Total number of deterministic and probabilistic pairs among all records in the cluster |
| `z_cluster_deterministic_edges` | Number of edges explained by deterministic rules |
| `z_cluster_centrality` | Edge density: `z_cluster_edges / (n*(n-1)/2)`. Values near 1 = dense cluster; lower = sparse |
| `z_cluster_determinism` | Proportion of connectivity driven by deterministic rules: `z_cluster_deterministic_edges / (n*(n-1)/2)` |

### Reading cluster statistics

```python
cluster_path = (zinggDir + "/" + modelId + "/stats/CLUSTER_*")

cluster_stats = spark.read.csv(cluster_path, header=True)
```

#### **Find large clusters with low density**

```python
from pyspark.sql.functions import col

cluster_stats \
  .filter((col("z_cluster_count") > 5) & (col("z_cluster_centrality") < 0.3)) \
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

| Field | Description |
|-------|-------------|
| `z_record_edges` | Total number of other records this record matches (deterministically and probabilistically) |
| `z_record_deterministic_edges` | Number of records this record matches deterministically |
| `z_cluster_count` | Number of records in the cluster this record belongs to |
| `z_record_centrality` | How central this record is within its cluster: `z_record_edges / (n-1)` |
| `z_record_determinism` | Proportion of this record's connectivity explained by deterministic rules: `z_record_deterministic_edges / (n-1)` |

### Reading record statistics

```python
record_path = (zinggDir + "/" + modelId + "/stats/RECORD_*")

record_stats = spark.read.csv(record_path, header=True)

#Find connector records(high centrality)
#that may be bridging unrelated clusters
from pyspark.sql.functions import col

record_stats \
  .filter(col("z_record_centrality") > 0.8) \
  .orderBy("z_record_centrality", ascending=False) \
  .show()
```
{% endtab %}
{% endtabs %}
