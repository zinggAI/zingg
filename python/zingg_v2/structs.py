from __future__ import annotations

from dataclasses import dataclass
from enum import StrEnum, auto
from typing import Optional, Union

from pyspark.sql.types import DataType, StructType


class ZinggMatchType(StrEnum):
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


class ZinggFormatType(StrEnum):
    CSV = auto()
    PARQUET = auto()
    ORC = auto()
    JDBC = auto()
    AVRO = auto()


class ZinggJobType(StrEnum):
    SPARK = auto()
    BIG_QUERY = auto()
    SNOWFLAKE = auto()


@dataclass
class ZinggField:
    filed_name: str
    fields: list[str]
    data_type: DataType
    match_type: ZinggMatchType


@dataclass
class ZinggData:
    name: str
    format: ZinggFormatType
    props: dict[str, str]
    schema: StructType


@dataclass
class ZinggBigQueryParams:
    views_enabled: bool
    credential_file: str
    table: str
    temp_gcs_bucket: str


@dataclass
class ZinggSnowFlakeParams:
    url: str
    user: str
    password: str
    database: str
    schema: str
    warehouse: str
    dbtable: str


@dataclass
class ZinggJobDefinition:
    job_type: ZinggJobType
    fields_definition: list[ZinggField]
    output: list[ZinggData]
    data: list[ZinggData]
    label_sample_size: float
    num_partitions: int
    model_id: int
    zingg_dir: str
    job_params: Optional[Union[ZinggSnowFlakeParams, ZinggBigQueryParams]] = None
