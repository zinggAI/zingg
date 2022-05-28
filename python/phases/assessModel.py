import pandas as pd
import seaborn as sn
import matplotlib.pyplot as plt
import logging

jvm = sc._jvm

class Client:
    
    def __init__(self):
        self.client = jvm.zingg.client.Client()
 
class Arguments:
    
    def __init__(self):
        self.args = jvm.zingg.client.Arguments()

    def setFieldDefinition(fieldDef):
        args.setFieldDefinition(fieldDef)

class FieldDefinition:
    
    def __init__(self, name, dataType, matchType):
        self.fd = jvm.zingg.client.FieldDefinition()
        self.fd.setName(name)
        self.fd.setDataType(dataType)
        self.fd.setMatchType(matchType)
        self.fd.setFields(name)

client = Client()
args = Arguments()
fname = FieldDefinition("fname","string",[sc._jvm.zingg.client.MatchType.FUZZY])
lname = FieldDefinition("lname","string",[sc._jvm.zingg.client.MatchType.FUZZY])

fieldDef = sc._jvm.java.util.ArrayList()
fieldDef.add(fname)
fieldDef.add(lname)

args.setFieldDefinition(fieldDef)
options = sc._jvm.zingg.client.ClientOptions(["--phase", "label",  "--conf", "dummy", "--license", "dummy", "--email", "xxx@yyy.com"])

#Zingg execution for the given phase
client = sc._jvm.zingg.client.Client(args, options)
client.init()
client.execute()

mark_spark = client.getMarkedRecords()
mark = mark_spark.select("*").toPandas()
marked = client.getMarkedRecordsStat(mark, value)
matched_marked = client.getMatchedMarkedRecordsStat(mark)
unmatched_marked = client.getUnmatchedMarkedRecordsStat(mark)
unsure_marked = client.getUnsureMarkedRecordsStat(mark)


confusion_matrix = pd.crosstab(marked['z_isMatch'], marked['z_prediction'], rownames=['Actual'], colnames=['Predicted'])

sn.heatmap(confusion_matrix, annot=True)
plt.show()
