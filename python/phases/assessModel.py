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
markedDF = DataFrame(jMarkedDF, sqlContext)
print(markedDF)
pMarkedDF = markedDF.toPandas()
display(pMarkedDF)

#marked = client.getMarkedRecordsStat(mark, value)
#matched_marked = client.getMatchedMarkedRecordsStat(mark)
#unmatched_marked = client.getUnmatchedMarkedRecordsStat(mark)
#unsure_marked = client.getUnsureMarkedRecordsStat(mark)

