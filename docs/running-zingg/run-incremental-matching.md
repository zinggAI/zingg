---
description: >-
  Update the identity graph with new, changed, or deleted records without
  re-running the full match on your entire dataset.
tags:
  - ent
  - tag: enterprise-only
    primary: true
---

# 🚀 Run Incremental Matching

{% hint style="info" icon="building" %}
**Enterprise only.** Incremental matching updates the identity graph and preserves Zingg IDs across runs. Not available in Community.
{% endhint %}

{% hint style="danger" icon="triangle-exclamation" %}
**BEFORE YOU BEGIN:** The initial match phase must have been completed first. Run incremental only after a full match has been run on your base dataset.

Output is written to the same location as your match output. There is no separate output path for\
incremental results.
{% endhint %}

Re-running matching on entire datasets is wasteful and loses the lineage of matched records against a persistent identifier. Incremental matching lets you process new, updated, and deleted\
records and match them to existing clusters, without starting over.

New records that do not find a match receive their own new Zingg ID. Records that match an existing cluster are assigned that cluster's Zingg ID. Cluster merges, unmerges, and assignments happen automatically. Human-approved cluster decisions from previous runs are preserved and not overridden.

{% tabs %}
{% tab title="Enterprise Python" %}
### Step 1: Imports

```python
from zingg.client import *
from zingg.pipes import *
from zinggEC.enterprise.common.epipes import *
from zinggEC.enterprise.common.EArguments import *
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggEC.enterprise.common.IncrementalArguments import *
from zinggES.enterprise.spark.ESparkClient import *
```

### Step 2: Set up base args

```python
args = EArguments()
recId = EFieldDefinition(
    "recId",
    "string",
    MatchType.DONT_USE
)
recId.setPrimaryKey(True)

fname = EFieldDefinition(
    "fname",
    "string",
    MatchType.FUZZY
)
lname = EFieldDefinition(
    "lname",
    "string",
    MatchType.FUZZY
)
stNo = EFieldDefinition(
    "stNo",
    "string",
    MatchType.FUZZY
)
add1 = EFieldDefinition(
    "add1",
    "string",
    MatchType.FUZZY
)
add2 = EFieldDefinition(
    "add2",
    "string",
    MatchType.FUZZY
)
city = EFieldDefinition(
    "city",
    "string",
    MatchType.FUZZY
)
areacode = EFieldDefinition(
    "areacode",
    "string",
    MatchType.FUZZY
)
state = EFieldDefinition(
    "state",
    "string",
    MatchType.FUZZY
)
dob = EFieldDefinition(
    "dob",
    "string",
    MatchType.FUZZY
)
ssn = EFieldDefinition(
    "ssn",
    "string",
    MatchType.FUZZY
)

fieldDefs = [
    recId,
    fname,
    lname,
    stNo,
    add1,
    add2,
    city,
    areacode,
    state,
    dob,
    ssn
]
args.setFieldDefinition(fieldDefs)
args.setModelId("100")
args.setZinggDir("/tmp/models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)

schema = (
    "recId string, fname string, "
    "lname string, stNo string, add1 string, "
    "add2 string, city string, areacode string, "
    "state string, dob string, ssn string"
)

inputPipe = ECsvPipe("testFebrl", "examples/febrl/test.csv", schema)
args.setData(inputPipe)

outputPipe = ECsvPipe("resultFebrl", "/tmp/febrlOutput")
outputPipe.setHeader("true")
args.setOutput(outputPipe)
```

### Step 3: Create `IncrementalArguments`

```python
incrArgs = IncrementalArguments()
incrArgs.setParentArgs(args)
```

{% hint style="success" icon="lightbulb" %}
`setParentArgs()` inherits all base configuration from notebook 01 - field definitions, model ID, zinggDir, and output pipe. You only need to configure the incremental input pipe separately.
{% endhint %}

### Step 4: Configure incremental input pipe

```python
incrPipe = ECsvPipe("testFebrlIncr", "examples/febrl/test-incr.csv", schema)
incrArgs.setIncrementalData(incrPipe)

outputTmpPipe = ECsvPipe("outputTmp", "/tmp/zinggOutput_febrl_tmp")
outputTmpPipe.setHeader("true")
incrArgs.setOutputTmp(outputTmpPipe)
```

