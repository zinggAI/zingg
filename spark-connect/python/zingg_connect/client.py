"""
zingg_connect.client
----------------------
Spark-Connect-native entry point for running Zingg phases, replacing
zingg.client.Zingg's py4j object-proxy with a single gRPC command per run.
No py4j, no _jvm/_gateway, no _jsparkSession/_jdf reach-ins, no Databricks
Connect special-casing -- see zingg_connect._connect for the wire bridge.

Known limitation: the interactive label/findAndLabel loop needs a Spark
Connect RelationPlugin (to stream row data back), which is not implemented
in this module yet -- see zingg.spark.connect.server.ZinggCommandPlugin's
docstring. Only the fire-and-execute phases are supported here: train,
match, trainMatch, link, findTrainingData, generateDocs, recommend,
updateLabel.
"""

from zingg_connect._connect import execute_zingg_command, fetch_zingg_relation
from zingg_connect.options import ZinggOptions
from zingg_connect.proto import zingg_command_pb2 as pb2

_UNSUPPORTED_PHASES = {ZinggOptions.LABEL, ZinggOptions.FIND_AND_LABEL}


class Zingg:
    """Main entry point for running a Zingg phase over Spark Connect.

    :param args: arguments for training and matching
    :type args: zingg_connect.arguments.Arguments
    :param options: client options for this run (phase, license, email, ...)
    :type options: zingg_connect.options.ClientOptions
    :param remote: Spark Connect connection string (e.g. "sc://localhost:15002"),
        or an existing pyspark.sql.connect.session.SparkSession /
        pyspark.sql.connect.client.core.SparkConnectClient
    """

    def __init__(self, args, options, remote):
        self.inpArgs = args
        self.inpOptions = options
        self.remote = remote

    def init(self):
        """Validates the requested phase is supported over this module's
        CommandPlugin path. Kept as a separate method (a no-op otherwise) so
        existing call sites that do `z.init(); z.execute()` keep working --
        unlike the py4j client, init/execute/cleanup all run atomically,
        server-side, inside a single ZinggCommand RPC."""
        phase = self.inpOptions.getPhase()
        if phase in _UNSUPPORTED_PHASES:
            raise NotImplementedError(
                f"Phase '{phase}' returns row data to the caller and needs a "
                "Spark Connect RelationPlugin -- not implemented yet in "
                "zingg_connect. Supported phases: "
                + ", ".join(p for p in ZinggOptions.ALL if p not in _UNSUPPORTED_PHASES)
            )

    def execute(self):
        """Sends the phase, Arguments, and ClientOptions as one ZinggCommand
        over Spark Connect and blocks until the server-side phase completes."""
        self.init()
        command = pb2.ZinggCommand(
            phase=self.inpOptions.getPhase(),
            args=self.inpArgs.to_proto(),
            options=self.inpOptions.to_proto(),
        )
        execute_zingg_command(self.remote, command)

    def initAndExecute(self):
        """Runs init and execute consecutively."""
        self.init()
        self.execute()

    def setArguments(self, args):
        self.inpArgs = args

    def getArguments(self):
        return self.inpArgs

    def setOptions(self, options):
        self.inpOptions = options

    def getOptions(self):
        return self.inpOptions

    def getUnmarkedPairs(self):
        """Fetch the unmarked training pairs from the server so they can be
        labelled on the client. Uses the RelationPlugin path (returns row data),
        unlike execute() which is fire-and-execute only.

        Returns a PyArrow Table of the pairs still needing a label. Intended for
        the label / findAndLabel phases; requires findTrainingData to have run.
        """
        command = pb2.ZinggCommand(
            phase=self.inpOptions.getPhase(),
            args=self.inpArgs.to_proto(),
            options=self.inpOptions.to_proto(),
        )
        return fetch_zingg_relation(self.remote, command)
