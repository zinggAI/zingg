from zingg import *
from zingg.pipes import *

#build the arguments for zingg
args = Arguments()
#set field definitions
recid = FieldDefinition("recid", "string", MatchType.DONT_USE)
title = FieldDefinition("title", "string", MatchType.FUZZY)
surname = FieldDefinition("surname", "string", MatchType.EXACT)
suburb = FieldDefinition("suburb","string", MatchType.FUZZY)
postcode = FieldDefinition("postcode", "double", MatchType.EXACT)

fieldDefs = [recid, title, surname, suburb, postcode]
args.setFieldDefinition(fieldDefs)
#set the modelid and the zingg dir
args.setModelId("103")
args.setZinggDir("models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.4)

#reading dataset into inputPipe and settint it up in 'args'
#below line should not be required if you are reading from in memory dataset
#in that case, replace df with input df
df = spark.read.format("csv").schema("recid string, title string, surname string, suburb string, postcode double ").load("examples/ncVoters5M/5Party-ocp20/")
dfSchemaA = str(df.schema.json())

inputPipe = CsvPipe("test")
inputPipe.setLocation("examples/ncVoters5M/5Party-ocp20/")
inputPipe.setSchema(dfSchemaA)
args.setData(inputPipe)

#setting outputpipe in 'args'
outputPipe = CsvPipe("ncVotersResult")
outputPipe.setLocation("/tmp/ncVotersOutput")
args.setOutput(outputPipe)

options = ClientOptions()
options.setPhase("trainMatch")

#Zingg execution for the given phase
zingg = Zingg(args, options)
zingg.initAndExecute()