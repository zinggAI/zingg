---
layout: default
title: Snowflake
parent: Data Sources and Sinks
nav_order: 4
---
## Using Snowflake As the Data Source or Data Sink

Check a step by step tutorial at [Towards Data Science](https://towardsdatascience.com/identifying-duplicates-in-snowflake-e95b3f3fce2b)

The config value for the data and output attributes of the JSON is

```json
 "data" : [ {
			"name":"test", 
			"format":"net.snowflake.spark.snowflake", 
			"props": {
				"sfUrl": "rfa59271.snowflakecomputing.com",
				"sfUser": "sonalgoyal",
				"sfPassword":"ZZ",					
				"sfDatabase":"TEST",				
				"sfSchema":"PUBLIC",					
				"sfWarehouse":"COMPUTE_WH",
				"dbtable": "FEBRL"				
			}
		} ]
```

One must include Snowflake JDBC driver and Spark dependency on the spark classpath. Please set the following environment variable before running Zingg. The jars can be downloaded from maven repositary ([1](https://mvnrepository.com/artifact/net.snowflake/snowflake-jdbc), [2](https://mvnrepository.com/artifact/net.snowflake/spark-snowflake))
```
export ZINGG_EXTRA_JARS=snowflake-jdbc-3.13.18.jar,spark-snowflake_2.12-2.10.0-spark_3.1.jar
```
Optionally, instead of setting env variable **ZINGG_EXTRA_JARS** as above, equivalent property **spark.jars** can be set in Zingg's configuration file (config/zingg.conf).