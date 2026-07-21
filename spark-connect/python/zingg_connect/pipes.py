"""
zingg_connect.pipes
---------------------
Pipe wrappers that build the wire zingg_command_pb2.Pipe message directly.
Mirrors zingg.pipes' Pipe/CsvPipe/BigQueryPipe/SnowflakePipe/UCPipe surface,
minus the JVM object handle underneath.
"""

from zingg_connect.proto import zingg_command_pb2 as pb2


class Pipe:
    """Pipe for working with different data sources. One pipe can be used
    at multiple places with different tables, locations, queries, etc.

    :param name: name of the pipe
    :type name: str
    :param format: format of the pipe, e.g. Pipe.FORMAT_CSV
    :type format: str
    """

    FORMAT_CSV = "csv"
    FORMAT_PARQUET = "parquet"
    FORMAT_JSON = "json"
    FORMAT_TEXT = "text"
    FORMAT_XLS = "com.crealytics.spark.excel"
    FORMAT_AVRO = "avro"
    FORMAT_JDBC = "jdbc"
    FORMAT_CASSANDRA = "org.apache.spark.sql.cassandra"
    FORMAT_SNOWFLAKE = "net.snowflake.spark.snowflake"
    FORMAT_ELASTIC = "org.elasticsearch.spark.sql"
    FORMAT_EXASOL = "com.exasol.spark"
    FORMAT_BIGQUERY = "bigquery"
    FORMAT_UNITYCATALOG = "delta"

    # zingg.common.client.pipe.FilePipe constants
    LOCATION = "location"
    PATH = "path"
    HEADER = "header"
    DELIMITER = "delimiter"
    TABLE = "table"

    def __init__(self, name, format):
        self._pipe = pb2.Pipe(name=name, format=format)

    def to_proto(self):
        return self._pipe

    def addProperty(self, name, value):
        """:param name: property name
        :param value: property value (stored as a string; the wire props
            map is string -> string). Booleans are lowercased ("true"/"false")
            to match Spark's own option-string convention (e.g. the CSV
            reader's "header" option), not Python's "True"/"False".
        """
        if isinstance(value, bool):
            value = str(value).lower()
        self._pipe.props[name] = str(value)

    def setSchema(self, s):
        """:param s: schema string for the pipe"""
        self._pipe.schema = s

    def setMode(self, mode):
        self._pipe.mode = mode

    def toString(self):
        return str(self._pipe)


class CsvPipe(Pipe):
    """Pipe for reading/writing delimited text files.

    :param name: name of the pipe
    :param location: (optional) file/directory location
    :param schema: (optional) schema string for the pipe
    """

    def __init__(self, name, location=None, schema=None):
        Pipe.__init__(self, name, Pipe.FORMAT_CSV)
        if location is not None:
            self.addProperty(Pipe.LOCATION, location)
        if schema is not None:
            self.setSchema(schema)

    def setDelimiter(self, delimiter):
        self.addProperty(Pipe.DELIMITER, delimiter)

    def setLocation(self, location):
        self.addProperty(Pipe.LOCATION, location)

    def setHeader(self, header):
        self.addProperty(Pipe.HEADER, header)


class BigQueryPipe(Pipe):
    """Pipe for working with BigQuery."""

    def __init__(self, name):
        Pipe.__init__(self, name, Pipe.FORMAT_BIGQUERY)

    def setCredentialFile(self, file):
        self.addProperty("credentialsFile", file)

    def setTable(self, table):
        self.addProperty("table", table)

    def setTemporaryGcsBucket(self, bucket):
        self.addProperty("temporaryGcsBucket", bucket)

    def setViewsEnabled(self, isEnabled):
        self.addProperty("viewsEnabled", isEnabled)


class SnowflakePipe(Pipe):
    """Pipe for working with Snowflake."""

    def __init__(self, name):
        Pipe.__init__(self, name, Pipe.FORMAT_SNOWFLAKE)
        self.addProperty("application", "zingg_zingg")

    def setURL(self, url):
        self.addProperty("sfUrl", url)

    def setUser(self, user):
        self.addProperty("sfUser", user)

    def setPassword(self, passwd):
        self.addProperty("sfPassword", passwd)

    def setDatabase(self, db):
        self.addProperty("sfDatabase", db)

    def setSFSchema(self, schema):
        self.addProperty("sfSchema", schema)

    def setWarehouse(self, warehouse):
        self.addProperty("sfWarehouse", warehouse)

    def setDbTable(self, dbtable):
        self.addProperty("dbtable", dbtable)


class UCPipe(Pipe):
    """Pipe for Delta tables in Databricks Unity Catalog.

    :param name: name of the pipe
    :param table: table to read/write in the Catalog
    """

    def __init__(self, name, table=None):
        Pipe.__init__(self, name, Pipe.FORMAT_UNITYCATALOG)
        if table is not None:
            self.addProperty(Pipe.TABLE, table)

    def setTable(self, table):
        self.addProperty(Pipe.TABLE, table)
