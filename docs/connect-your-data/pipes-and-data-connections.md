---
description: What a Zingg pipe is and how you configure one to connect your data.
---

# ☁️ Pipes and Data Connections

A Zingg pipe is a configuration object that tells Zingg where to find your source data and where to write resolved output. It is how Zingg connects to your datastore, whether that is a\
CSV file, a Delta table, a Snowflake table, a Parquet path on S3, or any other Spark-supported connector.

You configure a pipe by giving it a name, a format, and the connection properties for your specific datastore. The pipe is then passed to your Zingg arguments object `args.setData()`.\
for input, `args.setOutput()` for output.

### What every pipe configuration contains

Every pipe, whether input or output, has three attributes.

| Attribute | What it does |
|---|---|
| `name` | A unique label for this pipe. Used internally by Zingg to identify the data source or destination in logs and output. |
| `format` | The Spark connector format string for your datastore: csv, parquet, delta, net.snowflake.spark.snowflake, jdbc, avro, and others. |
| `props` / `options` | The connection properties are passed to `spark.read` and `spark.write` path, delimiter, header, credentials, and any connector- specific settings. |

### Configuring a pipe

You can configure pipes in two ways. Both work in Community and Enterprise. Use whichever fits your workflow.

{% tabs %}
{% tab title="Community" %}
Create pipe objects and attach them to your arguments object.

### **Community**

```python
from zingg.client import *
from zingg.pipes import *
```

#### **Input Pipe**

```python
inputPipe = CsvPipe(
    "testFebrl",
    "examples/febrl/test.csv",
    schema)
args.setData(inputPipe)
```

#### **Output Pipe**

<pre class="language-python"><code class="lang-python">outputPipe = CsvPipe(
<strong>    "resultFebrl",
</strong>    "/tmp/febrlOutput")
args.setOutput(outputPipe)
</code></pre>
{% endtab %}

{% tab title="Enterprise" %}


### **Enterprise**

```python
from zinggEC.enterprise.common.epipes import *
from zinggEC.enterprise.common.EArguments import *
```

#### **Input Pipe**

```python
inputPipe = ECsvPipe("testFebrl", "examples/febrl/test.csv", schema)
args.setData(inputPipe)
```

#### **Output Pipe**

```python
outputPipe = ECsvPipe("resultFebrl", "/tmp/febrlOutput")
outputPipe.addProperty("header", "true")
args.setOutput(outputPipe)
```
{% endtab %}

{% tab title="JSON config" %}
Define pipes in the data and output sections of your JSON config file.

### **Community**

```json
{
  "data" : [ {
    "name" : "testFebrl",
    "format" : "csv",
    "props" : {
      "location" : "examples/febrl/test.csv",
      "delimiter" : ",",
      "header" : "false"
    }
  } ],
  "output" : [ {
    "name" : "resultFebrl",
    "format" : "csv",
    "props" : {
      "location" : "/tmp/febrlOutput",
      "delimiter" : ",",
      "header" : "true"
    }
  } ]
}
```

### **Enterprise**

```json
{
  "data" : [ {
    "name" : "testFebrl",
    "format" : "csv",
    "props" : {
      "location" : "examples/febrl/test.csv",
      "delimiter" : ",",
      "header" : "false"
    },
    "schema" : "id string, fname string, lname string, stNo string, add1 string, add2 string, city string, state string, areacode string, dob string, ssn string"
  } ],
  "output" : [ {
    "name" : "OUTPUT_FEBRL",
    "format" : "csv",
    "props" : {
      "location" : "/tmp/zinggOutputNew/",
      "delimiter" : ",",
      "header" : true
    }
  } ],
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
{% endtab %}
{% endtabs %}

{% hint style="info" icon="circle-info" %}
`CsvPipe` and the standard `Pipe` class are Community APIs. `ECsvPipe`, `UCPipe`, and the other `E*Pipe` classes are Enterprise APIs that add features like Unity Catalog table access, Lakehouse table support, and stats output. The connection properties and basic pipe operations are identical, only the class name and the import path change.

See [Install Zingg → Snowflake](../running-zingg/install-zingg.md) for the full setup.
{% endhint %}

### What you can connect Zingg to

Zingg connects to any datastore that has a Spark connector, plus Snowflake natively in Enterprise. The pipe format string is all that changes.

| Datastore | Format string | Edition |
|---|---|---|
| CSV / TSV | csv | All |
| Parquet | parquet | All |
| Avro | avro | All |
| JSON | json | All |
| Delta tables (Databricks) | delta | All |
| Unity Catalog (Databricks) | delta | All |
| OneLake / Fabric | `abfss://` path + csv or parquet | All |
| Snowflake (as data source via Spark) | `net.snowflake.spark.snowflake` | All |
| Snowflake (native — no Spark cluster) | Configured via Snowflake properties file—no pipe class | Enterprise |
| AWS S3 | csv / parquet via `s3a://` | All |
| Google Cloud Storage | csv / parquet via `gs://` | All |
| BigQuery | `com.google.cloud.spark.bigquery` | All |
| PostgreSQL / MySQL / JDBC | `jdbc` | All |
| Cassandra | `org.apache.spark.sql.cassandra` | All |
| MongoDB | mongo | All |
| Neo4j | `org.neo4j.spark.DataSource` | All |
| Exasol | `com.exasol.spark` | All |
| Redshift | `jdbc` (redshift driver) | All |

{% hint style="success" icon="book-open" %}
**Read more**:

* For the full connection config including required JARs, props, and code examples for each datastore - [Cloud Warehouses](connect-cloud-warehouses/) | [Cloud Storage](connect-cloud-storage.md) | [File Formats](connect-file-formats.md) | [Relational Databases](connect-relational-databases.md) | [NoSQL Databases](connect-nosql-databases.md) | [Neo4j](connect-graph-databases-neo4j.md)
* To configure your pipes step by step as part of your Zingg setup - [Configure Zingg](../running-zingg/configure-zingg.md)
* To understand what Zingg adds to your output alongside your input fields (`Z_CLUSTER`, `Z_MINSCORE`, `Zingg ID` and others) - [Interpret Output Scores](../interpreting-results/interpret-output-scores.md)
{% endhint %}

