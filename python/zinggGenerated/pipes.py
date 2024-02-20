from zingg.otherThanGenerated import *
import logging
LOG = logging.getLogger("zingg.pipes")

JPipe = getJVM().zingg.spark.client.pipe.SparkPipe
FilePipe = getJVM().zingg.common.client.pipe.FilePipe
JStructType = getJVM().org.apache.spark.sql.types.StructType

'''
Actual pipe def in the args. One pipe can be used at multiple places with different tables, locations, queries etc
 
 @author sgoyal
'''
class Pipe:
    def __init__(self, name, format):
        self.pipe = getJVM().zingg.spark.client.pipe.SparkPipe()
        self.pipe.setName(name)
        self.pipe.setFormat(format)

    def setSchema(self, schema):
        self.pipe.setSchema(schema)

    def getName(self):
        return self.pipe.getName()

    def setName(self, name):
        self.pipe.setName(name)

    def getFormat(self):
        return self.pipe.getFormat()

    def setFormat(self, sinkType):
        self.pipe.setFormat(sinkType)

    def setProp(self, k, v):
        self.pipe.setProp(k, v)

    def toString(self):
        return self.pipe.toString()

