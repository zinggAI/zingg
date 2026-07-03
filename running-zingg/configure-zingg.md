---
description: >-
  Set up your field definitions, pipes, and arguments - the foundation every
  subsequent Zingg phase depends on.
---

# Configure Zingg

Configuring Zingg is the first step in every Zingg workflow. This page follows notebook 01 step by step; defining your fields, configuring your pipes, and creating the arguments object that all subsequent phases inherit.

Complete this page before running any other phase. Every phase reads the configuration set here:

* `findTrainingData` → \[[Create Training Data](create-training-data.md)]
* `label` → \[[Label Training Pairs](label-training-pairs.md)]
* `train` → \[[Build and Save the Model](build-and-save-the-model.md)]
* `match` → \[[Run the Match Phase](run-the-match-phase.md)]
* `link` → \[[Link Across Datasets](link-across-datasets.md)]
* `runIncremental` → \[[Run Incremental Matching](run-incremental-matching.md)]

{% hint style="success" icon="right-long" %}
Full parameter reference with all config options and schema → [Configuration Schema](../reference/configuration-schema.md)
{% endhint %}

{% tabs %}
{% tab title="Community (OS)" %}
### Step 1: Verify installation

```python
from zingg.client import* from zingg.pipes import*
```

Verify in your notebook

```python
!pip show zingg
```

### Step 2: Build the arguments object

#### Python

```python
args = Arguments() args.setModelId("100") args.setZinggDir("models")
           args.setNumPartitions(4) args.setLabelDataSampleSize(0.5)
```

{% hint style="info" icon="right-long" %}
`setModelId` - unique name for this model.&#x20;

`setZinggDir` - where Zingg writes model files and training data.&#x20;

Use the same `modelId` in all subsequent phases for this run.
{% endhint %}

#### JSON

```
{
  "modelId" : "100",
              "zinggDir" : "models",
                           "numPartitions" : 4,
                           "labelDataSampleSize" : 0.5
}
```

#### Understanding `numPartitions` and `labelDataSampleSize`

**`numPartitions`** - The number of Spark partitions over which input data is distributed. Set this to approximately 20–30 times the number of worker cores. This is the most important configuration for performance.

**`labelDataSampleSize`** - Fraction of the data scanned to find candidate pairs. Adjust between `0.0001` and `0.1`. If `findTrainingData` is slow, reduce to `0.05` or lower. If it is too small, Zingg may not find the right edge cases.

### Step 3: Define fields and match types

#### Python

```python
fname = FieldDefinition("fname", "string", MatchType.FUZZY) lname =
    FieldDefinition("lname", "string", MatchType.FUZZY) stNo = FieldDefinition(
        "stNo", "string",
        MatchType.FUZZY) add1 = FieldDefinition("add1", "string",
                                                MatchType.FUZZY) add2 =
        FieldDefinition("add2", "string", MatchType.FUZZY) city =
            FieldDefinition("city", "string", MatchType.FUZZY) areacode =
                FieldDefinition("areacode", "string", MatchType.FUZZY) state =
                    FieldDefinition("state", "string", MatchType.FUZZY) dob =
                        FieldDefinition("dob", "string", MatchType.FUZZY) ssn =
                            FieldDefinition("ssn", "string", MatchType.FUZZY)

                                fieldDefs = [
                                  fname, lname, stNo, add1, add2, city,
                                  areacode, state, dob,
                                  ssn
                                ] args.setFieldDefinition(fieldDefs)
```

#### JSON

