---
description: >-
  Zingg runtime properties file (zingg.conf) reference - Spark config, JAR
  paths, and  connector-specific settings.
---

# Runtime Properties

The `zingg.conf` file sets Spark configuration properties that Zingg passes to spark-submit at runtime. It lives in the `config/` directory of your Zingg installation and is referenced via the `properties-file` flag.

### Core Spark properties

```bash
spark.master=local[*]
spark.executor.memory=4g
spark.driver.memory=4g
spark.sql.shuffle.partitions=200
```

<table><thead><tr><th width="309.85546875" valign="top">Property</th><th width="117.21484375" valign="top">Default</th><th valign="top">Description</th></tr></thead><tbody><tr><td valign="top"><code>spark.master</code></td><td valign="top"><code>local[*]</code></td><td valign="top">Spark master URL. Use <code>local[*]</code> for local development (all cores). For cluster mode, set to your cluster manager URL. On managed platforms (Databricks, Fabric, EMR) this is managed automatically; do not override.</td></tr><tr><td valign="top"><code>spark.executor.memory</code></td><td valign="top"><code>4g</code></td><td valign="top">Memory allocated per executor. Increase if you see OOM errors during training or matching. For large datasets (1M+ records) try <code>8g</code> or <code>16g</code>.</td></tr><tr><td valign="top"><code>spark.driver.memory</code></td><td valign="top"><code>4g</code></td><td valign="top">Memory allocated to the Spark driver. Usually matches or is slightly lower than <code>spark.executor.memory</code>.</td></tr><tr><td valign="top"><code>spark.sql.shuffle.partitions</code></td><td valign="top"><code>200</code></td><td valign="top">Number of partitions used during shuffle operations. Increase for very large datasets during the <code>match</code> phase. A common value for large datasets is <code>400</code>–<code>800</code>.</td></tr></tbody></table>

### Adding JARs for connectors

Separate multiple JARs with commas.

#### Snowflake

```bash
spark.jars=snowflake-jdbc-3.13.19.jar,spark-snowflake_2.12-2.10.0-spark_3.1.jar
```

#### BigQuery

```bash
spark.jars=spark-bigquery-with-dependencies_2.12-0.24.2.jar,gcs-connector-hadoop2-latest.jar
```

#### BigQuery Hadoop Property

```bash
spark.hadoop.fs.gs.impl=com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem
```

#### AWS S3

```bash
spark.jars=hadoop-aws-3.1.0.jar,aws-java-sdk-bundle-1.11.271.jar
```

#### JDBC (replace with your driver)

```bash
spark.jars=postgresql-42.3.1.jar
```

{% hint style="danger" icon="right-long" %}
JAR versions above are illustrative. Check the Zingg GitHub releases page for the verified versions tested with each Zingg release: `github.com/zinggAI/zingg/releases` .
{% endhint %}

### GCP Dataproc Properties

For GCP Dataproc, inject JARs via cluster creation properties.

**Key**: spark.jars

**Value**:

* `gs://BUCKET/zingg-0.6.0.jar`
* `gs://BUCKET/spark-3.5-bigquery-0.44.1.jar`
* `gs://BUCKET/gcs-connector-hadoop3-latest.jar`

On GCP Dataproc, JARs are injected at cluster creation time via cluster properties rather than in `zingg.conf`. The `spark.jars` key in the cluster properties accepts a comma-separated GCS path list.

#### Enterprise Snowflake Properties

The `zingg.conf` equivalent is the `snowEnv.txt` properties file. It sets Snowflake connection properties rather than Spark properties.

```bash
URL={snowflake_url}
USER={snowflake_user_name}
PASSWORD={snowflake_password}
ROLE={role}
WAREHOUSE={warehouse}
DB={database_name}
SCHEMA={schema}
CLIENT_SESSION_KEEP_ALIVE_HEARTBEAT_FREQUENCY=900
```

Pass this file via `--properties-file` when running any Zingg phase on Snowflake.

```bash
./scripts/zingg.sh --phase findTrainingData --conf config.json --properties-file snowEnv.txt
```

`CLIENT_SESSION_KEEP_ALIVE_HEARTBEAT_FREQUENCY`: seconds between client attempts to update the session token. Valid range: `900` to `3600`.

{% hint style="success" icon="right-long" %}
**Read more**:

* For connector-specific JAR setup per platform - [Connect Data](https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/connect-your-data)
* For platform-specific runtime config - [Run on Cloud](https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/platform-guides)
{% endhint %}
