import logging
from python.api.zingg import *
import pandas as pd

LOG = logging.getLogger("zingg.pipes")

Format = jvm.zingg.client.pipe.Format
FilePipe = jvm.zingg.client.pipe.FilePipe

class CsvPipe(Pipe):
    def __init__(self, name):
        Pipe.__init__(self, name, Format.CSV.type())
        Pipe.addProperty(self, FilePipe.HEADER,"true")
    def setDelimiter(self, delimiter):
        Pipe.addProperty(self, "delimiter", delimiter)
    def setLocation(self, location):
        Pipe.addProperty(self, FilePipe.LOCATION, location)

class BigQueryPipe(Pipe):
    def __init__(self,name):
        Pipe.__init__(self, name, "bigquery")
        Pipe.addProperty(self, "viewsEnabled", "true")
    def setCredentialFile(self, file):
        Pipe.addProperty(self, "credentialsFile", file)
    def setTable(self, table):
        Pipe.addProperty(self, "table", table)
    def setTemporaryGcsBucket(self, bucket):
        Pipe.addProperty(self, "temporaryGcsBucket", bucket)
    def setViewsEnabled(self, isEnabled):
        Pipe.addProperty(self, "viewsEnabled", isEnabled)

class SnowflakePipe(Pipe):
    def __init__(self,name):
        Pipe.__init__(self, name, Format.SNOWFLAKE.type())
    def setURL(self, url):
        Pipe.addProperty(self, "sfUrl", url)
    def setUser(self, user):
        Pipe.addProperty(self, "sfUser", user)
    def setPassword(self, passwd):
        Pipe.addProperty(self, "sfPassword", passwd)
    def setDatabase(self, db):
        Pipe.addProperty(self, "sfDatabase", db)
    def setSFSchema(self, schema):
        Pipe.addProperty(self, "sfSchema", schema)
    def setWarehouse(self, warehouse):
        Pipe.addProperty(self, "sfWarehouse", warehouse)
    def setDbTable(self, dbtable):
        Pipe.addProperty(self, "dbtable", dbtable)

class InMemoryPipe(Pipe):
    def __init__(self, name):
        Pipe.__init__(self, name, Format.INMEMORY.type())
    def setDataset(self, ds):
        Pipe.getPipe(self).setDataset(ds)
    def getDataset(self):
        return Pipe.getPipe(self).getDataset()
