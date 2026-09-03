---
description: >-
  Distinguish deduplication (within one dataset), linking (across two duplicate-free datasets), and resolving (across many systems at scale). Zingg handles all three.
---

# Deduplication, linking and resolving

These three operations are often confused. They are distinct in scope and complexity.

**DEDUPLICATION - within one dataset**

Deduplication identifies that two records within the same system represent the same entity. This is the most constrained version of the problem same schema, same\
source, single dataset.

Zingg's `match` phase does just that. You run it against one dataset. Every record that resolves to the same entity gets the same `Z Cluster` or `Zingg ID`.

**LINKING - across two datasets**

Linking identifies that a record in dataset A matches a record in dataset B, where each dataset is individually duplicate-free. Linking is required for reference data mastering, enrichment, and dataset joins when exact key matches do not exist.

Zingg's link phase does this. Same trained model, two input datasets. The output shows which records from each dataset represent the same entity.

**RESOLVING - across many systems at scale**

Full entity resolution identifies the same entity across multiple systems, with no shared key, inconsistent formats, partial data, and records that change over time. This is the general problem that Zingg is built to solve.

{% hint style="success" icon="right-long" %}
**Read more**:

* [Run the match phase](../../running-zingg/run-the-match-phase.md)
* [Link across datasets](../../running-zingg/link-across-datasets.md)
{% endhint %}
