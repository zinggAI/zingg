---
title: Exasol
parent: Data Sources and Sinks
nav_order: 4
---

# Exasol

[Exasol](https://www.exasol.com/) is a in-memory database built for analytics. You can use it to interact with Zingg AI.

## Prerequisites

To use the Exasol database with Zingg, first you should download the required dependencies and set them in the Zingg configuration file.

### Download Assembled Exasol Spark Connector Jar

Please download the latest assembled Exasol [spark-connector](https://github.com/exasol/spark-connector/releases) jar file. Go to the `Assets` section below, and make sure you download the jar file with `-assembly` suffix in the file name.

### Update Zingg Configuration

After downloading the Exasol spark-connector jar file, you should update the `spark.jars` parameter in [Zingg's runtime properties.](../stepbystep/zingg-runtime-properties.md) so that it can find the Exasol dependencies.

For example:

```
spark.jars=spark-connector_2.12-1.3.0-spark-3.3.2-assembly.jar
```

If there are more than one jar files, please use comma as separator. Additionally, please change the version accordingly so that it matches your Zingg and Spark versions.

## Connector Settings

Finally, create a configuration JSON file for Zingg, and update the `data` or `output` settings accordingly.

For example:

```json
...
 "data": [
    {
        "name": "input",
        "format": "com.exasol.spark",
        "props": {
            "host": "10.11.0.2",
            "port": "8563",
            "username": "sys",
            "password": "exasol",
            "query": "SELECT * FROM DB_SCHEMA.CUSTOMERS"
        }
    }
 ],
 ...
```

 Similarly, for output:

 ```json
 ...
"output": [
    {
        "name": "output",
        "format": "com.exasol.spark",
        "props": {
            "host": "10.11.0.2",
            "port": "8563",
            "username": "sys",
            "password": "exasol",
            "create_table": "true",
            "table": "DB_SCHEMA.ENTITY_RESOLUTION",
        },
        "mode": "Append"
    }
],
...
```

Zingg uses [Exasol Spark connector](https://github.com/exasol/spark-connector) underneath, so please also check out the [user guide](https://github.com/exasol/spark-connector/blob/main/doc/user_guide/user_guide.md) and [configuration options](https://github.com/exasol/spark-connector/blob/main/doc/user_guide/user_guide.md#configuration-options) for more information.
