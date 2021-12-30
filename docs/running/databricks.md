---
layout: default
title: Running on Databricks
parent: Running Zingg on Cloud
nav_order: 6
---
## Running on Databricks

The cloud environment does not have the system console for the labeller to work. Please refer to 
[Notebook based labelling](https://github.com/zinggAI/zingg/issues/79) to get the notebooks which can run on Databricks

Zingg is run as a Spark Jar task witht he following parameters

````json
{
  "name": "Zingg",
  "new_cluster": {
    "spark_version": "7.3.x-scala2.12",
    "node_type_id": "r3.xlarge",
    "aws_attributes": {
      "availability": "ON_DEMAND"
    },
    "num_workers": 5
  },
  "libraries": [
    {
      "jar": "dbfs:/zingg.jar"
    },
    {
      "maven": {
        "coordinates": "org.jsoup:jsoup:1.7.2"
      }
    }
  ],
  "email_notifications": {
    "on_start": [],
    "on_success": [],
    "on_failure": []
  },
  "timeout_seconds": 3600,
  "max_retries": 1,
  "spark_jar_task": {
    "main_class_name": "zingg.client.Client",
    "parameters": [
    "--phase",
    "findTrainingData",
    "--conf",
    "dbfs:/config.json"
    ]
  }
}
````

The config file for Databricks needs modifications to accept dbfs locations. Here is a sample config that worked
````
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
````


