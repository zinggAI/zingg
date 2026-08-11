---
description: >-
  Walk through the complete Zingg workflow on your local machine using Docker
  install, configure, find candidate pairs, label, train, and run match. Covers
  Community (open source) and Enterprise
---

# Quick Start (Docker)

This page walks you through the full Zingg workflow on your local machine using Docker. You will install Zingg, connect sample data, find candidate pairs, label those pairs, train the model, and run match to see your first results. By the end you will have run every phase of the Zingg workflow and seen entity resolution working on your local machine with real data.

{% hint style="success" icon="right-long" %}
New to entity resolution? Read [Entity Resolution](../zingg-concepts/entity-resolution/) for the problem space and why Zingg's approach works.
{% endhint %}

{% tabs %}
{% tab title="Local Spark (Docker)" %}
{% hint style="success" icon="right-long" %}
Fastest way to get started. Uses Docker and the Zingg Python API. No Spark cluster setup required. Covers Community (open source) and Enterprise - pull the Docker image and follow the same steps for either edition.
{% endhint %}

### Prerequisites

* Docker installed on your machine
* At least 4 GB of free RAM
* Permission to bind-mount local directories (for output and model persistence)

### **Step 1: Pull the Zingg Docker image and start a container**

Pull the Zingg Docker image:

```bash
docker pull zingg/zingg:0.5.0
```

Start a bash session inside the container:

```bash
docker run -it zingg/zingg:0.5.0 bash
```

If you see a permission error, use the volume mount form:

```bash
docker run -v /tmp:/tmp -it zingg/zingg:0.5.0 bash
```

The Zingg Python package is already installed inside the Docker container. Zingg Python programs run via the `zingg.sh` script provided with the Zingg release.

### **Step 2: Use the bundled sample data**

The Docker image ships with the FEBRL sample dataset and a sample config file:

* Sample data: `examples/febrl/test.csv`
* Sample config: `examples/febrl/config.json` (Community) or `examples/febrl/configSnow.json` (Snowflake variant)

If you want to use your own data instead, copy it into the running container from your local machine:

{% code expandable="true" %}
```bash
docker cp /path/to/your-data.csv <container_id>:/zingg/your-data.csv
```
{% endcode %}

{% hint style="danger" icon="right-long" %}
Replace `<container_id>` with your running container's ID (find it with `docker ps`).
{% endhint %}

### **Step 3: Set up the configuration**

Create or edit your configuration. The config defines your field definitions, input and output paths, model ID, and partition settings. Both editions use the same JSON structure — only the Python class differs between editions.

{% hint style="success" icon="right-long" %}
`FUZZY` handles typos and abbreviations. `EXACT` requires character-for-character match. `DONT_USE` excludes a field from matching but keeps it in output. For all match types → [Match Types](../zingg-concepts/how-zingg-learns/match-types/)
{% endhint %}

#### Python - Community

```python
from zingg.client import *
from zingg.pipes import *

args = Arguments()
args.setModelId("100")
args.setZinggDir("models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)

# Field definitions

fname    = FieldDefinition("fname", "string", MatchType.FUZZY)
lname    = FieldDefinition("lname", "string", MatchType.FUZZY)
stNo     = FieldDefinition("stNo", "string", MatchType.FUZZY)
add1     = FieldDefinition("add1", "string", MatchType.FUZZY)
add2     = FieldDefinition("add2", "string", MatchType.FUZZY)
city     = FieldDefinition("city", "string", MatchType.FUZZY)
areacode = FieldDefinition("areacode", "string", MatchType.FUZZY)
state    = FieldDefinition("state", "string", MatchType.FUZZY)
dob      = FieldDefinition("dob", "string", MatchType.EXACT)
ssn      = FieldDefinition("ssn", "string", MatchType.EXACT)

fieldDefs = [fname, lname, stNo, add1, add2, city, areacode, state, dob, ssn]
args.setFieldDefinition(fieldDefs)

# Input schema and pipe
schema = "id string, fname string, lname string, stNo string, add1 string, add2 string, city string, areacode string, state string, dob string, ssn  string"

inputPipe = CsvPipe("testFebrl", "examples/febrl/test.csv", schema)
args.setData(inputPipe)

# Output pipe

outputPipe = CsvPipe("resultFebrl", "/tmp/febrlOutput")
args.setOutput(outputPipe)
                                                        
```

#### Python - Enterprise

