---
description: >-
  Configure Zingg to connect to Neo4j  for writing resolved entity output  to a
  graph database.
---

# Connect Graph Databases (Neo4j)

{% hint style="success" icon="right-long" %}
New to Zingg pipes? Understand how pipes work before configuring them - [Pipes and data connections](pipes-and-data-connections.md).
{% endhint %}

The Neo4j connection configuration is available on this page. Zingg resolves entities into clusters and writes them to Neo4j, where the graph database handles downstream relationship analysis. Available in all editions.

### Python API

```python
from zingg.client import*
from zingg.pipes import*

neo4jPipe =
Pipe("neo", "org.neo4j.spark.DataSource")
neo4jPipe.addProperty("url", "bolt://localhost:7687")
neo4jPipe.addProperty("labels", "Person")
args.setData(neo4jPipe)
```

### JSON Config

```json
{
  "data" : [ {
    "name" : "neo",
    "format" : "org.neo4j.spark.DataSource",
    "props" : {"url" : "bolt://localhost:7687", "labels" : "Person"}
  } ]
}
```

The `url` property is the Neo4j bolt connection URI. The labels property sets the Neo4j node label for the resolved entities that Zingg writes to the graph.

The Neo4j Spark connector JAR is required. Add it to your Spark classpath via `zingg.conf` before running.