```json
{
  "fieldDefinition" : [
    {
      "fieldName" : "fname",
      "matchType" : "FUZZY",
      "fields" : "fname",
      "dataType" : "string"
    },
    {
      "fieldName" : "lname",
      "matchType" : "FUZZY",
      "fields" : "lname",
      "dataType" : "string"
    },
    {
      "fieldName" : "stNo",
      "matchType" : "FUZZY",
      "fields" : "stNo",
      "dataType" : "string"
    },
    {
      "fieldName" : "add1",
      "matchType" : "FUZZY",
      "fields" : "add1",
      "dataType" : "string"
    },
    {
      "fieldName" : "add2",
      "matchType" : "FUZZY",
      "fields" : "add2",
      "dataType" : "string"
    },
    {
      "fieldName" : "city",
      "matchType" : "FUZZY",
      "fields" : "city",
      "dataType" : "string"
    },
    {
      "fieldName" : "areacode",
      "matchType" : "FUZZY",
      "fields" : "areacode",
      "dataType" : "string"
    },
    {
      "fieldName" : "state",
      "matchType" : "FUZZY",
      "fields" : "state",
      "dataType" : "string"
    },
    {
      "fieldName" : "dob",
      "matchType" : "FUZZY",
      "fields" : "dob",
      "dataType" : "string"
    },
    {
      "fieldName" : "ssn",
      "matchType" : "FUZZY",
      "fields" : "ssn",
      "dataType" : "string"
    }
  ]
}
```

{% hint style="success" icon="right-long" %}
**Read more**: Match types reference - [Match types](../zingg-concepts/how-zingg-learns/match-types/) | C[onfiguration schema](../reference/configuration-schema.md)&#x20;
{% endhint %}

### Step 4: Configure input and output pipes

#### Python

```python
schema =
    "id string, fname string, \
lname string, stNo string, add1 string, \
add2 string, city string, areacode string,\
 state string, dob string, ssn string"

    inputPipe = CsvPipe("testFebrl", "examples/febrl/test.csv", schema)
                    args.setData(inputPipe)

                        outputPipe =
        CsvPipe("resultFebrl", "/tmp/febrlOutput") args.setOutput(outputPipe)
```

#### JSON

```
{
  "data" : [ {
    "name" : "testFebrl",
    "format" : "csv",
    "props" : {
      "path" : "examples/febrl/test.csv",
      "delimiter" : ",",
      "header" : "true"
    },
    "schema" : "id string, fname string, lname string, stNo string, add1 "
               "string, add2 string, city string, areacode string, state "
               "string, dob string, ssn string"
  } ],
           "output"
      : [ {
        "name" : "resultFebrl",
        "format" : "csv",
        "props" :
            {"path" : "/tmp/febrlOutput", "delimiter" : ",", "header" : "true"}
      } ]
}
```

#### Configuring through environment variables

If you do not want to pass sensitive values such as passwords through the config file, configure them through system environment variables. Wrap the variable name in dollar signs in your config:

* Strings: `"$var$"` (with quotes)
* Booleans and numerics: `$var$` (without quotes)

```json
{
  "output" : [ {
    "name" : "unifiedCustomers",
    "format" : "net.snowflake.spark.snowflake",
    "props" : {"path" : "$location$", "password" : "$passwd$"}
  } ],
             "labelDataSampleSize" : 0.5,
             "numPartitions" : 4,
             "modelId" : "$modelId$",
                         "zinggDir" : "models",
                                      "collectMetrics" : "$collectMetrics$"
}
```

{% hint style="success" icon="right-long" %}
**Read more**: For all supported connector types and formats - [Connect Your Data](https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/connect-your-data)
{% endhint %}
{% endtab %}

{% tab title="Enterprise" %}
### Step 1: Verify installation and imports

```python
from zingg.client import *
from zingg.pipes import *
from zinggEC.enterprise.common.epipes \
    import *
from zinggEC.enterprise.common.EArguments \
    import *
from zinggEC.enterprise.common.EFieldDefinition \
    import EFieldDefinition
```

Verify the `zinggEC` package is installed by running `!pip show zinggEC` in a separate cell.

### Step 2: Build the Enterprise arguments object

```python
args = EArguments()
args.setModelId("100")
args.setZinggDir("/tmp/models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)
```

Set the blocking strategy directly after `setLabelDataSampleSize`. If not set, the model follows `DEFAULT`.

#### Python

```python
args.setBlockingModel("DEFAULT")
```

#### JSON

```json
{
  "modelId": "100",
  "zinggDir": "/tmp/models",
  "numPartitions": 4,
  "labelDataSampleSize": 0.5
}
```

### Step 3: Define fields with `EFieldDefinition`

#### Python

