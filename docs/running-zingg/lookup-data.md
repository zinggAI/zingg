---
description: >-
  Look up specific records in your match output to find which entity cluster
  they belong to and get their Zingg ID.
tags:
  - ent
---

# Lookup Data

{% hint style="info" icon="right-long" %}
**Enterprise** only. The lookup feature assigns a Zingg ID to given lookup records by finding which entity cluster they belong to in the existing match output.
{% endhint %}

After running the match phase, you sometimes need to look up specific records to determine which entity cluster they belong to. The lookup feature handles this; for given lookup records, it assigns the Zingg ID that describes which entity cluster each record belongs to.

This is useful for operational post-match queries: looking up a new customer record to see if it already exists in your resolved entity graph or checking which cluster a specific record resolves to without re-running the full match.

### Run the lookup phase

```bash
./scripts/zingg.sh \
  --phase runLookup \
  --conf <location to lookupConf.json>
```

### `lookupConf.json` structure

```json
{
  "config" : "config.json",
             "lookupData"
      : [ {"name" : "lookup-test-data", "format" : "inMemory"} ],
        "lookupOutput" : [ {
          "name" : "lookup-output",
          "format" : "csv",
          "props" : {
            "path" : "/tmp/zinggOutput/lookup",
            "delimiter" : ",",
            "header" : true
          }
        } ]
}
```

<details>

<summary><strong>What do the lookupConf.json fields mean?</strong></summary>

* **`config`**\
  Points to your base config.json - the same configuration file used for your match phase. Zingg uses this information to understand your field definitions and model location.
* **`lookupData`**\
  The records you want to look up are available here. The format "`inMemory`" means the lookup records are provided directly in memory rather than from a file. Replace with a file-based pipe config if your lookup records are in a CSV or other format.
* **`lookupOutput`**\
  Where Zingg writes the lookup results. Each lookup record in the output will have its assigned Zingg ID showing which entity cluster it belongs to.

</details>

**CHECK WITH SONAL -** [**https://docs.zingg.ai/latest/stepbystep/lookup**](https://docs.zingg.ai/latest/stepbystep/lookup) **-**

**The lookup page on the live docs is minimal. Three things to confirm before publishing:**

1. **What does the output look like? Which columns appear in lookup output?**
2. **Is lookupData always "inMemory" or can it be a file pipe (CSV, Parquet etc.)?**
3. **Is lookup available in all Enterprise tiers or only specific editions?**