```python
from zingg.client import *
from zingg.pipes import *
from zinggEC.enterprise.common.epipes import *
from zinggEC.enterprise.common.EArguments import *
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition

args = EArguments()
args.setModelId("100")
args.setZinggDir("models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)

# Field definitions

fname = EFieldDefinition("fname", "string", MatchType.FUZZY)
lname = EFieldDefinition("lname", "string", MatchType.FUZZY)
stNo = EFieldDefinition("stNo", "string", MatchType.FUZZY)
add1 = EFieldDefinition("add1", "string", MatchType.FUZZY)
add2 = EFieldDefinition("add2", "string", MatchType.FUZZY)
city = EFieldDefinition("city", "string", MatchType.FUZZY)
areacode = EFieldDefinition("areacode", "string", MatchType.FUZZY)
state = EFieldDefinition("state", "string", MatchType.FUZZY)
dob = EFieldDefinition("dob", "string", MatchType.EXACT)
ssn = EFieldDefinition("ssn", "string", MatchType.EXACT)

fieldDefs = [fname, lname, stNo, add1, add2, city, areacode, state, dob, ssn]
args.setFieldDefinition(fieldDefs)

# Input schema and pipe
schema = "id string, fname string, lname string, stNo string, add1 string, add2 string, city string, areacode string, state string, dob string, ssn  string"

inputPipe = ECsvPipe("testFebrl", "examples/febrl/test.csv", schema)
args.setData(inputPipe)

# Output pipe

outputPipe = ECsvPipe("resultFebrl", "/tmp/febrlOutput")
outputPipe.setHeader("true")
args.setOutput(outputPipe)
```

{% hint style="danger" icon="right-long" %}
The JSON config blocks below are the equivalent declarations of the Python above. Use either approach - Python API for in-notebook orchestration, JSON for shell-driven workflows. Community and Enterprise use the same JSON structure with the addition of `outputStats` in Enterprise.
{% endhint %}

#### JSON - Community

```json
{
  "fieldDefinition": [
    {"fieldName": "fname", "matchType": "fuzzy",
     "fields": "fname", "dataType": "string"},
    {"fieldName": "lname", "matchType": "fuzzy",
     "fields": "lname", "dataType": "string"},
    {"fieldName": "stNo", "matchType": "fuzzy",
     "fields": "stNo", "dataType": "string"},
    {"fieldName": "add1", "matchType": "fuzzy",
     "fields": "add1", "dataType": "string"},
    {"fieldName": "add2", "matchType": "fuzzy",
     "fields": "add2", "dataType": "string"},
    {"fieldName": "city", "matchType": "fuzzy",
     "fields": "city", "dataType": "string"},
    {"fieldName": "areacode", "matchType": "fuzzy",
     "fields": "areacode", "dataType": "string"},
    {"fieldName": "state", "matchType": "fuzzy",
     "fields": "state", "dataType": "string"},
    {"fieldName": "dob", "matchType": "exact",
     "fields": "dob", "dataType": "string"},
    {"fieldName": "ssn", "matchType": "exact",
     "fields": "ssn", "dataType": "string"}
  ],
  "data": [{
    "name": "testFebrl",
    "format": "csv",
    "props": {
      "location": "examples/febrl/test.csv",
      "delimiter": ",",
      "header": "false"
    }
  }],
  "output": [{
    "name": "resultFebrl",
    "format": "csv",
    "props": {
      "location": "/tmp/febrlOutput",
      "delimiter": ",",
      "header": "true"
    }
  }],
  "modelId": "100",
  "zinggDir": "models",
  "numPartitions": 4,
  "labelDataSampleSize": 0.5
}
```

#### JSON - Enterprise

```json
{
  "fieldDefinition": [
    {
     "fieldName": "fname",
    "matchType": "fuzzy",
     "fields": "fname", "dataType": "string"},
    {"fieldName": "lname", "matchType": "fuzzy",
     "fields": "lname", "dataType": "string"},
    {"fieldName": "stNo", "matchType": "fuzzy",
     "fields": "stNo", "dataType": "string"},
    {"fieldName": "add1", "matchType": "fuzzy",
     "fields": "add1", "dataType": "string"},
    {"fieldName": "add2", "matchType": "fuzzy",
     "fields": "add2", "dataType": "string"},
    {"fieldName": "city", "matchType": "fuzzy",
     "fields": "city", "dataType": "string"},
    {"fieldName": "areacode", "matchType": "fuzzy",
     "fields": "areacode", "dataType": "string"},
    {"fieldName": "state", "matchType": "fuzzy",
     "fields": "state", "dataType": "string"},
    {"fieldName": "dob", "matchType": "exact",
     "fields": "dob", "dataType": "string"},
    {"fieldName": "ssn", "matchType": "exact",
     "fields": "ssn", "dataType": "string"}
  ],
  "data": [{
    "name": "testFebrl",
    "format": "csv",
    "props": {
      "location": "examples/febrl/test.csv",
      "delimiter": ",",
      "header": "false"
    }
  }],
  "output": [{
    "name": "resultFebrl",
    "format": "csv",
    "props": {
      "location": "/tmp/febrlOutput",
      "delimiter": ",",
      "header": true
    }
  }],
  "outputStats": {
    "name": "stats",
    "format": "csv",
    "props": {
      "location": "/tmp/zinggStats_$ZINGG_DYNAMIC_STAT_NAME",
      "delimiter": ",",
      "header": true
    }
  },
  "modelId": "100",
  "zinggDir": "models",
  "numPartitions": 4,
  "labelDataSampleSize": 0.5
}
```

