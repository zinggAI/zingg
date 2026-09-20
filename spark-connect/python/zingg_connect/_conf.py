"""
zingg_connect._conf
-------------------
Builds the wire Arguments message from Zingg's own conf JSON -- the same file
the CLI and py4j clients take (see examples/febrl/config.json), so a dataset is
described in one place regardless of which client runs it.

This is the only module in the Spark Connect client that knows proto field
names. Every assignment is by name, never positional, so adding a field to
zingg_command.proto can never silently reinterpret an existing one.
"""

import json

from zingg_connect.proto import zingg_command_pb2 as pb2

# Zingg conf-JSON key -> proto field name, for the scalar Arguments fields.
_ARG_SCALARS = {
    "zinggDir": "zingg_dir",
    "modelId": "model_id",
    "jobId": "job_id",
    "collectMetrics": "collect_metrics",
    "numPartitions": "num_partitions",
    "labelDataSampleSize": "label_data_sample_size",
    "threshold": "threshold",
    "showConcise": "show_concise",
    "stopWordsCutoff": "stop_words_cutoff",
    "blockSize": "block_size",
    "column": "column",
}

# Proto fields declared as string that a conf file may naturally hold as a
# number -- "modelId": 100 is valid JSON and valid Zingg.
_STRING_FIELDS = {"zingg_dir", "model_id", "column"}

_PIPE_LISTS = {
    "data": "data",
    "output": "output",
    "trainingSamples": "training_samples",
}


def _as_prop(value):
    """Pipe props are string -> string on the Java side and in the wire map.
    JSON booleans therefore render as Spark's own option convention
    ("header": false -> "false"), never Python's "False"."""
    if isinstance(value, bool):
        return "true" if value else "false"
    return str(value)


def _match_types(value):
    """conf JSON writes matchType either as one string ("fuzzy"), a
    comma-separated string ("fuzzy,exact"), or a list. Java accepts any case."""
    if value is None:
        return []
    if isinstance(value, str):
        return [p.strip() for p in value.split(",") if p.strip()]
    return [str(v).strip() for v in value]


def pipe_to_proto(d):
    p = pb2.Pipe(name=d["name"], format=d["format"])
    for key, value in d.get("props", {}).items():
        p.props[key] = _as_prop(value)
    if d.get("schema") is not None:
        p.schema = d["schema"]
    if d.get("mode") is not None:
        p.mode = d["mode"]
    return p


def field_definition_to_proto(d):
    fd = pb2.FieldDefinition(
        field_name=d["fieldName"],
        data_type=d["dataType"],
        fields=d.get("fields", d["fieldName"]),
    )
    for name in _match_types(d.get("matchType")):
        fd.match_type.add(name=name)
    if d.get("stopWords") is not None:
        fd.stop_words = d["stopWords"]
    if d.get("abbreviations") is not None:
        fd.abbreviations = d["abbreviations"]
    return fd


def arguments_to_proto(conf):
    """:param conf: a dict in Zingg conf-JSON shape"""
    args = pb2.Arguments()
    for json_key, proto_field in _ARG_SCALARS.items():
        if json_key not in conf or conf[json_key] is None:
            continue
        value = conf[json_key]
        if proto_field in _STRING_FIELDS:
            value = str(value)
        setattr(args, proto_field, value)
    for json_key, proto_field in _PIPE_LISTS.items():
        for pipe in conf.get(json_key, []):
            getattr(args, proto_field).add().CopyFrom(pipe_to_proto(pipe))
    for fd in conf.get("fieldDefinition", []):
        args.field_definition.add().CopyFrom(field_definition_to_proto(fd))
    return args


def read_conf(path):
    """Loads a Zingg conf JSON file into a dict."""
    with open(path) as f:
        return json.load(f)
