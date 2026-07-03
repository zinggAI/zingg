---
description: >-
  Connect Zingg to Microsoft Fabric OneLake using abfss:// path format.
  Available in all editions.
---

# Connect Microsoft Fabric

{% hint style="info" icon="right-long" %}
On Microsoft Fabric, data lives in OneLake. Use the `abfss://` path format to connect Zingg to your Lakehouse. Available in all editions.
{% endhint %}

### Prerequisites

On Microsoft Fabric, data lives in OneLake. Use the `abfss://` path format to connect Zingg to your Lakehouse. The path follows the pattern `abfss://<workspace-id>@onelake.dfs.fabric.microsoft.com/<lakehouse-id>/Files/<filename>` for files, and `.../Tables/<tablename>` for Lakehouse tables. All formats — CSV, Parquet, Avro, JSON, Delta — are available in both Community and Enterprise. For the full Zingg installation, workspace setup, and any additional dependency configuration required on Fabric Spark pools, follow the [Microsoft Fabric Platform Guide](../../platform-guides/platform-guide-for-microsoft-fabric.md).

### Python API&#x20;

```python
from zingg.client import* from zingg.pipes import*

    schema =
    "rec_id string, fname string, \
lname string, stNo string, add1 string, \
add2 string, city string, \
areacode string, state string, \
dob string, ssn string"

    workspace_id = "<workspace-id>" lakehouse_id = "<lakehouse-id>" base_path =
        (f
         "abfss://{workspace_id}@onelake"
         ".dfs.fabric.microsoft.com/" f "{lakehouse_id}/Files")

            inputPipe =
                CsvPipe("inputpipe", f "{base_path}/yourdata.csv", schema)
                    args.setData(inputPipe)

                        outputPipe =
                    CsvPipe("resultOutput", f "{base_path}/Output")
                        args.setOutput(outputPipe)
```

### JSON Config

```json
{
  "data" : [ {
    "name" : "inputpipe",
    "format" : "csv",
    "props" : {
      "location" : "abfss://<workspace-id>@onelake.dfs.fabric.microsoft.com/"
                   "<lakehouse-id>/Files/yourdata.csv"
    },
    "schema" : "rec_id string, fname string,
    lname string,
    stNo string,
    add1 string,
    add2 string,
    city string,
    areacode string,
    state string,
    dob string,
    ssn string "
  } ],
           "output" : [ {
             "name" : "resultOutput",
             "format" : "csv",
             "props" : {
               "location" :
                   "abfss://<workspace-id>@onelake.dfs.fabric.microsoft.com/"
                   "<lakehouse-id>/Files/Output"
             }
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
    "props" : {
      "path" : "abfss://<workspace-id>@onelake.dfs.fabric.microsoft.com/"
               "<lakehouse-id>/Files/input.parquet"
    }
  } ]
}
```

#### **Delta (Lakehouse Tables)**

```json
{
  "data" : [ {
    "name" : "deltaInput",
    "format" : "delta",
    "props" : {
      "location" : "abfss://<workspace-id>@onelake.dfs.fabric.microsoft.com/"
                   "<lakehouse-id>/Tables/yourtable"
    }
  } ]
}
```

#### **Avro**

```
{
  "data" : [ {
    "name" : "avroInput",
    "format" : "avro",
    "props" : {
      "path" : "abfss://<workspace-id>@onelake.dfs.fabric.microsoft.com/"
               "<lakehouse-id>/Files/input.avro"
    }
  } ]
}
```
