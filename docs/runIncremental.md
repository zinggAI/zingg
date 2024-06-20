# Run Incremental Loads For New and Updated Records - Zingg Enterprise Only Feature

Rerunning matching on entire datasets is wasteful, and we lose the lineage of matched records against a persistent identifier. Using Zingg Enterprise, incremental loads can be run to match existing pre resolved entities. The new and updated records are matched to existing clusters, and new persistent ZINGG_IDs generated for records which do not find a match. If a record gets updated and Zingg Enterprise discovers that it is a more suitable match with another cluster, it will be reassigned. Cluster assignment, merge and unmerge happens automatically in the flow. Zingg Entperirse also takes care of human feedback on previously matched data to ensure that it doesnt override the approved records. 

The incremental phase is run as follows\
`./scripts/zingg.sh --phase runIncremental --conf <location to incrementalConf.json>`

Example incrementalConf.json:

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

runIncremental can also be triggerred using python by invoking\
`./scripts/zingg.sh --run examples/FebrlExample.py`

Python code example:

from zingg.client import *
from zingg.pipes import *
from zinggEC.enterprise.common.ApproverArguments import *
from zinggEC.enterprise.common.IncrementalArguments import *
from zinggEC.enterprise.common.epipes import *
from zinggEC.enterprise.common.EArguments import *
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggES.enterprise.spark.ESparkClient import EZingg
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

incrArgs = IncrementalArguments()
incrArgs.setParentArgs(args)
incrPipe = ECsvPipe("testFebrlIncr", "examples/febrl/test-incr.csv", schema)
incrArgs.setIncrementalData(incrPipe)

options = ClientOptions([ClientOptions.PHASE,"runIncremental",ClientOptions.LICENSE,<enterprise license file location>])

#Zingg execution for the given phase
zingg = EZingg(incrArgs, options)

zingg.initAndExecute()

