---
description: >-
  Preserve Zingg IDs when switching to a new trained model or migrating
  infrastructure without disrupting downstream systems.
tags:
  - ent
  - enterprise-only
---

# Reassign Zingg ID

{% hint style="info" icon="right-long" %}
Enterprise Plus only. Preserves Zingg IDs when switching to a new model. Maximises ID continuity and minimises downstream disruption. Available in ZinggES (Enterprise Plus).
{% endhint %}

When you evolve your Zingg setup - upgrading your model, migrating infrastructure, changing data schemas, or moving to a new platform; you want to preserve the Zingg IDs already flowing through your production systems.

The `reassignZinggId` phase carries Zingg IDs from your original production model to a new model's output. It maps clusters from the new model back to the original cluster assignments and preserves the original Zingg IDs wherever clusters overlap. New IDs are generated only where no match is found.

{% hint style="success" icon="right-long" %}
For what the Zingg ID is and how it works in Enterprise output → [Zingg ID and Z Cluster](../zingg-concepts/z-cluster-and-zingg-id.md)
{% endhint %}

### When to use Reassign Zingg ID

<details>

<summary><strong>Model upgrades</strong></summary>

When you add new features like nickname support, improve blocking strategies, or tune classifiers, the new model may produce different clusters than the original. Use `reassignZinggId` to carry over original IDs to the new model output wherever clusters overlap, so downstream systems are not disrupted.

</details>

<details>

<summary><strong>Infrastructure migration</strong></summary>

Are you moving from Spark to Snowflake or from Databricks to Microsoft Fabric?\
Use `reassignZinggId` to maintain the same Zingg IDs across platforms when the underlying data and model are otherwise equivalent.

</details>

<details>

<summary><strong>Schema changes</strong></summary>

If your data schema changes, new fields are added, fields are removed, or field types are updated. Use reassignZinggId to preserve IDs for records that can still be matched via primary key to the original output.

</details>

<details>

<summary><strong>Data migration</strong></summary>

When migrating data between systems or consolidating data sources, `reassignZinggId` maintains ID consistency across the migration. Downstream systems that reference Zingg IDs continue to work without any updates.

</details>

### How it works

The reassign phase compares two configurations:

* **Original configuration**: your production model with established Zingg IDs.
* **New configuration**: your updated model with improved features, new infrastructure, or schema changes.

The reassign phase then,

1. Identifies clusters in the new output that overlap with original clusters via primary key matching
2. Reassigns the original Zingg IDs to matching clusters wherever possible
3. Generates new Zingg IDs only when no match is found in the original clusters

### Worked example

**Original model (production):** 4 records in a cluster, Zingg ID = `ZID-7f3a`. Records: A, B, C, D.

**New model (upgraded):** 6 records in the equivalent cluster. Records: A, B, C, D, E, F (E and F are new records added since the last full match).

**After `reassignZinggId`:**

* Records A, B, C, D → `ZID-7f3a` (Zingg ID from the original output preserved via primary key overlap)
* Records E, F → `ZID-9b2c` (new Zingg IDs generated for records with no match in the original)

Downstream systems that reference `ZID-7f3a` continue to work without any change.

{% tabs %}
{% tab title="Enterprise Python" %}
### **Step 1: Define the original (baseline) configuration**

Set up `EArguments` matching your existing production model. This is the model whose Zingg IDs you want to preserve.

```python
from zingg.client import*
from zingg.pipes import*
from zinggEC.enterprise.common.EArguments import*
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggES.enterprise.spark.ESparkClient import*
from zinggEC.enterprise.common.TransformedOutputArguments import*
from zinggEC.enterprise.common.EClientOptions import*

originalArgs = EArguments()

id = EFieldDefinition("id", "string", MatchType.DONT_USE)
id.setPrimaryKey(True)
fname = EFieldDefinition("fname", "string", MatchType.FUZZY)
lname = EFieldDefinition("lname", "string", MatchType.FUZZY)

originalArgs.setFieldDefinition([id, fname, lname])
originalArgs.setModelId("107")
originalArgs.setZinggDir("./models")
originalArgs.setNumPartitions(4)

schema = (
    "id string, fname string, "
    "lname string, stNo string, "
    "add1 string, city string, "
    "areacode string, state string, "
    "dob string, ssn string"
)

originalInputPipe = ECsvPipe(
    "originalData",
    "examples/febrl5M/febrl_data.csv",
    schema
)
originalArgs.setData(originalInputPipe)

originalOutputPipe = ECsvPipe("originalOutput", "/tmp/zinggOutputOriginal")
originalOutputPipe.setHeader("true")
originalArgs.setOutput(originalOutputPipe)
```

Add all field definitions from your original production config - only a few are shown above as a pattern.

### **Step 2: Define the new (updated) configuration**

Set up `EArguments` for your new model. This is the model that produced the new cluster assignments which need Zingg IDs reassigned.

```python
newArgs = EArguments()
newArgs.setModelId("999")
newArgs.setZinggDir("./models")
```

Add all field definitions from your new model config - only the structure is shown above.

### **Step 3: Run `trainMatch` on the new configuration first**

The new model must produce its own match output before reassign can compare clusters.

```python
options = EClientOptions([
    EClientOptions.PHASE,
    "trainMatch"
])
zinggNew = EZingg(newArgs, options)
zinggNew.initAndExecute()
```

### **Step 4: Create `TransformedOutputArguments`**

`TransformedOutputArguments` wraps both configurations and the destination for the reassigned output.

```python
reassignArgs = TransformedOutputArguments()
reassignArgs.setParentArgs(newArgs)
reassignArgs.setOriginalArgs(originalArgs)

reassignOutputPipe = ECsvPipe("reassignedOutput", "/tmp/zinggReassigned")
reassignOutputPipe.setHeader("true")
reassignArgs.setTransformedOutputPath(reassignOutputPipe)
```

### Step 5: Execute `reassignZinggId`

```python
reassignOptions = EClientOptions([
    EClientOptions.PHASE,
    "reassignZinggId"
])
zinggReassign = EZingg(reassignArgs, reassignOptions)
zinggReassign.initAndExecute()
```
{% endtab %}

{% tab title="Enterprise JSON" %}
### **Configuration wrapper (`configReassign.json`)**

The JSON wrapper config references your new model config and specifies the destination for the reassigned output.

```json
{
  "config": "$ZINGG_ENT_REPO$/spark/examples/febrl5M/configUpdated.json",
  "transformedOutputPath": {
    "name": "reassignedOutput",
    "format": "csv",
    "props": {
      "location": "/tmp/zinggTransformedOutputReassigned5M",
      "delimiter": ",",
      "header": true
    }
  }
}
```

* `--conf` - the wrapper config (`configReassign.json`) that references your new model
* `--originalZinggId` - your original production configuration
* `--properties-file` - optional Zingg properties file

The CLI command takes three arguments:

```bash
./scripts/zingg.sh \
  --phase reassignZinggId \
  --conf examples/febrl/sparkIncremental/configReassign5M.json \
  --originalZinggId examples/febrl5M/config.json \
  --properties-file config/zingg.conf
```

{% hint style="success" icon="right-long" %}
The `--originalZinggId` flag takes the path to your original production config file. Despite the flag name, you are passing the **configuration file** that defined the original model - Zingg uses that config to locate the original output containing the Zingg IDs to be preserved.
{% endhint %}
{% endtab %}

{% tab title="Enterprise Snowflake" %}
_**CHECK WITH SONAL - Enterprise Snowflake content for this topic to be provivded by Sonal**_
{% endtab %}
{% endtabs %}
