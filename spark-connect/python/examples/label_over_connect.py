"""
Interactive labelling over Spark Connect (py4j-free), end to end:

  1. fetch the unmarked training pairs from the server   (Zingg.getUnmarkedPairs)
  2. mark each pair on the client                        (this script's CLI prompt)
  3. write the labels back to the server                 (Zingg.writeMarkedPairs)

Steps 1 and 3 are library calls; only the marking UI lives here, since that is
application specific (CLI here, but could be a notebook or web form).

Run findTrainingData for the same model first, so there are pairs to label.
All paths are arguments -- nothing is hardcoded. Example:

  python label_over_connect.py \
      --remote sc://localhost:15002 \
      --data examples/febrl/test.csv \
      --zingg-dir /tmp/zinggConnectModels --model-id 100

Use --auto to label without a human (match if ssn is equal), for smoke tests.
"""
import argparse

from zingg_connect import (
    Zingg, Arguments, ClientOptions, ZinggOptions,
    FieldDefinition, CsvPipe, MatchType,
)

MATCH, NO_MATCH, NOT_SURE = 1, 0, 2
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
    args.setData(CsvPipe("labelInput", data_path, FEBRL_SCHEMA))
    args.setOutput(CsvPipe("labelOutput", f"{zingg_dir}/output"))
    return args


def mark_one(rows, auto):
    """Return a label (1/0/2) for one candidate pair (rows share a z_cluster)."""
    if auto:
        return MATCH if len(set(rows["ssn"].tolist())) == 1 else NO_MATCH
    cols = ["z_cluster", "z_zid", "id", "fname", "lname", "stNo", "add1", "city", "state", "dob", "ssn"]
    cols = [c for c in cols if c in rows.columns]
    print(f"\n--- Cluster {rows['z_cluster'].iloc[0]}: are these the same person? ---")
    print(rows[cols].to_string(index=False))
    while True:
        ans = input("Mark  1=yes  0=no  2=not sure : ").strip()
        if ans in ("0", "1", "2"):
            return int(ans)
        print("please type 0, 1 or 2")


def main():
    p = argparse.ArgumentParser(description="Label Zingg training pairs over Spark Connect")
    p.add_argument("--remote", default="sc://localhost:15002", help="Spark Connect connection string")
    p.add_argument("--data", default="examples/febrl/test.csv", help="input data file")
    p.add_argument("--zingg-dir", default="/tmp/zinggConnectModels", help="Zingg model directory")
    p.add_argument("--model-id", default="100", help="model id")
    p.add_argument("--auto", action="store_true", help="auto-label (no human) for smoke tests")
    a = p.parse_args()

    zingg = Zingg(build_args(a.data, a.zingg_dir, a.model_id),
                  ClientOptions(phase=ZinggOptions.LABEL), a.remote)

    # 1) fetch pairs
    pairs = zingg.getUnmarkedPairs().to_pandas()
    clusters = list(dict.fromkeys(pairs["z_cluster"].tolist()))
    print(f"Fetched {len(pairs)} rows in {len(clusters)} candidate pairs")
    if not clusters:
        print("Nothing to label -- run findTrainingData first (or the model is confident).")
        return

    # 2) mark each pair
    labels = [(cid, mark_one(pairs[pairs["z_cluster"] == cid], a.auto)) for cid in clusters]
    counts = {l: sum(1 for _, x in labels if x == l) for l in (MATCH, NO_MATCH, NOT_SURE)}
    print(f"Marked: {counts[MATCH]} match, {counts[NO_MATCH]} no-match, {counts[NOT_SURE]} not-sure")

    # 3) write labels back
    n = zingg.writeMarkedPairs(labels)
    print(f"Wrote {n} marked pairs back to the server -- run train next.")


if __name__ == "__main__":
    main()
