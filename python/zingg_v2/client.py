from __future__ import annotations

import json
import os
import warnings
from dataclasses import fields
from typing import Optional, Sequence, Union

from pyspark.sql import SparkSession
from pyspark.sql.connect.dataframe import DataFrame as ConnectDataFrame

from zingg_v2 import models as models_v2
from zingg_v2.connect import ZinggJob
from zingg_v2.errors import ZinggArgumentsValidationError
from zingg_v2.pipes import Pipe


class Zingg:
    def __init__(self, args: Arguments, options: ClientOptions) -> None:
        self.args = args
        self.options = options

    def execute(self) -> None:
        # TODO: implement it
        # java_args: arguments in form of string
        # that is pairs of --key value
        java_args = self.options.getClientOptions()

        # java_job_definition is JSON definition of Zingg Job
        java_job_definition = self.args.writeArgumentsToJSONString()

        spark = SparkSession.getActiveSession()

        if spark is None:
            _warn_msg = "Spark Session is not initialized in the current thread!"
            _warn_msg += " It is strongly reccomend to init SparkSession manually!"
            warnings.warn(_warn_msg)
            spark = SparkSession.builder.getOrCreate()

        spark_connect = hasattr(spark, "_jvm")

        if spark_connect:
            _log_msg = "Submitting a Zingg Job\n"
            _log_msg += f"Arguments: {java_args}\n\n"
            _log_msg += java_job_definition
            _log_msg += "\n\n"
            print(java_job_definition)
            df = ConnectDataFrame.withPlan(ZinggJob(zingg_args=java_args, zingg_job=java_job_definition), spark)
            df_rows = df.collect()
            for row in df_rows:
                print(row.asDict())
        else:
            spark_classic

        raise NotImplementedError()

    def executeLabel(self) -> None:
        raise NotImplementedError()

    def executeLabelUpdate(self) -> None:
        raise NotImplementedError()

    def getMarkedRecords(self) -> None:
        raise NotImplementedError()

    def getUnmarkedRecords(self) -> None:
        raise NotImplementedError()

    def processRecordsCli(self, unmarkedRecords, args):
        raise NotImplementedError()

    def processRecordsCliLabelUpdate(self, lines, args):
        raise NotImplementedError()

    def writeLabelledOutput(self, updatedRecords, args):
        raise NotImplementedError()

    def writeLabelledOutputFromPandas(self, candidate_pairs_pd, args):
        raise NotImplementedError()

    def setArguments(self, args: Arguments) -> None:
        self.args = args

    def getArguments(self) -> Arguments:
        return self.args

    def getOptions(self) -> ClientOptions:
        return self.options

    def setOptions(self, options: ClientOptions) -> None:
        self.options = options

    def getMarkedRecordsStat(self, markedRecords, value):
        raise NotImplementedError()

    def getMatchedMarkedRecordsStat(self):
        raise NotImplementedError()

    def getUnmatchedMarkedRecordsStat(self):
        raise NotImplementedError()

    def getUnsureMarkedRecordsStat(self):
        raise NotImplementedError()


class FieldDefinition:
    def __init__(self, name: str, dataType: str, *matchType: Union[str, models_v2.MatchType]) -> None:
        match_types = []
        for mt in matchType:
            if not isinstance(mt, models_v2.MatchType):
                mt = models_v2.MatchType(mt)

        self._model_v2 = models_v2.FieldDefinition(fieldName=name, fields=name, dataType=dataType, matchType=match_types)

    def setStopWords(self, stopWords: str) -> None:
        self._model_v2.stopWords = stopWords

    def getFieldDefinition(self) -> str:
        return self._model_v2.model_dump_json()

    def to_v2(self) -> models_v2.FieldDefinition:
        return self._model_v2


