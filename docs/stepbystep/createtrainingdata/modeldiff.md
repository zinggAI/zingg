---
title: Comparing two models
parent: Step By Step Guide
nav_order: 15
description: Comparison of two outputs with different models
---

# Model Difference

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

## Understand Your Model Changes with Precision

When you evolve your Zingg model—changing field definitions from fuzzy to exact, adding new match types, introducing deterministic matching rules, or tuning blocking strategies—you need to understand the impact. The **diff** phase provides a detailed comparison between two model outputs, showing you exactly which records and clusters were affected by your changes. Make data-driven decisions about which model performs better for your use case.

## Why Use Diff?

- **Model Comparison**: Understand the impact of changing field types (fuzzy → exact, adding new match types)
- **Validation & Testing**: Verify that model improvements actually produce better results
- **Impact Analysis**: See which specific records and clusters changed between model versions
- **Confidence Building**: Make informed decisions about deploying new models to production
- **A/B Testing**: Compare different model configurations to find the optimal setup
- **Regression Detection**: Ensure new model changes don't degrade match quality
- **Cluster Evolution**: Track how clusters merge, split, or change as your model evolves

## How It Works

The diff phase compares two configurations:
1. **Original Configuration**: Your baseline model (e.g., with fuzzy matching)
2. **New Configuration**: Your updated model (e.g., with exact matching or new rules)

The diff phase analyzes both outputs and identifies:
- **Records that moved between clusters** due to model changes
- **New clusters** created in the updated model
- **Merged or split clusters** between the two models


## Usage

### Command Line

```bash
./scripts/zingg.sh --phase diff \
  --conf examples/febrl/sparkIncremental/configdiff.json \
  --compareTo examples/febrl/configBaseline.json \
  --properties-file config/zingg.conf
```

