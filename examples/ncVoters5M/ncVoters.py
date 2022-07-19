from zingg import *
from zingg.pipes import *

#build the arguments for zingg
args = Arguments()
#set field definitions
recid = FieldDefinition("recid", "string", MatchType.DONT_USE)
givenname = FieldDefinition("givenname", "string", MatchType.FUZZY)
surname = FieldDefinition("surname", "string", MatchType.EXACT)
suburb = FieldDefinition("suburb","string", MatchType.FUZZY)
postcode = FieldDefinition("postcode", "double", MatchType.EXACT)

fieldDefs = [recid, givenname, surname, suburb, postcode]
args.setFieldDefinition(fieldDefs)
#set the modelid and the zingg dir
args.setModelId("104")
args.setZinggDir("models")
args.setNumPartitions(4000)
args.setLabelDataSampleSize(0.1)

#reading dataset into inputPipe and settint it up in 'args'
#below line should not be required if you are reading from in memory dataset
#in that case, replace df with input df
schema = "recid string, givenname string, surname string, suburb string, postcode double "
inputPipe = CsvPipe("ncVotersTest", "examples/ncVoters5M/5Party-ocp20/", schema)
args.setData(inputPipe)

#setting outputpipe in 'args'
outputPipe = CsvPipe("ncVotersResult", "/tmp/ncVotersOutput")

args.setOutput(outputPipe)

options = ClientOptions()
options.setPhase("trainMatch")

#Zingg execution for the given phase
zingg = Zingg(args, options)
zingg.initAndExecute()
