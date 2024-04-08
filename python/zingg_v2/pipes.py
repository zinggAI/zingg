from __future__ import annotations

import json
import warnings
from typing import Optional, Union

from pandas import DataFrame as PDataFrame
from pyspark.sql import DataFrame, SparkSession
from pyspark.sql.types import StructType

from zingg_v2.structs import ZinggFileFormat


class Pipe:
    def __init__(self, name: str, format: Union[str, ZinggFileFormat]) -> None:
        self.name = name
        if not isinstance(format, ZinggFileFormat):
            format = ZinggFileFormat(format)
        self.format = format
        self.properties: dict[str, str] = {}
        self.schema: Optional[str] = None

    def getPipe(self):
        # TODO: implement it
        raise NotImplementedError()

    def addProperty(self, name: str, value: str) -> None:
        self.properties[name] = value

    def setSchema(self, schema: str) -> None:
        self.schema = schema

    def toString(self) -> str:
        return json.dumps({"name": self.name, "format": self.format, "schema": self.schema, "properties": json.dumps(self.properties)})


class CsvPipe(Pipe):
    def __init__(self, name: str, location: Optional[str] = None, schema: Optional[str] = None) -> None:
        super().__init__(name, ZinggFileFormat.CSV)
        if schema is not None:
            self.setSchema(schema)
        if location is not None:
            self.addProperty("location", location)

    def setDelimiter(self, delimiter: str) -> None:
        self.addProperty("delimiter", delimiter)

    def setLocation(self, location: str) -> None:
        self.addProperty("location", location)

    def setHeader(self, header: str) -> None:
        self.addProperty("header", header)


class BigQueryPipe(Pipe):
    def __init__(self, name: str) -> None:
        super().__init__(name, ZinggFileFormat.BIGQUERY)

    def setCredentialFile(self, credentials_file: str) -> None:
        self.addProperty("credentialsFile", credentials_file)

    def setTable(self, table: str) -> None:
        self.addProperty("table", table)

    def setTemporaryGcsBucket(self, bucket: str) -> None:
        self.addProperty("temporaryGcsBucket", bucket)

    def setViewsEnabled(self, isEnabled: bool) -> None:
        self.addProperty("viewsEnabled", "true" if isEnabled else "false")


class SnowflakePipe(Pipe):
    def __init__(self, name: str) -> None:
        super().__init__(name, ZinggFileFormat.SNOWFLAKE)
        self.addProperty("application", "zinggai_zingg")

    def setUrl(self, url: str) -> None:
        self.addProperty("sfUrl", url)

    def setUser(self, user: str) -> None:
        self.addProperty("sfUser", user)

    def setPassword(self, passwd: str) -> None:
        self.addProperty("sfPassword", passwd)

    def setDatabase(self, db: str) -> None:
        self.addProperty("sfDatabase", db)

    def setSFSchema(self, schema: str) -> None:
        self.addProperty("sfSchema", schema)

    def setWarehouse(self, warehouse: str) -> None:
        self.addProperty("sfWarehouse", warehouse)

    def setDbTable(self, dbtable: str) -> None:
        self.addProperty("dbtable", dbtable)


class InMemoryPipe(Pipe):
    def __init__(self, name: str, df: Optional[Union[DataFrame, PDataFrame]] = None) -> None:
        super().__init__(name, ZinggFileFormat.INMEMORY)
        self.df: Optional[DataFrame] = None
        if df is not None:
            self.setDataset(df)

    def setDataset(self, df: Union[DataFrame, PDataFrame]) -> None:
        if isinstance(df, PDataFrame):
            spark = SparkSession.getActiveSession()
            if spark is None:
                warnings.warn("No active Session Found!")
                spark = SparkSession.builder.getOrCreate()

            if self.schema is None:
                df = spark.createDataFrame(df)
            else:
                df = spark.createDataFrame(df, schema=StructType.fromJson(json.loads(self.schema)))

        self.df = df

    def getDataset(self) -> DataFrame:
        if self.df is None:
            raise ValueError("DataFrame is not set!")

        return self.df
