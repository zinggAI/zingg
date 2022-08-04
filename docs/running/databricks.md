---
title: Running on Databricks
parent: Running Zingg on Cloud
nav_order: 6
description: Instructions on working with Databricks
---

# Running on Databricks

The cloud environment does not have the system console for the labeler to work. Zingg is run as a Spark Submit Job along with a python notebook-based labeler specially created to run within the Databricks cloud.

Please refer to the [Databricks Zingg tutorial](https://medium.com/@sonalgoyal/identity-resolution-on-databricks-for-customer-360-591661bcafce) for a detailed tutorial.

Zingg is run as a Spark Submit task with the following parameters :

```json
{
"settings": {
"new_cluster": {
"spark_version": "7.3.x-scala2.12",
"spark_conf": {
"spark.databricks.cluster.profile": "singleNode",
"spark.master": "local[*, 4]"
},
"aws_attributes": {
"availability": "SPOT_WITH_FALLBACK",
"first_on_demand": 1,
"zone_id": "us-west-2a"
},
"node_type_id": "c5d.xlarge",
"driver_node_type_id": "c5d.xlarge",
"custom_tags": {
"ResourceClass": "SingleNode"
}
},
"spark_submit_task": {
"parameters": [
"--class",
"zingg.client.Client",
"dbfs:/FileStore/jars/e34dca2a_84a4_45fd_a9fd_fe0e1c7e283c-zingg_0_3_0_SNAPSHOT-aa6ea.jar",
"--phase=findTrainingData",
"--conf=/dbfs/FileStore/config.json",
"--license=abc"
]
},
"email_notifications": {},
"name": "test",
"max_concurrent_runs": 1
}
}
```

The config file for Databricks needs modifications to accept dbfs locations. Here is a sample config that worked :

```json
{	
	"fieldDefinition":[
		{
			"fieldName" : "fname",
			"matchType" : "email",
			"fields" : "fname",
			"dataType": "\"string\"" 
		},
		{
			"fieldName" : "lname",
			"matchType" : "fuzzy",
			"fields" : "lname",
			"dataType": "\"string\"" 
		},
		{
			"fieldName" : "stNo",
			"matchType": "fuzzy",
			"fields" : "stNo",
			"dataType": "\"string\"" 
		},
		{
			"fieldName" : "add1",
			"matchType": "fuzzy",
			"fields" : "add1",
			"dataType": "\"string\"" 
		},
		{
			"fieldName" : "add2",
			"matchType": "fuzzy",
			"fields" : "add2",
			"dataType": "\"string\"" 
		},
		{
			"fieldName" : "city",
			"matchType": "fuzzy",
			"fields" : "city",
			"dataType": "\"string\"" 
		},
		{
			"fieldName" : "state",
			"matchType": "fuzzy",
			"fields" : "state",
			"dataType": "\"string\"" 
		},
		{
			"fieldName" : "dob",
			"matchType": "fuzzy",
			"fields" : "dob",
			"dataType": "\"string\"" 
		},
		{
			"fieldName" : "ssn",
			"matchType": "fuzzy",
			"fields" : "ssn",
			"dataType": "\"string\"" 
		}
		],
		"output" : [{
			"name":"output", 
			"format":"csv", 
			"props": {
				"location": "/febrl120/zinggOutput",
				"delimiter": ",",
				"header":true
			}
		}],
		"data" : [{
			"name":"test", 
			"format":"csv", 
			"props": {
				"location": "/FileStore/test.csv",
				"delimiter": ",",
				"header":false					
			},
			"schema": 
				"{\"type\" : \"struct\",
				\"fields\" : [ 
					{\"name\":\"id\", \"type\":\"string\", \"nullable\":false}, 
					{\"name\":\"fname\", \"type\":\"string\", \"nullable\":true},
					{\"name\":\"lname\",\"type\":\"string\",\"nullable\":true} ,
					{\"name\":\"stNo\", \"type\":\"string\", \"nullable\":true}, 
					{\"name\":\"add1\", \"type\":\"string\", \"nullable\":true},
					{\"name\":\"add2\",\"type\":\"string\",\"nullable\":true} ,
					{\"name\":\"city\", \"type\":\"string\", \"nullable\":true}, 
					{\"name\":\"state\", \"type\":\"string\", \"nullable\":true},
					{\"name\":\"dob\",\"type\":\"string\",\"nullable\":true} ,
					{\"name\":\"ssn\",\"type\":\"string\",\"nullable\":true}
				]
			}"
		}],
		"labelDataSampleSize" : 0.1,
		"numPartitions":5000,
		"modelId": 101,
		"zinggDir": "/models"

}
```
