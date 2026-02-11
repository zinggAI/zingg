---
description: >-
  Ready to audit a cluster? Plug in the Zingg ID and go. Get pair-level
  traceability for clusters.
---

# Which records matched with each other in a cluster

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

Coming Soon!

Many times, records end up being in a cluster even when they may not have directly matched. For example Recod A matched with B, and B matched with C. Some records may match a lot more to other records in the cluster, while one record may just match with one andhence get included in the cluster. If you want to audit clusters, investigate anomalies, present rationale to stakeholders, or refine your matching strategy with confidence, it is important to understand pair wise matching.&#x20;

The `explain` phase helps us learn this.&#x20;

## Configuration

The `explain` configuration references the original matching config and defines where to write the results.

```json
{
  "config": "path_to_original_matching_config/config.json",
  "explainOutput": [
    {
      "name": "outputExplain",
      "format": "csv",
      "props": {
        "location": "/tmp/zinggOutput_explain",
        "delimiter": ",",
        "header": true
      }
    }
  ]
}  
```

Run the **explain** phase from your Zingg Enterprise Installation and pass the Zingg ID of the cluster you want to inspect:

```
./scripts/zingg.sh --phase explain --zinggid <zingg-id> --conf <path-to-explain-config.json>
```

Example:

```
./scripts/zingg.sh --phase explain \
  --zinggid ea67d79a-56a7-4431-ab55-d08bb3c10e2e \
  --conf ./examples/febrl/explainConfig.json
```

This will provide a pair-level view `(pk1, pk2)` for the selected Zingg ID

* `pk1`: Primary key (record ID) of the first record in the pair
* `pk2`: Primary key (record ID) of the second record in the pair

[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to https://www.zingg.ai/product/zingg-entity-resolution-compare-versions for individual tier features.
