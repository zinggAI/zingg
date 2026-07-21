"""
zingg_connect.options
----------------------
Phase names and client options for a Spark Connect Zingg run. Pure Python:
no JVM, no py4j, no gateway. Values mirror the Java side exactly so the two
stay in lockstep --
zingg.common.client.options.ZinggOptions (phase names) and
zingg.common.client.ClientOptions (the option keys), and
zingg.common.client.MatchTypes (match type names).
"""

from zingg_connect.proto import zingg_command_pb2 as pb2


class ZinggOptions:
    """Phase names accepted by ZinggCommand.phase. Matches
    zingg.common.client.options.ZinggOptions one for one."""

    TRAIN = "train"
    MATCH = "match"
    TRAIN_MATCH = "trainMatch"
    FIND_TRAINING_DATA = "findTrainingData"
    LABEL = "label"
    LINK = "link"
    GENERATE_DOCS = "generateDocs"
    RECOMMEND = "recommend"
    UPDATE_LABEL = "updateLabel"
    FIND_AND_LABEL = "findAndLabel"

    ALL = (
        TRAIN,
        MATCH,
        TRAIN_MATCH,
        FIND_TRAINING_DATA,
        LABEL,
        LINK,
        GENERATE_DOCS,
        RECOMMEND,
        UPDATE_LABEL,
        FIND_AND_LABEL,
    )


class MatchType:
    """Match type names. Matches zingg.common.client.MatchTypes one for one."""

    FUZZY = "FUZZY"
    EXACT = "EXACT"
    PINCODE = "PINCODE"
    EMAIL = "EMAIL"
    TEXT = "TEXT"
    NUMERIC = "NUMERIC"
    NUMERIC_WITH_UNITS = "NUMERIC_WITH_UNITS"
    NULL_OR_BLANK = "NULL_OR_BLANK"
    ONLY_ALPHABETS_EXACT = "ONLY_ALPHABETS_EXACT"
    ONLY_ALPHABETS_FUZZY = "ONLY_ALPHABETS_FUZZY"
    DONT_USE = "DONT_USE"


class ClientOptions:
    """Client option keys, mirroring the subset of
    zingg.common.client.ClientOptions that travels over Spark Connect
    (license/email/jobId/format/location/column). PHASE and CONF are kept as
    named constants for call-site familiarity, but neither is stored in the
    wire ClientOptions message: PHASE lives on ZinggCommand.phase directly,
    and CONF (a JSON config file path) has no meaning here since Arguments
    travels as a structured message instead of a file.
    """

    PHASE = "--phase"
    CONF = "--conf"
    LICENSE = "--license"
    EMAIL = "--email"
    JOBID = "--jobId"
    FORMAT = "--format"
    LOCATION = "--location"
    COLUMN = "--column"

    _KEY_TO_PROTO_FIELD = {
        LICENSE: "license",
        EMAIL: "email",
        JOBID: "job_id",
        FORMAT: "format",
        LOCATION: "location",
        COLUMN: "column",
    }

    def __init__(self, phase=None, argsSent=None):
        """
        :param phase: one of ZinggOptions.* -- may also be set later via setPhase
        :param argsSent: optional list of CLI-style tokens, e.g.
            ["--phase", "train", "--license", "lic.txt"], for call sites
            porting existing ClientOptions(args) usage.
        """
        self._options = pb2.ClientOptions()
        self._phase = phase
        self._values = {}
        if argsSent:
            self._parse(list(argsSent))
        if self.LICENSE not in self._values:
            self.setOptionValue(self.LICENSE, "zinggLic.txt")
        if self.EMAIL not in self._values:
            self.setOptionValue(self.EMAIL, "zingg@zingg.ai")

    def _parse(self, args):
        it = iter(args)
        for token in it:
            if token == self.PHASE:
                self._phase = next(it)
            elif token == self.CONF:
                next(it)  # no-op: Arguments travel structured, not as a JSON file path
            elif token in self._KEY_TO_PROTO_FIELD:
                self.setOptionValue(token, next(it))
            else:
                raise ValueError(f"Unrecognized option '{token}'")

    def setOptionValue(self, key, value):
        """Method to map option key to the given value"""
        if key == self.PHASE:
            self._phase = value
            return
        field = self._KEY_TO_PROTO_FIELD.get(key)
        if field is None:
            raise ValueError(f"Unknown option '{key}'")
        setattr(self._options, field, value)
        self._values[key] = value

    def getOptionValue(self, key):
        """Method to get value for the key option"""
        if key == self.PHASE:
            return self._phase
        return self._values.get(key)

    def getPhase(self):
        return self._phase

    def setPhase(self, newValue):
        self._phase = newValue

    def getConf(self):
        return None

    def hasLocation(self):
        return self.LOCATION in self._values

    def getLocation(self):
        return self._values.get(self.LOCATION)

    def to_proto(self):
        """Returns the wire zingg_connect.proto.zingg_command_pb2.ClientOptions
        message (phase is carried separately on ZinggCommand.phase)."""
        return self._options