class ClientOptions:
    def __init__(self, argsSent: Optional[Sequence[str]]) -> None:
        if argsSent is None:
            args = []
        else:
            args = [a for a in argsSent]

        self._opt_v2 = models_v2.ClientOptions(**{k: v for k, v in zip(args[:-1], args[1:])})
        print("arguments for client options are ", self._opt_v2.to_java_args())

    def getClientOptions(self) -> str:
        return " ".join(self._opt_v2.to_java_args())

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

    def to_v2(self) -> models_v2.ClientOptions:
        return self._opt_v2


class Arguments:
    def __init__(self):
        self._args_v2 = models_v2.Arguments()

    @staticmethod
    def _from_v2(arguments_v2: models_v2.Arguments) -> "Arguments":
        new_obj = Arguments()
        new_obj._args_v2 = arguments_v2
        return new_obj

    def setFieldDefinition(self, fieldDef: list[FieldDefinition]) -> None:
        self._args_v2.fieldDefinition = [fd.to_v2() for fd in fieldDef]

    def setData(self, *pipes: Pipe) -> None:
        self._args_v2.data = [pp.to_v2() for pp in pipes]

    def setOutput(self, *pipes: Pipe) -> None:
        self._args_v2.output = [pp.to_v2() for pp in pipes]

    def getZinggBaseModelDir(self) -> str:
        if isinstance(self._args_v2.modelId, int):
            model_id = str(self._args_v2.modelId)
        else:
            model_id = self._args_v2.modelId

        return os.path.join(
            self._args_v2.zinggDir,
            model_id,
        )

    def getZinggModelDir(self) -> str:
        return os.path.join(self.getZinggBaseModelDir(), "model")

    def getZinggBaseTrainingDataDir(self):
        return os.path.join(
            self.getZinggBaseModelDir(),
            "trainingData",
        )

    def getZinggTrainingDataUnmarkedDir(self) -> str:
        return os.path.join(
            self.getZinggBaseTrainingDataDir(),
            "unmarked",
        )

    def getZinggTrainingDataMarkedDir(self) -> str:
        return os.path.join(
            self.getZinggBaseTrainingDataDir(),
            "marked",
        )

    def setTrainingSamples(self, *pipes: Pipe) -> None:
        self._args_v2.trainingSamples = [pp.to_v2() for pp in pipes]

    def setModelId(self, id: str) -> None:
        self._args_v2.modelId = id

    def getModelId(self):
        return self._args_v2.modelId

    def setZinggDir(self, f: str) -> None:
        self._args_v2.zinggDir = f

    def setNumPartitions(self, numPartitions: int) -> None:
        self._args_v2.numPartitions = numPartitions

    def setLabelDataSampleSize(self, labelDataSampleSize: float) -> None:
        self._args_v2.labelDataSampleSize = labelDataSampleSize

    def writeArgumentsToJSON(self, fileName: str) -> None:
        with open(fileName, "w") as f_:
            json.dump(
                self._args_v2.model_dump_json(),
                f_,
            )

    def setStopWordsCutoff(self, stopWordsCutoff: float) -> None:
        self._args_v2.stopWordsCutoff = stopWordsCutoff

    def setColumn(self, column: str):
        self._args_v2.column = column

    @staticmethod
    def createArgumentsFromJSON(fileName: str, phase: str) -> "Arguments":
        with open(fileName, "r") as f_:
            json_string = json.load(f_)

        return Arguments.createArgumentsFromJSONString(json_string, phase)

    def writeArgumentsToJSONString(self) -> str:
        return self._args_v2.model_dump_json()

    @staticmethod
    def createArgumentsFromJSONString(jsonArgs: str, phase: str):
        args_v2 = models_v2.Arguments.model_validate(jsonArgs)

        if not args_v2.validate_phase(phase):
            raise ZinggArgumentsValidationError("Wrong args for the given phase")

        return Arguments._from_v2(args_v2)

    def copyArgs(self, phase):
        argsString = self.writeArgumentsToJSONString()
        return self.createArgumentsFromJSONString(argsString, phase)

    def to_v2(self) -> models_v2.Arguments:
        return self._args_v2
