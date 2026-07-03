---
description: >-
  Configure Zingg to read from and write to cloud object storage including AWS
  S3, Azure Blob Storage, and Google Cloud Storage.
---

# Connect Cloud Storage

{% hint style="success" icon="right-long" %}
New to Zingg pipes? Understand how pipes work before configuring them - [Pipes and data connections](pipes-and-data-connections.md).
{% endhint %}

Zingg can use cloud storage as both a data source and output destination. The JSON config and Python API sections below show how to connect to each storage platform. Available in\
all editions.

{% tabs %}
{% tab title="AWS S3" %}
### Prerequisites

S3 uses the `s3a://` path protocol and requires two Hadoop AWS JARs on the Spark classpath.

Before connecting, complete these steps:

1. Create S3 bucket and folder (e.g. bucket: `zingg28032023`, folder: `zingg`)
2. Export credentials.

```bash
export AWS_ACCESS_KEY_ID = <access key id> export AWS_SECRET_ACCESS_KEY =
    <access key>
```

If MFA enabled:

```bash
export AWS_SESSION_TOKEN = <session token>
```

3. Download Hadoop AWS JARs via Maven and set in `zingg.conf`.

`spark.jars=//hadoop-aws-3.1.0.jar,`\
`//aws-java-sdk-bundle-1.11.271.jar`

### Python API - Community (OS)

```python
from zingg.client import* from zingg.pipes import*

    schema = "field1 string, field2 string"

    inputPipe =
        CsvPipe("s3Input", "s3a://your-bucket/path/to/input.csv", schema)
            args.setData(inputPipe)

                outputPipe =
            CsvPipe("s3Output", "s3a://your-bucket/path/to/output")
                args.setOutput(outputPipe)
```

### Python API - Enterprise

{% hint style="info" icon="right-long" %}
Enterprise uses `ECsvPipe`. Replace import and class name only - the `s3a://` path pattern stays the same.
{% endhint %}

```python
from zinggEC.enterprise.common.epipes import* from
    zinggEC.enterprise.common.EArguments import*

        schema = "field1 string, field2 string"

    inputPipe =
        ECsvPipe("s3Input", "s3a://your-bucket/path/to/input.csv", schema)
            args.setData(inputPipe)

                outputPipe =
            ECsvPipe("s3Output", "s3a://your-bucket/path/to/output")
                outputPipe.setHeader("true") args.setOutput(outputPipe)
```

### JSON Config

```json
{
  "data" : [ {
    "name" : "s3Input",
    "format" : "csv",
    "props" : {
      "location" : "s3a://your-bucket/path/to/input.csv",
      "delimiter" : ",",
      "header" : "true"
    }
  } ],
           "output" : [ {
             "name" : "s3Output",
             "format" : "csv",
             "props" : {"location" : "s3a://your-bucket/path/to/output"}
           } ]
}
```

### Supported file formats

#### Parquet

```json
{
  "data" : [ {
    "name" : "parquetInput",
    "format" : "parquet",
    "props" : {"path" : "s3a://your-bucket/path/to/input.parquet"}
  } ]
}
```

#### Avro

```json
{
  "data" : [ {
    "name" : "avroInput",
    "format" : "avro",
    "props" : {"path" : "s3a://your-bucket/path/to/input.avro"}
  } ]
}
```

#### JSON

```json
{
  "data" : [ {
    "name" : "jsonInput",
    "format" : "json",
    "props" : {"path" : "s3a://your-bucket/path/to/input.json"}
  } ]
}
```

#### CLI commands

```bash
./ scripts / zingg.sh-- phase findTrainingData-- properties -
    file config / zingg.conf-- conf examples / febrl /
        config
            .json-- zinggDir s3a :  // zingg28032023/zingg

            ./
        scripts / zingg.sh-- phase match-- properties -
    file config / zingg.conf-- conf examples / febrl /
        config.json-- zinggDir s3a:  // zingg28032023/zingg
```

{% hint style="success" icon="right-long" %}
Setting `zinggDir` to an S3 path stores all Zingg model files and training data in S3. Models are saved at: `your-bucket/zingg/your-model-id/`
{% endhint %}
{% endtab %}

{% tab title="Azure Blob" %}
_**CHECK WITH SONAL - - NEED TEAMS HELP TO CHECK WHAT EXACTLY TO BE ADDED HERE**_
{% endtab %}

