import logging
from zingg import *
import pandas as pd

LOG = logging.getLogger("zingg.pipes")

Format = jvm.zingg.client.pipe.Format
FilePipe = jvm.zingg.client.pipe.FilePipe

class CsvPipe(Pipe):
    def __init__(self, name):
        Pipe.__init__(self, name, Format.CSV.type())

    def setDelimiter(self, delimiter):
        Pipe.addProperty(self, FilePipe.DELIMITER, delimiter)

    def setLocation(self, location):
        Pipe.addProperty(self, FilePipe.LOCATION, location)

    def setHeader(self, header):
        Pipe.addProperty(self, FilePipe.HEADER, header)

class BigQueryPipe(Pipe):
    VIEWS_ENABLED = "viewsEnabled"
    CREDENTIAL_FILE = "credentialsFile"
    TABLE = "table"
    TEMP_GCS_BUCKET="temporaryGcsBucket"

    def __init__(self,name):
        Pipe.__init__(self, name, Format.BIGQUERY.type())

    def setCredentialFile(self, file):
        Pipe.addProperty(self, BigQueryPipe.CREDENTIAL_FILE, file)

    def setTable(self, table):
        Pipe.addProperty(self, BigQueryPipe.TABLE, table)

    def setTemporaryGcsBucket(self, bucket):
        Pipe.addProperty(self, BigQueryPipe.TEMP_GCS_BUCKET, bucket)

    def setViewsEnabled(self, isEnabled):
        Pipe.addProperty(self, BigQueryPipe.VIEWS_ENABLED, isEnabled)

class SnowflakePipe(Pipe):
    URL = "sfUrl"
    USER = "sfUser"
    PASSWORD = "sfPassword"
    DATABASE ="sfDatabase"
    SCHEMA = "sfSchema"
    WAREHOUSE = "sfWarehouse"
    DBTABLE = "dbtable"

    def __init__(self,name):
        Pipe.__init__(self, name, Format.SNOWFLAKE.type())

    def setURL(self, url):
        Pipe.addProperty(self, SnowflakePipe.URL, url)

    def setUser(self, user):
        Pipe.addProperty(self, SnowflakePipe.USER, user)

    def setPassword(self, passwd):
        Pipe.addProperty(self, SnowflakePipe.PASSWORD, passwd)

    def setDatabase(self, db):
        Pipe.addProperty(self, SnowflakePipe.DATABASE, db)

    def setSFSchema(self, schema):
        Pipe.addProperty(self, SnowflakePipe.SCHEMA, schema)

    def setWarehouse(self, warehouse):
        Pipe.addProperty(self, SnowflakePipe.WAREHOUSE, warehouse)

    def setDbTable(self, dbtable):
        Pipe.addProperty(self, SnowflakePipe.DBTABLE, dbtable)

class InMemoryPipe(Pipe):
    def __init__(self, name, df = None):
        Pipe.__init__(self, name, Format.INMEMORY.type())
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
