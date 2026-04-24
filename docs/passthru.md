---
description: Supressing records entirely during matching yet preserving them in the output
---

# Pass Through Data

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

In many cases, our input consists for records which are partially populated, outdated or not fit for matching in some way or other. One likely cause for such data is a source system with poor quality information. For governance and compliance purposes, we still want these records in the identity graph, but we do not want them to play a role in matching.

Pass Through Configuration allows such records to appear in the Zingg output but be ignored during matching, linking and incremental processes. Each pass through record gets a unique ZINGG\_ID

### Example For Configuring In JSON:

```json
     "passthroughExpr": "is_deceased = true"
```

Note: Zingg internally applies the **negation** of the `passthroughExpr` to filter matching records. If the passthrough condition is being applied on nullable fields, please ensure that the negative of the passthrough expression yields the records which are NOT passthrough. For example:

```json
     "passthroughExpr": "is_deceased = true AND is_deceased is NOT NULL"
```



[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to https://www.zingg.ai/product/zingg-entity-resolution-compare-versions for individual tier features.