---
description: >-
  Connect Zingg to Neo4j graph database using Neo4j Spark connector. Configure Bolt URL and node labels for data input/output.
title: Neo4j
parent: Data Sources and Sinks
nav_order: 5
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