```python
recId    = EFieldDefinition("recId",
    "string", MatchType.DONT_USE)
recId.setPrimaryKey(True)

fname    = EFieldDefinition("fname",
    "string", MatchType.FUZZY)
lname    = EFieldDefinition("lname",
    "string", MatchType.FUZZY)
stNo     = EFieldDefinition("stNo",
    "string", MatchType.FUZZY)
add1     = EFieldDefinition("add1",
    "string", MatchType.FUZZY)
add2     = EFieldDefinition("add2",
    "string", MatchType.FUZZY)
city     = EFieldDefinition("city",
    "string", MatchType.FUZZY)
areacode = EFieldDefinition("areacode",
    "string", MatchType.FUZZY)
state    = EFieldDefinition("state",
    "string", MatchType.FUZZY)
dob      = EFieldDefinition("dob",
    "string", MatchType.FUZZY)
ssn      = EFieldDefinition("ssn",
    "string", MatchType.FUZZY)

fieldDefs = [recId, fname, lname, stNo,
    add1, add2, city, areacode, state,
    dob, ssn]
args.setFieldDefinition(fieldDefs)
```

{% hint style="info" icon="right-long" %}
Enterprise requires a primary key field for `runIncremental`. Mark the primary key field by calling `recId.setPrimaryKey(True)` if you plan to use incremental matching.
{% endhint %}

### Step 4: Configure input and output pipes

#### Python

```python
schema = ("recId string, fname string, "
    "lname string, stNo string, "
    "add1 string, add2 string, "
    "city string, areacode string, "
    "state string, dob string, "
    "ssn string")

inputPipe = ECsvPipe("testFebrl",
    "examples/febrl/test.csv", schema)
args.setData(inputPipe)

outputPipe = ECsvPipe("resultFebrl",
    "/tmp/febrlOutput")
outputPipe.setHeader("true")
args.setOutput(outputPipe)
```

### Step 5: Deterministic matching (optional)

{% hint style="info" icon="right-long" %}
Deterministic matching - **Enterprise** only.&#x20;

Skip this step if you only need probabilistic matching.
{% endhint %}

#### Python

```python
detMatchNameAdd = DeterministicMatching(
    'fname', 'stNo', 'add1')
detMatchNameDobSsn = DeterministicMatching(
    'fname', 'dob', 'ssn')
detMatchNameEmail = DeterministicMatching(
    'fname', 'email')
args.setDeterministicMatchingCondition(
    detMatchNameAdd,
    detMatchNameDobSsn,
    detMatchNameEmail)
```

#### JSON

```json
{
  "deterministicMatching": [
    {"matchCondition": [
      {"fieldName": "fname"},
      {"fieldName": "stNo"},
      {"fieldName": "add1"}
    ]},
    {"matchCondition": [
      {"fieldName": "fname"},
      {"fieldName": "dob"},
      {"fieldName": "ssn"}
    ]},
    {"matchCondition": [
      {"fieldName": "fname"},
      {"fieldName": "email"}
    ]}
  ]
}
```

### Step 6: Pass Through (optional)

{% hint style="info" icon="right-long" %}
Pass Through - **Enterprise** only.&#x20;

Excludes specific records from matching while still including them in output with their own `Zingg ID`.
{% endhint %}

Pass Through excludes specific records from matching while still including them in output with their own Zingg ID. Records matching the passthrough expression appear in the identity graph but never influence cluster formation.

**Note**: Zingg internally applies the negation of `passthroughExpr` to filter matching records. If the passthrough condition applies to nullable fields, ensure the negative of the expression yields the records that are `NOT` passthrough.

#### Python

```python
args.setPassthroughExpr("fname = 'matilda'")
```

#### JSON

```json
{
  "passthroughExpr": "fname = 'matilda'"
}
```

#### Example with null-safe expression

```json
{
  "passthroughExpr": "is_deceased = true AND is_deceased is NOT NULL"
}
```
{% endtab %}

{% tab title="Enterprise Snowflake" %}
**CHECK WITH SONAL ABOUT THIS TOPIC - NEEDS ENTIRELY DIFFERENT SET OF CONTENT TO BE DISCUSSED LATER.**
{% endtab %}
{% endtabs %}
