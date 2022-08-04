---
title: Neo4j
parent: Data Sources and Sinks
nav_order: 5
description: Instructions to fetch data from Neo4j
---

# Neo4j

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
