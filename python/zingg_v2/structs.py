from __future__ import annotations

from dataclasses import asdict, dataclass, fields
from enum import StrEnum, auto
from typing import Any, Optional, Sequence, Union

from pandas.core.frame import itertools


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


class ZinggFileFormat(StrEnum):
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


class FieldDefinition:
    def __init__(self, name: str, dataType: str, *matchType: Union[str, ZinggMatchType]) -> None:
        self.name = name
        self.dataType = dataType
        self.match_types = []
        for mt in matchType:
            if not isinstance(mt, ZinggMatchType):
                mt = ZinggMatchType(mt)

            self.match_types.append(mt)

        self.stopwords: Optional[str] = None

    def setStopWords(self, stopWords: str) -> None:
        self.stopwords = stopWords

    def getFieldDefinition(self) -> Any:
        # TODO: imeplement it
        # A single point where all the interactions with JVM should be
        raise NotImplementedError()


@dataclass
class ClientOptionsV2:
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


class ClientOptions:
    def __init__(self, argsSent: Optional[Sequence[str]]) -> None:
        if argsSent is None:
            args = []
        else:
            args = [a for a in argsSent]

        self._opt_v2 = ClientOptionsV2(**{k: v for k, v in zip(args[:-1], args[1:])})
        print("arguments for client options are ", self._opt_v2.to_java_args())

    def getClientOptions(self):
        java_args = self._opt_v2.to_java_args()
        # TODO: implement it by passing options ot JVM
        # A single point where all the interactions with JVM should be
        raise NotImplementedError()

    def getOptionValue(self, option: str) -> str:
        if option.startswith("--"):
            option = option[2:]

        if not hasattr(self._opt_v2, option):
            _msg = "Wrong option; possible options are: "
            _msg += ", ".join(f.name for f in fields(self._opt_v2))
            raise KeyError(_msg)

        return getattr(self._opt_v2, option)

    def setOptionValue(self, option: str, value: str) -> None:
        if option.startswith("--"):
            option = option[2:]

        if not hasattr(self._opt_v2, option):
            _msg = "Wrong option; possible options are: "
            _msg += ", ".join(f.name for f in fields(self._opt_v2))
            raise KeyError(_msg)

        setattr(self._opt_v2, option, value)

    def getPhase(self) -> str:
        return self._opt_v2.phase

    def setPhase(self, newValue: str) -> None:
        self._opt_v2.phase = newValue

    def getConf(self) -> str:
        return self._opt_v2.conf

    def hasLocation(self) -> bool:
        return self._opt_v2.location is None

    def getLocation(self) -> Optional[str]:
        return self._opt_v2.location
