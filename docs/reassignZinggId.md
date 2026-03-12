---
title: Reassign ZINGG ID
parent: Step By Step Guide
nav_order: 16
description: Reassign the ZINGG IDs for clusters from the original production model
---

# Reassign ZINGG ID

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

## Keep Your IDs Stable Across Changes

When you evolve your Zingg setup—whether upgrading your model, migrating infrastructure, changing data schemas, or moving to new platforms—you want to preserve the ZINGG IDs already flowing through your production systems. The **reassignZinggId** phase intelligently maximizes the preservation of original ZINGG IDs by mapping clusters from your new model back to your original cluster assignments, ensuring continuity and minimizing downstream disruption.

## Why Use Reassign?

- **Model Upgrades**: Add nickname support, improve blocking strategies, or tune classifiers without losing IDs
- **Infrastructure Migration**: Move from one platform to another (e.g., Spark to Snowflake) while maintaining ID continuity
- **Schema Changes**: Update your data schema or field definitions while preserving established IDs
- **Data Migration**: Migrate your data to new systems without disrupting operational workflows
- **Operational Continuity**: Downstream systems continue to work with stable, consistent IDs
- **Intelligent Mapping**: Automatically maximizes the reuse of original ZINGG IDs based on primary key overlap
- **Easy Integration**: No need to update downstream systems when you improve your model

## How It Works

The reassign phase compares two configurations:
1. **Original Configuration**: Your production model with established ZINGG IDs
2. **New Configuration**: Your updated model with improved features, new infrastructure, or schema changes

The reassign phase **maximizes the assignment of original ZINGG IDs** by:
- Identifying clusters in the new output that overlap with original clusters (via primary key matching)
- Reassigning the original ZINGG IDs to matching clusters wherever possible
- Generating new ZINGG IDs only when no match is found in the original clusters

## Usage

### Command Line

```bash
./scripts/zingg.sh --phase reassignZinggId \
  --conf examples/febrl/sparkIncremental/configReassign5M.json \
  --originalZinggId examples/febrl5M/config.json \
  --properties-file config/zingg.conf
```

**Key Parameters:**
- `--conf`: Points to a **wrapper configuration** that references your new/updated model config and specifies the output location for reassigned results
- `--originalZinggId`: Points to your **original production configuration**
- `--properties-file`: Zingg properties file (optional)

