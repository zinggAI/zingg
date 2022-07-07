import logging
import argparse
import pandas as pd
from pyspark.sql import DataFrame

from pyspark import SparkConf, SparkContext, SQLContext
from pyspark.sql.session import SparkSession
from py4j.java_collections import SetConverter, MapConverter, ListConverter

LOG = logging.getLogger("zingg")

sc = SparkContext.getOrCreate()
sqlContext = SQLContext(sc)
spark = SparkSession(sc)
jvm = sc._jvm
gateway = sc._gateway

ColName = jvm.zingg.client.util.ColName
MatchType = jvm.zingg.client.MatchType

class Zingg:

    def __init__(self, args, options):
        self.client = jvm.zingg.client.Client(args.getArgs(), options.getClientOptions())

    def init(self):
        self.client.init()
    def execute(self):
        self.client.execute()
    def initAndExecute(self):
        self.client.init()
        self.client.execute()
    def getMarkedRecords(self):
        return self.client.getMarkedRecords()
    def getUnmarkedRecords(self):
        return self.client.getUnmarkedRecords()
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
    def getMatchedMarkedRecordsStat(self):
        return self.client.getMatchedMarkedRecordsStat(self.getMarkedRecords())
    def getUnmatchedMarkedRecordsStat(self):
        return self.client.getUnmatchedMarkedRecordsStat(self.getMarkedRecords())
    def getUnsureMarkedRecordsStat(self):
        return self.client.getUnsureMarkedRecordsStat(self.getMarkedRecords())
    def getDfFromDs(self, data):
        return DataFrame(data, spark)
    def getPandasDfFromDs(self, data):
        df = self.getDfFromDs(data)
        return pd.DataFrame(df.collect(), columns=df.columns)

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
    def setArgs(self, argumentsObj):
        self.args = argumentsObj

    def setData(self, *pipes):
        dataPipe = gateway.new_array(jvm.zingg.client.pipe.Pipe, len(pipes))
        for idx, pipe in enumerate(pipes):
            dataPipe[idx] = pipe.getPipe()
        self.args.setData(dataPipe)

    def setOutput(self, *pipes):
        outputPipe = gateway.new_array(jvm.zingg.client.pipe.Pipe, len(pipes))
        for idx, pipe in enumerate(pipes):
            outputPipe[idx] = pipe.getPipe()
        self.args.setOutput(outputPipe)

    def setModelId(self, id):
        self.args.setModelId(id)
    def setZinggDir(self, f):
        self.args.setZinggDir(f)

    def setNumPartitions(self, numPartitions):
        self.args.setNumPartitions(numPartitions)

    def setLabelDataSampleSize(self, labelDataSampleSize):
        self.args.setLabelDataSampleSize(labelDataSampleSize)

    def writeArgumentsToJSON(self, fileName):
        jvm.zingg.client.Arguments.writeArgumentsToJSON(fileName, self.args)

    @staticmethod
    def createArgumentsFromJSON(fileName, phase):
        obj = Arguments()
        obj.args = jvm.zingg.client.Arguments.createArgumentsFromJSON(fileName, phase)
        return obj

class ClientOptions:

    PHASE = jvm.zingg.client.ClientOptions.PHASE
    CONF = jvm.zingg.client.ClientOptions.CONF
    LICENSE = jvm.zingg.client.ClientOptions.LICENSE
    EMAIL = jvm.zingg.client.ClientOptions.EMAIL
    LOCATION = jvm.zingg.client.ClientOptions.LOCATION

    def __init__(self, args = None):
        if(args!=None):
            self.co = jvm.zingg.client.ClientOptions(args)
        else:
            self.co = jvm.zingg.client.ClientOptions(["--phase", "trainMatch",  "--conf", "dummy", "--license", "dummy", "--email", "xxx@yyy.com"])
    def getClientOptions(self):
        return self.co
    def getOptionValue(self, option):
        return self.co.getOptionValue(option)
    def setOptionValue(self, option, value):
        self.co.get(option).setValue(value)
    def getPhase(self):
        return self.co.get(ClientOptions.PHASE).getValue()
    def setPhase(self, newValue):
        return self.co.get(ClientOptions.PHASE).setValue(newValue)
    def getConf(self):
        return self.co.get(ClientOptions.CONF).getValue()
    def hasLocation(self):
        if(self.co.get(ClientOptions.LOCATION)==None):
            return False
        else:
            return True
    def getLocation(self):
        return self.co.get(ClientOptions.LOCATION).getValue()

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

class Pipe:
    def __init__(self, name, format):
        self.pipe = jvm.zingg.client.pipe.Pipe()
        self.pipe.setName(name)
        self.pipe.setFormat(format)
    def getPipe(self):
        return self.pipe

    def addProperty(self, name, value):
        self.pipe.setProp(name, value)
    
    def setSchema(self, s):
        self.pipe.setSchema(s)

    def toString(self):
        return self.pipe.toString()

def parseArguments(argv):
    parser = argparse.ArgumentParser(description='Zingg\'s python APIs')
    mandatoryOptions = parser.add_argument_group('mandatory arguments')
    mandatoryOptions.add_argument('--phase', required=True,
                        help='python phase e.g. assessModel')
    mandatoryOptions.add_argument('--conf', required=True,
                        help='JSON configuration with data input output locations and field definitions')

    args, remaining_args = parser.parse_known_args()
    LOG.debug("args: ", args)
    return args