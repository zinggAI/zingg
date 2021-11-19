## Using Cassandra as the Data Source or Data Sink
```json
"output" : [
			{
			"name":"sampleTest", 
			"format":"CASSANDRA" ,
			"props": {
				"table":"dataschematest",
				"keyspace":"reifier",
				"cluster":"reifier",
				"spark.cassandra.connection.host":"192.168.0.6"
			},
			"sparkProps": {
				"spark.cassandra.connection.host":"127.0.0.1"
			},
			"mode":"Append"
		}
		]
```