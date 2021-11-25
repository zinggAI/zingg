---
layout: default
title: Running on Databricks
parent: Running Zingg on Cloud
nav_order: 6
---
## Running on Databricks
````json
{
  "name": "Zingg",
  "new_cluster": {
    "spark_version": "7.3.x-scala2.12",
    "node_type_id": "r3.xlarge",
    "aws_attributes": {
      "availability": "ON_DEMAND"
    },
    "num_workers": 5
  },
  "libraries": [
    {
      "jar": "dbfs:/zingg.jar"
    },
    {
      "maven": {
        "coordinates": "org.jsoup:jsoup:1.7.2"
      }
    }
  ],
  "email_notifications": {
    "on_start": [],
    "on_success": [],
    "on_failure": []
  },
  "timeout_seconds": 3600,
  "max_retries": 1,
  "spark_jar_task": {
    "main_class_name": "zingg.client.Client",
    "parameters": [
    "--phase",
    "findTrainingData",
    "--conf",
    "dbfs:/config.json"
  ]
  }
}
  }
}
````
