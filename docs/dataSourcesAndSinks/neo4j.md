---
layout: default
title: Neo4j
parent: Data Sources and Sinks
nav_order: 3
---
## Neo4j

```json
"data" : [{
		"name":"neo", 
		"format":"org.neo4j.spark.DataSource", 
		"props": {
			"url": "bolt://localhost:7687",
            "labels":"Person"		
			}	
		}]

```