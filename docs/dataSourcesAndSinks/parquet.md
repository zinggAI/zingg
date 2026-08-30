---
description: >-
  Connect Zingg to Parquet files using Spark's native Parquet reader. Configure path in data/output JSON configuration.
layout: default
title: Parquet
parent: Data Sources and Sinks
nav_order: 5
---


## Parquet files
```json
"data" : [{
		"name":"parquetFiles", 
		"format":"parquet", 
		"props": {
			"path": "/home/zingg"		
			}	
		}]
```