### Step 5: Run incremental matching

```python
options = ClientOptions([ClientOptions.PHASE, "runIncremental"])
zingg = EZingg(incrArgs, options)
zingg.initAndExecute()
```

### Step 6: Read output

```python
output = spark.read.csv(
    "/tmp/febrlOutput",
    header = True
)
display(output)
```

{% hint style="info" icon="circle-info" %}
Output is at the same path as your match output. New records get new Zingg IDs. Records matching existing clusters get the existing Zingg ID. No separate output path is needed.
{% endhint %}
{% endtab %}

{% tab title="Enterprise JSON" %}
If using the CLI instead of the Python API, create an `incrementalConf.json` file:

```json
{
  "config": "config.json",
  "incrementalData": [
    {
      "name": "customers_incr",
      "format": "csv",
      "props": {
        "path": "test-incr.csv",
        "delimiter": ",",
        "header": false
      },
      "schema": "recId string, fname string, lname string, stNo string, add1 string, add2 string, city string, state string, areacode string, dob string, ssn string"
    }
  ],
  "outputTmp": {
    "name": "customers_incr_temp",
    "format": "csv",
    "props": {
      "location": "/tmp/zinggOutput_febrl_tmp",
      "delimiter": ",",
      "header": true
    }
  }
}
```

**Run With**

```bash
./scripts/zingg.sh \
  --phase runIncremental \
  --conf <location to incrementalConf.json>
```

The `outputTmp` section specifies a temporary output location where Zingg writes intermediate results before final processing or merging with the main output.
{% endtab %}

{% tab title="Enterprise Snowflake" %}

Zingg on Snowflake can be run either from a local terminal via the CLI, or natively inside Snowflake as a job service.

### CLI (local terminal)

Create an `incrementalConf.json` file:

```json
{
	"config" : "path-to-config-file/configSnow.json",
    "incrementalData": [{
        "name":"INCR_Febrl_5M",
        "format":"snowflake",
        "props": {
            "table": "INCR_Febrl_5M"
        },
        "schema": "id string, fname string, lname string, stNo string, add1 string, add2 string, city string, state string, areacode string, dob string, ssn string" 
	}],
	"outputTmp" :{
        "name":"customers_incr_temp",
        "format":"snowflake",
        "props": {
            "table": "customers_incr_temp"
        }
	}
}
```

**Run With**

```bash
./scripts/zingg.sh \
  --phase runIncremental \
  --conf <location to incrementalConf.json> \
  --properties-file ./snowflakeConn.properties
```

The `outputTmp` section specifies a temporary output location where Zingg writes intermediate results before final processing or merging with the main output.

### Snowflake (job service)

Run the phase as an asynchronous job service inside your Snowflake compute pool.

```sql
EXECUTE JOB SERVICE
IN COMPUTE POOL CONTAINER_ZINGG_POOL
NAME = ZINGG_INCREMENTAL_ASYNC_JOB_SERVICE
ASYNC = true
EXTERNAL_ACCESS_INTEGRATIONS = (ALLOW_ALL_EAI)
FROM @specs SPECIFICATION_TEMPLATE_FILE = '<zingg-job-spec-file>'
USING (PHASE => 'runIncremental', CONFIG => '<config-name>');
```

`<zingg-job-spec-file>` defines the container and job service configuration Zingg needs to run inside Snowpark Container Services. It is maintained by your administrator and referenced from your `@specs` stage.

`<config-name>` is the name of your incremental configuration json file (e.g. `incrementalConf.json`).

**Monitor the job**

Retrieve the service logs to monitor execution and debug any failures.

```sql
SELECT SYSTEM$GET_SERVICE_LOGS('ZINGG_INCREMENTAL_ASYNC_JOB_SERVICE', 0, 'zingg-async-job-container');
```

`zingg-async-job-container` refers to the container name defined in the job specification.

**Stop the compute pool**

Once the job completes, stop the compute pool to release resources.

```sql
ALTER COMPUTE POOL CONTAINER_ZINGG_POOL STOP ALL;
```

{% endtab %}
{% endtabs %}