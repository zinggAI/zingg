---
description: >-
  
---

# Pass Through Data

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

In many cases, our input consists for records which are partially populated, outdated or not fit for matching in some way or other. One likely cause for such data is a source system with poor quality information. For governance and compliance purposes, we still want these records in the identity graph, but we do not want them to play a role in matching. 

Pass Through Configuration allows such records to appear in the Zingg output but be ignored during matching, linking and incremental processes. Each pass through record gets a unique ZINGG_ID

### Example For Configuring In JSON:

```json
     "passthroughExpr": "is_deceased = true"
```


[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to https://www.zingg.ai/product/zingg-entity-resolution-compare-versions for individual tier features.
