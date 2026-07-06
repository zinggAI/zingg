---
description: >-
  Run the explainOutput phase for a specific Zingg ID to see exactly how that
  cluster was formed.
tags:
  - ent
  - tag: enterprise-only
    primary: true
---

# Explain a Specific Cluster

{% hint style="info" icon="right-long" %}
Enterprise only. Requires a completed match or `runIncremental` phase before running.
{% endhint %}

The `explainOutput` phase takes a Zingg ID as input and returns the pair-level evidence for how\
that cluster formed. You can see which record pairs were compared, what their similarity scores were, and how transitive matching connected records through intermediate pairs.

{% tabs %}
{% tab title="Enterprise" %}
### Step 1: Import

```python
from zingg.client import*
from zingg.pipes import*
from zinggEC.enterprise.common.EArguments import*
from zinggEC.enterprise.common.epipes import*
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggEC.enterprise.common.ExplainArguments import*
from zinggEC.enterprise.common.EClientOptions import*
from zinggES.enterprise.spark.ESparkClient import*
```

### Step 2: Set up `ExplainArgument`

Use the same arguments setup as your Configure Zingg notebook. All field definitions, pipes, `modelId`, and `zinggDir` must match the original match run exactly.

```python
args = EArguments()
args.setModelId("your-model-id")
args.setZinggDir("/tmp/models")
```

### **Step 3: Set the Zingg ID to explain**

Find Zingg IDs in the `ZINGG_ID` column of your match output. Replace the value below with the Zingg ID you want to explain.

```python
args.setZinggId("ea67d79a-56a7-4431-ab55-d08bb3c10e2e")
```

### **Step 4: Run the explain phase**

```python
options = ClientOptions([ ClientOptions.PHASE, "explainOutput" ])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

### **Step 5: Read the explain output**

```python
explain_output = spark.read.parquet(f"{zinggDir}/{modelId}/explainOutput")
explain_output.show()
```

The output shows pair-level evidence for how the cluster formed. Each row is a record pair with their similarity score.

{% hint style="success" icon="right-long" %}
**Read more**: `explainOutput` covers probabilistic matches only. Clusters formed through deterministic matching rules may return empty or partial results. For context on deterministic matching - [Configure Zingg](../running-zingg/configure-zingg.md).
{% endhint %}

### Using the CLI instead of Python API

If you prefer the CLI, create an `explainConfig.json` and run with the `--zinggid` flag:

#### **`explainConfig.json`**

```json
{
  "config" : "path_to_original_matching_config/config.json",
  "explainOutput" : [ {
    "name" : "outputExplain",
    "format" : "csv",
    "props" : {
      "location" : "/tmp/zinggOutput_explain",
      "delimiter" : ",",
      "header" : true
    }
  } ]
}
```

#### CLI command

```bash
./scripts/zingg.sh --phase explainOutput --zinggid ea67d79a-56a7-4431-ab55-d08bb3c10e2e --conf ./examples/febrl/explainConfig.json
```
{% endtab %}

{% tab title="Enterprise Snowflake" %}
**CONTENT FOR THIS SECTION TO BE PROVIDED BY SONAL LATER**
{% endtab %}
{% endtabs %}
