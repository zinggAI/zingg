---
description: When it is not enough to match once
tags:
  - tag: enterprise-only
    primary: true
---

# Incremental Flow

Data in enteprise systems gets regularly updated. The business expands, and new customer records come in. Entities change - people move, get new phones, register new businesses. Some systems cease and records get deleted. Matching once is powerful, but it ceases to be enough in such cases. A full rematch is costly, and loses state.&#x20;

The process of updating the identity graph with new, changed, or deleted records without re-running the full match across the entire dataset is known as incremental flow in Zingg. Zingg incorporates incoming records into existing clusters, handles cluster merges and unmerges automatically, generates new Zingg IDs for records that do not match any existing cluster, and preserves human-approved decisions so they are not overridden.

Many tools claim incremental capability but cannot maintain match quality and stable IDs simultaneously. This feature is a key Zingg Enterprise differentiator. _Enterprise only_.

{% hint style="success" icon="right-long" %}
**Read more:** [Run incremental matching](../../running-zingg/run-incremental-matching.md)
{% endhint %}

