---
description: >-
  See Zingg Community resolve duplicate records in under 5 minutes no setup, no
  configuration, no data preparation needed.
---

# Quick Start (Docker)

{% hint style="success" icon="right-long" %}
**Prerequisites:** Docker installed on your machine. Nothing else required.
{% endhint %}

The fastest way to understand what Zingg does is to see it work. This page uses Docker and\
a pre-trained model bundled with Zingg to run entity resolution on a sample dataset in three\
commands. There is no installation, no configuration, and no labeling required.

You will see Zingg take records that look different variations in name, address, and\
date of birth, and resolve them into clusters representing the same real-world person. That\
is entity resolution.

### Run Zingg in 3 commands

#### Step 1: Pull the Zingg Docker image

```bash
docker pull zingg/zingg
```

#### Step 2: Start the container

```bash
docker run -it zingg/zingg bash
```

If you see a permission error, use:

```bash
docker run -v /tmp:/tmp -it zingg/zingg bash
```

#### Step 3: Run match using the bundled FEBRL sample data and pre-trained model

```bash
./scripts/zingg.sh --phase match --conf examples/febrl/config.json
```

{% hint style="success" icon="right-long" %}
### **What just happened**

Zingg read the `FEBRL` sample dataset bundled in the Docker image, applied a pre-trained entity resolution model to it, and wrote the resolved output to the output path configured in `examples/febrl/config.json`.

No training was needed because the model was pre-built for this dataset.
{% endhint %}

### Reading the output at /tmp/zinggOutput

The output contains all your input fields plus three columns added by Zingg:

* `Z_CLUSTER` - a unique identifier shared by all records Zingg resolved as the same\
  entity. Records with the same `Z_CLUSTER` value represent the same real-world person\
  in the `FEBRL` dataset.
* `Z_MINSCORE` - the lowest similarity score between any two records in that cluster.\
  Indicates how confidently the weakest link in the cluster was matched.
* `Z_MAXSCORE` - the highest similarity score between any two records in that cluster.\
  Indicates the strongest match within the cluster.

{% hint style="success" icon="right-long" %}
**Read more**: To know more about output scores, check [Interpret Output Score](../interpreting-results/interpret-output-scores.md).
{% endhint %}



<details>

<summary><strong>What is the FEBRL dataset?</strong></summary>

FEBRL (Freely Extensible Biomedical Record Linkage) is a synthetic dataset generator\
that produces realistic person records with deliberate variations. The records contain\
fields for first name, last name, street number, address lines, city, state, date of birth, and a national identifier.

The variations are intentional - the same person appears multiple times with different\
spellings, missing fields, and formatting differences across records. This makes FEBRL\
ideal for demonstrating entity resolution because the correct answer is known: Zingg's\
output can be verified against the ground truth built into the dataset.

Zingg ships with a pre-trained model for FEBRL under the models folder in the release. This is why Experience Zingg requires no training step - the model is already built.

</details>

<details>

<summary><strong>What happens after this?</strong></summary>

Coingratulations! You just experience Zingg uses a pre-trained model on a sample dataset. In a real implementation, you would:

1. Connect your own data using Zingg pipes.
2. Define the fields you want to match on and choose a match type for each.
3. Run `findTrainingData` to generate candidate pairs from your data.
4. Label those pairs as "Match" or "No Match" to teach Zingg what a match looks like in your domain.
5. Train the model on your labeled pairs.
6. Run `match` or `link` on your full dataset.

The Quick Start page walks you through this full workflow on your own platform using sample data provided by the Zingg team.

As long as your input columns and field types are not changing, the same model works - you do not need to retrain. If you change a match type, you can add more labeled pairs on top of existing training data.

</details>

{% hint style="warning" icon="right-long" %}
Enterprise adds persistent identity with Zingg ID, incremental matching, and production-grade notebooks. [Talk to us about Enterprise](https://www.zingg.ai/company/contact/contact)
{% endhint %}

{% tabs %}
{% tab title="Local Spark (Docker)" %}
{% hint style="success" icon="right-long" %}
Fastest way to get started. Uses Docker and the Zingg Python API. No Spark cluster setup required. Covers Community (open source) - pull the Docker image and follow the steps.
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
`FUZZY` handles typos and abbreviations. `EXACT` requires character-for-character match. `DONT_USE` excludes a field from matching but keeps it in output. For all match types → [Match Types](../zingg-concepts/zingg-configuration/field-definition/match-types/)
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
options = ClientOptions([ClientOptions.PHASE, "findTrainingData"])

zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### **Python - Enterprise**

```python
options = ClientOptions([ClientOptions.PHASE, "findTrainingData"])

zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### CLI (both editions)

```bash
./scripts/zingg.sh --phase findTrainingData --conf config.json
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
options = ClientOptions([ClientOptions.PHASE, "label"])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### **Python - Enterprise**

```python
options = ClientOptions([ClientOptions.PHASE, "label"])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### CLI (both editions)

```bash
./scripts/zingg.sh --phase label --conf config.json --showConcise=true
```

The `--showConcise=true` flag shows only fields used for matching and hides `DONT_USE` fields.

### **Step 6: Train the model**

Once you have enough labelled pairs, run the `train` phase. Zingg builds blocking and similarity models from your labelled training data and persists them to `zinggDir/modelId`. These models can be reused on newer datasets without retraining.

#### **Python - Community**

```python
options = ClientOptions([ClientOptions.PHASE, "train"])

zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### **Python - Enterprise**

```python
options = ClientOptions([ClientOptions.PHASE, "train"])

zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### CLI (both editions)

```bash
./scripts/zingg.sh --phase train --conf config.json
```

### **Step 7: Run match and resolve identities**

Run the `match` phase. Zingg applies the trained models to your full dataset and writes matched records to the output location configured in Step 3.

#### **Python - Community**

```python
options = ClientOptions([ClientOptions.PHASE, "match"])

zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### **Python - Enterprise**

```python
options = ClientOptions([ClientOptions.PHASE, "match"])

zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### CLI (both editions)

```bash
./scripts/zingg.sh --phase match --conf config.json
```

### **Reading the match output**

Zingg adds output columns to your input. Community produces `Z_CLUSTER`, `Z_MINSCORE`, and `Z_MAXSCORE`. Enterprise produces `ZINGG_ID` (a persistent GUID), `Z_MINSCORE`, and `Z_MAXSCORE`.

For threshold guidance and full output column definitions → [Interpret Output Scores](../interpreting-results/interpret-output-scores.md)
{% endtab %}
{% endtabs %}



{% hint style="success" icon="right-long" %}
Completed the walkthrough? Next steps:

* Connect your own data - [Connect Data](../connect-your-data/pipes-and-data-connections.md)
* Full configuration reference - [Configure Zingg](configure-zingg.md)
* Understanding output scores - [Interpreting output](../interpreting-results/interpret-output-scores.md)
{% endhint %}

{% hint style="warning" icon="right-long" %}
Enterprise Quick Start uses EArguments, ECsvPipe, and EZingg in 7 production-grade notebooks. [Talk to us about Enterprise](https://www.zingg.ai/company/contact/contact) to get access to the full notebook sequence.
{% endhint %}
