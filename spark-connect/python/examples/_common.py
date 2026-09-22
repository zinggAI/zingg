"""
Shared plumbing for the Spark Connect examples.

Only the parts that are identical between run_over_connect.py and
label_over_connect.py live here -- the dataset description, the connection
arguments, and how a --remote/--profile pair becomes something
zingg_connect.Zingg accepts. Each example keeps whatever is its own.
"""

from zingg_connect import Arguments, FieldDefinition, CsvPipe, MatchType

FEBRL_SCHEMA = ("id string, fname string, lname string, stNo string, add1 string, "
                "add2 string, city string, areacode string, state string, "
                "dob string, ssn string")

_FUZZY_FIELDS = ("fname", "lname", "stNo", "add1", "add2", "city",
                 "areacode", "state", "dob", "ssn")


def build_args(data_path, zingg_dir, model_id, name="run"):
    """Describe the febrl dataset in code. Pass a conf JSON to the examples
    instead (--conf) to describe it in one file, as the CLI does.

    :param name: prefix for the pipe names, so a model's pipes say which
        example created them
    """
    args = Arguments()
    # id carried through (DONT_USE) so it shows up in the pairs; the rest are fuzzy-matched
    defs = [FieldDefinition("id", "string", MatchType.DONT_USE)]
    defs += [FieldDefinition(n, "string", MatchType.FUZZY) for n in _FUZZY_FIELDS]
    args.setFieldDefinition(defs)
    args.setModelId(model_id)
    args.setZinggDir(zingg_dir)
    args.setNumPartitions(4)
    args.setLabelDataSampleSize(0.5)
    args.setData(CsvPipe(f"{name}Input", data_path, FEBRL_SCHEMA))
    args.setOutput(CsvPipe(f"{name}Output", f"{zingg_dir}/output"))
    return args


def add_connection_args(p):
    """The options every example shares: where the server is, and what to run against."""
    p.add_argument("--remote", default="sc://localhost:15002",
                   help="Spark Connect connection string")
    p.add_argument("--profile", help="Databricks CLI profile (~/.databrickscfg) to connect "
                                     "through instead of --remote; required when the profile "
                                     "uses OAuth rather than a PAT.")
    p.add_argument("--data", default="examples/febrl/test.csv", help="input data file")
    p.add_argument("--zingg-dir", default="/tmp/zinggConnectModels", help="Zingg model directory")
    p.add_argument("--model-id", default="100", help="model id")
    p.add_argument("--conf", help="Zingg conf JSON (e.g. examples/febrl/config.json); "
                                  "supplies schema, fieldDefinition and pipes. When given, "
                                  "--data/--zingg-dir/--model-id are ignored.")
    return p


def resolve_remote(a):
    """--profile wins over --remote: a Databricks profile may use OAuth
    (auth_type=databricks-cli), where there is no token to put in an sc:// URL,
    so the session itself carries the credentials. zingg_connect accepts either
    a connection string or a connect SparkSession."""
    if getattr(a, "profile", None):
        from databricks.connect import DatabricksSession
        return DatabricksSession.builder.profile(a.profile).getOrCreate()
    return a.remote
