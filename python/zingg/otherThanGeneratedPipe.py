from zingg.PipeGenerated import *

class ExtendedPipeGenerated(Pipe):
    def __init__(self, name, format):
        super().__init__(name, format)
    
    def getPipe(self):
        return self.pipe
    
    def addProperty(self, name, value):
        """ Method for adding different properties of pipe

        :param name: name of the property
        :type name: String
        :param value: value you want to set for the property
        :type value: String
        """
        self.pipe.setProp(name, value)

class CsvPipe(ExtendedPipeGenerated):
    """ Class CsvPipe: used for working with text files which uses a pipe symbol to separate units of text that belong in different columns.

    :param name: name of the pipe.
    :type name: String
    :param location: (optional) location from where we read data
    :type location: String or None
    :param schema: (optional) json schema for the pipe
    :type schema: Schema or None
    """
    def __init__(self, name, location = None, schema = None):
        ExtendedPipeGenerated.__init__(self, name, JPipe.FORMAT_CSV)
        if(location != None):
            ExtendedPipeGenerated.addProperty(self, FilePipe.LOCATION, location)
            if(schema != None):
                #df = spark.read.format(JPipe.FORMAT_CSV).schema(schema).load(location)
                #s = JStructType.fromDDL(schema)
                ExtendedPipeGenerated.setSchema(self, schema)
                print("set schema ")
    
    def setDelimiter(self, delimiter):
        """ This method is used to define delimiter of CsvPipe

        :param delimiter: a sequence of one or more characters for specifying the boundary between separate, independent regions in data streams
        :type delimiter: String
        """
        ExtendedPipeGenerated.addProperty(self, "delimiter", delimiter)
    

    def setLocation(self, location):
        """ Method to set location of pipe

        :param location: location from where we read data
        :type location: String
        """
        ExtendedPipeGenerated.addProperty(self, FilePipe.LOCATION, location)

    def setHeader(self, header):
        """ Method to set header property of pipe

        :param header: true if pipe have header, false otherwise
        :type header: Boolean
        """
        ExtendedPipeGenerated.addProperty(self, FilePipe.HEADER, header)
    
class BigQueryPipe(ExtendedPipeGenerated):
    """ Pipe Class for working with BigQuery pipeline

    :param name: name of the pipe.
    :type name: String
    """

    VIEWS_ENABLED = "viewsEnabled"
    CREDENTIAL_FILE = "credentialsFile"
    TABLE = "table"
    TEMP_GCS_BUCKET="temporaryGcsBucket"

    def __init__(self,name):
        ExtendedPipeGenerated.__init__(self, name, JPipe.FORMAT_BIGQUERY)

    def setCredentialFile(self, file):
        """ Method to set Credential file to the pipe

        :param file: credential file name
        :type file: String
        """
        ExtendedPipeGenerated.addProperty(self, "credentialsFile", file)

    def setTable(self, table):
        """ Method to set Table to the pipe

        :param table: provide table parameter
        :type table: String
        """
        ExtendedPipeGenerated.addProperty(self, "table", table)

    def setTemporaryGcsBucket(self, bucket):
        """ Method to set TemporaryGcsBucket to the pipe

        :param bucket: provide bucket parameter
        :type bucket: String
        """
        ExtendedPipeGenerated.addProperty(self, "temporaryGcsBucket", bucket)

    def setViewsEnabled(self, isEnabled):
        """ Method to set if viewsEnabled parameter is Enabled or not

        :param isEnabled: provide boolean parameter which defines if viewsEnabled option is enable or not
        :type isEnabled: Bool
        """
        ExtendedPipeGenerated.addProperty(self, "viewsEnabled", isEnabled)


class SnowflakePipe(ExtendedPipeGenerated):
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
        ExtendedPipeGenerated.__init__(self, name, JPipe.FORMAT_SNOWFLAKE)
        ExtendedPipeGenerated.addProperty(self, "application", "zinggai_zingg")
        

    def setURL(self, url):
        """ Method to set url to the pipe

        :param url: provide url for this pipe
        :type url: String
        """
        ExtendedPipeGenerated.addProperty(self, "sfUrl", url)

    def setUser(self, user):
        """ Method to set User to the pipe
        
        :param user: provide User parameter.
        :type user: String
        """
        ExtendedPipeGenerated.addProperty(self, "sfUser", user)

    def setPassword(self, passwd):
        """ Method to set Password to the pipe
        
        :param passwd: provide Password parameter.
        :type passwd: String
        """
        ExtendedPipeGenerated.addProperty(self, "sfPassword", passwd)

    def setDatabase(self, db):
        """ Method to set Database to the pipe
        
        :param db: provide Database parameter.
        :type db: Database
        """
        ExtendedPipeGenerated.addProperty(self, "sfDatabase", db)

    def setSFSchema(self, schema):
        """ Method to set Schema to the pipe
        
        :param schema: provide schema parameter.
        :type schema: Schema
        """
        ExtendedPipeGenerated.addProperty(self, "sfSchema", schema)

    def setWarehouse(self, warehouse):
        """ Method to set warehouse parameter to the pipe
        
        :param warehouse: provide warehouse parameter.
        :type warehouse: String
        """
        ExtendedPipeGenerated.addProperty(self, "sfWarehouse", warehouse)

    def setDbTable(self, dbtable):
        """ description
        
        :param dbtable: provide bucket parameter.
        :type dbtable: String
        """
        ExtendedPipeGenerated.addProperty(self, "dbtable", dbtable)     
        

class InMemoryPipe(ExtendedPipeGenerated):
    """ Pipe Class for working with InMemory pipeline

    :param name: name of the pipe
    :type name: String
    :param df: provide dataset for this pipe (optional)
    :type df: Dataset or None
    """    

    def __init__(self, name, df = None):
        ExtendedPipeGenerated.__init__(self, name, JPipe.FORMAT_INMEMORY)
        if (df is not None):
            self.setDataset(df)

    def setDataset(self, df):
        """ Method to set DataFrame of the pipe
        
        :param df: pandas or spark dataframe for the pipe
        :type df: DataFrame
        """
        if (isinstance(df, pd.DataFrame)):
            print('schema of pandas df is ' , ExtendedPipeGenerated.getPipe(self).getSchema())
            if (ExtendedPipeGenerated.getPipe(self).getSchema() is not None):
                ds = getSparkSession().createDataFrame(df, schema=ExtendedPipeGenerated.getPipe(self).getSchema())
            else:
                ds = getSparkSession().createDataFrame(df)
            
            ExtendedPipeGenerated.getPipe(self).setDataset(ds._jdf)
        elif (isinstance(df, DataFrame)):
            ExtendedPipeGenerated.getPipe(self).setDataset(df._jdf)
        else:
            LOG.error(" setDataset(): NUll or Unsupported type: %s", type(df))

    def getDataset(self):
        """ Method to get Dataset from pipe
        
        :return: dataset of the pipe in the format of spark dataset 
        :rtype: Dataset<Row>
        """
        return ExtendedPipeGenerated.getPipe(self).getDataset().df()