import logging
import pandas as pd
from pyspark.sql import DataFrame

from pyspark import SparkConf, SparkContext, SQLContext
from pyspark.sql.session import SparkSession
from py4j.java_collections import SetConverter, MapConverter, ListConverter

sc = SparkContext.getOrCreate()
sqlContext = SQLContext(sc)
spark = SparkSession(sc)
jvm = sc._jvm
gateway = sc._gateway

class Zingg:

    def __init__(self, args, options):
        self.client = jvm.zingg.client.Client(args.getArgs(), options.getClientOptions())

    def init(self):
        self.client.init()
    def execute(self):
        self.client.execute()
    def getMarkedRecords(self):
        return self.client.getMarkedRecords()
    def getUnMarkedRecords(self):
        return self.client.getUnMarkedRecords()
    def setArguments(self, args):
        self.client.setArguments()
    def getArguments(self):
        return self.client.getArguments()
    def getOptions(self):
        return self.client.getOptions()
    def setOptions(self, options):
        return self.client.setOptions(options)
    def getMarkedRecordsStat(self, markedRecords, value):
        return self.client.getMarkedRecordsStat(markedRecords, value)
    def getMatchedMarkedRecordsStat(self, markedRecords):
        return self.client.getMatchedMarkedRecordsStat(markedRecords)
    def getUnmatchedMarkedRecordsStat(self, markedRecords):
        return self.client.getUnmatchedMarkedRecordsStat(markedRecords)
    def getUnsureMarkedRecordsStat(self, markedRecords):
        return self.client.getUnsureMarkedRecordsStat(markedRecords)
    def getDfFromDs(self, data):
        return DataFrame(data, sqlContext)
    def getPandasDfFromDs(self, data):
        return self.getDfFromDs(data).toPandas()

class Arguments:

    def __init__(self):
        self.args = jvm.zingg.client.Arguments()
    def setFieldDefinition(self, fieldDef):
        #convert python objects to java fd objects
        javaFieldDef = []
        for f in fieldDef:
            javaFieldDef.append(f.getFieldDefinition())
        self.args.setFieldDefinition(javaFieldDef)

    def getArgs(self):
        return self.args

    def setData(self, pipe):
        dataPipe = gateway.new_array(jvm.zingg.client.pipe.Pipe, 1)
        dataPipe[0] = pipe.getPipe()
        self.args.setData(dataPipe)

    def setOutput(self, pipe):
        outputPipe = gateway.new_array(jvm.zingg.client.pipe.Pipe, 1)
        outputPipe[0] = pipe.getPipe()
        self.args.setOutput(outputPipe)

    def setModelId(self, id):
        self.args.setModelId(id)
    def setZinggDir(self, f):
        self.args.setZinggDir(f)

    def setNumPartitions(self, numPartitions):
        self.args.setNumPartitions(numPartitions)

    def setLabelDataSampleSize(self, labelDataSampleSize):
        self.args.setLabelDataSampleSize(labelDataSampleSize)

    @staticmethod
    def writeArgumentsToJSON(fileName, args):
        jvm.zingg.client.Arguments.writeArgumentsToJSON(fileName, args)

class ClientOptions:

    def __init__(self, phase="label", conf="dummy"):
        self.co = sc._jvm.zingg.client.ClientOptions(["--phase", phase, "--conf", conf, "--license", "dummy", "--email", "xxx@yyy.com"])

    def getClientOptions(self):
        return self.co

    def getPhase(self):
        return self.co.get("--phase").getValue()

    def getConf(self):
        return self.co.get("--conf").getValue()

class FieldDefinition:
    def __init__(self, name, dataType, *matchType):
        self.fd = jvm.zingg.client.FieldDefinition()
        self.fd.setFieldName(name)
        self.fd.setDataType(self.stringify(dataType))
        self.fd.setMatchType(matchType)
        self.fd.setFields(name)

    def getFieldDefinition(self):
        return self.fd

    # dataType should be stringify'ed before it is set in fd object
    def stringify(self, str):
        return '"' + str + '"'

class MatchType:
    @staticmethod
    def type(type):
        return jvm.zingg.client.MatchType.getMatchType(type)

class Pipe:
    def __init__(self, name, format):
        self.pipe = sc._jvm.zingg.client.pipe.Pipe()
        self.pipe.setName(name)
        self.pipe.setFormat(sc._jvm.zingg.client.pipe.Format.getPipeType(format))
    def getPipe(self):
        return self.pipe

    def addProperty(self, name, value):
        self.pipe.setProp(name, value)
