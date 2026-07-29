"""
Run a Zingg phase over Spark Connect (py4j-free), end to end.

Sends the phase, Arguments, and ClientOptions as one ZinggCommand to a running
Spark Connect server and blocks until it completes server-side.

Supported phases: findTrainingData, train, match, trainMatch, link,
generateDocs, recommend, updateLabel. (label / findAndLabel need the
RelationPlugin round-trip -- see label_over_connect.py instead.)

All paths are arguments -- nothing is hardcoded. Example:

  python run_over_connect.py train \\
      --remote sc://localhost:15002 \\
      --data examples/febrl/test.csv \\
      --zingg-dir /tmp/zinggConnectModels --model-id 100

train/match/trainMatch need labelled data first -- run findTrainingData and
label_over_connect.py (or seed marked pairs) before them.
"""
import argparse

from zingg_connect import (
    Zingg, Arguments, ClientOptions, ZinggOptions,
    FieldDefinition, CsvPipe, MatchType,
)

FEBRL_SCHEMA = ("id string, fname string, lname string, stNo string, add1 string, "
                "add2 string, city string, areacode string, state string, "
                "dob string, ssn string")


def build_args(data_path, zingg_dir, model_id):
    args = Arguments()
    # id carried through (DONT_USE) so it shows up in the pairs; the rest are fuzzy-matched
    defs = [FieldDefinition("id", "string", MatchType.DONT_USE)]
    defs += [FieldDefinition(n, "string", MatchType.FUZZY) for n in
             ("fname", "lname", "stNo", "add1", "add2", "city", "areacode", "state", "dob", "ssn")]
    args.setFieldDefinition(defs)
    args.setModelId(model_id)
    args.setZinggDir(zingg_dir)
    args.setNumPartitions(4)
    args.setLabelDataSampleSize(0.5)
    args.setData(CsvPipe("runInput", data_path, FEBRL_SCHEMA))
    args.setOutput(CsvPipe("runOutput", f"{zingg_dir}/output"))
    return args


def main():
    p = argparse.ArgumentParser(description="Run a Zingg phase over Spark Connect")
    p.add_argument("phase", nargs="?", default=ZinggOptions.FIND_TRAINING_DATA,
                    help="Zingg phase to run (default: findTrainingData)")
    p.add_argument("--remote", default="sc://localhost:15002", help="Spark Connect connection string")
    p.add_argument("--data", default="examples/febrl/test.csv", help="input data file")
    p.add_argument("--zingg-dir", default="/tmp/zinggConnectModels", help="Zingg model directory")
    p.add_argument("--model-id", default="100", help="model id")
    a = p.parse_args()

    zingg = Zingg(build_args(a.data, a.zingg_dir, a.model_id),
                  ClientOptions(phase=a.phase), a.remote)
    zingg.initAndExecute()
    print(f"DONE -- {a.phase} ran on the Spark Connect server")


if __name__ == "__main__":
    main()
