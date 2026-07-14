<!-- Zingg Enterprise documentation master file, created by
sphinx-quickstart on Wed Jul 16 14:57:19 2025.
You can adapt this file completely to your liking, but it should at least
contain the root `toctree` directive. -->

# Zingg Enterpise Entity Resolution Python Package

Zingg Enterprise Python APIs for entity resolution, identity resolution, record linkage, data mastering and deduplication using ML
([https://www.zingg.ai](https://www.zingg.ai))

#### NOTE
Requires python 3.6+; spark 3.5.0
Otherwise, `zinggES.enterprise.spark.ESparkClient()` cannot be executed

* [Zingg Enterprise Common Package](zinggEC.md)
  * [ApproverArguments](ApproverArguments.md)
    * [zinggEC.enterprise.common.ApproverArguments](ApproverArguments.md#zinggec-enterprise-common-approverarguments)
    * [`ApproverArguments`](ApproverArguments.md#zinggEC.enterprise.common.ApproverArguments.ApproverArguments)
      * [`ApproverArguments.getApprovalQuery()`](ApproverArguments.md#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.getApprovalQuery)
      * [`ApproverArguments.getArgs()`](ApproverArguments.md#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.getArgs)
      * [`ApproverArguments.getDestination()`](ApproverArguments.md#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.getDestination)
      * [`ApproverArguments.getParentArgs()`](ApproverArguments.md#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.getParentArgs)
      * [`ApproverArguments.setApprovalQuery()`](ApproverArguments.md#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.setApprovalQuery)
      * [`ApproverArguments.setArgs()`](ApproverArguments.md#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.setArgs)
      * [`ApproverArguments.setDestination()`](ApproverArguments.md#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.setDestination)
      * [`ApproverArguments.setParentArgs()`](ApproverArguments.md#zinggEC.enterprise.common.ApproverArguments.ApproverArguments.setParentArgs)
  * [IncrementalArguments](IncrementalArguments.md)
    * [zinggEC.enterprise.common.IncrementalArguments](IncrementalArguments.md#zinggec-enterprise-common-incrementalarguments)
    * [`IncrementalArguments`](IncrementalArguments.md#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments)
      * [`IncrementalArguments.getArgs()`](IncrementalArguments.md#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.getArgs)
      * [`IncrementalArguments.getDeleteAction()`](IncrementalArguments.md#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.getDeleteAction)
      * [`IncrementalArguments.getDeletedData()`](IncrementalArguments.md#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.getDeletedData)
      * [`IncrementalArguments.getIncrementalData()`](IncrementalArguments.md#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.getIncrementalData)
      * [`IncrementalArguments.getOutputTmp()`](IncrementalArguments.md#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.getOutputTmp)
      * [`IncrementalArguments.getParentArgs()`](IncrementalArguments.md#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.getParentArgs)
      * [`IncrementalArguments.setArgs()`](IncrementalArguments.md#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.setArgs)
      * [`IncrementalArguments.setDeleteAction()`](IncrementalArguments.md#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.setDeleteAction)
      * [`IncrementalArguments.setDeletedData()`](IncrementalArguments.md#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.setDeletedData)
      * [`IncrementalArguments.setIncrementalData()`](IncrementalArguments.md#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.setIncrementalData)
      * [`IncrementalArguments.setOutputTmp()`](IncrementalArguments.md#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.setOutputTmp)
      * [`IncrementalArguments.setParentArgs()`](IncrementalArguments.md#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments.setParentArgs)
  * [MappingMatchType](MappingMatchType.md)
    * [zinggEC.enterprise.common.MappingMatchType](MappingMatchType.md#zinggec-enterprise-common-mappingmatchtype)
    * [`MappingMatchType`](MappingMatchType.md#zinggEC.enterprise.common.MappingMatchType.MappingMatchType)
      * [`MappingMatchType.getMappingMatchType()`](MappingMatchType.md#zinggEC.enterprise.common.MappingMatchType.MappingMatchType.getMappingMatchType)
  * [epipes](epipes.md)
    * [zinggEC.enterprise.common.epipes](epipes.md#zinggec-enterprise-common-epipes)
    * [`ECsvPipe`](epipes.md#zinggEC.enterprise.common.epipes.ECsvPipe)
      * [`ECsvPipe.setDelimiter()`](epipes.md#zinggEC.enterprise.common.epipes.ECsvPipe.setDelimiter)
      * [`ECsvPipe.setHeader()`](epipes.md#zinggEC.enterprise.common.epipes.ECsvPipe.setHeader)
      * [`ECsvPipe.setLocation()`](epipes.md#zinggEC.enterprise.common.epipes.ECsvPipe.setLocation)
    * [`EPipe`](epipes.md#zinggEC.enterprise.common.epipes.EPipe)
      * [`EPipe.getPassthroughExpr()`](epipes.md#zinggEC.enterprise.common.epipes.EPipe.getPassthroughExpr)
      * [`EPipe.getPassthruData()`](epipes.md#zinggEC.enterprise.common.epipes.EPipe.getPassthruData)
      * [`EPipe.getUsableData()`](epipes.md#zinggEC.enterprise.common.epipes.EPipe.getUsableData)
      * [`EPipe.hasPassThru()`](epipes.md#zinggEC.enterprise.common.epipes.EPipe.hasPassThru)
      * [`EPipe.setPassthroughExpr()`](epipes.md#zinggEC.enterprise.common.epipes.EPipe.setPassthroughExpr)
    * [`InMemoryPipe`](epipes.md#zinggEC.enterprise.common.epipes.InMemoryPipe)
      * [`InMemoryPipe.getDataset()`](epipes.md#zinggEC.enterprise.common.epipes.InMemoryPipe.getDataset)
      * [`InMemoryPipe.setDataset()`](epipes.md#zinggEC.enterprise.common.epipes.InMemoryPipe.setDataset)
    * [`UCPipe`](epipes.md#zinggEC.enterprise.common.epipes.UCPipe)
      * [`UCPipe.setTable()`](epipes.md#zinggEC.enterprise.common.epipes.UCPipe.setTable)
  * [EArguments](EArguments.md)
    * [zinggEC.enterprise.common.EArguments](EArguments.md#zinggec-enterprise-common-earguments)
    * [`DeterministicMatching`](EArguments.md#zinggEC.enterprise.common.EArguments.DeterministicMatching)
      * [`DeterministicMatching.getDeterministicMatching()`](EArguments.md#zinggEC.enterprise.common.EArguments.DeterministicMatching.getDeterministicMatching)
    * [`EArguments`](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments)
      * [`EArguments.getArgs()`](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments.getArgs)
      * [`EArguments.getData()`](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments.getData)
      * [`EArguments.getDeterministicMatching()`](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments.getDeterministicMatching)
      * [`EArguments.getFieldDefinition()`](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments.getFieldDefinition)
      * [`EArguments.getOutputStats()`](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments.getOutputStats)
      * [`EArguments.getPassthroughExpr()`](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments.getPassthroughExpr)
      * [`EArguments.getPrimaryKey()`](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments.getPrimaryKey)
      * [`EArguments.setArgs()`](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments.setArgs)
      * [`EArguments.setBlockingModel()`](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments.setBlockingModel)
      * [`EArguments.setData()`](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments.setData)
      * [`EArguments.setDeterministicMatchingCondition()`](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments.setDeterministicMatchingCondition)
      * [`EArguments.setFieldDefinition()`](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments.setFieldDefinition)
      * [`EArguments.setOutputStats()`](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments.setOutputStats)
      * [`EArguments.setPassthroughExpr()`](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments.setPassthroughExpr)
  * [EFieldDefinition](EFieldDefinition.md)
    * [zinggEC.enterprise.common.EFieldDefinition](EFieldDefinition.md#zinggec-enterprise-common-efielddefinition)
    * [`EFieldDefinition`](EFieldDefinition.md#zinggEC.enterprise.common.EFieldDefinition.EFieldDefinition)
      * [`EFieldDefinition.getMatchTypeArray()`](EFieldDefinition.md#zinggEC.enterprise.common.EFieldDefinition.EFieldDefinition.getMatchTypeArray)
      * [`EFieldDefinition.getPrimaryKey()`](EFieldDefinition.md#zinggEC.enterprise.common.EFieldDefinition.EFieldDefinition.getPrimaryKey)
      * [`EFieldDefinition.setPrimaryKey()`](EFieldDefinition.md#zinggEC.enterprise.common.EFieldDefinition.EFieldDefinition.setPrimaryKey)

# API Reference

* [Module Index](py-modindex.md)
* [Index](genindex.md)
* [Search Page](search.md)

# Example API Usage

```python
from zingg.client import *
from zingg.pipes import *
from zinggEC.enterprise.common.ApproverArguments import *
from zinggEC.enterprise.common.IncrementalArguments import *
from zinggEC.enterprise.common.MappingMatchType import *
from zinggEC.enterprise.common.epipes import *
from zinggEC.enterprise.common.EArguments import *
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggES.enterprise.spark.ESparkClient import *
import os

#build the arguments for zingg
args = EArguments()
#set field definitions
recId = EFieldDefinition("recId", "string", MatchType.DONT_USE)
recId.setPrimaryKey(True)
fname = EFieldDefinition("fname", "string", MatchType.FUZZY)
# for mapping match type
#fname = EFieldDefinition("fname", "string", MatchType.FUZZY, MappingMatchType("MAPPING", "NICKNAMES_TEST"))
lname = EFieldDefinition("lname", "string", MatchType.FUZZY)
stNo = EFieldDefinition("stNo", "string", MatchType.FUZZY)
add1 = EFieldDefinition("add1","string", MatchType.FUZZY)
add2 = EFieldDefinition("add2", "string", MatchType.FUZZY)
city = EFieldDefinition("city", "string", MatchType.FUZZY)
areacode = EFieldDefinition("areacode", "string", MatchType.FUZZY)
state = EFieldDefinition("state", "string", MatchType.FUZZY)
dob = EFieldDefinition("dob", "string", MatchType.FUZZY)
ssn = EFieldDefinition("ssn", "string", MatchType.FUZZY)

fieldDefs = [recId, fname, lname, stNo, add1, add2, city, areacode, state, dob, ssn]
args.setFieldDefinition(fieldDefs)
#set the modelid and the zingg dir
args.setModelId("100")
args.setZinggDir("./models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)

# Set the blocking strategy for the Zingg Model as either DEFAULT or WIDER - if you do not set anything, the model follows DEFAULT strategy
args.setBlockingModel("DEFAULT")

#setting pass thru condition
args.setPassthroughExpr("fname = 'matilda'")

#setting deterministic matching conditions
dm1 = DeterministicMatching('fname','stNo','add1')
dm2 = DeterministicMatching('ssn')
dm3 = DeterministicMatching('fname','stNo','lname')
args.setDeterministicMatchingCondition(dm1,dm2,dm3)

#reading dataset into inputPipe and setting it up in 'args'
#below line should not be required if you are reading from in memory dataset
#in that case, replace df with input df
schema = "recId string, fname string, lname string, stNo string, add1 string, add2 string, city string, areacode string, state string, dob string, ssn  string"
inputPipe = ECsvPipe("testFebrl", "examples/febrl/test.csv", schema)
args.setData(inputPipe)

outputPipe = ECsvPipe("resultFebrl", "/tmp/febrlOutput")
outputPipe.setHeader("true")
args.setOutput(outputPipe)

# Zingg execution for the given phase
# options = ClientOptions([ClientOptions.PHASE,"findAndLabel"])

options = ClientOptions([ClientOptions.PHASE,"trainMatch"])
zingg = EZingg(args, options)
zingg.initAndExecute()

incrArgs = IncrementalArguments()
incrArgs.setParentArgs(args)
incrPipe = ECsvPipe("testFebrlIncr", "examples/febrl/test-incr.csv", schema)
incrArgs.setIncrementalData(incrPipe)

incrOptions = ClientOptions([ClientOptions.PHASE,"runIncremental"])
zinggIncr = EZingg(incrArgs, incrOptions)
zinggIncr.initAndExecute()
```
