---
description: >-
  Connect Zingg to MongoDB using MongoDB Spark connector. Configure URI with database and collection for data input/output.
layout: default
title: MongoDB
parent: Data Sources and Sinks
nav_order: 5
---


## MongoDB

```json
"data" : [{
		"name":"mongodb", 
		"format":"mongo", 
		"props": {
			"uri": "mongodb://127.0.0.1/people.contacts"		
			}	
		}]

```
