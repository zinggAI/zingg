from zingg.client import *
from zingg.pipes import *

#build the arguments for zingg
args = Arguments()
#set field definitions
id = FieldDefinition("id", "string", MatchType.DONT_USE)
title = FieldDefinition("title", "string", MatchType.NUMERIC)
description = FieldDefinition("description", "string", MatchType.TEXT)
description.setStopWords("examples/amazon-google/stopWords.csv")
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
schema = "id string, title string, description string, manufacturer string, price double "
inputPipeAmazon = CsvPipe("testAmazon", "examples/amazon-google/Amazon.csv", schema)
inputPipeGoogle = CsvPipe("testGoogle", "examples/amazon-google/GoogleProducts.csv", schema)

args.setData(inputPipeAmazon,inputPipeGoogle)

#setting outputpipe in 'args'
outputPipe = CsvPipe("resultAmazonGoogle", "/tmp/AwsGoogleOutput")

args.setOutput(outputPipe)

options = ClientOptions([ClientOptions.PHASE,"link"])

#Zingg execution for the given phase
zingg = Zingg(args, options)
zingg.initAndExecute()
