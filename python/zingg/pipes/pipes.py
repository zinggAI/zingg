import logging
from python.zingg import *
import pandas as pd

LOG = logging.getLogger("zingg.pipes")

JPipe = jvm.zingg.client.pipe.Pipe
FilePipe = jvm.zingg.client.pipe.FilePipe

class CsvPipe(Pipe):
    def __init__(self, name):
        Pipe.__init__(self, name, JPipe.FORMAT_CSV)
        Pipe.addProperty(self, FilePipe.HEADER,"true")

    def setDelimiter(self, delimiter):
        Pipe.addProperty(self, "delimiter", delimiter)

    def setLocation(self, location):
        Pipe.addProperty(self, FilePipe.LOCATION, location)

class BigQueryPipe(Pipe):
    def __init__(self,name):
        Pipe.__init__(self, name, JPipe.FORMAT_BIGQUERY)
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
        Pipe.__init__(self, name, JPipe.FORMAT_SNOWFLAKE)
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
    def __init__(self, name, df = None):
        Pipe.__init__(self, name, JPipe.FORMAT_INMEMORY)
        if (df is not None):
            self.setDataset(df)

    def setDataset(self, df):
        if (isinstance(df, pd.DataFrame)):
            ds = spark.createDataFrame(df)
            Pipe.getPipe(self).setDataset(ds._jdf)
        elif (isinstance(df, DataFrame)):
            Pipe.getPipe(self).setDataset(df._jdf)
        else:
            LOG.error(" setDataset(): NUll or Unsuported type: %s", type(df))

    def getDataset(self):
        return Pipe.getPipe(self).getDataset()