{% tab title="GCS" %}
On Google Cloud Storage, data lives in GCS buckets accessed via the `gs://` path format. Use `gs://<bucket-name>/<path-to-file>` to connect Zingg to your bucket. All formats — CSV, Parquet, JSON, Avro, Delta — are available in both Community and Enterprise.

{% hint style="success" icon="right-long" %}
**Read more**: For the full Zingg installation, Dataproc cluster setup, and any additional dependency configuration required when running Zingg on Google Cloud, follow the [GCP Dataproc Platform Guide](../platform-guides/platform-guide-for-gcp-dataproc.md).
{% endhint %}

### **Python API - Community (OS)**

```python
from zingg.client import* from zingg.pipes import*

    schema =
    ("id string, fname string, "
     "lname string, stNo string, "
     "add1 string, add2 string, "
     "city string, state string, "
     "areacode string, dob string, "
     "ssn string")

        inputPipe = CsvPipe("testFebrl", "gs://your-bucket/test.csv", schema)
                        args.setData(inputPipe)

                            outputPipe =
            CsvPipe("resultFebrl", "gs://your-bucket/Output")
                args.setOutput(outputPipe)
```

### **Python API - Enterprise**

```python
from zinggEC.enterprise.common.epipes import* from
    zinggEC.enterprise.common.EArguments import*

        schema =
    ("id string, fname string, "
     "lname string, stNo string, "
     "add1 string, add2 string, "
     "city string, state string, "
     "areacode string, dob string, "
     "ssn string")

        inputPipe = ECsvPipe("testFebrl", "gs://your-bucket/test.csv", schema)
                        args.setData(inputPipe)

                            outputPipe =
            ECsvPipe("resultFebrl", "gs://your-bucket/Output")
                outputPipe.setHeader("true") args.setOutput(outputPipe)
```

### **JSON Config**

```json
{
  "data" : [ {
    "name" : "testFebrl",
    "format" : "csv",
    "props" : {
      "location" : "gs://your-bucket/test.csv",
      "delimiter" : ",",
      "header" : "false"
    },
    "schema" : "id string, fname string, lname string, stNo string, add1 "
               "string, add2 string, city string, state string, areacode "
               "string, dob string, ssn string"
  } ],
           "output" : [ {
             "name" : "resultFebrl",
             "format" : "csv",
             "props" : {
               "location" : "gs://your-bucket/Output",
               "delimiter" : ",",
               "header" : "true"
             }
           } ]
}
```

### **Setting a Spark checkpoint on GCS**

For long-running Zingg jobs on Spark (especially on Dataproc), set a Spark checkpoint directory in GCS so Spark can recover state and manage memory during the entity resolution process:

```python
checkpoint_path =
    "gs://your-bucket/zingg_checkpoint" spark.sparkContext.setCheckpointDir(
        checkpoint_path)
```

The checkpoint directory is a Spark feature, not a Zingg-specific configuration. It provides Spark with persistent storage for intermediate computation metadata during multi-stage jobs. Recommended for any large-dataset Zingg run on Dataproc.

### **Supported file formats on GCS**

GCS supports all standard Spark formats. Replace `format` and the path suffix in the props.

#### **Parquet**

```json
{
  "data" : [ {
    "name" : "parquetInput",
    "format" : "parquet",
    "props" : {"path" : "gs://your-bucket/input.parquet"}
  } ]
}
```

#### **JSON**

```json
{
  "data" : [ {
    "name" : "jsonInput",
    "format" : "json",
    "props" : {"path" : "gs://your-bucket/input.json"}
  } ]
}
```

#### **Avro**

```json
{
  "data" : [ {
    "name" : "avroInput",
    "format" : "avro",
    "props" : {"path" : "gs://your-bucket/input.avro"}
  } ]
}
```

{% hint style="info" icon="right-long" %}
For Delta format on GCS, see the Azure Databricks tab — Delta is Enterprise only and works the same way with the path schema swapped to `gs://`.
{% endhint %}
{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
**Read more**:

* For cloud warehouses - [Connect Cloud Warehouses](connect-cloud-warehouses/)
* For flat files - [Connect File Formats](connect-file-formats.md)
{% endhint %}