**Note:** The `--conf` parameter requires a wrapper configuration (see [Configuration Wrapper](#configuration-wrapper) section below) that includes a `transformedOutputPath` to specify where the reassigned output should be written. This is different from a plain model configuration file.

### Python API

Python users can pass configuration objects directly instead of JSON files.

```python
from zingg.client import *
from zingg.pipes import *
from zinggEC.enterprise.common.EArguments import *
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggES.enterprise.spark.ESparkClient import *
from zinggEC.enterprise.common.TransformedOutputArguments import *
from zinggEC.enterprise.common.EClientOptions import *

# Step 1: Define your ORIGINAL production configuration
# This is your current production setup with established ZINGG IDs
originalArgs = EArguments()
id = EFieldDefinition("id", "string", MatchType.DONT_USE)
id.setPrimaryKey(True)
fname = EFieldDefinition("fname", "string", MatchType.FUZZY)
lname = EFieldDefinition("lname", "string", MatchType.FUZZY)
stNo = EFieldDefinition("stNo", "string", MatchType.FUZZY)
add1 = EFieldDefinition("add1", "string", MatchType.FUZZY)
city = EFieldDefinition("city", "string", MatchType.FUZZY)
areacode = EFieldDefinition("areacode", "string", MatchType.FUZZY)
state = EFieldDefinition("state", "string", MatchType.FUZZY)
dob = EFieldDefinition("dob", "string", MatchType.FUZZY)
ssn = EFieldDefinition("ssn", "string", MatchType.FUZZY)

originalArgs.setFieldDefinition([id, fname, lname, stNo, add1, city, areacode, state, dob, ssn])
originalArgs.setModelId("107")
originalArgs.setZinggDir("./models")
originalArgs.setNumPartitions(4)

schema = "id string, fname string, lname string, stNo string, add1 string, city string, areacode string, state string, dob string, ssn string"
originalInputPipe = ECsvPipe("originalData", "examples/febrl5M/febrl_data_5M.csv.gz", schema)
originalArgs.setData(originalInputPipe)
originalOutputPipe = ECsvPipe("originalOutput", "/tmp/zinggOutputOriginal")
originalOutputPipe.setHeader("true")
originalArgs.setOutput(originalOutputPipe)

# IMPORTANT: trainMatch phase must already be completed for the original configuration
# The reassign phase reads the output from the original model

# Step 2: Define your NEW/UPDATED configuration
# Example: Changed some fields from FUZZY to EXACT matching (model upgrade scenario)
newArgs = EArguments()
id = EFieldDefinition("id", "string", MatchType.DONT_USE)
id.setPrimaryKey(True)
fname = EFieldDefinition("fname", "string", MatchType.FUZZY)
stNo = EFieldDefinition("stNo", "string", MatchType.EXACT)  # Changed from FUZZY
add1 = EFieldDefinition("add1", "string", MatchType.FUZZY)
city = EFieldDefinition("city", "string", MatchType.EXACT)   # Changed from FUZZY
areacode = EFieldDefinition("areacode", "string", MatchType.EXACT)  # Changed from FUZZY
state = EFieldDefinition("state", "string", MatchType.EXACT)  # Changed from FUZZY
dob = EFieldDefinition("dob", "string", MatchType.EXACT)  # Changed from FUZZY

newArgs.setFieldDefinition([id, fname, stNo, add1, city, areacode, state, dob])
newArgs.setModelId("999")
newArgs.setZinggDir("./models")
newArgs.setNumPartitions(4)
newArgs.setLabelDataSampleSize(0.5)

newInputPipe = ECsvPipe("newData", "examples/febrl5M/febrl_data_5M.csv.gz", schema)
newArgs.setData(newInputPipe)
newOutputPipe = ECsvPipe("newOutput", "/tmp/zinggOutputNew")
newOutputPipe.setHeader("true")
newArgs.setOutput(newOutputPipe)

# IMPORTANT: Run trainMatch on the new configuration first
# This generates the output that will be used for reassignment
options = EClientOptions([EClientOptions.PHASE, "trainMatch"])
zinggNew = EZingg(newArgs, options)
zinggNew.initAndExecute()

# Step 3: Create TransformedOutputArguments for reassignZinggId
reassignArgs = TransformedOutputArguments()
reassignArgs.setParentArgs(newArgs)        # New configuration
reassignArgs.setOriginalArgs(originalArgs) # Original configuration

# Set output path for reassigned results
reassignOutputPipe = ECsvPipe("reassignedOutput", "/tmp/zinggReassigned")
reassignOutputPipe.setHeader("true")
reassignArgs.setTransformedOutputPath(reassignOutputPipe)

# Step 4: Execute reassignZinggId
# The phase will read outputs from both configurations and maximize original ID preservation
reassignOptions = EClientOptions([EClientOptions.PHASE, "reassignZinggId"])
zinggReassign = EZingg(reassignArgs, reassignOptions)
zinggReassign.initAndExecute()
```

## Configuration Files

### Configuration Wrapper (`examples/febrl/sparkIncremental/configReassign5M.json`)

This wrapper configuration points to your new configuration and specifies where to write the reassigned output:

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

**Note**: 
- This wrapper only points to the new configuration. The original configuration is specified via the `--originalZinggId` command-line parameter.
- The `name` field in `transformedOutputPath` can be any arbitrary identifier for the output pipe - it's used internally by Zingg to identify this output destination.

### New Configuration (`examples/febrl5M/configUpdated.json`)

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
      "location": "examples/febrl5M/febrl_data_5M.csv.gz",
      "delimiter": ",",
      "header": false
    },
    "schema": "id string, fname string, lname string, stNo string, add1 string, city string, areacode string, state string, dob string, ssn string"
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

### Original Configuration (`examples/febrl5M/config.json`)

Your production configuration with established ZINGG IDs:

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
      "location": "examples/febrl5M/febrl_data_5M.csv.gz",
      "delimiter": ",",
      "header": false
    },
    "schema": "id string, fname string, lname string, stNo string, add1 string, city string, areacode string, state string, dob string, ssn string"
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
  "labelDataSampleSize": 0.01,
  "numPartitions": 4000,
  "modelId": 107,
  "zinggDir": "models"
}
```

## Output Format

The output is written to the `transformedOutputPath` specified in your configuration and contains the same structure as the new model's output, but with reassigned ZINGG IDs that maximize preservation of the original IDs.

## Use Cases

### 1. Model Upgrades
When you add new features like nicknames, better blocking strategies, or improved classifiers, use reassignZinggId to maintain ID continuity.

### 2. Infrastructure Migration
Moving from Spark to Snowflake, or vice versa? Reassign helps you maintain the same ZINGG IDs across platforms.

### 3. Schema Evolution
If your data schema changes (new fields, removed fields, different field types), reassignZinggId preserves IDs for records that can still be matched via primary key.

### 4. Data Migration
Migrating from Databricks to Fabric, or vice versa? When migrating data to new systems or consolidating data sources, maintain ID consistency across the migration.

### 5. Platform Upgrades
Upgrading Spark versions, Snowflake features, or other platform components? Keep your IDs stable through the upgrade process.


[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to https://www.zingg.ai/product/zingg-entity-resolution-compare-versions for individual tier features.
