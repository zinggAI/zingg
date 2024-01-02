---
nav_order: 6
description: A note about telemetry and product usage data collection in Zingg
---

# Security And Privacy

Zingg models are built on your data and deployed within your network. No data leaves your environment.

However, Zingg does collect usage metrics and writes them to Google Analytics. This is done to understand and improve the user experience. Please be assured that **Zingg does not capture any user data or input data and will never do so.**

The following details are captured:

* **Data source type:** data format e.g. CSV, snowflake, parquet
* **Fields count:** number of fields used for training
* **Record Count:** total number of total records pasing through Zingg
* **Execution Time:** execution time of the program
* **Matches and Nonmatches:** the number of matched and nonmatched records
* **Domain:** helps Zingg developers to understand our user enterprises
* **JDK information:** version, flavour
* **OS information:** operating system type
* **Databricks Environment:** 

An example analytics event while running Zingg is
```
{"client_id":"sonal-mac.local","events":[{"name":"findTrainingData","params":{"numMatchFields":"10.0","numTotalFields":"11.0","modelId":"100","java.version":"1.8.0_342-internal","dataFormat":"csv","os.arch":"x86_64","java.home":"/Library/Java/JavaVirtualMachines/openlogic-openjdk-8.jdk/Contents/Home/jre","executionTime":"93.0","zingg_version":"0.4.0","domain":"sonal-mac.local","os.name":"Mac OS X","DB_INSTANCE_TYPE":null,"DATABRICKS_RUNTIME_VERSION":null,"outputFormat":"csv"}}]}
```

To view the information captured while running Zingg, edit log4j2.properties and turn loggin level of zingg_analytics to warn
```
logger.zingg_analytics.level = warn
```

If you do not wish to send the data, please set collectMetrics flag to false in the configuration JSON while running Zingg. A blank event will be logged in this case
```
{"client_id":"sonal-mac.local","events":[{"name":"findTrainingData"}],"user_id":"sonal-mac.local"}
```

