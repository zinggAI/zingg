---
description: >-
  The open-source Python interface to Zingg. Configure arguments, define fields,
  build pipes, and run every Community phase from Python.
---

# Community Python API

The Community Zingg Python API is the open source way to run Zingg from Python. It provides classes for arguments, field definitions, client options, pipes, and the Zingg execution client.

Use this package when you want a free, open-source Zingg installation running on Spark. For Enterprise features like deterministic matching, blocking strategy, primary keys, pass-through, mapping match types, and incremental matching, see [Enterprise ZinggEC Python API](enterprise-zinggec-python-api.md).

### Requirements

* Python 3.6+
* Spark 3.5.0

### Install the package

```bash
pip install zingg
```

### Modules and classes

#### **`zingg.client` module**

<table><thead><tr><th width="245.59375" valign="top">Class</th><th valign="top">Purpose</th></tr></thead><tbody><tr><td valign="top"><code>Zingg</code></td><td valign="top">The Zingg execution client. Pass it <code>args</code> and <code>options</code>, call <code>initAndExecute()</code>.</td></tr><tr><td valign="top"><code>ZinggWithSpark</code></td><td valign="top">Same as <code>Zingg</code> but for use when an existing Spark session is already present (e.g. in a Databricks or Fabric notebook).</td></tr><tr><td valign="top"><code>Arguments</code></td><td valign="top">Configuration object holding the model ID, Zingg directory, partition count, field definitions, and pipes.</td></tr><tr><td valign="top"><code>ClientOptions</code></td><td valign="top">Holds the phase name. Pass to the Zingg execution client.</td></tr><tr><td valign="top"><code>FieldDefinition</code></td><td valign="top">Defines a single field with its name, data type, and match type.</td></tr></tbody></table>

#### **`zingg.pipes` module**

<table><thead><tr><th width="249.9453125" valign="top">Class</th><th valign="top">Purpose</th></tr></thead><tbody><tr><td valign="top"><code>Pipe</code></td><td valign="top">Base class for any input or output. Use for generic JDBC, custom formats, or any source where the specialised classes do not fit.</td></tr><tr><td valign="top"><code>CsvPipe</code></td><td valign="top">Read or write CSV files.</td></tr><tr><td valign="top"><code>BigQueryPipe</code></td><td valign="top">Read from or write to Google BigQuery.</td></tr><tr><td valign="top"><code>SnowflakePipe</code></td><td valign="top">Read from or write to Snowflake (via Spark connector).</td></tr></tbody></table>

