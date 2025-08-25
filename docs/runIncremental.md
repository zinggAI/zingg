---
title: Adding incremental data
parent: Step By Step Guide
nav_order: 10
description: >-
  Building a continuously updated identity graph with new, updated, and deleted
  records
---

# Adding Incremental Data

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

Rerunning matching on entire datasets is wasteful, and we lose the lineage of matched records against a persistent identifier. Using the[ incremental flow](https://www.learningfromdata.zingg.ai/p/zingg-incremental-flow) feature in [Zingg Enterprise](https://www.zingg.ai/company/zingg-enterprise), incremental loads can be run to match existing pre-resolved entities. The new and updated records are matched to existing clusters, and new persistent [**ZINGG\_IDs**](https://www.learningfromdata.zingg.ai/p/hello-zingg-id) are generated for records that do not find a match. If a record gets updated and Zingg Enterprise discovers that it is a more suitable match with another cluster, it will be reassigned. Cluster assignment, merge, and unmerge happens automatically in the flow. Zingg Enterprise also takes care of human feedback on previously matched data to ensure that it does not override the approved records.

### The incremental phase is run as follows:

`./scripts/zingg.sh --phase runIncremental --conf <location to incrementalConf.json>`

### Example incrementalConf.json:

```json
{      
    "config" : "config.json",  
    "incrementalData": [{  
            "name":"customers_incr",   
            "format":"csv",   
            "props": {  
                "location": "test-incr.csv",  
                "delimiter": ",",  
                "header":false  
            },  
            "schema": "recId string, fname string, lname string, stNo string, add1 string, add2 string, city string, state string, areacode string, dob string, ssn  string"   
        }  
    ]   
}  
```

### runIncremental can also be triggered using Python by invoking:

`./scripts/zingg.sh --run examples/FebrlExample.py`

#### Python Code Example:

```{python}
#import the packages  
  
from zingg.client import *  
from zingg.pipes import *  
from zinggEC.enterprise.common.ApproverArguments import *  
from zinggEC.enterprise.common.IncrementalArguments import *  
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
args.setZinggDir("/tmp/models")  
args.setNumPartitions(4)  
args.setLabelDataSampleSize(0.5)  
  
#reading dataset into inputPipe and settint it up in 'args'  
schema = "recId string, fname string, lname string, stNo string, add1 string, add2 string, city string, areacode string, state string, dob string, ssn  string"  
inputPipe = ECsvPipe("testFebrl", "examples/febrl/test.csv", schema)  
args.setData(inputPipe)  
  
outputPipe = ECsvPipe("resultFebrl", "/tmp/febrlOutput")  
outputPipe.setHeader("true")  
  
args.setOutput(outputPipe)  
  
#Run findAndLabel  
options = ClientOptions([ClientOptions.PHASE,"findAndLabel"])  
zingg = EZingg(args, options)  
zingg.initAndExecute()  
  
#Run trainMatch after above completes  
options = ClientOptions([ClientOptions.PHASE,"trainMatch"])  
zingg = EZingg(args, options)  
zingg.initAndExecute()  
  
#Now run incremental on output generated above  
incrArgs = IncrementalArguments()  
incrArgs.setParentArgs(args)  
incrPipe = ECsvPipe("testFebrlIncr", "examples/febrl/test-incr.csv", schema)  
incrArgs.setIncrementalData(incrPipe)  
  
options = ClientOptions([ClientOptions.PHASE,"runIncremental"])  
zingg = EZingg(incrArgs, options)  
zingg.initAndExecute()  
```

[^1]: Zingg Enterprise is an advance version of Zingg Community with production grade features
