---
description: >-
  Enterprise Plus Python API - the Spark client (EZingg, EZinggWithSpark) used
  by Enterprise Plus Zingg distributions.
---

# Enterprise ZinggES Python API

ZinggES is the Zingg Enterprise Spark Python package. It provides the Spark client classes - `EZingg` and `EZinggWithSpark` and used by Enterprise Plus distributions. The API is otherwise identical to ZinggEC.

Use this when you have an Enterprise Plus licence. The arguments, field definitions, pipes, and execution pattern are the same as ZinggEC; only the import package and Spark client classes differ.

{% hint style="success" icon="right-long" %}
**Read more**:

* For Enterprise (and Enterprise Lite) without the Spark client - [Enterprise ZinggEC Python API](enterprise-zinggec-python-api.md)
* For Community - [Community Python API](community-python-api.md)
{% endhint %}

{% hint style="warning" icon="right-long" %}
ZinggES requires a Zingg Enterprise Plus licence and the `zinggES` package. [Contact Zingg to get access](https://www.zingg.ai/company/contact/contact)
{% endhint %}

### Requirements

* Python 3.6+
* Spark 3.5.0
* Zingg Enterprise Plus licence

### Install the package

```bash
pip install zinggES
```

### Modules and classes

#### **`zinggES.enterprise.spark` package**

<table><thead><tr><th valign="top">Module</th><th valign="top">Classes</th><th valign="top">Purpose</th></tr></thead><tbody><tr><td valign="top"><code>ESparkClient</code></td><td valign="top"><code>EZingg</code>, <code>EZinggWithSpark</code></td><td valign="top">Enterprise Plus Spark execution clients</td></tr></tbody></table>

`EZingg` is the standard Enterprise Plus client. `EZinggWithSpark` is for environments where a Spark session already exists (Databricks, Fabric notebooks) - it reuses that session instead of creating a new one.

{% hint style="success" icon="right-long" %}
All ZinggEC classes (`EArguments`, `EFieldDefinition`, `ECsvPipe`, `UCPipe`, `IncrementalArguments`, `ApproverArguments`, `MappingMatchType`, etc) work identically with ZinggES. Only the Spark client class changes.
{% endhint %}

### Imports

```python
from zingg.client import*
from zingg.pipes import*
from zinggEC.enterprise.common.ApproverArguments import*
from zinggEC.enterprise.common.IncrementalArguments import*
from zinggEC.enterprise.common.MappingMatchType import*
from zinggEC.enterprise.common.epipes import*
from zinggEC.enterprise.common.EArguments import*
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggES.enterprise.spark.ESparkClient import*
```

### Execute Zingg Enterprise Plus phases

Use `EZingg` (or `EZinggWithSpark` in notebook environments where a Spark session exists). The pattern is the same as ZinggEC.

#### **Run `trainMatch`**

```python
options = ClientOptions([ ClientOptions.PHASE, "trainMatch" ])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

**Run any other phase** `findTrainingData`, `label`, `findAndLabel`, `generateDocs`, `train`, `match`, `link`, `updateLabel`, `diff` — by changing the phase name. See [Enterprise ZinggEC Python API](enterprise-zinggec-python-api.md) for the full list.

### Using `EZinggWithSpark` in notebooks

When running inside a Databricks, Fabric, or other notebook where a Spark session already exists, use `EZinggWithSpark`:

```python
zingg = EZinggWithSpark(args, options)
zingg.initAndExecute()
```

`EZinggWithSpark` reuses the existing Spark session instead of creating a new one. Recommended for notebook environments.

### Incremental matching

```python
incrArgs = IncrementalArguments()
incrArgs.setParentArgs(args)
incrPipe = ECsvPipe("testFebrlIncr", "examples/febrl/test-incr.csv", schema)
incrArgs.setIncrementalData(incrPipe)

incrOptions = ClientOptions([ ClientOptions.PHASE, "runIncremental" ])
zinggIncr = EZingg(incrArgs, incrOptions)
zinggIncr.initAndExecute()
```

### Full example

The full example matches the ZinggEC example exactly. The only difference is the `import` for `ESparkClient` and that the Spark client is invoked as `EZingg` from `zinggES.enterprise.spark.ESparkClient` instead of from `zinggEC`.

```python
from zingg.client import*
from zingg.pipes import*
from zinggEC.enterprise.common.ApproverArguments import*
from zinggEC.enterprise.common.IncrementalArguments import*
from zinggEC.enterprise.common.MappingMatchType import*
from zinggEC.enterprise.common.epipes import*
from zinggEC.enterprise.common.EArguments import*
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggES.enterprise.spark.ESparkClient import*

args = EArguments()

recId = EFieldDefinition("recId", "string", MatchType.DONT_USE)
recId.setPrimaryKey(True)

fname = EFieldDefinition("fname", "string", MatchType.FUZZY)
fieldDefs = [ recId, fname ]
args.setFieldDefinition(fieldDefs)

args.setModelId("100")
args.setZinggDir("./models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)
args.setBlockingModel("DEFAULT")
args.setPassthroughExpr("fname = 'matilda'")

dm1 = DeterministicMatching('fname', 'stNo', 'add1')
args.setDeterministicMatchingCondition(dm1)

schema = (
  "recId string, fname string, "
  "lname string"
)

inputPipe = ECsvPipe("testFebrl", "examples/febrl/test.csv", schema)
args.setData(inputPipe)

outputPipe = ECsvPipe("resultFebrl", "/tmp/febrlOutput")
outputPipe.setHeader("true")
args.setOutput(outputPipe)

options = ClientOptions([ ClientOptions.PHASE, "trainMatch" ])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

{% hint style="success" icon="right-long" %}
**Read more**: The full example shown on the [ZinggEC Python API reference](https://docs.zingg.ai/latest/working-with-python/working-with-python-enterprise/index) and the [ZinggES Python API reference](https://docs.zingg.ai/latest/working-with-python/working-with-python-enterprise/index-1) is identical, except for which Spark client package is imported.
{% endhint %}

{% hint style="warning" icon="right-long" %}
Auto-generated API reference for `ESparkClient`, `EZingg`, `EZinggWithSpark`: [Zingg Enterprise Spark Python API on GitHub](https://github.com/zinggAI/zingg/blob/main/docs/pythonES/markdown/ESparkClient.md).
{% endhint %}
