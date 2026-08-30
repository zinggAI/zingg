---
description: >-
  Hardware required for different sizes of data. Real-world benchmarks across
  local Spark, AWS, and large clusters to help you size your Zingg deployment.
---

# Hardware Sizing and Benchmarks

Zingg has been built to scale. Performance depends on three factors:

* The number of records to be matched
* The number of fields to be compared against each other
* The actual number of duplicates in your data

Use the benchmarks below as starting points when sizing your Zingg deployment. Your actual runtime will vary based on data complexity, field types, blocking strategy, and the match types in your configuration.

### Benchmarks

Real-world Zingg runs across different dataset sizes and hardware:

| Records | Reported By | Fields | Hardware | Runtime |
|---|---|---|---|---|
| 120k | Zingg Team | febrl test schema | 4 cores, 10 GB RAM, local Spark | 5 minutes |
| 5m (North Carolina Voters) | Zingg Team | voter schema | 4 cores, 10 GB RAM, local Spark | ~4 hours |
| 9m | Zingg Team | 3 fields (first name, last name, email) | AWS m5.24xlarge - 96 cores, 384 GB RAM | 45 minutes |
| 80m | Community User | 8 to 10 fields | 1 driver (128 GB RAM, 32 cores) + 8 workers (224 GB RAM, 64 cores) | Less than 2 hours |

### When to use Spark local mode

For up to a few million records, it may be easier to run Zingg on a single machine in Spark local mode rather than provisioning a cluster. The first two benchmarks above (120k and 5m records) ran in this mode.

The threshold to move from local mode to a cluster is roughly:

* Up to \~5 million records - local mode on a single machine works well
* Above \~5 million records - cluster mode (Databricks, Fabric, EMR, or self-managed Spark) recommended
* Above \~50 million records - multi-worker cluster strongly recommended

### Docker for local development

For local development, proof of concept work, and validating the match workflow before sizing production hardware, the Zingg Docker image runs on any machine with Docker installed. No cluster setup required.

{% hint style="success" icon="right-long" %}
**Read more**:

* [Quick Start](../running-zingg/quick-start-docker.md) - run Zingg locally via Docker
* [Install Zingg](../running-zingg/install-zingg.md) - full installation options across platforms
* [Configure Zingg](../running-zingg/configure-zingg.md) - `numPartitions` and partitioning configuration that affect performance"
{% endhint %}
