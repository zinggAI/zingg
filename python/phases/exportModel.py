from zingg import *
from pyspark.sql import DataFrame
import pandas as pd
import seaborn as sn
import matplotlib.pyplot as plt
from IPython.display import display

args = Arguments()
fname = FieldDefinition("fname","\"string\"",[sc._jvm.zingg.client.MatchType.FUZZY])
lname = FieldDefinition("lname","\"string\"",[sc._jvm.zingg.client.MatchType.FUZZY])
fieldDef = [fname, lname]
options = sc._jvm.zingg.client.ClientOptions(["--phase", "label",  "--conf", "dummy", "--license", "dummy", "--email", "xxx@yyy.com"])
inputPipe = Pipe("test", "csv")
inputPipe.addProperty("location", "examples/febrl/test.csv")
args.setData(inputPipe)
args.setModelId("100")
args.setZinggDir("models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)
args.setFieldDefinition(fieldDef)
print(args.getArgs)
#Zingg execution for the given phase
client = Client(args, options)
client.init()
client.execute()
jMarkedDF = client.getMarkedRecords()
print(jMarkedDF)
labeledData = DataFrame(jMarkedDF, sqlContext)
print(labeledData)
pMarkedDF = labeledData.toPandas()
display(pMarkedDF)

#Exporting the labelled data as CSV
baseCols = ['z_cluster', 'z_zid', 'z_prediction', 'z_score', 'z_source', 'z_isMatch']
sourceDataColumns =  [c for c in labeledData.columns if c not in  baseCols]
additionalTrainingColumns = ['z_cluster','z_isMatch']
trainingSampleColumns = [*additionalTrainingColumns, *sourceDataColumns]
trainingSamples = labeledData.select(trainingSampleColumns)
trainingSamples.coalesce(1).write.csv("output/")

#Getting shema
trainingSamples.schema.jsonValue()
trainingSamples.show()
trainingSamples.columns
print(trainingSampleColumns)