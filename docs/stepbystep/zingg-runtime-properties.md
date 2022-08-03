---
description: Memory, external jars and other runtime properties
---

# Zingg Runtime Properties

Zingg jobs can be passed JVM and other settings through a properties file. A sample file exists at [config/zingg.conf](https://github.com/zinggAI/zingg/blob/main/config/zingg.conf). The properties can be passed by invoking

`./scripts/zingg.sh --properties-file <location to file> --conf conf.json`&#x20;

To include jars for Snowflake/BigQuery/MySQL etc, please download them and add them to the spark.jars property.