**Key Parameters:**
- `--conf`: Points to your **new/updated configuration** (the model you want to evaluate)
- `--compareTo`: Points to your **original/baseline configuration** (the model you're comparing against)
- `--properties-file`: Zingg properties file (optional)

### Python API

Python users can pass configuration objects directly instead of JSON files. See the complete example in `diffExample.py` below.

```python
from zingg.client import *
from zingg.pipes import *
from zinggEC.enterprise.common.EArguments import *
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggES.enterprise.spark.ESparkClient import *
from zinggEC.enterprise.common.TransformedOutputArguments import *
from zinggEC.enterprise.common.EClientOptions import *

# Step 1: Define your ORIGINAL/BASELINE configuration
# This is the model you're comparing against (e.g., with fuzzy matching)
originalArgs = EArguments()
id = EFieldDefinition("id", "string", MatchType.DONT_USE)
id.setPrimaryKey(True)
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

originalArgs.setFieldDefinition([id, fname, lname, stNo, add1, add2, city, areacode, state, dob, ssn])
originalArgs.setModelId("100")
originalArgs.setZinggDir("./models")
originalArgs.setNumPartitions(4)

schema = "id string, fname string, lname string, stNo string, add1 string, add2 string, city string, areacode string, state string, dob string, ssn string"
originalInputPipe = ECsvPipe("originalData", "examples/febrl/test.csv", schema)
originalArgs.setData(originalInputPipe)
originalOutputPipe = ECsvPipe("originalOutput", "/tmp/zinggOutputOriginal")
originalOutputPipe.setHeader("true")
originalArgs.setOutput(originalOutputPipe)

# IMPORTANT: trainMatch phase must already be completed for the original configuration
# The diff phase reads the output from the original model

# Step 2: Define your NEW/UPDATED configuration
# Example: Changed some fields from FUZZY to EXACT, added deterministic matching
newArgs = EArguments()
id = EFieldDefinition("id", "string", MatchType.DONT_USE)
id.setPrimaryKey(True)
fname = EFieldDefinition("fname", "string", MatchType.FUZZY)
lname = EFieldDefinition("lname", "string", MatchType.FUZZY)
stNo = EFieldDefinition("stNo", "string", MatchType.EXACT)      # Changed from FUZZY
add1 = EFieldDefinition("add1", "string", MatchType.FUZZY)
add2 = EFieldDefinition("add2", "string", MatchType.FUZZY)
city = EFieldDefinition("city", "string", MatchType.EXACT)      # Changed from FUZZY
areacode = EFieldDefinition("areacode", "string", MatchType.EXACT)  # Changed from FUZZY
state = EFieldDefinition("state", "string", MatchType.EXACT)    # Changed from FUZZY
dob = EFieldDefinition("dob", "string", MatchType.EXACT)        # Changed from FUZZY
ssn = EFieldDefinition("ssn", "string", MatchType.FUZZY)

newArgs.setFieldDefinition([id, fname, lname, stNo, add1, add2, city, areacode, state, dob, ssn])
newArgs.setModelId("999")
newArgs.setZinggDir("./models")
newArgs.setNumPartitions(4)

# Added deterministic matching rules
dm1 = DeterministicMatching('fname', 'stNo', 'add1')
dm2 = DeterministicMatching('ssn')
newArgs.setDeterministicMatchingCondition(dm1, dm2)

newInputPipe = ECsvPipe("newData", "examples/febrl/test.csv", schema)
newArgs.setData(newInputPipe)
newOutputPipe = ECsvPipe("newOutput", "/tmp/zinggOutputNew")
newOutputPipe.setHeader("true")
newArgs.setOutput(newOutputPipe)

# IMPORTANT: Run trainMatch on the new configuration first
options = EClientOptions([EClientOptions.PHASE, "trainMatch"])
zinggNew = EZingg(newArgs, options)
zinggNew.initAndExecute()

# Step 3: Create TransformedOutputArguments for diff
diffArgs = TransformedOutputArguments()
diffArgs.setParentArgs(newArgs)        # New configuration
diffArgs.setCompareToArgs(originalArgs) # Original configuration to compare against

# Set output path for diff results
diffOutputPipe = ECsvPipe("diffOutput", "/tmp/zinggDiff")
diffOutputPipe.setHeader("true")
diffArgs.setTransformedOutputPath(diffOutputPipe)

# Step 4: Execute diff phase
diffOptions = EClientOptions([EClientOptions.PHASE, "diff"])
zinggDiff = EZingg(diffArgs, diffOptions)
zinggDiff.initAndExecute()
```

## Configuration Files

### Configuration Wrapper (`examples/febrl/sparkIncremental/configdiff.json`)

This wrapper configuration points to your new configuration and specifies where to write the diff output:

```json
{
    "config": "$ZINGG_ENT_REPO$/spark/examples/febrl/configdiffUpdated.json",
    "transformedOutputPath": {
        "name": "newOutput",
        "format": "csv",
        "props": {
            "location": "/tmp/zinggTransformedOutputDiff",
            "delimiter": ",",
            "header": true
        }
    }
}
```

**Note**: This wrapper only points to the new configuration. The original/baseline configuration is specified via the `--compareTo` command-line parameter.

### New Configuration (`examples/febrl/configdiffUpdated.json`)

Your updated model configuration with changes—for example, switching some fields from `fuzzy` to `exact` matching:

```json
{
  "fieldDefinition": [
    {
      "fieldName": "id",
      "matchType": "dont_use",
      "fields": "id",
      "dataType": "string",
      "primaryKey": true
    },
    {
      "fieldName": "fname",
      "matchType": "fuzzy",
      "fields": "fname",
      "dataType": "string"
    },
    {
      "fieldName": "lname",
      "matchType": "fuzzy",
      "fields": "lname",
      "dataType": "string"
    },
    {
      "fieldName": "stNo",
      "matchType": "exact",
      "fields": "stNo",
      "dataType": "string"
    },
    {
      "fieldName": "add1",
      "matchType": "fuzzy",
      "fields": "add1",
      "dataType": "string"
    },
    {
      "fieldName": "city",
      "matchType": "exact",
      "fields": "city",
      "dataType": "string"
    },
    {
      "fieldName": "areacode",
      "matchType": "exact",
      "fields": "areacode",
      "dataType": "string"
    },
    {
      "fieldName": "state",
      "matchType": "exact",
      "fields": "state",
      "dataType": "string"
    },
    {
      "fieldName": "dob",
      "matchType": "exact",
      "fields": "dob",
      "dataType": "string"
    }
  ],
  "output": [{
    "name": "OUTPUT_FEBRL",
    "format": "csv",
    "props": {
      "location": "/tmp/zinggOutputNew/",
      "delimiter": ",",
      "header": true
    }
  }],
  "data": [{
    "name": "test",
    "format": "csv",
    "props": {
      "location": "examples/febrl/testmodeldiff.csv",
      "delimiter": ",",
      "header": false
    },
    "schema": "id string, fname string, lname string, stNo string, add1 string, add2 string, city string, state string, areacode string, dob string, ssn string"
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
  "labelDataSampleSize": 0.5,
  "numPartitions": 4,
  "modelId": 999,
  "zinggDir": "models"
}
```

### Original Configuration (`examples/febrl/configBaseline.json`)

Your baseline model configuration (the one you're comparing against):

```json
{
  "fieldDefinition": [
    {
      "fieldName": "id",
      "matchType": "dont_use",
      "fields": "id",
      "dataType": "string",
      "primaryKey": true
    },
    {
      "fieldName": "fname",
      "matchType": "fuzzy",
      "fields": "fname",
      "dataType": "string"
    },
    {
      "fieldName": "lname",
      "matchType": "fuzzy",
      "fields": "lname",
      "dataType": "string"
    },
    {
      "fieldName": "stNo",
      "matchType": "fuzzy",
      "fields": "stNo",
      "dataType": "string"
    },
    {
      "fieldName": "add1",
      "matchType": "fuzzy",
      "fields": "add1",
      "dataType": "string"
    },
    {
      "fieldName": "add2",
      "matchType": "fuzzy",
      "fields": "add2",
      "dataType": "string"
    },
    {
      "fieldName": "city",
      "matchType": "fuzzy",
      "fields": "city",
      "dataType": "string"
    },
    {
      "fieldName": "areacode",
      "matchType": "fuzzy",
      "fields": "areacode",
      "dataType": "string"
    },
    {
      "fieldName": "state",
      "matchType": "fuzzy",
      "fields": "state",
      "dataType": "string"
    },
    {
      "fieldName": "dob",
      "matchType": "fuzzy",
      "fields": "dob",
      "dataType": "string"
    },
    {
      "fieldName": "ssn",
      "matchType": "fuzzy",
      "fields": "ssn",
      "dataType": "string"
    }
  ],
  "output": [{
    "name": "output",
    "format": "csv",
    "props": {
      "location": "/tmp/zinggOutputOriginal",
      "delimiter": ",",
      "header": true
    }
  }],
  "data": [{
    "name": "test",
    "format": "csv",
    "props": {
      "location": "examples/febrl/testmodeldiff.csv",
      "delimiter": ",",
      "header": false
    },
    "schema": "id string, fname string, lname string, stNo string, add1 string, add2 string, city string, state string, areacode string, dob string, ssn string"
  }],
  "deterministicMatching": [
    {
      "matchCondition": [
        {"fieldName": "fname"},
        {"fieldName": "stNo"},
        {"fieldName": "add1"}
      ]
    },
    {
      "matchCondition": [
        {"fieldName": "ssn"}
      ]
    }
  ],
  "outputStats": {
    "name": "stats",
    "format": "csv",
    "props": {
      "location": "/tmp/zinggStats_$ZINGG_DYNAMIC_STAT_NAME",
      "delimiter": ",",
      "header": true
    }
  },
  "labelDataSampleSize": 0.5,
  "numPartitions": 4,
  "modelId": 100,
  "zinggDir": "models"
}
```

## Output Format

The diff output contains **only the records that were impacted** by the model changes, making it easy to focus on what actually changed.

### Output Fields

The diff output includes:
- **Primary Keys of records** from both configurations
- **ZINGG_ID_UPDATED**: ZINGG ID from the new model
- **ZINGG_ID_ORIGINAL**: Cluster ID from the new model


This allows you to see side-by-side how each record's cluster assignment changed between the two models.

##  Enhanced Diff Output with Outer Join
Coming Soon.
We're developing an enhanced diff that will provide even more comprehensive comparison capabilities. This enhanced diff will include:

- **All records from both models**, including those that appear in only one output
- **Enhanced null handling**: Intelligent merging of primary key columns when records appear in only one model


## Use Cases

### 1. Field Type Changes
Compare fuzzy vs. exact matching to find the right balance between precision and recall.

### 2. Deterministic Matching Evaluation
See how adding or changing deterministic matching rules affects your clusters.

### 3. Blocking Strategy Comparison
Compare DEFAULT vs. WIDER blocking strategies to understand the performance/accuracy trade-off.

### 4. Model Validation
Validate that model improvements (nicknames, better classifiers, etc.) actually improve results.

### 5. Regression Testing
Ensure that model updates don't accidentally degrade match quality for specific data patterns.

### 6. Configuration Tuning
Experiment with different labelDataSampleSize, numPartitions, or other parameters and see the impact.


[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to https://www.zingg.ai/product/zingg-entity-resolution-compare-versions for individual tier features.
