---
description: >-
  Configure Zingg pipes for CSV, Parquet,  Avro, JSON, TSV, and XML flat file
  formats.
---

# Connect File Formats

{% hint style="success" icon="right-long" %}
New to Zingg pipes? Understand how pipes work before configuring them - [Pipes and data connections](pipes-and-data-connections.md).
{% endhint %}

This page covers standalone flat file connections - reading and writing files directly from a local path or mounted file system.

For files stored in cloud platforms, use the format sections within each platform tab.

* Files on Databricks or Fabric - Connect cloud warehouses
* Files on S3 - Connect cloud storage

{% hint style="success" icon="right-long" %}
All file formats shown here are available in Community and Enterprise.

The only edition difference is the pipe class in the Python API:

* `CsvPipe` for Community
* `ECsvPipe` for Enterprise
{% endhint %}

{% tabs %}
{% tab title="CSV" %}
### Python API - Community

```python
from zingg.client import*
from zingg.pipes import*

schema = "field1 string, \
field2 string, field3 string"

inputPipe = CsvPipe("csvInput", "/path/to/input.csv", schema)
args.setData(inputPipe)

outputPipe = CsvPipe("csvOutput", "/path/to/output")
args.setOutput(outputPipe)
```

### Python API - Enterprise

{% hint style="info" icon="right-long" %}
Enterprise uses ECsvPipe. The pipe setup is identical - only the class `name` and `import` changes.
{% endhint %}

```python
from zinggEC.enterprise.common.epipes import*
from zinggEC.enterprise.common.EArguments import*

schema = "field1 string, \
field2 string, field3 string"

inputPipe = ECsvPipe("csvInput", "/path/to/input.csv", schema)
args.setData(inputPipe)

outputPipe = ECsvPipe("csvOutput", "/path/to/output")
outputPipe.setHeader("true")
args.setOutput(outputPipe)
```

### JSON Config

```json
{
  "data" : [ {
    "name" : "csvInput",
    "format" : "csv",
    "props" : {
      "location" : "/path/to/input.csv",
      "delimiter" : ",",
      "header" : "true"
    },
    "schema" : "field1 string,
    field2 string,
    field3 string "
  } ],
  "output" : [ {
    "name" : "csvOutput",
    "format" : "csv",
    "props" : {"location" : "/path/to/output", "header" : "true"}
  } ]
}
```

{% hint style="success" icon="right-long" %}
If your CSV has no header row, set `header`: `false` and define the schema field explicitly.

Schema uses Spark SQL types: string, integer, double, date, timestamp.
{% endhint %}
{% endtab %}

{% tab title="Parquet" %}
### Python API

```python
from zingg.client import*
from zingg.pipes import*

parquetPipe = Pipe("parquetFiles", "parquet")
parquetPipe.addProperty("path", "/home/zingg")
args.setData(parquetPipe)
```

### **JSON Config**

{% hint style="info" icon="right-long" %}
Parquet uses `path` as the property key, not `location` as CSV uses.
{% endhint %}

```json
{
  "data" : [ {
    "name" : "parquetFiles",
    "format" : "parquet",
    "props" : {
      "path" : "/home/zingg"
    }
  } ]
}
```
{% endtab %}

{% tab title="Avro" %}
### Python API

```python
from zingg.client import*
from zingg.pipes import*

avroPipe = Pipe("avroInput", "avro")
avroPipe.addProperty("path", "/path/to/input.avro")
args.setData(avroPipe)
```

### JSON Config

```json
{
  "data" : [ {
    "name" : "avroInput",
    "format" : "avro",
    "props" : {
      "path" : "/path/to/input.avro"
    }
  } ]
}
```
{% endtab %}

{% tab title="JSON" %}
### Python API

```python
from zingg.client import*
from zingg.pipes import*

jsonPipe = Pipe("jsonInput", "json")
jsonPipe.addProperty("path", "/path/to/input.json")
args.setData(jsonPipe)
```

### JSON Config

```json
{
  "data" : [ {
    "name" : "jsonInput",
    "format" : "json",
    "props" : {
      "path" : "/path/to/input.json"
    }
  } ]
}
```

{% hint style="info" icon="right-long" %}
* For multiline JSON files, add `multiLine`: `true` to props.
* For JSON Lines (one object per line), no additional props needed.
{% endhint %}
{% endtab %}

{% tab title="XML" %}
_**XML connector documentation is being confirmed. This section will be updated with the JSON config and Python API once verified**_
{% endtab %}
{% endtabs %}

<details>

<summary><strong>What about TSV and XLSX?</strong></summary>

**TSV**

TSV uses the same CSV connector with a tab delimiter.

```json
{
  "data": [
    {
      "name": "tsvInput",
      "format": "csv",
      "props": {
        "location": "/path/to/input.tsv",
        "delimiter": "\t",
        "header": "true"
      }
    }
  ]
}
```

**Python API - Community**

```python
from zingg.client import *
from zingg.pipes import *

tsvPipe = CsvPipe(
    "tsvInput",
    "/path/to/input.tsv",
    schema
)
tsvPipe.addProperty("sep", "\t")
args.setData(tsvPipe)
```

**XLSX**

_**CHECK WITH SONAL - XLSX is listed as supported on zingg.ai but the connector format string and Python class are not confirmed on any live docs page. Please confirm the XLSX config and whether it is all editions or ENT only.**_

{% hint style="info" icon="right-long" %}
For files on cloud platforms:

* Databricks and Fabric - [Connect Cloud Warehouses](connect-cloud-warehouses/)
* S3 - [Connect Cloud Storage](connect-cloud-storage.md)
{% endhint %}

</details>
