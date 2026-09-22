# zingg-spark-connect

A py4j-free Python API for Zingg, built on Apache Spark Connect's plugin
extension mechanism. `proto/` and `server-plugin/` are modules of the root
build; `python/zingg_connect` is a separate pip package alongside
`python/zingg`, which is left untouched.

For step-by-step setup and phase runs -- local and Databricks -- see
[SPARK_CONNECT_GUIDE.md](../SPARK_CONNECT_GUIDE.md) at the repo root.

## Layout

```
spark-connect/
  proto/           zingg_command.proto -- single source of truth for the wire
                   schema (Pipe, FieldDefinition, Arguments, ClientOptions,
                   ZinggCommand). `mvn generate-sources` here regenerates both
                   the Java classes (target/generated-sources/protobuf) and
                   the Python stub (python/zingg_connect/proto/zingg_command_pb2.py)
                   from the same .proto file in one step.
  server-plugin/   ZinggCommandPlugin (CommandPlugin) and ZinggRelationPlugin
                   (RelationPlugin). Both unpack a ZinggCommand and dispatch it
                   into the existing, completely unmodified
                   IZingg/ZinggOptions/SparkZFactory execution path
                   (zingg.spark.client.SparkClient underneath). No
                   matching/training logic lives here, only the bridge. A `dbr`
                   profile additionally emits a Databricks-compatible jar.
  python/          zingg_connect -- a new pip package (not zingg.client) with
                   the same public method names as today's python/zingg/client.py
                   and python/zingg/pipes.py, built entirely on
                   pyspark.sql.connect's own gRPC client machinery.
```

## Why field numbers matter here

`zingg_command.proto` assigns every field an explicit tag number and documents
the rule at the top of the file: numbers are permanent once shipped, removed
fields get `reserved` instead of being deleted, and new fields always take
the next unused number. Wire compatibility depends entirely on these numbers,
never on declaration order or Python call-site argument order -- every
wrapper class in `zingg_connect` builds proto messages via named field
assignment (`args.zingg_dir = ...`, `pipe.props[name] = value`,
`args.field_definition.add().CopyFrom(...)`), never positionally, so adding a
field to `Arguments` later cannot silently reinterpret an existing one.

## Building

One command per target, from the repo root:

```bash
mvn -Dspark=3.5 clean install -DskipTests    # OSS Spark 3.5 (default)
mvn -Dspark=4.2 clean install -DskipTests    # the 4.x line: 4.0 / 4.1 / 4.2
mvn -Dspark=dbr clean install -DskipTests    # Databricks Runtime 15.4
```

Use `install` rather than `package`: `zc.sh` reads the connect jars from `~/.m2`.

Each profile pins the Spark, Scala and protobuf versions for the whole reactor,
so switching lines needs no edits. `-Dspark=dbr` additionally builds a
`-dbr`-classified plugin jar for the Databricks Connect runtime; that jar is
self-contained (it carries the generated proto classes and its own protobuf
runtime), so a Databricks cluster needs only it plus the assembly jar.

The plugin interface shape differs across Spark lines -- 3.x declares Scala
traits taking Spark's shaded protobuf `Any`, while 4.x moved to plain
`byte[]`/`Optional`. Both plugins implement both overloads, so one jar serves
either. Verify against a release's actual jar (`javap`), never its GitHub
source, which has diverged from the shipped artifacts before.

## Running

Local, via `zc.sh` at the repo root:

```bash
export SPARK_HOME=~/spark
./zc.sh start
./zc.sh run findTrainingData
./zc.sh label auto
./zc.sh stop
```

Against Databricks, via `zc-dbr.sh`:

```bash
export DATABRICKS_PROFILE=<profile>
./zc-dbr.sh run findTrainingData
./zc-dbr.sh label auto
```

Both are exercised end to end -- every phase except `updateLabel` (prompts on
stdin, which a server has none of) and, on Unity Catalog volumes,
`generateDocs` (writes through the local filesystem API). See the guide for
cluster setup, the conf JSON, and troubleshooting.

## The interactive label loop

`CommandPlugin#process` only signals handled/not-handled back to Spark
Connect's planner -- there is no channel for it to return row data to the
client. That is fine for every phase that only needs success/failure, but the
`label`/`findAndLabel` loop also has to hand the unmarked pairs *to* the
client, so it is split across both extension points:

1. `Zingg.getUnmarkedPairs()` sends the `ZinggCommand` as a **Relation**.
   `ZinggRelationPlugin` answers with a `LogicalPlan` of the pairs still
   needing a label, and Spark streams the rows back.
2. The marking happens on the client, where the human is.
3. `Zingg.writeMarkedPairs()` sends the decisions back as a **Command**, with
   `ZinggCommand.labels` populated (cluster id -> 1/0/2).
   `ZinggCommandPlugin` records them via `Client.applyLabels`.

Step 3 deliberately does not write the marked pipe itself. `applyLabels`
installs a `MapLabelCollector` and runs `Labeller.execute()`, so a remote
front end goes through the same pipeline as the terminal one -- same
preprocessing, same `LabellerUtil.postProcessLabel`, same marked-pipe write.
Anything a front end could get subtly wrong lives on the server, once.
