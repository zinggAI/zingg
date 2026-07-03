---
description: >-
  Zingg's underlying data structure for resolved entities and how it differs
  between Community and Enterprise.
tags:
  - ent
---

# Identity Graph

Zingg produces an identity graph as its output. Nodes are records. Edges connect records that Zingg has identified as representing the same real-world entity. A cluster is a connected component in this graph, a group of records that all resolve to the same entity. Not all entity resolution systems work this way; some only produce pairwise match decisions without joining them into a graph. Zingg always produces the complete graph

Zingg's identity graph is this structure. What differs between Community and Enterprise is not the graph itself but how it is managed over time.

{% hint style="success" icon="right-long" %}
* **Open Source:** `Z Cluster`, non-persistent and may change between runs.
* **Enterprise:** `Zingg ID`, stable, globally unique, persistent across incremental updates.
{% endhint %}

<figure><img src="../.gitbook/assets/identity-graph.png" alt=""><figcaption></figcaption></figure>

### Community - a complete identity graph, rebuilt fresh every run

Every Zingg Community run produces a complete, queryable identity graph. Every record in your dataset is assigned a `Z Cluster`. All records sharing the same `Z Cluster` represent the same real-world entity.

You can query it, export it, join it with other datasets, power analytics, and feed it into downstream models immediately after every run. You do not need to install anything beyond your existing Spark environment, write rules, or maintain a separate MDM system to get a working identity graph from your data.

The `Z Cluster` is non-persistent between runs, and it is reassigned fresh each time the match job runs. This means the graph is always accurate to your latest data, but the IDs themselves are not safe to store as stable references in downstream systems. For applications where the identity identifier needs to persist across re-runs, incremental updates, and model changes, that is the boundary where Enterprise begins.

### Enterprise - persistent graph

In Enterprise, the identity graph is persistent. Once a `Zingg ID` is assigned to a resolved entity, it does not change between incremental runs. New and updated records are incorporated into the existing graph. Clusters merge, split, and grow, but `Zingg IDs` for established clusters remain stable. Downstream systems can store and reference `Zingg IDs` with confidence that they will remain valid.

The incremental flow is, at its core, an engine for maintaining and updating this persistent identity graph as your data changes. Cluster merges, unmerges, and new record assignments all happen automatically. New runs do not override human-approved cluster decisions.

{% hint style="warning" icon="right-long" %}
Persistent identity graph with stable Zingg ID across runs is Enterprise only.\
[Talk to us about Enterprise](https://www.zingg.ai/company/contact/contact).
{% endhint %}

{% hint style="success" icon="right-long" %}
Community gives you a complete, accurate identity graph after every run.

Enterprise gives you the same graph with stable `Zingg IDs` , so the graph can grow incrementally and downstream systems can reference it safely across time.

**Read more**:

* [Z Cluster vs Zingg ID](z-cluster-and-zingg-id.md)
* [Zingg Models](how-zingg-learns/zingg-models/)
* [Run incremental matching](../running-zingg/run-incremental-matching.md) &#x20;
* [Community Vs Enterprise](community-vs-enterprise/)
{% endhint %}

