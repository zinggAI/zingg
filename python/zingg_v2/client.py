from __future__ import annotations

from collections.abc import Sequence
from typing import Optional, Union

from pyspark.sql import SparkSession

from . import structs
from .errors import JobBuilderNotInitialized


class ZinggJobBuilder:
    def __init__(self) -> None:
        self._fields: list[structs.ZinggField] = []
        self._data: list[structs.ZinggData] = []
        self._output: list[structs.ZinggData] = []
        self._label_data_sample_size: Optional[float] = None
        self._num_partitions: Optional[int] = None
        self._model_id: Optional[int] = None
        self._zingg_dir: Optional[str] = None
        self._job_type: structs.ZinggJobType = structs.ZinggJobType.SPARK
        self._job_params: Optional[Union[structs.ZinggBigQueryParams, structs.ZinggSnowFlakeParams]] = None
        self._job_definition: Optional[structs.ZinggJobDefinition] = None

    def add_field(self, field: structs.ZinggField) -> "ZinggJobBuilder":
        self._fields.append(field)
        return self

    def set_fields(self, fields: Sequence[structs.ZinggField]) -> "ZinggJobBuilder":
        self._fields = [f for f in fields]
        return self

    def get_fields(self) -> tuple[structs.ZinggField, ...]:
        return tuple(self._fields)

    def add_data(self, data: structs.ZinggData) -> "ZinggJobBuilder":
        self._data.append(data)
        return self

    def set_data(self, data: Sequence[structs.ZinggData]) -> "ZinggJobBuilder":
        self._data = [d for d in data]
        return self

    def get_data(self) -> tuple[structs.ZinggData, ...]:
        return tuple(self._data)

    def set_label_data_sample_size(self, label_sample_size: float) -> "ZinggJobBuilder":
        self._label_data_sample_size = label_sample_size
        return self

    def get_label_data_sample_size(self) -> float:
        if self._label_data_sample_size is None:
            raise JobBuilderNotInitialized("Label data sample size is not set")
        else:
            return self._label_data_sample_size

    def set_num_partitions(self, num_partitions: int) -> "ZinggJobBuilder":
        self._num_partitions = num_partitions
        return self

    def get_num_partitions(self) -> int:
        if self._num_partitions is None:
            raise JobBuilderNotInitialized("Num partitions is not set")
        else:
            return self._num_partitions

    def set_model_id(self, model_id: int) -> "ZinggJobBuilder":
        self._model_id = model_id
        return self

    def get_model_id(self) -> int:
        if self._model_id is None:
            raise JobBuilderNotInitialized("Model ID is not set")
        else:
            return self._model_id

    def set_zingg_dir(self, zingg_dir: str) -> "ZinggJobBuilder":
        self._zingg_dir = zingg_dir
        return self

    def get_zingg_dir(self) -> str:
        if self._zingg_dir is None:
            raise JobBuilderNotInitialized("Zingg Directory is not set")
        else:
            return self._zingg_dir

    def set_job_type(self, job_type: structs.ZinggJobType) -> "ZinggJobBuilder":
        self._job_type = job_type
        return self

    def get_job_type(self) -> structs.ZinggJobType:
        return self._job_type

    def set_params(self, params: Union[structs.ZinggSnowFlakeParams, structs.ZinggBigQueryParams]) -> "ZinggJobBuilder":
        if isinstance(params, structs.ZinggSnowFlakeParams) and (self._job_type != structs.ZinggJobType.SNOWFLAKE):
            print(f"Warning! You are trying to add Snowflake parameters, but the current type of the job is {self._job_type}!")

        if isinstance(params, structs.ZinggBigQueryParams) and (self._job_type != structs.ZinggJobType.BIG_QUERY):
            print(f"Warning! You are trying to add BigQuery parameters, but the current type of the job is {self._job_type}!")

        self._job_params = params
        return self

    def get_params(self) -> Optional[Union[structs.ZinggSnowFlakeParams, structs.ZinggBigQueryParams]]:
        return self._job_params
        
    def build_job_definition(self) -> structs.ZinggJobDefinition:
        if not self._is_initialized():
            err_msg = "Job is not properly initialized."
            err_msg += "\n\tCheck that zingg dir, model_id, num_partitions and label sample size are set"
            err_msg += "\n\tIf job type is BigQuery or Snowflake, check that corresponding params are set"
            raise JobBuilderNotInitialized(err_msg)

        return structs.ZinggJobDefinition(
            job_type=self._job_type,
            fields_definition=self._fields,
            output=self._output,
            data=self._data,
            label_sample_size=self._label_data_sample_size,
            num_partitions=self._num_partitions,
            model_id=self._model_id,
            zingg_dir=self._zingg_dir,
            job_params=self._job_params,
        )

    def _is_initialized(self) -> bool:
        res = True
        res &= self._label_data_sample_size is not None
        res &= self._num_partitions is not None
        res &= self._model_id is not None
        res &= self._zingg_dir is not None
        res &= len(self._data) >= 1
        res &= len(self._output) >= 1
        res &= len(self._fields) >= 1

        if self._job_type == structs.ZinggJobType.BIG_QUERY:
            res &= self._job_params is not None
            res &= isinstance(self._job_params, structs.ZinggBigQueryParams)

        if self._job_type == structs.ZinggJobType.SNOWFLAKE:
            res &= self._job_params is not None
            res &= isinstance(self._job_params, structs.ZinggSnowFlakeParams)

        return res


def run_zingg_job(job_definition: structs.ZinggJobDefinition, spark: SparkSession) -> None:
    is_spark_connect = hasattr(spark, "_jvm")

    if not is_spark_connect:
        raise NotImplementedError()
        # TODO: implemnt spark classic pipe generation from JobDefinition
    else:
        raise NotImplementedError()
        # TODO: call Zingg on a side of SparkConnect Server by passing parameters from PySpark via command
