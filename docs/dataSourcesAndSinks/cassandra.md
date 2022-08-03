---
title: Cassandra
parent: Data Sources and Sinks
nav_order: 5
---

# Cassandra

```json
"output" : [
			{
			"name":"sampleTest", 
			"format":"CASSANDRA" ,
			"props": {
				"table":"dataschematest",
				"keyspace":"zingg",
				"cluster":"zingg",
				"spark.cassandra.connection.host":"192.168.0.6"
			},
			"sparkProps": {
				"spark.cassandra.connection.host":"127.0.0.1"
			},
			"mode":"Append"
		}
		]
```
