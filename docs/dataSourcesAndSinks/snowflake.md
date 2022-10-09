---
title: Snowflake
parent: Data Sources and Sinks
nav_order: 4
description: Instructions to work with Snowflake
---

# Snowflake

Check a step-by-step tutorial at [Towards Data Science](https://towardsdatascience.com/identifying-duplicates-in-snowflake-e95b3f3fce2b).

The config value for the data and output attributes of the JSON is:

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
				"dbtable": "FEBRL",
				"application":"zinggai_zingg"			
			}
		} ]
```

One must include Snowflake JDBC driver and Spark dependency on the classpath. The jars can be downloaded from the maven repository ([1](https://mvnrepository.com/artifact/net.snowflake/snowflake-jdbc), [2](https://mvnrepository.com/artifact/net.snowflake/spark-snowflake)).

For Zingg to discover the Snowflake jars, please add the property **spark.jars** in [Zingg's runtime properties.](../stepbystep/zingg-runtime-properties.md)

```
spark.jars=snowflake-jdbc-3.13.18.jar,spark-snowflake_2.12-2.10.0-spark_3.1.jar
```

