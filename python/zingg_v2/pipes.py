from __future__ import annotations

import json
import warnings
from typing import Optional, Union

from pandas import DataFrame as PDataFrame
from pyspark.sql import DataFrame, SparkSession
from pyspark.sql.types import StructType

from zingg_v2 import models as models_v2


class Pipe:
    def __init__(self, name: str, format: Union[str, models_v2.DataFormat]) -> None:
        if not isinstance(format, models_v2.DataFormat):
            format = models_v2.DataFormat(format)
        self._pipe_v2 = models_v2.Pipe(name=name, format=format)

    def getPipe(self) -> str:
        return self.toString()

    def addProperty(self, name: str, value: str) -> None:
        self._pipe_v2.props[name] = value

    def setSchema(self, schema: str) -> None:
        self.schema = schema

    def toString(self) -> str:
        return json.dumps(self._pipe_v2.model_dump_json())

    def to_v2(self) -> models_v2.Pipe:
        return self._pipe_v2


class CsvPipe(Pipe):
    def __init__(self, name: str, location: Optional[str] = None, schema: Optional[str] = None) -> None:
        super().__init__(name, models_v2.DataFormat.CSV)
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
        super().__init__(name, models_v2.DataFormat.BIGQUERY)

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
        super().__init__(name, models_v2.DataFormat.SNOWFLAKE)
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
        super().__init__(name, models_v2.DataFormat.INMEMORY)
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
