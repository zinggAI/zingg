from zingg import *
from pyspark.sql import DataFrame
import pandas as pd
import seaborn as sn
import matplotlib.pyplot as plt
from IPython.display import display

args = Arguments()
fname = FieldDefinition("fname","string", MatchType.type("FUZZY"), MatchType.type("EXACT"), MatchType.type("PINCODE"))
lname = FieldDefinition("lname","string", MatchType.type("FUZZY"), MatchType.type("EXACT"))
fieldDef = [fname, lname]
options = ClientOptions()
inputPipe = Pipe("test", "csv")
inputPipe.addProperty("location", "examples/febrl/test.csv")
args.setData(inputPipe)
args.setModelId("100")
args.setZinggDir("models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)
args.setFieldDefinition(fieldDef)
Arguments.writeArgumentsToJSON("python/ArgumentsToFile.json", args.getArgs())
#Zingg execution for the given phase
client = Zingg(args, options)
client.init()
client.execute()

jMarkedDF = client.getMarkedRecords()
markedDF = client.getDfFromDs(jMarkedDF)
pMarkedDF = client.getPandasDfFromDs(jMarkedDF)
display(pMarkedDF)

jUnMarkedDF = client.getUnMarkedRecords()
unMarkedDF = client.getDfFromDs(jUnMarkedDF)
pUnMarkedDF = client.getPandasDfFromDs(jUnMarkedDF)

total_marked = markedDF.count()
total_unmarked = pUnMarkedDF.shape[0]
matched_marked = client.getMatchedMarkedRecordsStat(jMarkedDF)
unmatched_marked = client.getUnmatchedMarkedRecordsStat(jMarkedDF)
unsure_marked = client.getUnsureMarkedRecordsStat(jMarkedDF)
print()
print("No. of Records Marked   : ", total_marked)
print("No. of Records UnMarked : ", total_unmarked)
print("No. of Matches          : ", matched_marked)
print("No. of Non-Matches      : ", unmatched_marked)
print("No. of Not Sure         : ", unsure_marked)

confusion_matrix = pd.crosstab(pMarkedDF['z_isMatch'], pMarkedDF['z_prediction'], rownames=['Actual'], colnames=['Predicted'])
sn.heatmap(confusion_matrix, annot=True)
plt.show()