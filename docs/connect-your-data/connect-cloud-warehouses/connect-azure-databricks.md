---
description: >-
  Connect Zingg to Azure Databricks using CsvPipe (Community) or ECsvPipe and
  UCPipe (Enterprise). Supports CSV, Parquet, Avro, JSON, Delta, and Unity
  Catalog
---

# Connect Azure Databricks

{% hint style="info" icon="right-long" %}
All standard Spark formats (CSV, Parquet, JSON, Avro) are available in Community. Delta format and Unity Catalog are Enterprise only.
{% endhint %}

### Prerequisites

On Azure Databricks, data sits in cloud storage (Azure Data Lake Storage Gen2) or in Unity Catalog tables. Use the `abfss://` path format to connect Zingg to cloud storage paths, or use `UCPipe` (Enterprise only) for governed Unity Catalog tables. All standard Spark formats — CSV, Parquet, JSON, Avro — are available in both Community and Enterprise; Delta format and Unity Catalog are Enterprise-only. For the full Zingg installation, cluster setup, and any additional dependency configuration required on Databricks clusters, follow the [Azure Databricks Platform Guide](../../platform-guides/platform-guide-for-azure-databricks.md).

### **Python API - Community**

{% hint style="info" icon="right-long" %}
`dbfs:/` paths are deprecated in Databricks. Use Unity Catalog paths for tables or cloud storage paths (`s3a://`, `abfss://)` for files. For Unity Catalog table access, Enterprise uses UCPipe—see below.
{% endhint %}

```python
from zingg.client import*
from zingg.pipes import*

schema = "id string, fname string, \
lname string, stNo string, add1 string, \
add2 string, city string, \
areacode string, state string, \
dob string, ssn string"

inputPipe = CsvPipe("databricksInput", "dbfs:/FileStore/input.csv", schema)
args.setData(inputPipe)

outputPipe = CsvPipe("databricksOutput", "dbfs:/tmp/zinggOutput")
args.setOutput(outputPipe)
```

### Python API - Enterprise

{% hint style="warning" icon="right-long" %}
**Enterprise** uses `ECsvPipe`. Supports all formats including Delta and Unity Catalog.
{% endhint %}

```python
from zinggEC.enterprise.common.epipes import*
from zinggEC.enterprise.common.EArguments import*
```

For CSV or Parquet fields in cloud storage

```python
schema = "rec_id string, fname string, \
lname string, stNo string, add1 string, \
add2 string, city string, \
areacode string, state string, \
dob string, ssn string"

inputPipe = ECsvPipe("databricksInput", "abfss://path/to/input.csv", schema)
args.setData(inputPipe)

outputPipe = ECsvPipe("databricksOutput", "abfss://path/to/output")
outputPipe.setHeader("true")
args.setOutput(outputPipe)
```

For Unity Catalog tables

```python
inputPipe = UCPipe("databricksInput", "catalog.schema.tablename")
args.setData(inputPipe)
```

{% hint style="info" icon="right-long" %}
Enterprise supports Unity Catalog via UCPipe. Replace catalog, schema, and tablename with your Unity Catalog path. UCPipe is recommended for governed Delta tables in Databricks Enterprise deployments.
{% endhint %}

### Supported file formats

#### Parquet

```json
{
  "data" : [ {
    "name" : "parquetInput",
    "format" : "parquet",
    "props" : {
      "path" : "/path/to/input.parquet"
    }
  } ]
}
```

#### Delta format—Enterprise only

```json
{
  "data" : [ {
    "name" : "deltaInput",
    "format" : "delta",
    "props" : {
      "location" : "/path/to/delta/table"
    }
  } ]
}
```

#### Avro

```json
{
  "data" : [ {
    "name" : "avroInput",
    "format" : "avro",
    "props" : {
      "path" : "dbfs:/FileStore/input.avro"
    }
  } ]
}
```
