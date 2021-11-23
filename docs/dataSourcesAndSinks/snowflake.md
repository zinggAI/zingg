---
layout: default
title: Snowflake
parent: Data Sources and Sinks
nav_order: 2
---
## Using Snowflake As the Data Source or Data Sink

Check a step by step tutorial at https://towardsdatascience.com/identifying-duplicates-in-snowflake-e95b3f3fce2b

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