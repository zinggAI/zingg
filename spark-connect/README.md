# zingg-spark-connect (spike / WIP)

Standalone modules exploring a py4j-free Python API for Zingg, built on Apache
Spark Connect's command-plugin extension mechanism. **Nothing here is wired
into the root build yet** (root `pom.xml`'s `<modules>` list, the `assembly`
jar, and `python/zingg` are all untouched) -- this is meant to be merged in
deliberately once it's been exercised against a real Spark Connect server.

## Layout

```
spark-connect/
  proto/           zingg_command.proto -- single source of truth for the wire
                   schema (Pipe, FieldDefinition, Arguments, ClientOptions,
                   ZinggCommand). `mvn generate-sources` here regenerates both
                   the Java classes (target/generated-sources/protobuf) and
                   the Python stub (python/zingg_connect/proto/zingg_command_pb2.py)
                   from the same .proto file in one step.
  server-plugin/   ZinggCommandPlugin -- a Spark Connect CommandPlugin that
                   unpacks a ZinggCommand and dispatches it into the existing,
                   completely unmodified IZingg/ZinggOptions/SparkZFactory
                   execution path (zingg.spark.client.SparkClient underneath).
                   No matching/training logic lives here, only the bridge.
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

## What actually works today

- `mvn install` in `proto/` then `server-plugin/` builds cleanly against the
  already-installed `zingg:*:0.7.0` artifacts, for both the `spark-3.4` and
  `spark-3.5` Maven profiles (`-Dspark=3.4` / `-Dspark=3.5`, mirroring the
  root pom's profiles). The plugin interface shape was verified by
  decompiling the actual released `spark-connect_2.12` jars for 3.4.0 and
  3.5.5 (both match: a Scala trait, `Option[BoxedUnit] process(shaded Any,
  SparkConnectPlanner)` -- this differs from the plain `boolean`/`byte[]`
  shape currently on the apache/spark master branch source, so if zingg ever
  moves to Spark 4.x this signature needs re-verifying against that release's
  actual jar, not just its GitHub source).
- The Python package (`python/zingg_connect`) imports and builds real
  `ZinggCommand` proto messages end to end (see the sanity check below) using
  only `pyspark.sql.connect.client.core.SparkConnectClient` -- no py4j,
  `_jvm`, `_gateway`, `_jsparkSession`/`_jdf`, and no Databricks Connect
  sideloading anywhere in this module.
- Not yet run against a live Spark Connect server end-to-end -- that's the
  natural next step (start one with `sbin/start-connect-server.sh --jars
  <zingg jars> --conf
  spark.connect.extensions.command.classes=zingg.spark.connect.server.ZinggCommandPlugin`
  and point `zingg_connect.Zingg` at it).

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
