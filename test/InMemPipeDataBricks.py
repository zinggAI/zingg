from zingg.client import *
from zingg.pipes import *
from pyspark.sql.types import *
import pandas

#build the arguments for zingg
args = Arguments()
#set field definitions
id = FieldDefinition("id", "string", MatchType.DONT_USE)
title = FieldDefinition("title", "string", MatchType.NUMERIC)
description = FieldDefinition("description", "string", MatchType.TEXT)
description.setStopWords("dbfs:/FileStore/tables/stopWords.csv")
manufacturer = FieldDefinition("manufacturer","string", MatchType.FUZZY)
price = FieldDefinition("price", "double", MatchType.FUZZY)

fieldDefs = [id, title, description, manufacturer, price]
args.setFieldDefinition(fieldDefs)
#set the modelid and the zingg dir
args.setModelId("103")
args.setZinggDir("dbfs:/FileStore/tables/models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.4)


schema = StructType([StructField("id", StringType(), True)\
                   ,StructField("title", StringType(), True)\
                   ,StructField("description", StringType(), True)\
                   ,StructField("manufacturer", StringType(), True)\
                   ,StructField("price", DoubleType(), True)])


inputPipeAmazon=InMemoryPipe("amz")
inputPipeAmazon.setDataset(getSparkSession().read.format("csv").schema(schema).load("dbfs:/FileStore/tables/Amazon.csv"))
inputPipeGoogle=InMemoryPipe("google")
inputPipeGoogle.setDataset(getSparkSession().read.format("csv").schema(schema).load("dbfs:/FileStore/tables/GoogleProducts.csv"))

args.setData(inputPipeAmazon,inputPipeGoogle)

#setting outputpipe in 'args'
outputPipe = CsvPipe("resultAmazonGoogle", "dbfs:/FileStore/tables/AwsGoogleOutput")

args.setOutput(outputPipe)

inpPhase = input("Enter phase: ")

options = ClientOptions([ClientOptions.PHASE,inpPhase])

#Zingg execution for the given phase
zingg = Zingg(args, options)

if (inpPhase!="label"):
    zingg.initAndExecute()
else:
    print("label phase")
    zingg.init()
    unmarkedRecords = zingg.getUnmarkedRecords()
    unmarkedRecords.show()
    print(unmarkedRecords.count())
    updatedRecords = zingg.processRecordsCli(unmarkedRecords,args)
    print("updated records")
    if updatedRecords is not None:
        updatedRecords.show()
        zingg.writeLabelledOutput(updatedRecords,args)

