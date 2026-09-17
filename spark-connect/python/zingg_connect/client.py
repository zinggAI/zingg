"""
zingg_connect.client
----------------------
Spark-Connect-native entry point for running Zingg phases, replacing
zingg.client.Zingg's py4j object-proxy with a single gRPC command per run.
No py4j, no _jvm/_gateway, no _jsparkSession/_jdf reach-ins, no Databricks
Connect special-casing -- see zingg_connect._connect for the wire bridge.

Every phase runs as one ZinggCommand. The fire-and-execute phases (train,
match, trainMatch, link, findTrainingData, generateDocs, recommend,
updateLabel) go through execute(). The interactive label/findAndLabel loop
is two calls instead of one, because the decisions are made where the human
is: getUnmarkedPairs() reads the pairs back through the RelationPlugin, and
writeMarkedPairs() sends the labels for the server to record.
"""

from zingg_connect._connect import execute_zingg_command, fetch_zingg_relation
from zingg_connect.options import ZinggOptions
from zingg_connect.proto import zingg_command_pb2 as pb2

_LOOP_PHASES = {ZinggOptions.LABEL, ZinggOptions.FIND_AND_LABEL}


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
        """Validates the requested phase can run as a single fire-and-execute
        command. Kept as a separate method (a no-op otherwise) so existing call
        sites that do `z.init(); z.execute()` keep working -- unlike the py4j
        client, init/execute/cleanup all run atomically, server-side, inside a
        single ZinggCommand RPC."""
        phase = self.inpOptions.getPhase()
        if phase in _LOOP_PHASES:
            raise NotImplementedError(
                f"Phase '{phase}' is a loop, not a single call: the server has no "
                "terminal to prompt at. Use getUnmarkedPairs() to read the pairs, "
                "label them here, then writeMarkedPairs() to record them."
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

        Returns a PyArrow Table of the pairs still needing a label. For the
        label phase this requires findTrainingData to have run; for
        findAndLabel the finder is run here first, as its own command, so that
        no job is executed while the server is transforming the relation plan.
        """
        phase = self.inpOptions.getPhase()
        if phase == ZinggOptions.FIND_AND_LABEL:
            execute_zingg_command(self.remote, pb2.ZinggCommand(
                phase=ZinggOptions.FIND_TRAINING_DATA,
                args=self.inpArgs.to_proto(),
                options=self.inpOptions.to_proto(),
            ))
        command = pb2.ZinggCommand(
            phase=phase,
            args=self.inpArgs.to_proto(),
            options=self.inpOptions.to_proto(),
        )
        return fetch_zingg_relation(self.remote, command)

    def writeMarkedPairs(self, labels):
        """Send the client's label decisions to the server, which records them
        through Zingg's own labelling pipeline (the same preprocessing,
        postprocessing and marked-pipe write the terminal labeller performs),
        so a subsequent train() picks them up.

        :param labels: iterable of (z_cluster, label), label 1=match, 0=no, 2=not sure
        :returns: number of labelled pairs sent
        """
        labels = dict(labels)
        if not labels:
            return 0
        command = pb2.ZinggCommand(
            phase=self.inpOptions.getPhase(),
            args=self.inpArgs.to_proto(),
            options=self.inpOptions.to_proto(),
        )
        for cluster, label in labels.items():
            command.labels[str(cluster)] = int(label)
        execute_zingg_command(self.remote, command)
        return len(labels)
