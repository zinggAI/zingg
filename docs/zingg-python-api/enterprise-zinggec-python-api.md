---
description: >-
  Adds deterministic matching, blocking strategy, primary keys, pass-through,
  mapping match types, incremental matching, cluster approval, output stats, and
  additional pipe types.
tags:
  - tag: enterprise-only
    primary: true
---

# Enterprise ZinggEC Python API

ZinggEC is the Zingg Enterprise Common Python API. It extends the Community API with features required for production identity resolution - persistent Zingg IDs, deterministic matching, pass-through, output statistics, incremental matching, cluster approval, and additional pipe types for Unity Catalog and in-memory data.

{% hint style="success" icon="right-long" %}
**Read more**:

* [Community Python API](community-python-api.md) - Community (open source)
* [Enterprise ZinggES Python API](enterprise-zingges-python-api.md) - Enterprise Plus with Spark client
{% endhint %}

{% hint style="warning" icon="right-long" %}
ZinggEC requires a Zingg Enterprise licence and the `zinggEC` package. [Contact Zingg to get access](https://www.zingg.ai/company/contact/contact).
{% endhint %}

### Requirements

* Python 3.6+
* Spark 3.5.0
* Zingg Enterprise licence

### Install the package

```bash
pip install zinggEC
```

### Modules and classes

#### **`zinggEC.enterprise.common` package**

<table><thead><tr><th width="203.640625" valign="top">Module</th><th width="222.890625" valign="top">Classes</th><th valign="top">Purpose</th></tr></thead><tbody><tr><td valign="top"><code>ApproverArguments</code></td><td valign="top"><code>ApproverArguments</code></td><td valign="top">Cluster approval workflow - set approval query and destination for human-reviewed clusters</td></tr><tr><td valign="top"><code>IncrementalArguments</code></td><td valign="top"><code>IncrementalArguments</code></td><td valign="top">Incremental matching - incremental data, deleted data, delete action, output temp directory, parent args</td></tr><tr><td valign="top"><code>MappingMatchType</code></td><td valign="top"><code>MappingMatchType</code></td><td valign="top">User-supplied lookup file for nickname/abbreviation matching</td></tr><tr><td valign="top"><code>epipes</code></td><td valign="top"><code>ECsvPipe</code>, <code>EPipe</code>, <code>InMemoryPipe</code>, <code>UCPipe</code></td><td valign="top">Enterprise pipe types including CSV, base pipe, in-memory DataFrame, and Unity Catalog table</td></tr><tr><td valign="top"><code>EArguments</code></td><td valign="top"><code>EArguments</code>, <code>DeterministicMatching</code></td><td valign="top">Enterprise arguments object plus deterministic matching rules</td></tr><tr><td valign="top"><code>EFieldDefinition</code></td><td valign="top"><code>EFieldDefinition</code></td><td valign="top">Enterprise field definition with primary key support and mapping match type</td></tr></tbody></table>

{% hint style="success" icon="right-long" %}
Full auto-generated method signatures for every class are at the [Zingg Enterprise Common Python API reference on GitHub](https://github.com/zinggAI/zingg/blob/main/docs/pythonEC/markdown/zinggEC.md).
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
```

### Build the Enterprise arguments object

```python
args = EArguments()
args.setModelId("100")
args.setZinggDir("./models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)
```

Set the blocking strategy. If not set, the model uses `DEFAULT`. `WIDER` is also available for cases where you want to cast a wider blocking net.

```python
args.setBlockingModel("DEFAULT")
```

### Define fields with `EFieldDefinition`

```python
recId = EFieldDefinition("recId", "string", MatchType.DONT_USE)
recId.setPrimaryKey(True)

fname = EFieldDefinition("fname", "string", MatchType.FUZZY)
lname = EFieldDefinition("lname", "string", MatchType.FUZZY)
stNo = EFieldDefinition("stNo", "string", MatchType.FUZZY)
add1 = EFieldDefinition("add1", "string", MatchType.FUZZY)
add2 = EFieldDefinition("add2", "string", MatchType.FUZZY)
city = EFieldDefinition("city", "string", MatchType.FUZZY)
areacode = EFieldDefinition("areacode", "string", MatchType.FUZZY)
state = EFieldDefinition("state", "string", MatchType.FUZZY)
dob = EFieldDefinition("dob", "string", MatchType.FUZZY)
ssn = EFieldDefinition("ssn", "string", MatchType.FUZZY)

fieldDefs = [
  recId, fname, lname, stNo, add1, add2,
  city, areacode, state, dob,
  ssn
]
args.setFieldDefinition(fieldDefs)
```

{% hint style="info" icon="right-long" %}
`setPrimaryKey(True)` marks the primary key field, required for `runIncremental` to track records correctly across runs.
{% endhint %}

### **Using `MappingMatchType` on a field**

For fields where you want to use a mapping file (nicknames, abbreviations, aliases), pass a `MappingMatchType` to `EFieldDefinition`:

```python
fname = EFieldDefinition("fname", "string", MatchType.FUZZY,
                         MappingMatchType("MAPPING", "NICKNAMES_TEST"))
```

The second argument to `MappingMatchType` is the mapping file name (without extension). The mapping file must be present in your working directory.

{% hint style="success" icon="right-long" %}
**Read more**: For the full mapping file format and rules → [Mapping match type](../zingg-concepts/how-zingg-learns/match-types/)
{% endhint %}

### Pass through

Exclude records matching the expression from matching while still keeping them in the output:

```python
args.setPassthroughExpr("fname = 'matilda'")
```

For null-safe expressions:

```python
args.setPassthroughExpr(
    "is_deceased = true "
    "AND is_deceased IS NOT NULL")
```

{% hint style="success" icon="right-long" %}
Pass through excludes records from cluster formation. They still appear in the identity graph and output with their own Zingg ID. Zingg internally applies the negation of `passthroughExpr` to filter matching records - ensure the negative of your expression yields the records that are NOT pass-through.
{% endhint %}

### Deterministic matching

Add hard rules where exact field matches always result in a match, regardless of probabilistic score:

```python
dm1 = DeterministicMatching('fname', 'stNo', 'add1')
dm2 = DeterministicMatching('ssn')
dm3 = DeterministicMatching('fname', 'stNo', 'lname')
args.setDeterministicMatchingCondition(dm1, dm2, dm3)
```

{% hint style="success" icon="right-long" %}
**Read more**: For full deterministic matching concepts and configuration → [Deterministic vs Probabilistic Matching](../zingg-concepts/entity-resolution/deterministic-vs-probabilistic-matching.md)
{% endhint %}

### Output statistics

Enterprise writes match quality statistics to three separate files using the `$ZINGG_DYNAMIC_STAT_NAME` placeholder, replaced at runtime with `SUMMARY`, `CLUSTER`, and `RECORD`:

```python
statsOutputPipe = ECsvPipe("stats", "/tmp/zinggStats_$ZINGG_DYNAMIC_STAT_NAME")
statsOutputPipe.setHeader("true")
args.setOutputStats(statsOutputPipe)
```

{% hint style="success" icon="right-long" %}
**Read more**: For statistics interpretation → [Output Statistics](../interpreting-results/output-statistics.md)
{% endhint %}

### Pipes - Enterprise edition

#### **`ECsvPipe`**

CSV input/output. Same as `CsvPipe` but for Enterprise.

```python
inputPipe = ECsvPipe("testFebrl", "examples/febrl/test.csv", schema)
args.setData(inputPipe)

outputPipe = ECsvPipe("resultFebrl", "/tmp/febrlOutput")
outputPipe.setHeader("true")
args.setOutput(outputPipe)
```

#### **`EPipe`**

Base pipe class with pass-through capability. Use for any source where the specialised classes do not fit.

#### **`InMemoryPipe`**

For reading from or writing to an in-memory Spark DataFrame instead of a file:

```python
inMemPipe = InMemoryPipe("testFebrl")
inMemPipe.setDataset(df)
args.setData(inMemPipe)
```

Use this in notebook environments where you already have data in a DataFrame and do not want to write it to disk first.

#### **`UCPipe`**

For Databricks Unity Catalog tables:

```python
ucPipe = UCPipe("testFebrl")
ucPipe.setTable("catalog.schema.your_table")
args.setData(ucPipe)
```

Use this when your data lives in a Unity Catalog table on Databricks.

### Execute Zingg Enterprise phases

ZinggEC uses the `EZingg` client. Pattern is the same as Community — set options with the phase name, call `initAndExecute()`.

#### **Run `findTrainingData`**

```python
options = ClientOptions([ ClientOptions.PHASE, "findTrainingData" ])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### **Run `label`**

```python
options = ClientOptions([ ClientOptions.PHASE, "label" ])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### **Run `findAndLabel`**

Enterprise convenience — combines `findTrainingData` and `label`

```python
options = ClientOptions([ ClientOptions.PHASE, "findAndLabel" ])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### **Run `generateDocs`**

```python
options = ClientOptions([ ClientOptions.PHASE, "generateDocs" ])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### **Run `train`**

```python
options = ClientOptions([ ClientOptions.PHASE, "train" ])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### **Run `trainMatch`**

Enterprise convenience — combines `train` and `match`

```python
options = ClientOptions([ ClientOptions.PHASE, "trainMatch" ])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### **Run `match`**

```python
options = ClientOptions([ ClientOptions.PHASE, "match" ])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### **Run `link`**

```python
options = ClientOptions([ ClientOptions.PHASE, "link" ])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### **Run `updateLabel`**

Revisit and correct previously marked pairs.

```python
options = ClientOptions([ ClientOptions.PHASE, "updateLabel" ])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### **Run `diff`**

Compare two model outputs.

```python
options = ClientOptions([ ClientOptions.PHASE, "diff" ])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

### Incremental matching with `IncrementalArguments`

`runIncremental` requires `IncrementalArguments` instead of plain `EArguments`. It tracks new, changed, and deleted records, applies them to the existing identity graph, and writes the updated graph.

#### Build incremental

```python
incrArgs = IncrementalArguments()
incrArgs.setParentArgs(args)
```

#### Incremental data (new and changed records)

```python
incrPipe = ECsvPipe("testFebrlIncr", "examples/febrl/test-incr.csv", schema)
incrArgs.setIncrementalData(incrPipe)
```

#### Optional - deleted data

```python
deletedPipe = ECsvPipe("testFebrlDeleted", "examples/febrl/test-deleted.csv", schema)
incrArgs.setDeletedData(deletedPipe)
```

#### Optional - deleted action

```python
incrArgs.setDeleteAction("HARD_DELETE")
```

#### Optional - temporary output directory

```python
incrArgs.setOutputTmp("/tmp/zinggIncrTmp")
```

#### Execute

```python
incrOptions = ClientOptions([ ClientOptions.PHASE, "runIncremental" ])
zinggIncr = EZingg(incrArgs, incrOptions)
zinggIncr.initAndExecute()
```

{% hint style="success" icon="right-long" %}
For the full incremental matching workflow → [Run Incremental Matching](../running-zingg/run-incremental-matching.md)
{% endhint %}

### Cluster approval with `ApproverArguments`

`ApproverArguments` is the Enterprise workflow for human-reviewed cluster approval — where domain experts approve or reject specific clusters before they enter the production identity graph.

```python
from zinggEC.enterprise.common.ApproverArguments import*
```

#### Build approver arguments

```python
apprArgs = ApproverArguments()
apprArgs.setParentArgs(args)
```

#### Approval SQL query identifies clusters

Requiring human review.

```python
apprArgs.setApprovalQuery(
    "SELECT * FROM clusters "
    "WHERE confidence < 0.85")
```

#### Destination for approved clusters

```python
destPipe = ECsvPipe("approved", "/tmp/approvedClusters")
               apprArgs.setDestination(destPipe)
```

#### Execute approval phase

```python
destPipe = ECsvPipe("approved", "/tmp/approvedClusters")
apprArgs.setDestination(destPipe)
apprOptions = ClientOptions([ ClientOptions.PHASE, "approve" ])
zinggAppr = EZingg(apprArgs, apprOptions)
zinggAppr.initAndExecute()
```

{% hint style="success" icon="right-long" %}
**Read more**: For the full cluster approval workflow → [Cluster Approval](../running-zingg/cluster-approval.md)
{% endhint %}

### Full example

Complete working example combining all the above.

```python
from zingg.client import*
from zingg.pipes import*
from zinggEC.enterprise.common.ApproverArguments import*
from zinggEC.enterprise.common.IncrementalArguments import*
from zinggEC.enterprise.common.MappingMatchType import*
from zinggEC.enterprise.common.epipes import*
from zinggEC.enterprise.common.EArguments import*
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition

args = EArguments()

recId = EFieldDefinition("recId", "string", MatchType.DONT_USE)
recId.setPrimaryKey(True)

fname = EFieldDefinition("fname", "string", MatchType.FUZZY)
lname = EFieldDefinition("lname", "string", MatchType.FUZZY)
stNo = EFieldDefinition("stNo", "string", MatchType.FUZZY)
add1 = EFieldDefinition("add1", "string", MatchType.FUZZY)
add2 = EFieldDefinition("add2", "string", MatchType.FUZZY)
city = EFieldDefinition("city", "string", MatchType.FUZZY)
areacode = EFieldDefinition("areacode", "string", MatchType.FUZZY)
state = EFieldDefinition("state", "string", MatchType.FUZZY)
dob = EFieldDefinition("dob", "string", MatchType.FUZZY)
ssn = EFieldDefinition("ssn", "string", MatchType.FUZZY)

fieldDefs = [
  recId, fname, lname, stNo, add1, add2, city,
  areacode, state, dob,
  ssn
]
args.setFieldDefinition(fieldDefs)

args.setModelId("100")
args.setZinggDir("./models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)
args.setBlockingModel("DEFAULT")

args.setPassthroughExpr("fname = 'matilda'")

dm1 = DeterministicMatching('fname', 'stNo', 'add1')
dm2 = DeterministicMatching('ssn')
dm3 = DeterministicMatching('fname', 'stNo', 'lname')
args.setDeterministicMatchingCondition(dm1, dm2, dm3)

schema = (
  "recId string, fname string, "
  "lname string, stNo string, "
  "add1 string, add2 string, "
  "city string, areacode string, "
  "state string, dob string, "
  "ssn string"
)

inputPipe = ECsvPipe("testFebrl", "examples/febrl/test.csv", schema)
args.setData(inputPipe)

outputPipe = ECsvPipe("resultFebrl", "/tmp/febrlOutput")
outputPipe.setHeader("true")
args.setOutput(outputPipe)

options = ClientOptions([
  ClientOptions.PHASE,
  "trainMatch"
])
zingg = EZingg(args, options)
zingg.initAndExecute()

incrArgs = IncrementalArguments()
incrArgs.setParentArgs(args)

incrPipe = ECsvPipe("testFebrlIncr", "examples/febrl/test-incr.csv", schema)
incrArgs.setIncrementalData(incrPipe)

incrOptions = ClientOptions([
  ClientOptions.PHASE,
  "runIncremental"
])
zinggIncr = EZingg(incrArgs, incrOptions)
zinggIncr.initAndExecute()
```

{% hint style="success" icon="right-long" %}
**Read more**: Auto-generated API reference for every class and method: [Zingg Enterprise Common Python API on GitHub](https://github.com/zinggAI/zingg/blob/main/docs/pythonEC/markdown/zinggEC.md)
{% endhint %}
