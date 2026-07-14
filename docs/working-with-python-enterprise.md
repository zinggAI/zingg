---
description: A whole new way to work with Zingg Enterprise!
---

# Working With Python

## Example API Usage

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

