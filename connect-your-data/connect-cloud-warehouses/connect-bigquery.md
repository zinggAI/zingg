---
description: >-
  Connect Zingg to Google BigQuery as a data source and output destination.
  Requires two dependency JARs and Google Cloud credentials.
---

# Connect BigQuery

### Prerequisites

1. BigQuery requires two dependency JARs. Download and add to `zingg.conf.`

```bash
spark.jars =./ spark - bigquery - with - dependencies_2 .12 - 0.24.2.jar,
    ./ gcs - connector - hadoop2 - latest.jar
```

2. Set the Spark Hadoop property.

```bash
spark.hadoop.fs.gs.impl = com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem
```

3. If running outside Google Cloud, set credentials.

```bash
export GOOGLE_APPLICATION_CREDENTIALS =
    / path / to / service - account - key.json
```

For the full Zingg installation, cluster setup, and any additional dependency configuration required when connecting Spark to BigQuery, follow the [GCP Dataproc Platform Guide](../../platform-guides/platform-guide-for-gcp-dataproc.md).

### Python API

```python
from zingg.client import* from zingg.pipes import*

    bqInput =
    BigQueryPipe("bqInput")
        bqInput.addProperty("credentialsFile", "/path/to/key.json")
            bqInput.addProperty("table", "project-id.dataset.tablename")
                bqInput.addProperty("viewsEnabled", "true")
                    args.setData(bqInput)

                        bqOutput =
        BigQueryPipe("bqOutput") bqOutput
            .addProperty("credentialsFile", "/path/to/key.json") bqOutput
            .addProperty("table", "project-id.dataset.outputtable") bqOutput
            .addProperty("temporaryGcsBucket", "your-gcs-bucket")
                args.setOutput(bqOutput)
```

### JSON Config

```json
{
  "data" : [ {
    "name" : "test",
    "format" : "bigquery",
    "props" : {
      "credentialsFile" : "/path/to/key.json",
      "table" : "project-id.dataset.table",
      "viewsEnabled" : true
    }
  } ],
           "output" : [ {
             "name" : "output",
             "format" : "bigquery",
             "props" : {
               "credentialsFile" : "/path/to/key.json",
               "table" : "project-id.dataset.output",
               "temporaryGcsBucket" : "your-gcs-bucket"
             }
           } ]
}
```

{% hint style="success" icon="right-long" %}
### **JAR download links:**

* **spark-bigquery-with-dependencies:** repo1.maven.org/maven2/com/google/\
  cloud/spark/spark-bigquery-with-dependencies\_2.12/0.24.2/
* **gcs-connector-hadoop2:** storage.googleapis.com/hadoop-lib/ gcs/gcs-connector-hadoop2-latest.jar
{% endhint %}

