---
layout: default
title: Snowflake
parent: Data Sources and Sinks
nav_order: 2
---
## Using Snowflake As the Data Source or Data Sink

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