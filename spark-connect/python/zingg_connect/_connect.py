"""
zingg_connect._connect
-------------------------
Bridge to a live Spark Connect gRPC session.

Built only on pyspark.sql.connect's own client machinery
(pyspark.sql.connect.client.core.SparkConnectClient), which is itself a typed
wrapper over a grpc channel plus Spark Connect's generated protobuf stubs.
This module never touches py4j, never touches pyspark's classic
SparkContext/_jvm/_gateway, never reaches into ._jsparkSession/._jdf, and
never special-cases Databricks Connect -- Spark Connect's own wire protocol
is the only integration point.
"""

from google.protobuf import any_pb2
from pyspark.sql.connect.client.core import SparkConnectClient
from pyspark.sql.connect.proto import commands_pb2
from pyspark.sql.connect.proto import base_pb2, relations_pb2


class ZinggConnectError(Exception):
    """Raised when a Zingg Spark Connect command fails on the server."""


def _resolve_client(remote):
    """
    :param remote: one of --
        * a Spark Connect connection string, e.g. "sc://localhost:15002"
        * a pyspark.sql.connect.session.SparkSession (its public .client
          property is used)
        * an already-constructed SparkConnectClient
    """
    if isinstance(remote, SparkConnectClient):
        return remote
    client = getattr(remote, "client", None)
    if isinstance(client, SparkConnectClient):
        return client
    if isinstance(remote, str):
        return SparkConnectClient(remote)
    raise TypeError(
        "remote must be a Spark Connect connection string, a "
        "pyspark.sql.connect.session.SparkSession, or a "
        "pyspark.sql.connect.client.core.SparkConnectClient; got "
        f"{type(remote)!r}"
    )


def execute_zingg_command(remote, zingg_command):
    """Packs a ZinggCommand into a Spark Connect Command extension and
    executes it, blocking until the server-side phase completes.

    Note: the server-side ZinggCommandPlugin has no channel to return a data
    payload for this command (see zingg.spark.connect.server.ZinggCommandPlugin);
    a thrown exception on the server propagates here as a ZinggConnectError,
    and a normal return means the phase completed with no result rows.

    :param remote: see _resolve_client
    :param zingg_command: a zingg_connect.proto.zingg_command_pb2.ZinggCommand
    """
    client = _resolve_client(remote)

    extension = any_pb2.Any()
    extension.Pack(zingg_command)

    command = commands_pb2.Command()
    command.extension.CopyFrom(extension)

    try:
        client.execute_command(command)
    except Exception as e:
        raise ZinggConnectError(
            f"Zingg phase '{zingg_command.phase}' failed over Spark Connect: {e}"
        ) from e


def fetch_zingg_relation(remote, zingg_command):
    """Sends a ZinggCommand as a Spark Connect *relation* (not a command) and
    returns the resulting rows as a PyArrow Table.

    This is the two-way channel the interactive label loop needs: the server
    side (ZinggRelationPlugin) returns a LogicalPlan of the unmarked pairs, and
    Spark streams those rows back here. Contrast execute_zingg_command(), which
    can only report success/failure with no data payload.

    :param remote: see _resolve_client
    :param zingg_command: a zingg_command_pb2.ZinggCommand (phase label/findAndLabel)
    """
    client = _resolve_client(remote)

    relation = relations_pb2.Relation()
    relation.extension.Pack(zingg_command)

    plan = base_pb2.Plan()
    plan.root.CopyFrom(relation)

    try:
        table, _schema = client.to_table(plan)
        return table
    except Exception as e:
        raise ZinggConnectError(
            f"Zingg relation for phase '{zingg_command.phase}' failed over Spark Connect: {e}"
        ) from e
