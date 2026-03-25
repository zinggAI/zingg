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

### For Nullable Columns

In many datasets, certain records may have missing or empty values in key fields such as `fName`. These records are not suitable for matching but should still be preserved in the final output for completeness and governance purposes.

**Important**: Zingg internally applies the **negation** of the `passthroughExpr` to filter matching records. Therefore, you should specify the condition for records you want to **KEEP for matching**, not the ones you want to skip.

#### Example:

To **skip** the records where `fName = 'madison'` for matching:

```json
"passthroughExpr": "fName = 'madison' And fName IS NOT NULL"
```
**How it works:**
- Zingg applies the negation: `NOT (fName = 'madison' AND fName IS NOT NULL)`
- Which simplifies to: `fName != 'madison' OR fName IS NULL`
- Records where `fName != 'madison'` and not null → **Included in matching**
- All other records → **Pass through only** (excluded from matching)

[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to https://www.zingg.ai/product/zingg-entity-resolution-compare-versions for individual tier features.**

