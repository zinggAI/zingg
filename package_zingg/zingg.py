import logging

from pyspark import SparkConf, SparkContext, SQLContext
from pyspark.sql.session import SparkSession
from py4j.java_collections import SetConverter, MapConverter, ListConverter

conf = SparkConf().set("spark.jars", "zingg_jar/zingg-0.3.3-SNAPSHOT.jar")
sc = SparkContext( conf=conf)

# sc = SparkContext.getOrCreate()
sqlContext = SQLContext(sc)
spark = SparkSession(sc)
jvm = sc._jvm
gateway = sc._gateway

class Client:
    
    def __init__(self, args, options):
        self.client = jvm.zingg.client.Client(args.getArgs(), options)
    
    def init(self):
        self.client.init()
    def execute(self):
        self.client.execute()
    def getMarkedRecords(self):
        return self.client.getMarkedRecords()
        
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
    
    ## TODO as setData
    def setOutput(self, pipe):
        self.args.setOutput([pipe.getPipe()])
    
    def setModelId(self, id):
        self.args.setModelId(id)
    def setZinggDir(self, f):
        self.args.setZinggDir(f)
    
    def setNumPartitions(self, numPartitions):
        self.args.setNumPartitions(numPartitions)
    
    def setLabelDataSampleSize(self, labelDataSampleSize):
        self.args.setLabelDataSampleSize(labelDataSampleSize)

class FieldDefinition:
    
    def __init__(self, name, dataType, matchType):
        self.fd = jvm.zingg.client.FieldDefinition()
        self.fd.setFieldName(name)
        self.fd.setDataType(dataType)
        self.fd.setMatchType(matchType)
        self.fd.setFields(name)
    
    def getFieldDefinition(self):
        return self.fd
        
class Pipe:
    def __init__(self, name, format):
        self.pipe = sc._jvm.zingg.client.pipe.Pipe()
        self.pipe.setName(name)
        self.pipe.setFormat(sc._jvm.zingg.client.pipe.Format.getPipeType(format))
    def getPipe(self):
        return self.pipe
    
    def addProperty(self, name, value):
        self.pipe.setProp(name, value)
