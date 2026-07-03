---
description: What a Zingg pipe is and how you configure one to connect your data.
---

# Pipes and Data Connections

A Zingg pipe is a configuration object that tells Zingg where to find your source data and where to write resolved output. It is how Zingg connects to your datastore, whether that is a\
CSV file, a Delta table, a Snowflake table, a Parquet path on S3, or any other Spark-supported connector.

You configure a pipe by giving it a name, a format, and the connection properties for your specific datastore. The pipe is then passed to your Zingg arguments object `args.setData()`.\
for input, `args.setOutput()` for output.

### What every pipe configuration contains

Every pipe, whether input or output, has three attributes.

<table><thead><tr><th width="152.70703125" valign="top">Attribute</th><th valign="top">What it does</th></tr></thead><tbody><tr><td valign="top"><code>name</code></td><td valign="top">A unique label for this pipe. Used internally by Zingg to identify the data source or destination in logs and output.</td></tr><tr><td valign="top"><code>format</code></td><td valign="top">The Spark connector format string for your datastore: csv, parquet, delta, net.snowflake.spark.snowflake, jdbc, avro, and others.</td></tr><tr><td valign="top"><code>props</code> / <code>options</code></td><td valign="top">The connection properties are passed to <code>spark.read</code> and<br><code>spark.write</code> path, delimiter, header, credentials, and any connector-<br>specific settings.</td></tr></tbody></table>

### Configuring a pipe

You can configure pipes in two ways. Both work in Community and Enterprise. Use whichever fits your workflow.

{% tabs %}
{% tab title="Python API" %}
Create pipe objects and attach them to your arguments object.

### **Community (OS)**

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

```python
outputPipe = CsvPipe(
    "resultFebrl",
    "/tmp/febrlOutput")
args.setOutput(outputPipe)
```

### **Enterprise**

```python
from zinggEC.enterprise.common.epipes import* from
    zinggEC.enterprise.common.EArguments import*
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

### **Community (OS)**

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
    "schema" : "id string, fname string, lname string, stNo string, add1 "
               "string, add2 string, city string, state string, areacode "
               "string, dob string, ssn string"
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
    "name" : "stats", "format" : "csv", "props" : {
      "location" : "/tmp/zinggStats_$ZINGG_DYNAMIC_STAT_NAME",
                   "delimiter" : ",",
                                 "header" : true
    }
  }
}
```
{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
`CsvPipe` and the standard `Pipe` class are Community APIs. `ECsvPipe`, `UCPipe`, and the other `E*Pipe` classes are Enterprise APIs that add features like Unity Catalog table access, Lakehouse table support, and stats output. The connection properties and basic pipe operations are identical, only the class name and the import path change.

See [Install Zingg → Snowflake](../running-zingg/install-zingg.md) for the full setup.
{% endhint %}

### What you can connect Zingg to

Zingg connects to any datastore that has a Spark connector, plus Snowflake natively in Enterprise. The pipe format string is all that changes.

<table><thead><tr><th valign="top">Datastore</th><th valign="top">Format string</th><th valign="top">Edition</th></tr></thead><tbody><tr><td valign="top">CSV / TSV</td><td valign="top">csv</td><td valign="top">All</td></tr><tr><td valign="top">Parquet</td><td valign="top">parquet</td><td valign="top">All</td></tr><tr><td valign="top">Avro</td><td valign="top">avro</td><td valign="top">All</td></tr><tr><td valign="top">JSON</td><td valign="top">json</td><td valign="top">All</td></tr><tr><td valign="top">Delta tables (Databricks)</td><td valign="top">delta</td><td valign="top">All</td></tr><tr><td valign="top">Unity Catalog (Databricks)</td><td valign="top">delta</td><td valign="top">All</td></tr><tr><td valign="top">OneLake / Fabric</td><td valign="top"><code>abfss://</code> path + csv or parquet</td><td valign="top">All</td></tr><tr><td valign="top">Snowflake (as data source via Spark)</td><td valign="top"><code>net.snowflake.spark.snowflake</code></td><td valign="top">All</td></tr><tr><td valign="top">Snowflake (native — no Spark cluster)</td><td valign="top">Configured via Snowflake properties file—no pipe class</td><td valign="top">Enterprise</td></tr><tr><td valign="top">AWS S3</td><td valign="top">csv / parquet via <code>s3a://</code></td><td valign="top">All</td></tr><tr><td valign="top">Google Cloud Storage</td><td valign="top">csv / parquet via <code>gs://</code></td><td valign="top">All</td></tr><tr><td valign="top">BigQuery</td><td valign="top"><code>com.google.cloud.spark.bigquery</code></td><td valign="top">All</td></tr><tr><td valign="top">PostgreSQL / MySQL / JDBC</td><td valign="top"><code>jdbc</code></td><td valign="top">All</td></tr><tr><td valign="top">Cassandra</td><td valign="top">CASSANDRA</td><td valign="top">All</td></tr><tr><td valign="top">MongoDB</td><td valign="top">mongo</td><td valign="top">All</td></tr><tr><td valign="top">Neo4j</td><td valign="top"><code>org.neo4j.spark.DataSource</code></td><td valign="top">All</td></tr><tr><td valign="top">Exasol</td><td valign="top"><code>com.exasol.spark</code></td><td valign="top">All</td></tr><tr><td valign="top">Redshift</td><td valign="top"><code>jdbc</code> (redshift driver)</td><td valign="top">All</td></tr></tbody></table>

{% hint style="success" icon="right-long" %}
**Read more**:

* For the full connection config including required JARs, props, and code examples for each datastore - [Connect Data](https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/connect-your-data)
* To configure your pipes step by step as part of your Zingg setup - [Configure Zingg](../running-zingg/configure-zingg.md)
* To understand what Zingg adds to your output alongside your input fields (`Z_CLUSTER`, `Z_MINSCORE`, `Zingg ID` and others) - [Interpret Output Scores](../interpreting-results/interpret-output-scores.md)&#x20;
{% endhint %}