{% hint style="success" icon="right-long" %}
Full auto-generated method signatures for every class above are at the [Zingg Python OSS API reference on GitHub](https://github.com/zinggAI/zingg/blob/main/docs/pythonOss/markdown/zingg.client.md).
{% endhint %}

### Imports

```python
from zingg.client import *
from zingg.pipes import *
```

### Build the arguments object

```python
args = Arguments()
args.setModelId("100")
args.setZinggDir("models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)
```

### Define fields and match types

```python
fname = FieldDefinition("fname", "string", MatchType.FUZZY)
lname = FieldDefinition("lname", "string", MatchType.FUZZY)
stNo = FieldDefinition("stNo", "string", MatchType.FUZZY)
add1 = FieldDefinition("add1", "string", MatchType.FUZZY)
add2 = FieldDefinition("add2", "string", MatchType.FUZZY)
city = FieldDefinition("city", "string", MatchType.FUZZY)
areacode = FieldDefinition("areacode", "string", MatchType.FUZZY)
state = FieldDefinition("state", "string", MatchType.FUZZY)
dob = FieldDefinition("dob", "string", MatchType.FUZZY)
ssn = FieldDefinition("ssn", "string", MatchType.FUZZY)

fieldDefs = [
  fname, lname, stNo, add1, add2, city,
  areacode, state, dob,
  ssn
]
args.setFieldDefinition(fieldDefs)
```

{% hint style="success" icon="right-long" %}
**Read more**: For all match types and combinations → [Match Types](../zingg-concepts/how-zingg-learns/match-types/)
{% endhint %}

### Configure input and output pipes

#### CSV pipe example:

```python
schema = (
  "id string, fname string, "
  "lname string, stNo string, "
  "add1 string, add2 string, "
  "city string, areacode string, "
  "state string, dob string, "
  "ssn string"
)

inputPipe = CsvPipe("testFebrl", "examples/febrl/test.csv", schema)
args.setData(inputPipe)

outputPipe = CsvPipe("resultFebrl", "/tmp/febrlOutput")
args.setOutput(outputPipe)
```

#### For BigQuery

```python
bqPipe = BigQueryPipe("bqInput", "your-project.your-dataset.your-table")
```

#### For Snowflake

```python
snowPipe = SnowflakePipe("snowInput", "your-snowflake-table")
```

#### For generic pipes

```python
genericPipe = Pipe("genericInput", "jdbc")
genericPipe.addProperty("url", "jdbc:postgresql://host:5432/db")
genericPipe.addProperty("dbtable", "customers")
genericPipe.addProperty("driver", "org.postgresql.Driver")
genericPipe.addProperty("user", "your_user")
genericPipe.addProperty("password", "your_password")
```

{% hint style="success" icon="right-long" %}
**Read more**: For all connector formats and full pipe configuration → [Connect Your Data](https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/connect-your-data)
{% endhint %}

### Execute Zingg phases

Run any phase by passing its name to `ClientOptions`. The same pattern works for `findTrainingData`, `label`, `train`, `match`, `link`, `findAndLabel`, `updateLabel`, `generateDocs`, and any other Community phase.

#### **Run `findTrainingData`:**

```python
options = ClientOptions([ ClientOptions.PHASE, "findTrainingData" ])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### **Run `label`:**

```python
options = ClientOptions([ ClientOptions.PHASE, "label" ])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### **Run `findAndLabel`**

Combines `findTrainingData` and `label` into one call — use for smaller datasets where `findTrainingData` runs quickly.

```python
options = ClientOptions([ ClientOptions.PHASE, "findAndLabel" ])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### **Run `updateLabel`**

Revisit and correct previously marked pairs — run `generateDocs` first to identify pairs to update

```python
options = ClientOptions([ ClientOptions.PHASE, "updateLabel" ])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### **Run `generateDocs`**

```python
options = ClientOptions([ ClientOptions.PHASE, "generateDocs" ])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### **Run `train`**

```python
options = ClientOptions([ ClientOptions.PHASE, "train" ])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### **Run `match`**

```python
options = ClientOptions([ ClientOptions.PHASE, "match" ])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### **Run `link`**

```python
options = ClientOptions([ ClientOptions.PHASE, "link" ])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

### Using `ZinggWithSpark` in notebooks

When running inside a Databricks, Fabric, or other notebook where a Spark session already exists, use `ZinggWithSpark` instead of `Zingg`:

```python
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()
```

`ZinggWithSpark` reuses the existing Spark session instead of creating a new one. This is the recommended class for notebook environments.

### Full example

Complete working example combining all the steps above:

```python
from zingg.client import *
from zingg.pipes import *

args = Arguments()

fname = FieldDefinition("fname", "string", MatchType.FUZZY)
lname = FieldDefinition("lname", "string", MatchType.FUZZY)
stNo = FieldDefinition("stNo", "string", MatchType.FUZZY)
add1 = FieldDefinition("add1", "string", MatchType.FUZZY)
add2 = FieldDefinition("add2", "string", MatchType.FUZZY)
city = FieldDefinition("city", "string", MatchType.FUZZY)
areacode = FieldDefinition("areacode", "string", MatchType.FUZZY)
state = FieldDefinition("state", "string", MatchType.FUZZY)
dob = FieldDefinition("dob", "string", MatchType.FUZZY)
ssn = FieldDefinition("ssn", "string", MatchType.FUZZY)

fieldDefs = [
  fname, lname, stNo, add1, add2, city,
  areacode, state, dob,
  ssn
]
args.setFieldDefinition(fieldDefs)

args.setModelId("100")
args.setZinggDir("models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)

schema = (
  "id string, fname string, "
  "lname string, stNo string, "
  "add1 string, add2 string, "
  "city string, areacode string, "
  "state string, dob string, "
  "ssn string"
)

inputPipe = CsvPipe("testFebrl", "examples/febrl/test.csv", schema)
args.setData(inputPipe)

outputPipe = CsvPipe("resultFebrl", "/tmp/febrlOutput")
args.setOutput(outputPipe)

options = ClientOptions([
  ClientOptions.PHASE,
  "match"
])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

### Example notebooks and reference

Working example notebooks are at `github.com/zinggAI/zingg/tree/main/examples/febrl`. Start with `FebrlExample.py`.

{% hint style="success" icon="right-long" %}
**Read more**: Auto-generated API reference for every class and method: [Zingg Community Python API on GitHub](https://github.com/zinggAI/zingg/blob/main/docs/pythonOss/markdown/zingg.client.md)
{% endhint %}
