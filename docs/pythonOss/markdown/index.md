<!-- zingg documentation master file, created by
sphinx-quickstart on Thu Jul  7 12:24:41 2022.
You can adapt this file completely to your liking, but it should at least
contain the root `toctree` directive. -->

# Community Zingg Entity Resolution Python Package

Community Zingg Python APIs for entity resolution, identity resolution, record linkage, data mastering and deduplication using ML
([https://www.zingg.ai](https://www.zingg.ai))

#### NOTE
Requires python 3.6+; spark 3.5.0
Otherwise, [`zingg.client.Zingg()`](_autosummary/zingg.client.Zingg.md#zingg.client.Zingg) cannot be executed

# API Documentation

* [zingg.client module](zingg.client.md)
  * [Classes](zingg.client.md#classes)
    * [zingg.client.Zingg](_autosummary/zingg.client.Zingg.md)
    * [zingg.client.ZinggWithSpark](_autosummary/zingg.client.ZinggWithSpark.md)
    * [zingg.client.Arguments](_autosummary/zingg.client.Arguments.md)
    * [zingg.client.ClientOptions](_autosummary/zingg.client.ClientOptions.md)
    * [zingg.client.FieldDefinition](_autosummary/zingg.client.FieldDefinition.md)
* [zingg.pipes module](zingg.pipes.md)
  * [Classes](zingg.pipes.md#classes)
    * [zingg.pipes.Pipe](_autosummary/zingg.pipes.Pipe.md)
    * [zingg.pipes.CsvPipe](_autosummary/zingg.pipes.CsvPipe.md)
    * [zingg.pipes.BigQueryPipe](_autosummary/zingg.pipes.BigQueryPipe.md)
    * [zingg.pipes.SnowflakePipe](_autosummary/zingg.pipes.SnowflakePipe.md)

# Example API Usage

```python
from zingg.client import *
from zingg.pipes import *

#build the arguments for zingg
args = Arguments()
#set field definitions
fname = FieldDefinition("fname", "string", MatchType.FUZZY)
lname = FieldDefinition("lname", "string", MatchType.FUZZY)
stNo = FieldDefinition("stNo", "string", MatchType.FUZZY)
add1 = FieldDefinition("add1","string", MatchType.FUZZY)
add2 = FieldDefinition("add2", "string", MatchType.FUZZY)
city = FieldDefinition("city", "string", MatchType.FUZZY)
areacode = FieldDefinition("areacode", "string", MatchType.FUZZY)
state = FieldDefinition("state", "string", MatchType.FUZZY)
dob = FieldDefinition("dob", "string", MatchType.FUZZY)
ssn = FieldDefinition("ssn", "string", MatchType.FUZZY)

fieldDefs = [fname, lname, stNo, add1, add2, city, areacode, state, dob, ssn]

args.setFieldDefinition(fieldDefs)
#set the modelid and the zingg dir
args.setModelId("100")
args.setZinggDir("models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)

#reading dataset into inputPipe and setting it up in 'args'
schema = "id string, fname string, lname string, stNo string, add1 string, add2 string, city string, areacode string, state string, dob string, ssn  string"
inputPipe = CsvPipe("testFebrl", "examples/febrl/test.csv", schema)
args.setData(inputPipe)
outputPipe = CsvPipe("resultFebrl", "/tmp/febrlOutput")

args.setOutput(outputPipe)

options = ClientOptions([ClientOptions.PHASE,"match"])

#Zingg execution for the given phase
zingg = Zingg(args, options)
zingg.initAndExecute()
```
