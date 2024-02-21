from zingg.zinggGenerated.client import *
from zingg.zinggGenerated.pipes import *
from zingg.otherThanGenerated import *
from zingg.otherThanGeneratedPipe import *
from zingg.otherThanGeneratedArguments import *
from zingg.otherThanGeneratedFieldDefinition import *

#build the arguments for zingg
args = ExtendedArgumentsGenerated()
#set field definitions
fname = ExtendedFieldDefinitionGenerated("fname", "string", MatchType.FUZZY)
lname = ExtendedFieldDefinitionGenerated("lname", "string", MatchType.FUZZY)
stNo = ExtendedFieldDefinitionGenerated("stNo", "string", MatchType.FUZZY)
add1 = ExtendedFieldDefinitionGenerated("add1","string", MatchType.FUZZY)
add2 = ExtendedFieldDefinitionGenerated("add2", "string", MatchType.FUZZY)
city = ExtendedFieldDefinitionGenerated("city", "string", MatchType.FUZZY)
areacode = ExtendedFieldDefinitionGenerated("areacode", "string", MatchType.FUZZY)
state = ExtendedFieldDefinitionGenerated("state", "string", MatchType.FUZZY)
dob = ExtendedFieldDefinitionGenerated("dob", "string", MatchType.FUZZY)
ssn = ExtendedFieldDefinitionGenerated("ssn", "string", MatchType.FUZZY)

fieldDefs = [fname, lname, stNo, add1, add2, city, areacode, state, dob, ssn]

args.setFieldDefinition(fieldDefs)
#set the modelid and the zingg dir
args.setModelId("0102")
args.setZinggDir("models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)

#reading dataset into inputPipe and settint it up in 'args'
#below line should not be required if you are reading from in memory dataset
#in that case, replace df with input df
schema = "id string, fname string, lname string, stNo string, add1 string, add2 string, city string, areacode string, state string, dob string, ssn  string"
inputPipe = CsvPipe("testFebrl", "examples/febrl/test.csv", schema)
args.setData(inputPipe)
outputPipe = CsvPipe("resultFebrl", "/tmp/febrlOutput")

args.setOutput(outputPipe)

options = ClientOptions([ClientOptions.PHASE,"match"])

#Zingg execution for the given phase
zingg = Zingg(args, options)
zingg.initAndExecute()