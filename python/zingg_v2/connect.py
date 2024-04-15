from __future__ import annotations

from typing import TYPE_CHECKING

from pyspark.sql.connect import proto
from pyspark.sql.connect.plan import LogicalPlan

if TYPE_CHECKING:
    from pyspark.sql.connect.client import SparkConnectClient

from zingg_v2.proto.connect_plugins_pb2 import SubmitZinggJob


class ZinggJob(LogicalPlan):
    def __init__(self, zingg_args: str, zingg_job: str) -> None:
        super().__init__(None)
        self._args = zingg_args
        self._job_json = zingg_job

    def plan(self, session: SparkConnectClient) -> proto.Relation:
        plan = self._create_proto_relation()
        zingg_submit = SubmitZinggJob(args=self._args, options=self._job_json)
        plan.extension.Pack(zingg_submit)

        return plan
