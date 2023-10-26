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

#reading dataset into inputPipe and settint it up in 'args'
#below line should not be required if you are reading from in memory dataset
#in that case, replace df with input df
schema = "id string, fname string, lname string, stNo string, add1 string, add2 string, city string, state string, areacode string, dob string, ssn  string"
inputPipe = CsvPipe("testFebrl", "examples/febrl/test.csv", schema)

args.setData(inputPipe)

#setting outputpipe in 'args'
outputPipe = CsvPipe("resultFebrlDetMat", "/tmp/febrlOutputDetMat")

matching_conditions = [
    {
        "matchCondition": [
            {"fieldName": "fname"},
            {"fieldName": "stNo"},
            {"fieldName": "add1"}
        ]
    },
    {
        "matchCondition": [
            {"fieldName": "recId"}
        ]
    }
]

expected_condition = DeterministicMatching(match_condition=matching_conditions)
# print("expected_condition:", expected_condition)
# print(type(expected_condition))
args.setDeterministicMatchingCondition(expected_condition)

actual_condition = args.getDeterministicMatchingCondition()
# print("actual_condition:", actual_condition)

args.setOutput(outputPipe)

options = ClientOptions([ClientOptions.PHASE,"match"])

#Zingg execution for the given phase
zingg = Zingg(args, options)
zingg.initAndExecute()