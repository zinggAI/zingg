## Using Google BigQuery to read and write data with Zingg

Zingg can seemlessly work with Google BigQuery. Please find below details about the properties that must be set. 

The two driver jars namely **spark-bigquery-with-dependencies_2.12-0.24.2.jar** and  **gcs-connector-hadoop2-latest.jar** are required to work with BigQuery. To include these BigQuery drivers to the spark classpath, set the following environment variable before running Zingg.

```bash
export ZINGG_EXTRA_JARS=./spark-bigquery-with-dependencies_2.12-0.24.2.jar,./gcs-connector-hadoop2-latest.jar
export ZINGG_EXTRA_SPARK_CONF="--conf spark.hadoop.fs.gs.impl=com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem"
```

If Zingg is run from outside Google cloud, it requires authentication, please set the following env variable to the location of the file containing service account key. A service account key can be created and downloaded in json format from [Google Cloud console](https://cloud.google.com/docs/authentication/getting-started)

```bash
export GOOGLE_APPLICATION_CREDENTIALS=path to google service account key file
```

Connection properties for BigQuery as data source and data sink are given below. If you are curious to know more about how Spark connects to BigQuery, you may look at the [Spark BigQuery connector documentation](https://github.com/GoogleCloudDataproc/spark-bigquery-connector).

### Properties for reading data from BigQuery:

The property **"credentialsFile"** should point to the google service account key file location. This is the same path that is used to set variable **GOOGLE_APPLICATION_CREDENTIALS**. The **"table"** property should point to a BigQuery table that contains source data. The property **"viewsEnabled"** must be set to true only.

```json
    "data" : [{
        "name":"test", 
         "format":"bigquery", 
        "props": {
            "credentialsFile": "/home/work/product/final/zingg-1/mynotification-46566-905cbfd2723f.json",
            "table": "mynotification-46566.zinggdataset.zinggtest",
            "viewsEnabled": true
        }
    }],
``` 


### Properties for writing data to BigQuery:

To write to BigQuery, a bucket needs to be created and assigned to the **"temporaryGcsBucket"** property.

```json
    "output" : [{
        "name":"output", 
        "format":"bigquery",
        "props": {
            "credentialsFile": "/home/work/product/final/zingg-1/mynotification-46566-905cbfd2723f.json",
            "table": "mynotification-46566.zinggdataset.zinggOutput",
            "temporaryGcsBucket":"zingg-test",
         }
    }],
```

### Notes:
 * The library **"gcs-connector-hadoop2-latest.jar"** can be downloaded from [Google](https://storage.googleapis.com/hadoop-lib/gcs/gcs-connector-hadoop2-latest.jar)
 and the library **"spark-bigquery-with-dependencies_2.12-0.24.2"** from [maven repo](https://repo1.maven.org/maven2/com/google/cloud/spark/spark-bigquery-with-dependencies_2.12/0.24.2/spark-bigquery-with-dependencies_2.12-0.24.2.jar)
 * A typical service account key file look like below. format of the file is json.

 ```json
 {
  "type": "service_account",
  "project_id": "mynotification-46566",
  "private_key_id": "905cbfd273ff9205d1cabfe06fa6908e54534",
  "private_key": "-----BEGIN PRIVATE KEY-----CERT.....",
  "client_id": "11143646541283115487",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/zingtest%44mynotification-46566.iam.gserviceaccount.com"
}
```