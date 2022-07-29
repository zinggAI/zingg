"""
zingg.pipes
--------------------------
This module is submodule of zingg to work with different types of Pipes. Classes of this module inherit the Pipe class, and use that class to create many different types of pipes.
"""


import logging
from zingg.client import *

LOG = logging.getLogger("zingg.pipes")

JPipe = jvm.zingg.client.pipe.Pipe
FilePipe = jvm.zingg.client.pipe.FilePipe

class Pipe:
    """ Pipe class for working with different data-pipelines. Actual pipe def in the args. One pipe can be used at multiple places with different tables, locations, queries, etc

    :param name: name of the pipe
    :type name: String
    :param format: formate of pipe e.g. bigquery,InMemory, etc.
    :type format: Format
    """

    def __init__(self, name, format):
        self.pipe = jvm.zingg.client.pipe.Pipe()
        self.pipe.setName(name)
        self.pipe.setFormat(format)

    def getPipe(self):
        """ Method to get Pipe 

        :return: pipe parameter values in the format of a list of string 
        :rtype: Pipe
        """
        return self.pipe

    def addProperty(self, name, value):
        """ Method for adding different properties of pipe

        :param name: name of the property
        :type name: String
        :param value: value you want to set for the property
        :type value: String
        """
        self.pipe.setProp(name, value)
    
    def setSchema(self, s):
        """ Method to set pipe schema value

        :param s: json schema for the pipe
        :type s: Schema
        """
        self.pipe.setSchema(s)

    def toString(self):
        """ Method to get pipe parameter values

        :return: pipe information in list format
        :rtype: List[String]
        """
        return self.pipe.toString()


class CsvPipe(Pipe):
    """ Class CsvPipe: used for working with text files which uses a pipe symbol to separate units of text that belong in different columns.

    :param name: name of the pipe.
    :type name: String
    :param location: (optional) location from where we read data
    :type location: String or None
    :param schema: (optional) json schema for the pipe
    :type schema: Schema or None
    """
    def __init__(self, name, location = None, schema = None):
        Pipe.__init__(self, name, JPipe.FORMAT_CSV)
        if(location != None):
            Pipe.addProperty(self, FilePipe.LOCATION, location)
            if(schema != None):
                df = spark.read.format(JPipe.FORMAT_CSV).schema(schema).load(location)
                Pipe.setSchema(self, str(df.schema.json()))
    
    def setDelimiter(self, delimiter):
        """ This method is used to define delimiter of CsvPipe

        :param delimiter: a sequence of one or more characters for specifying the boundary between separate, independent regions in data streams
        :type delimiter: String
        """
        Pipe.addProperty(self, "delimiter", delimiter)

    def setLocation(self, location):
        """ Method to set location of pipe

        :param location: location from where we read data
        :type location: String
        """
        Pipe.addProperty(self, FilePipe.LOCATION, location)

    def setHeader(self, header):
        """ Method to set header property of pipe

        :param header: true if pipe have header, false otherwise
        :type header: Boolean
        """
        Pipe.addProperty(self, FilePipe.HEADER, header)

class BigQueryPipe(Pipe):
    """ Pipe Class for working with BigQuery pipeline

    :param name: name of the pipe.
    :type name: String
    """

    VIEWS_ENABLED = "viewsEnabled"
    CREDENTIAL_FILE = "credentialsFile"
    TABLE = "table"
    TEMP_GCS_BUCKET="temporaryGcsBucket"

    def __init__(self,name):
        Pipe.__init__(self, name, JPipe.FORMAT_BIGQUERY)

    def setCredentialFile(self, file):
        """ Method to set Credential file to the pipe

        :param file: credential file name
        :type file: String
        """
        Pipe.addProperty(self, "credentialsFile", file)

    def setTable(self, table):
        """ Method to set Table to the pipe

        :param table: provide table parameter
        :type table: String
        """
        Pipe.addProperty(self, "table", table)

    def setTemporaryGcsBucket(self, bucket):
        """ Method to set TemporaryGcsBucket to the pipe

        :param bucket: provide bucket parameter
        :type bucket: String
        """
        Pipe.addProperty(self, "temporaryGcsBucket", bucket)

    def setViewsEnabled(self, isEnabled):
        """ Method to set if viewsEnabled parameter is Enabled or not

        :param isEnabled: provide boolean parameter which defines if viewsEnabled option is enable or not
        :type isEnabled: Bool
        """
        Pipe.addProperty(self, "viewsEnabled", isEnabled)


class SnowflakePipe(Pipe):
    """ Pipe Class for working with Snowflake pipeline

    :param name: name of the pipe
    :type name: String
    """
    URL = "sfUrl"
    USER = "sfUser"
    PASSWORD = "sfPassword"
    DATABASE ="sfDatabase"
    SCHEMA = "sfSchema"
    WAREHOUSE = "sfWarehouse"
    DBTABLE = "dbtable"

    def __init__(self,name):
        Pipe.__init__(self, name, JPipe.FORMAT_SNOWFLAKE)

    def setURL(self, url):
        """ Method to set url to the pipe

        :param url: provide url for this pipe
        :type url: String
        """
        Pipe.addProperty(self, "sfUrl", url)

    def setUser(self, user):
        """ Method to set User to the pipe
        
        :param user: provide User parameter.
        :type user: String
        """
        Pipe.addProperty(self, "sfUser", user)

    def setPassword(self, passwd):
        """ Method to set Password to the pipe
        
        :param passwd: provide Password parameter.
        :type passwd: String
        """
        Pipe.addProperty(self, "sfPassword", passwd)

    def setDatabase(self, db):
        """ Method to set Database to the pipe
        
        :param db: provide Database parameter.
        :type db: Database
        """
        Pipe.addProperty(self, "sfDatabase", db)

    def setSFSchema(self, schema):
        """ Method to set Schema to the pipe
        
        :param schema: provide schema parameter.
        :type schema: Schema
        """
        Pipe.addProperty(self, "sfSchema", schema)

    def setWarehouse(self, warehouse):
        """ Method to set warehouse parameter to the pipe
        
        :param warehouse: provide warehouse parameter.
        :type warehouse: String
        """
        Pipe.addProperty(self, "sfWarehouse", warehouse)

    def setDbTable(self, dbtable):
        """ description
        
        :param dbtable: provide bucket parameter.
        :type dbtable: String
        """
        Pipe.addProperty(self, "dbtable", dbtable)     
        

class InMemoryPipe(Pipe):
    """ Pipe Class for working with InMemory pipeline

    :param name: name of the pipe
    :type name: String
    :param df: provide dataset for this pipe (optional)
    :type df: Dataset or None
    """    

    def __init__(self, name, df = None):
        Pipe.__init__(self, name, JPipe.FORMAT_INMEMORY)
        if (df is not None):
            self.setDataset(df)

    def setDataset(self, df):
        """ Method to set DataFrame of the pipe
        
        :param df: pandas or spark dataframe for the pipe
        :type df: DataFrame
        """
        if (isinstance(df, pd.DataFrame)):
            ds = spark.createDataFrame(df)
            Pipe.getPipe(self).setDataset(ds._jdf)
        elif (isinstance(df, DataFrame)):
            Pipe.getPipe(self).setDataset(df._jdf)
        else:
            LOG.error(" setDataset(): NUll or Unsuported type: %s", type(df))

    def getDataset(self):
        """ Method to get Dataset from pipe
        
        :return: dataset of the pipe in the format of spark dataset 
        :rtype: Dataset<Row>
        """
        return Pipe.getPipe(self).getDataset()