{% hint style="info" icon="right-long" %}
`outputStats` is Enterprise-only. The `$ZINGG_DYNAMIC_STAT_NAME` placeholder is replaced at runtime with `SUMMARY`, `CLUSTER`, or `RECORD` for the three different stats files Zingg produces.

For full configuration schema with all parameters → [Configuration Schema](../reference/configuration-schema.md)
{% endhint %}

### **Step 4: Find candidate pairs**

Zingg scans your dataset using the field rules defined in Step 3 and selects the most informative pairs for labeling - edge cases where the model has the most to learn. Candidate pairs are written to `zinggDir/modelId`.

#### **Python - Community**

```python
options = ClientOptions([ ClientOptions.PHASE, "findTrainingData" ])

zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### **Python - Enterprise**

```python
options = ClientOptions([ ClientOptions.PHASE, "findTrainingData" ]) 

zingg = EZingg(args, options) 
zingg.initAndExecute()
```

#### CLI (both editions)

```bash
./zingg.sh --phase findTrainingData --conf config.json
```

### **Step 5: Label pairs**

Zingg shows you the pairs selected by `findTrainingData`. For each pair, decide:

* `1` - Match: these records represent the same real-world entity
* `0` - Not a match: these records are different entities
* `2` - Not sure: when you cannot decide

{% hint style="success" icon="right-long" %}
Zingg selects the most informative pairs from your data - not random samples. Label until all field types and data variation patterns in your schema are represented. Repeat Steps 4–5 in a loop if needed. If accuracy needs improvement after the first match run, return to labeling and focus on patterns that are missing or underrepresented.
{% endhint %}

#### **Python - Community**

```python
options = ClientOptions([ ClientOptions.PHASE, "label" ]) 
zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### **Python - Enterprise**

```python
options = ClientOptions([ ClientOptions.PHASE, "label" ])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### CLI (both editions)

```bash
./zingg.sh --phase label --conf config.json --showConcise=true
```

The `--showConcise=true` flag shows only fields used for matching and hides `DONT_USE` fields.

### **Step 6: Train the model**

Once you have enough labelled pairs, run the `train` phase. Zingg builds blocking and similarity models from your labelled training data and persists them to `zinggDir/modelId`. These models can be reused on newer datasets without retraining.

#### **Python - Community**

```python
options = ClientOptions([ ClientOptions.PHASE, "train" ])

zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### **Python - Enterprise**

```python
options = ClientOptions([ ClientOptions.PHASE, "train" ])

zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### CLI (both editions)

```bash
./zingg.sh --phase train --conf config.json
```

### **Step 7: Run match and resolve identities**

Run the `match` phase. Zingg applies the trained models to your full dataset and writes matched records to the output location configured in Step 3.

#### **Python - Community**

```python
options = ClientOptions([ ClientOptions.PHASE, "match" ])

zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### **Python - Enterprise**

```python
options = ClientOptions([ ClientOptions.PHASE, "match" ])

zingg = EZingg(args, options) 
zingg.initAndExecute()
```

#### CLI (both editions)

```bash
./zingg.sh --phase match --conf config.json
```

### **Reading the match output**

Zingg adds output columns to your input. Community produces `Z_CLUSTER`, `Z_MINSCORE`, and `Z_MAXSCORE`. Enterprise produces `ZINGG_ID` (a persistent GUID), `Z_MINSCORE`, and `Z_MAXSCORE`.

For threshold guidance and full output column definitions → [Interpret Output Scores](../interpreting-results/interpret-output-scores.md)
{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
Completed the walkthrough? Next steps:

* Connect your own data - [Connect Data](https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/connect-your-data)
* Full configuration reference - [Configure Zingg](configure-zingg.md)
* Understanding output scores - [Interpreting output](../interpreting-results/interpret-output-scores.md)
{% endhint %}

{% hint style="warning" icon="right-long" %}
Enterprise Quick Start uses EArguments, ECsvPipe, and EZingg in 7 production-grade notebooks. [Talk to us about Enterprise](https://www.zingg.ai/company/contact/contact) to get access to the full notebook sequence.
{% endhint %}
