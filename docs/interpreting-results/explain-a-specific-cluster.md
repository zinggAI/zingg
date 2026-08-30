---
description: >-
  Run the explain phase for a specific Zingg ID to see exactly how that
  cluster was formed.
tags:
  - ent
  - tag: enterprise-only
    primary: true
---

# 📊 Explain a Specific Cluster

{% hint style="info" icon="right-long" %}
Enterprise only. Requires a completed match or `runIncremental` phase before running.
{% endhint %}

The `explain` phase takes a Zingg ID as input and returns the pair-level evidence for how\
that cluster formed. You can see which record pairs were compared, what their similarity scores were, and how transitive matching connected records through intermediate pairs.

{% tabs %}
{% tab title="Enterprise" %}
### Step 1: Import

```python
from zingg.client import *
from zingg.pipes import *
from zinggEC.enterprise.common.EArguments import *
from zinggEC.enterprise.common.epipes import *
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggEC.enterprise.common.ExplainArguments import *
from zinggEC.enterprise.common.EClientOptions import *
from zinggES.enterprise.spark.ESparkClient import *
```

### Step 2: Set up the base arguments

Use the same arguments setup. All field definitions, pipes, `modelId`, and `zinggDir` must match the original match run exactly.

```python
args = EArguments()
args.setModelId("your-model-id")
args.setZinggDir("/tmp/models")
# 📊 ... same field definitions and pipes as your original match run
```

### Step 3: Wrap the arguments and set where the explain output goes

`explain` needs a pipe telling it where to write the result.

```python
explainArgs = ExplainArguments()
explainArgs.setParentArgs(args)

explainPipe = ECsvPipe("outputExplain", "/tmp/zinggOutput_explain")
explainPipe.setHeader("true")
explainArgs.setExplainOutput(explainPipe)
```

### Step 4: Set the phase and the Zingg ID to explain

Find Zingg IDs in the `ZINGG_ID` column of your match output. The Zingg ID is passed as a client option alongside the phase.

```python
options = EClientOptions([EClientOptions.PHASE, "explain", EClientOptions.ZINGG_ID, "ea67d79a-56a7-4431-ab55-d08bb3c10e2e"])
```

### Step 5: Run the explain phase

```python
zingg = EZingg(explainArgs, options)
zingg.initAndExecute()
```

### Step 6: Read the explain output

Read from wherever you pointed `explainOutput` in Step 3 — a CSV path here, but it can be any pipe format (parquet, Snowflake table, etc.).

```python
explain_output = spark.read.csv("/tmp/zinggOutput_explain", header=True)
explain_output.show()
```

The output shows pair-level evidence for how the cluster formed. Each row is a record pair.

{% hint style="success" icon="right-long" %}
**Read more**: `explain` covers probabilistic matches only. Clusters formed through deterministic matching rules may return empty or partial results. For context on deterministic matching - [Configure Zingg](../running-zingg/configure-zingg.md).
{% endhint %}

### Using the CLI instead of Python API

Create an `explainConfig.json` and run with the `--zinggid` flag:

#### `explainConfig.json`

```json
{
  "config" : "path_to_original_matching_config/config.json",
  "explainOutput" : {
    "name" : "outputExplain",
    "format" : "csv",
    "props" : {
      "location" : "/tmp/zinggOutput_explain",
      "delimiter" : ",",
      "header" : true
    }
  }
}
```

#### CLI command

```bash
./scripts/zingg.sh --phase explain --zinggid ea67d79a-56a7-4431-ab55-d08bb3c10e2e --conf ./examples/febrl/explainConfig.json
```
{% endtab %}

{% tab title="Enterprise Snowflake" %}
{% hint style="info" icon="right-long" %}
Same requirements apply: a completed match or `runIncremental` phase, and results cover probabilistic matches only.
{% endhint %}

Run `explain` on Snowflake the same way as other phases — via the local CLI script, pointing at a Snowflake-flavored `explainConfig.json` and your Snowflake connection properties.

#### `explainConfig.json`

```json
{
  "config" : "path_to_original_matching_config/configSnow.json",
  "explainOutput" : {
    "name" : "outputExplain",
    "format" : "snowflake",
    "props" : {
      "table" : "EXPLAIN_CUSTOMERS"
    }
  }
}
```

#### CLI command

```bash
./scripts/zingg.sh --phase explain --zinggid ea67d79a-56a7-4431-ab55-d08bb3c10e2e \
  --conf ./examples/febrl/explainConfig.json \
  --properties-file <location to snowflake.properties>
```

The `explainOutput` result is written to the Snowflake table configured in `props.table` (`EXPLAIN_CUSTOMERS` above).
{% endtab %}
{% endtabs %}
