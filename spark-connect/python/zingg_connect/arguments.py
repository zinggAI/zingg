"""
zingg_connect.arguments
-------------------------
Arguments/FieldDefinition wrappers that build the wire zingg_command_pb2
messages directly. Every setter assigns a named proto field (or appends to
a named repeated field) -- never a positional tuple/list -- so adding or
removing a field on the .proto later never silently changes what an existing
setter call means. This replaces zingg.client.Arguments/FieldDefinition's
py4j object-handle wrappers; in particular it needs no
getGateway().new_array(...) for building Pipe[] arrays -- a repeated proto
field is just appended to directly.
"""

from zingg_connect.proto import zingg_command_pb2 as pb2


class FieldDefinition:
    """Defines one field used in matching.

    :param name: name of the field
    :type name: str
    :param dataType: type of the data, e.g. "string", "float"
    :type dataType: str
    :param matchType: one or more zingg_connect.options.MatchType.* values
    """

    def __init__(self, name, dataType, *matchType):
        self._fd = pb2.FieldDefinition(
            field_name=name,
            data_type=dataType,
            fields=name,
        )
        for mt in matchType:
            self._fd.match_type.add(name=mt)

    def setStopWords(self, stopWords):
        """:param stopWords: location of the stop words csv file"""
        self._fd.stop_words = stopWords

    def to_proto(self):
        return self._fd


class Arguments:
    """Supplies match arguments to Zingg over Spark Connect. Mirrors the
    setter surface of zingg.client.Arguments."""

    def __init__(self):
        self._args = pb2.Arguments()

    def to_proto(self):
        return self._args

    def setFieldDefinition(self, fieldDefs):
        """:param fieldDefs: list of FieldDefinition"""
        del self._args.field_definition[:]
        for fd in fieldDefs:
            self._args.field_definition.add().CopyFrom(fd.to_proto())

    def setData(self, *pipes):
        """:param pipes: input data pipes, e.g. setData(pipe1, pipe2, ...)"""
        del self._args.data[:]
        for p in pipes:
            self._args.data.add().CopyFrom(p.to_proto())

    def setOutput(self, *pipes):
        """:param pipes: output pipes, e.g. setOutput(pipe1, pipe2, ...)"""
        del self._args.output[:]
        for p in pipes:
            self._args.output.add().CopyFrom(p.to_proto())

    def setTrainingSamples(self, *pipes):
        """:param pipes: existing training sample pipes"""
        del self._args.training_samples[:]
        for p in pipes:
            self._args.training_samples.add().CopyFrom(p.to_proto())

    def setZinggDir(self, f):
        self._args.zingg_dir = f

    def getZinggDir(self):
        return self._args.zingg_dir

    def setModelId(self, modelId):
        self._args.model_id = modelId

    def getModelId(self):
        return self._args.model_id

    def setJobId(self, jobId):
        self._args.job_id = jobId

    def setCollectMetrics(self, collectMetrics):
        self._args.collect_metrics = collectMetrics

    def setNumPartitions(self, numPartitions):
        self._args.num_partitions = numPartitions

    def setLabelDataSampleSize(self, labelDataSampleSize):
        self._args.label_data_sample_size = labelDataSampleSize

    def setThreshold(self, threshold):
        self._args.threshold = threshold

    def setShowConcise(self, showConcise):
        self._args.show_concise = showConcise

    def setStopWordsCutoff(self, stopWordsCutoff):
        self._args.stop_words_cutoff = stopWordsCutoff

    def setBlockSize(self, blockSize):
        self._args.block_size = blockSize

    def setColumn(self, column):
        self._args.column = column
