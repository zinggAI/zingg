---
description: >-
  Build a knowledge graph on top of your resolved entities to analyze
  relationships across your data.
tags:
  - ent
---

# Knowledge Graph

{% hint style="info" icon="right-long" %}
Enterprise Plus only. Knowledge Graph is available in Zingg Enterprise Plus (ZinggES). Not available in Community, Enterprise Lite, or Enterprise.
{% endhint %}

Entity resolution resolves who your entities are. The Knowledge Graph goes one step further - it maps the relationships between those entities once they are resolved.

After Zingg groups records into clusters and assigns Zingg IDs, the knowledge graph layer builds a graph structure on top of those resolved entities. This lets you query not just, "Is this the same customer?" but also "how are these customers connected to each other through shared addresses, accounts, devices, or transactions?"

Common applications include fraud detection (finding rings of connected entities), customer householding (understanding which individuals belong to the same household), supplier network analysis, and compliance screening (identifying indirect relationships between\
sanctioned entities and your counterparties).

### How it fits in the Zingg workflow

The Knowledge Graph is a post-match feature. The sequence is:

1. Run a match or incremental to resolve entities and assign Zingg IDs.
2. Zingg IDs become the nodes in the knowledge graph.
3. The knowledge graph layer maps edges between nodes based on shared attributes, transactions, or relationships defined by your data model.
4. Query the graph to find connected entities, paths between nodes, and clusters of related records.

_**CHECK WITH TEAM/SONAL—Knowledge Graph is confirmed as an Enterprise Plus feature on the compare-versions page but is not yet documented on any live docs page. Please provide:**_

1. _**How the Knowledge Graph is configured and invoked**_
2. _**The graph database or format used (Neo4j, native graph store?)**_
3. _**A worked example showing node and edge definition**_
4. _**How to query the graph after entity resolution runs**_

_**Full content and code example will be added once the above is confirmed**_

{% hint style="success" icon="right-long" %}
**Read more:**

* Entity resolution and graph analysis with Neo4j - Connect [graph databases](../connect-your-data/connect-graph-databases-neo4j.md)&#x20;
{% endhint %}

