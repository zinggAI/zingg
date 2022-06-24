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
dfA = spark.read.format("csv").schema("id string, title string, description string, manufacturer string, price double ").load("examples/amazon-google/Amazon.csv")
dfG = spark.read.format("csv").schema("id string, title string, description string, manufacturer string, price double ").load("examples/amazon-google/GoogleProducts.csv")

inputPipe = []

PipeA = CsvPipe("test")
PipeA.setLocation("examples/amazon-google/Amazon.csv")
PipeG = CsvPipe("test")
PipeG.setLocation("examples/amazon-google/GoogleProducts.csv")

dfSchemaA = str(dfA.schema.json())
dfSchemaG = str(dfG.schema.json())

PipeA.setSchema(dfSchemaA)
PipeG.setSchema(dfSchemaG)

inputPipe.append(PipeA)
inputPipe.append(PipeG)


args.setData(PipeA)
args.setData(PipeG)


#setting outputpipe in 'args'
outputPipeG = CsvPipe("resultGoogle")
outputPipeG.setLocation("/tmp")
outputPipeA = CsvPipe("resultAmazon")
outputPipeA.setLocation("/tmp")


args.setOutput(outputPipeA)

args.setOutput(outputPipeG)

options = ClientOptions()
options.setPhase("trainMatch")

#Zingg execution for the given phase
zingg = Zingg(args, options)
zingg.initAndExecute()