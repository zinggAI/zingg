import sys
from zingg.client import *
from zingg.pipes import *

#build the arguments for zingg
args = Arguments()
#set field definitions
fname = FieldDefinition("fname", "string", MatchTypes.FUZZY)
lname = FieldDefinition("lname", "string", MatchTypes.FUZZY)
stNo = FieldDefinition("stNo", "string", MatchTypes.FUZZY)
add1 = FieldDefinition("add1","string", MatchTypes.FUZZY)
add2 = FieldDefinition("add2", "string", MatchTypes.FUZZY)
city = FieldDefinition("city", "string", MatchTypes.FUZZY)
areacode = FieldDefinition("areacode", "string", MatchTypes.FUZZY)
state = FieldDefinition("state", "string", MatchTypes.FUZZY)
dob = FieldDefinition("dob", "string", MatchTypes.FUZZY)
ssn = FieldDefinition("ssn", "string", MatchTypes.FUZZY)

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
schema = "id string, fname string, lname string, stNo string, add1 string, add2 string, city string, areacode string, state string, dob string, ssn  string"
inputPipe = CsvPipe("testFebrl", "examples/febrl/test.csv", schema)
args.setData(inputPipe)
outputPipe = CsvPipe("resultFebrl", "/tmp/febrlOutput")

args.setOutput(outputPipe)

# options = ClientOptions([ClientOptions.PHASE,"findTrainingData"])

#if one needs to pass properties-file and other command line args
# ./scripts/zingg.sh --run examples/febrl/FebrlExample.py --phase trainMatch --properties-file config/zingg.conf
# comment the above and uncomment the line below
options = ClientOptions(sys.argv[1:])

#Zingg execution for the given phase
zingg = Zingg(args, options)
#flag represent collectMetrics
zingg.printBanner(True)
zingg.initAndExecute()