from __future__ import annotations

import itertools
from dataclasses import asdict, dataclass
from enum import StrEnum, auto
from typing import Any, Optional, Union

from pydantic import BaseModel


class MatchType(StrEnum):
    FUZZY = auto()
    EXACT = auto()
    DONT_USE = auto()
    EMAIL = auto()
    PINCODE = auto()
    NULL_OR_BLANK = auto()
    TEXT = auto()
    NUMERIC = auto()
    NUMERIC_WITH_UNITS = auto()
    ONLY_ALPHABETS_EXACT = auto()
    ONLY_ALPHABETS_FUZZY = auto()


class DataFormat(StrEnum):
    CSV = auto()
    PARQUET = auto()
    JSON = auto()
    TEXT = auto()
    XLS = "com.crealytics.spark.excel"
    AVRO = auto()
    JDBC = auto()
    CASSANDRA = "org.apache.spark.sql.cassandra"
    SNOWFLAKE = "net.snowflake.spark.snowflake"
    ELASTIC = "org.elasticsearch.spark.sql"
    EXACOL = "com.exasol.spark"
    BIGQUERY = auto()
    INMEMORY = auto()


class FieldDefinition(BaseModel):
    matchType: Union[MatchType, list[MatchType]]
    dataType: str
    fieldName: str
    fields: str
    stopWords: Optional[str] = None
    abbreviations: Optional[str] = None


class Pipe(BaseModel):
    name: str
    format: DataFormat
    props: dict[str, Any] = {}
    schema: Optional[str] = None
    mode: Optional[str] = None


class Arguments(BaseModel):
    output: Optional[list[Pipe]] = None
    data: Optional[list[Pipe]] = None
    zinggDir: str = "/tmp/zingg"
    trainingSamples: Optional[list[Pipe]] = None
    fieldDefinition: Optional[list[FieldDefinition]] = None
    numPartitions: int = 10
    labelDataSampleSize: float = 0.01
    modelId: Union[str, int] = "1"
    threshold: float = 0.5
    jobId: int = 1
    collectMetrics: bool = True
    showConcise: bool = False
    stopWordsCutoff: float = 0.1
    blockSize: int = 100
    column: Optional[str] = None

    def validate_phase(self, phase: str) -> bool:
        is_valid = True
        if phase in ["train", "match", "trainMatch", "link"]:
            is_valid &= self.trainingSamples is not None
            is_valid &= self.data is not None
            is_valid &= self.numPartitions is not None
            is_valid &= self.fieldDefinition is not None

        elif phase in ["seed", "seedDB"]:
            is_valid &= self.data is not None
            is_valid &= self.numPartitions is not None
            is_valid &= self.fieldDefinition is not None

        elif phase != "WEB":
            is_valid &= self.data is not None
            is_valid &= self.numPartitions is not None

        return is_valid


@dataclass
class ClientOptions:
    phase: str = "peekModel"
    license: str = "zinggLic.txt"
    email: str = "zingg@zingg.ai"
    conf: str = "dummyConf.json"
    preprocess: Optional[str] = None
    jobId: Optional[str] = None
    format: Optional[str] = None
    zinggDir: Optional[str] = None
    modelId: Optional[str] = None
    collectMetrics: Optional[str] = None
    showConcise: Optional[str] = None
    location: Optional[str] = None
    column: Optional[str] = None
    remote: Optional[str] = None

    def to_java_args(self) -> list[str]:
        return list(itertools.chain.from_iterable([[f"--{key}", value] for key, value in asdict(self) if value is not None]))
