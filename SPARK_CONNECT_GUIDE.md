# Running Zingg over Spark Connect

Zingg's Spark Connect client (`zingg_connect`) talks to a Spark Connect server
over gRPC — no py4j, no JVM in the client process. This guide covers both
targets end to end:

- **[Part A — Local](#part-a--local-spark-connect-server)**: a Spark Connect server on your machine.
- **[Part B — Databricks](#part-b--databricks)**: a Databricks cluster as the server, driven from your laptop.

Both run the same code. The difference is only where the server lives and how
you authenticate to it.

---

## How it works

Zingg registers two Spark Connect extensions on the **server**:

| plugin | extension point | used for |
|---|---|---|
| `ZinggCommandPlugin` | command | every fire-and-execute phase, and recording labels |
| `ZinggRelationPlugin` | relation | returning unmarked pairs to the client |

A command can only report success or failure, so the interactive label loop is
split in two: the relation plugin hands the candidate pairs **to** the client,
the human (or a rule) marks them there, and a command sends the decisions
**back** for the server to record through Zingg's own labelling pipeline.

That means `label` is two client calls rather than one — everything else is a
single call.

---

# Part A — Local Spark Connect server

## A1. Prerequisites

- JDK 11 (Spark 3.5) — `java -version`
- Apache Spark 3.5.x extracted somewhere, e.g. `~/spark`
- Maven 3.8+
- Python 3.9+

## A2. Build the jars

```bash
cd <zingg-repo>
mvn -Dspark=3.5 clean install -DskipTests
```

This produces, and installs into `~/.m2`:

```
assembly/target/zingg-0.7.0.jar
spark-connect/proto/target/zingg-spark-connect-proto-0.7.0.jar
spark-connect/server-plugin/target/zingg-spark-connect-server-plugin-0.7.0.jar
```

Use `install`, not `package` — the launcher reads the connect jars from `~/.m2`.

## A3. Create the client virtualenv

The client's PySpark must match the server's Spark line.

```bash
python3 -m venv ~/zingg-connect-venv
~/zingg-connect-venv/bin/pip install pyspark==3.5.0 pandas pyarrow
~/zingg-connect-venv/bin/pip install -e spark-connect/python
```

## A4. Start the server

```bash
export SPARK_HOME=~/spark
./zc.sh env      # prints the resolved Spark/Scala/protobuf/python it will use
./zc.sh start
```

`zc.sh start` launches Spark's Connect server with the Zingg jars on its
classpath and both plugins registered:

```
--conf spark.connect.extensions.command.classes=zingg.spark.connect.server.ZinggCommandPlugin
--conf spark.connect.extensions.relation.classes=zingg.spark.connect.server.ZinggRelationPlugin
```

It listens on `sc://localhost:15002`. Stop it later with `./zc.sh stop`.

## A5. Run the phases

```bash
./zc.sh run findTrainingData     # build candidate pairs
./zc.sh label auto               # fetch pairs -> mark -> write back
./zc.sh run train
./zc.sh run match
```

`label auto` marks pairs without a human (matching on `ssn`) for smoke tests.
Drop `auto` to be prompted for each pair:

```bash
./zc.sh label                    # 1=match  0=no match  2=unsure  9=quit
```

Other phases take the same form:

```bash
./zc.sh run trainMatch
./zc.sh run link
./zc.sh run recommend
```

**`train` needs at least 5 matches and 5 non-matches.** A single
`findTrainingData` + `label` round typically yields fewer, so repeat those two
steps 3–4 times before training. Already-labelled pairs are excluded
automatically, so each round presents new ones.

Shortcuts:

```bash
./zc.sh fullloop    # findTrainingData -> label auto -> train -> match
./zc.sh workflow    # same, but seeds pre-labelled data instead of labelling
./zc.sh clean       # delete the model directory and start over
```

## A6. Where output lands

Defaults to `/tmp/zinggConnectModels-<spark-version>/`:

```
100/trainingData/unmarked   candidate pairs
100/trainingData/marked     labelled pairs
100/model                   trained model
output/                     match results
```

Override with `MODEL_DIR`, `MODEL_ID` or `DATA` before invoking `zc.sh`.

---

# Part B — Databricks

The cluster is the Connect server. You install two jars on it, register the
plugins, and drive it from your laptop.

## B1. Create the cluster

In **Compute → Create compute**:

| setting | value |
|---|---|
| Policy | Unrestricted |
| Access mode | **Dedicated** (single user) |
| Databricks Runtime | **15.4 LTS** (Spark 3.5.0, Scala 2.12) — non-ML |
| Node type | any; a single node is enough for the febrl sample |

**Access mode matters.** Zingg needs unrestricted JVM access, which Standard
(shared) mode does not permit. Serverless cannot take custom jars or Spark
configs at all.

Note the **cluster ID** from the URL or the compute page — you need it later.

## B2. Create Unity Catalog volumes

Everything the cluster reads or writes must be on a cluster-visible path. In a
notebook or the SQL editor:

```sql
CREATE CATALOG IF NOT EXISTS zingg_catalog;
CREATE SCHEMA  IF NOT EXISTS zingg_catalog.aditya;
CREATE VOLUME  IF NOT EXISTS zingg_catalog.aditya.jars;
CREATE VOLUME  IF NOT EXISTS zingg_catalog.aditya.data;
CREATE VOLUME  IF NOT EXISTS zingg_catalog.aditya.zingg;
CREATE VOLUME  IF NOT EXISTS zingg_catalog.aditya.output;
```

They surface as `/Volumes/<catalog>/<schema>/<volume>/…` on the cluster.

## B3. Build the Databricks jars

```bash
cd <zingg-repo>
mvn -Dspark=dbr clean install -DskipTests
```

`-Dspark=dbr` pins Spark **3.5.0** (what DBR 15.4 runs, rather than the newest
3.5 patch) and produces a plugin jar built for the Databricks Connect runtime:

```
assembly/target/zingg-0.7.0.jar
spark-connect/server-plugin/target/zingg-spark-connect-server-plugin-0.7.0-dbr.jar
```

**Only these two.** The `-dbr` jar already contains the generated proto classes
and its own protobuf runtime, so no separate proto or protobuf-java jar is
needed — and installing them alongside would conflict.

## B4. Upload the jars and the data

Using the Databricks CLI (`brew install databricks`):

```bash
P=<your-profile>
V=dbfs:/Volumes/zingg_catalog/aditya

databricks fs cp --overwrite assembly/target/zingg-0.7.0.jar \
  $V/jars/zingg-0.7.0.jar --profile $P
databricks fs cp --overwrite \
  spark-connect/server-plugin/target/zingg-spark-connect-server-plugin-0.7.0-dbr.jar \
  $V/jars/zingg-spark-connect-server-plugin-0.7.0-dbr.jar --profile $P
databricks fs cp --overwrite examples/febrl/test.csv \
  $V/data/test.csv --profile $P
```

Or drag them in through the Catalog UI.

## B5. Install the jars as cluster libraries

**Compute → your cluster → Libraries → Install new → Volumes**, and add both:

```
/Volumes/zingg_catalog/aditya/jars/zingg-0.7.0.jar
/Volumes/zingg_catalog/aditya/jars/zingg-spark-connect-server-plugin-0.7.0-dbr.jar
```

## B6. Register the plugins

**Compute → your cluster → Edit → Advanced options → Spark → Spark config.**

DBR already registers plugins of its own, so **append** to the existing values
rather than replacing them — comma-separated, no spaces:

```
spark.connect.extensions.command.classes io.delta.connect.DeltaCommandPlugin,com.databricks.spark.connect.plugin.QueryHistoryCommandPlugin,zingg.spark.connect.server.ZinggCommandPlugin
spark.connect.extensions.relation.classes io.delta.connect.DeltaRelationPlugin,com.databricks.spark.connect.plugin.DbutilsRelationPlugin,zingg.spark.connect.server.ZinggRelationPlugin
```

Copy the existing values from your own cluster first — they vary by runtime.

**Restart the cluster** to apply the libraries and the config.

## B7. Configure the client

Authenticate the CLI once:

```bash
databricks auth login --host https://<workspace-host> --profile zingg-dbr
```

Add the cluster id to that profile in `~/.databrickscfg`:

```ini
[zingg-dbr]
host       = https://<workspace-host>
cluster_id = 0919-152719-3riqhtzl
auth_type  = databricks-cli
```

Create the client virtualenv. `databricks-connect` must match the runtime, and
it supplies its own PySpark — so install `zingg_connect` with `--no-deps` so it
cannot pull plain PySpark over the top:

```bash
python3.11 -m venv ~/Desktop/zingg-dbr-jars/.venv
~/Desktop/zingg-dbr-jars/.venv/bin/pip install 'databricks-connect==15.4.*' pandas pyarrow
~/Desktop/zingg-dbr-jars/.venv/bin/pip install --no-deps -e spark-connect/python
```

Check the connection:

```bash
~/Desktop/zingg-dbr-jars/.venv/bin/python -c "
from databricks.connect import DatabricksSession
print(DatabricksSession.builder.profile('zingg-dbr').getOrCreate().range(5).count())"
```

`5` means the client can reach the cluster.

## B8. Describe the dataset

Databricks runs are driven by a Zingg conf JSON — the same format the CLI takes.
[`spark-connect/python/examples/databricks.json`](spark-connect/python/examples/databricks.json)
ships as the default:

```json
{
  "fieldDefinition": [ … ],
  "data":   [{ "name":"test",   "format":"csv",
               "props": { "path":"/Volumes/zingg_catalog/aditya/data/test.csv",
                          "delimiter":",", "header":false },
               "schema":"id string, fname string, …" }],
  "output": [{ "name":"output", "format":"csv",
               "props": { "path":"/Volumes/zingg_catalog/aditya/output",
                          "delimiter":",", "header":true }}],
  "modelId": 100,
  "zinggDir": "/Volumes/zingg_catalog/aditya/zingg"
}
```

Point the paths at your own volumes. Everything in this file resolves **on the
cluster**, never on your laptop.

## B9. Run the phases

```bash
export DATABRICKS_PROFILE=zingg-dbr

./zc-dbr.sh env                    # show connection + the paths from the JSON
./zc-dbr.sh run findTrainingData
./zc-dbr.sh label auto
./zc-dbr.sh run train
./zc-dbr.sh run match
```

Identical to the local flow, minus `start`/`stop` — the cluster is already
running. Same `train` threshold applies: repeat `findTrainingData` + `label`
3–4 times first.

To use a different config file:

```bash
CONF=path/to/other.json ./zc-dbr.sh run match
```

Results appear under the `output` path from the JSON; the model and training
data under `zinggDir/<modelId>/`.

---

## Troubleshooting

**`ClassNotFoundException: zingg.spark.connect.server.ZinggRelationPlugin`**
Cluster libraries finish attaching a little after the cluster reports *Running*.
Confirm both jars show **Installed** on the Libraries tab, then retry. If it
persists, check the Spark config kept DBR's own plugin classes alongside Zingg's.

**`Unable to train as insufficient training data found`**
Fewer than 5 matches or 5 non-matches. Run more `findTrainingData` + `label`
rounds.

**`MISSING jar: …/.m2/…` (local)**
Build with `mvn … clean install`, not `package` — `zc.sh` reads from `~/.m2`.

**Client hangs or fails to authenticate (Databricks)**
The CLI token expires. Re-run `databricks auth login --profile zingg-dbr`.

**Paths silently empty**
On Databricks every path resolves on the cluster. A local path such as `/tmp/x`
writes to the driver's disk and disappears on restart — use `/Volumes/…`.

## Known limitations

- **`updateLabel`** prompts on standard input, which a server does not have; it
  is only usable from the classic CLI.
- **`generateDocs`** writes HTML through the local filesystem API, which Unity
  Catalog volumes do not accept. It works locally.

## Phase reference

| phase | what it does |
|---|---|
| `findTrainingData` | builds candidate pairs into `trainingData/unmarked` |
| `label` | fetch pairs → mark → record into `trainingData/marked` |
| `findAndLabel` | as `label`, but run `findTrainingData` first |
| `train` | trains the model from marked pairs |
| `match` | deduplicates the input, writing to the output pipe |
| `trainMatch` | `train` then `match` |
| `link` | matches across datasets rather than within one |
| `recommend` | suggests stop words |
