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

from zingg_connect import Zingg, ClientOptions, ZinggOptions, loadArguments
from _common import build_args, add_connection_args, resolve_remote


def main():
    p = argparse.ArgumentParser(description="Run a Zingg phase over Spark Connect")
    p.add_argument("phase", nargs="?", default=ZinggOptions.FIND_TRAINING_DATA,
                    help="Zingg phase to run (default: findTrainingData)")
    add_connection_args(p)
    a = p.parse_args()

    args = loadArguments(a.conf) if a.conf else build_args(a.data, a.zingg_dir, a.model_id, "run")
    zingg = Zingg(args, ClientOptions(phase=a.phase), resolve_remote(a))
    zingg.initAndExecute()
    print(f"DONE -- {a.phase} ran on the Spark Connect server")


if __name__ == "__main__":
    main()
