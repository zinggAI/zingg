from zingg import *
from zingg.pipes import *

#build the arguments for zingg
args = Arguments()
#set field definitions
id = FieldDefinition("id", "string", MatchType.DONT_USE)
title = FieldDefinition("title", "string", MatchType.NUMERIC)
description = FieldDefinition("description", "string", MatchType.TEXT)
manufacturer = FieldDefinition("manufacturer","string", MatchType.FUZZY)
price = FieldDefinition("price", "double", MatchType.FUZZY)

fieldDefs = [id, title, description, manufacturer, price]
args.setFieldDefinition(fieldDefs)
#set the modelid and the zingg dir
args.setModelId("103")
args.setZinggDir("models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.4)

#reading dataset into inputPipe and settint it up in 'args'
#below line should not be required if you are reading from in memory dataset
#in that case, replace df with input df
df = spark.read.format("csv").schema("id string, title string, description string, manufacturer string, price double ").load("examples/amazon-google/data/")
dfSchema = str(df.schema.json())

inputPipe = CsvPipe("test")
inputPipe.setLocation("examples/amazon-google/data/")
inputPipe.setSchema(dfSchema)
args.setData(inputPipe)

#setting outputpipe in 'args'
outputPipe = CsvPipe("resultAmazon")
outputPipe.setLocation("/tmp")
args.setOutput(outputPipe)

options = ClientOptions()
options.setPhase("link")

#Zingg execution for the given phase
zingg = Zingg(args, options)
zingg.initAndExecute()