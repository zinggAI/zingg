---
nav_order: 6
description: A note about telemetry and product usage data collection in Zingg
---

# Security And Privacy

Zingg models are built on your data and deployed within your network. No data leaves your environment.

However, Zingg does collect usage metrics and writes them to Google Analytics. This is done to understand and improve the user experience. Please be assured that **Zingg does not capture any input data and will never do so. No input or output data ever leaves any Zingg deployment. The privacy and governance settings of the execution eivironments of Zingg are always enforced due to the native and on-premise deployment of all editions and versions of Zingg. This is valid for the base Operating System, Databricks, Snowflake, Bigquery, Fabirc, Azure, AWS, GCP, Private Data Centre or any other platforms where you run Zingg.**

The following details are captured:

* **Data source type:** data format e.g. CSV, snowflake, parquet
* **Fields count:** number of fields used for training
* **Record Count:** total number of total records pasing through Zingg
* **Execution Time:** execution time of the program
* **Matches and Nonmatches:** the number of matched and nonmatched records in the model
* **Domain:** helps Zingg developers to understand our user enterprises
* **Zingg user id:** Zingg OS User ID 
* **JDK information:** version, flavour
* **OS information:** operating system type
* **Databricks Environment:** 

An example analytics event while running Zingg is
```
event is {"client_id":"localhost","events":[{"name":"match","params":{"executionTime":"1.743246748E9","country":"IN","zingg_version":"0.5.1","modelId":"100","domain":"localhost","java_version":"11.0.18","os_arch":"aarch64","os_name":"Mac OS X","DB_INSTANCE_TYPE":null,"DATABRICKS_RUNTIME_VERSION":null,"dataCount":"65.0","ZINGG_HOME":"true"}}],"user_id":"zingg"}
```

To view the information captured while running Zingg, edit log4j2.properties and turn loggin level of zingg_analytics to warn
```
logger.zingg_analytics.level = warn
```

If you do not wish to send the data, please set collectMetrics flag to false in the configuration JSON while running Zingg. A blank event will be logged in this case
```
{"client_id":"localhost","events":[{"name":"train"}],"user_id":"zingg"}
```

