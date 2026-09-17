#!/usr/bin/env bash
# Helper for running the Zingg Spark Connect end-to-end loop locally.
#
# Works against any supported Spark line -- everything is derived from the
# distribution that SPARK_HOME points at, so switching between Spark 3.5 and
# Spark 4.x is just a matter of exporting a different SPARK_HOME.
#
# Usage:
#   export SPARK_HOME=~/spark-4.2.0      # or ~/spark for the 3.5 line
#   ./zc.sh start          # start the Spark Connect server (with Zingg plugins)
#   ./zc.sh stop           # stop the server
#   ./zc.sh run [phase]    # run_over_connect.py for a phase (default findTrainingData)
#   ./zc.sh pairs [auto]   # label_over_connect.py: fetch pairs, mark, write back (over Connect)
#   ./zc.sh label          # SEED pre-labeled data (stand-in; see note below)
#   ./zc.sh clean          # remove previous model/output dirs
#   ./zc.sh fullloop       # full pipeline with REAL labelling over Connect: ftd -> label(auto) -> train -> match
#   ./zc.sh workflow       # full pipeline with SEEDED labels: ftd -> label(seed) -> train -> match
#   ./zc.sh env            # print the resolved settings and exit (useful when debugging)
#
# Overridable by exporting before invoking: VENV_PY, MODEL_DIR, MODEL_ID,
# PROTOBUF_VERSION, SCALA_BIN, SPARK_VERSION, JAVA_HOME, M2.
#
# NOTE on 'label' vs 'pairs': real labelling over Spark Connect works via the
# ZinggRelationPlugin (see `pairs`/label_over_connect.py -- fetch unmarked
# pairs, mark, write back) and is exercised end to end by `fullloop`. The
# `label` step is a separate, faster stand-in: it copies pre-labeled
# ("marked") Febrl training data shipped in the repo into the model dir, in
# place of a real labelling session -- `workflow` uses this shortcut.

set -euo pipefail

# ---------- resolve the environment ---------------------------------------

SPARK="${SPARK_HOME:?set SPARK_HOME to the Spark distribution you want to run against}"
WORKTREE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
M2="${M2:-$HOME/.m2/repository}"

# Read the Spark and Scala versions off the distribution rather than pinning
# them, so this script cannot drift from whatever SPARK_HOME actually is.
_core_jar="$(ls "$SPARK"/jars/spark-core_*.jar 2>/dev/null | head -1 || true)"
if [ -z "$_core_jar" ]; then
  echo "ERROR: no spark-core jar under $SPARK/jars -- is SPARK_HOME correct?" >&2
  exit 1
fi
_core_jar="$(basename "$_core_jar")"
SCALA_BIN="${SCALA_BIN:-$(echo "$_core_jar" | sed -E 's/^spark-core_([0-9.]+)-.*/\1/')}"
SPARK_VERSION="${SPARK_VERSION:-$(echo "$_core_jar" | sed -E 's/^spark-core_[0-9.]+-(.*)\.jar$/\1/')}"

# protobuf-java must match what this Spark line's spark-connect server pins.
# Same table as the <protobuf.version> properties in the root pom.xml.
case "$SPARK_VERSION" in
  3.5.*) PROTOBUF_VERSION="${PROTOBUF_VERSION:-3.23.4}" ;;
  4.0.*) PROTOBUF_VERSION="${PROTOBUF_VERSION:-4.29.3}" ;;
  4.1.*) PROTOBUF_VERSION="${PROTOBUF_VERSION:-4.33.0}" ;;
  4.2.*) PROTOBUF_VERSION="${PROTOBUF_VERSION:-4.33.5}" ;;
  *) echo "ERROR: no protobuf version mapped for Spark $SPARK_VERSION; export PROTOBUF_VERSION" >&2; exit 1 ;;
esac

# The pyspark client must match the server's major line, so each has its own venv.
case "$SPARK_VERSION" in
  3.*) VENV_PY="${VENV_PY:-$HOME/zingg-connect-venv/bin/python}" ;;
  4.*) VENV_PY="${VENV_PY:-$HOME/zingg-connect-venv-42/bin/python}" ;;
esac

# Spark 4.x needs Java 17+; Spark 3.5 runs on 11. Fail loudly rather than
# letting spark-submit die with a confusing bytecode error.
# Note: `/usr/libexec/java_home -v 17` does NOT fail when 17 is absent -- it
# returns whatever JDK it does have -- so every candidate must be verified.
_is_java17plus() {
  [ -x "$1/bin/java" ] || return 1
  "$1/bin/java" -version 2>&1 | head -1 | grep -qE '"(1[7-9]|[2-9][0-9])'
}

case "$SPARK_VERSION" in
  4.*)
    _found=""
    for _cand in "${JAVA_HOME:-}" \
                 "$(/usr/libexec/java_home -v 17 2>/dev/null || true)" \
                 "$(brew --prefix openjdk@17 2>/dev/null || true)/libexec/openjdk.jdk/Contents/Home"; do
      [ -n "$_cand" ] || continue
      if _is_java17plus "$_cand"; then _found="$_cand"; break; fi
    done
    if [ -z "$_found" ]; then
      echo "ERROR: Spark $SPARK_VERSION needs Java 17+, none found." >&2
      echo "       Install one (brew install openjdk@17) or export JAVA_HOME." >&2
      exit 1
    fi
    JAVA_HOME="$_found"
    export JAVA_HOME
    ;;
esac

# Models written by different Spark lines should not share a directory.
MODEL_DIR="${MODEL_DIR:-/tmp/zinggConnectModels-$SPARK_VERSION}"
MODEL_ID="${MODEL_ID:-100}"
# Spark's 1g driver default is not enough for Zingg's match phase on a
# long-lived Connect server -- it OOMs with "Java heap space".
DRIVER_MEMORY="${DRIVER_MEMORY:-4g}"
OUTPUT_DIR="$MODEL_DIR/output"
MARKED_SRC="$WORKTREE/test/testFebrl/$MODEL_ID/trainingData/marked"
MARKED_DST="$MODEL_DIR/$MODEL_ID/trainingData/marked"

JARS="$WORKTREE/assembly/target/zingg-0.7.0.jar"
JARS="$JARS,$M2/zingg/zingg-spark-connect-proto/0.7.0/zingg-spark-connect-proto-0.7.0.jar"
JARS="$JARS,$M2/zingg/zingg-spark-connect-server-plugin/0.7.0/zingg-spark-connect-server-plugin-0.7.0.jar"
JARS="$JARS,$M2/com/google/protobuf/protobuf-java/$PROTOBUF_VERSION/protobuf-java-$PROTOBUF_VERSION.jar"

# Spark 4.x ships spark-connect in its own jars/ dir; 3.5 does not. Adding
# --packages on top of a bundled jar just invites a version clash, so only ask
# for it when the distribution lacks it.
if ls "$SPARK"/jars/spark-connect_*.jar >/dev/null 2>&1; then
  CONNECT_PKG_ARGS=()
  CONNECT_PKG_DESC="<bundled in distribution>"
else
  CONNECT_PKG_ARGS=(--packages "org.apache.spark:spark-connect_${SCALA_BIN}:${SPARK_VERSION}")
  CONNECT_PKG_DESC="${CONNECT_PKG_ARGS[*]}"
fi

EXAMPLES="$WORKTREE/spark-connect/python/examples"
DATA="${DATA:-$WORKTREE/examples/febrl/test.csv}"
CONN_ARGS=(--remote sc://localhost:15002 --data "$DATA" --zingg-dir "$MODEL_DIR" --model-id "$MODEL_ID")

# ---------- commands -------------------------------------------------------

env_show() {
  cat <<EOF
SPARK_HOME       $SPARK
spark / scala    $SPARK_VERSION / $SCALA_BIN
JAVA_HOME        ${JAVA_HOME:-<inherited>}
protobuf-java    $PROTOBUF_VERSION
connect package  $CONNECT_PKG_DESC
python           $VENV_PY
driver memory    $DRIVER_MEMORY
model dir        $MODEL_DIR (model $MODEL_ID)
data             $DATA
EOF
}

preflight() {
  local missing=0
  local IFS=,
  for j in $JARS; do
    [ -f "$j" ] || { echo "MISSING jar: $j" >&2; missing=1; }
  done
  [ -x "$VENV_PY" ] || { echo "MISSING python: $VENV_PY" >&2; missing=1; }
  [ "$missing" -eq 0 ] || {
    echo "Build first: mvn -Dspark=${SPARK_VERSION%.*} clean install -DskipTests" >&2
    exit 1
  }
}

start() {
  preflight
  SPARK_LOCAL_IP=127.0.0.1 "$SPARK/sbin/start-connect-server.sh" \
    ${CONNECT_PKG_ARGS[@]+"${CONNECT_PKG_ARGS[@]}"} \
    --driver-memory "$DRIVER_MEMORY" \
    --jars "$JARS" \
    --conf spark.driver.bindAddress=127.0.0.1 \
    --conf spark.connect.extensions.command.classes=zingg.spark.connect.server.ZinggCommandPlugin \
    --conf spark.connect.extensions.relation.classes=zingg.spark.connect.server.ZinggRelationPlugin
  wait_up
}

wait_up() {
  echo "waiting for server on 15002..."
  for _ in $(seq 1 30); do
    if lsof -tiTCP:15002 -sTCP:LISTEN >/dev/null 2>&1; then echo "server UP"; return 0; fi
    sleep 2
  done
  echo "server did NOT come up in time -- see $SPARK/logs/" >&2; return 1
}

run()   { preflight; "$VENV_PY" "$EXAMPLES/run_over_connect.py" "${1:-findTrainingData}" "${CONN_ARGS[@]}"; }

pairs() {
  preflight
  local args=("${CONN_ARGS[@]}")
  [ "${1:-}" = "auto" ] && args+=(--auto)
  "$VENV_PY" "$EXAMPLES/label_over_connect.py" "${args[@]}"
}

# full loop with REAL labelling over Connect (auto-marked for an unattended test)
fullloop() {
  echo "===== 1/4 findTrainingData ====="; run findTrainingData
  echo "===== 2/4 label (fetch pairs -> mark -> write back, over Connect) ====="; pairs auto
  echo "===== 3/4 train ====="; run train
  echo "===== 4/4 match ====="; run match
  echo "===== full label loop complete -- results under $OUTPUT_DIR ====="
}

stop()  { "$SPARK/sbin/stop-connect-server.sh"; }
clean() { rm -rf "$MODEL_DIR"; echo "cleaned $MODEL_DIR"; }

label() {
  echo ">>> [label] Seeding pre-labeled Febrl data as a fast stand-in for a human labeling session."
  echo ">>> [label] For real labelling over Spark Connect instead, use: ./zc.sh pairs [auto]"
  if [ ! -d "$MARKED_SRC" ]; then
    echo "ERROR: marked source not found: $MARKED_SRC" >&2; exit 1
  fi
  mkdir -p "$MARKED_DST"
  cp "$MARKED_SRC"/*.parquet "$MARKED_DST"/ 2>/dev/null || cp -R "$MARKED_SRC"/. "$MARKED_DST"/
  echo ">>> [label] seeded $(ls "$MARKED_DST"/*.parquet 2>/dev/null | wc -l | tr -d ' ') parquet file(s) into $MARKED_DST"
}

workflow() {
  echo "===== STEP 1/4: findTrainingData (over Spark Connect) ====="
  run findTrainingData
  echo "===== STEP 2/4: label (SEEDED stand-in -- not over Connect) ====="
  label
  echo "===== STEP 3/4: train (over Spark Connect) ====="
  run train
  echo "===== STEP 4/4: match (over Spark Connect) ====="
  run match
  echo "===== workflow complete -- results under $OUTPUT_DIR ====="
}

case "${1:-}" in
  start)    start ;;
  run)      run "${2:-}" ;;
  pairs)    pairs "${2:-}" ;;
  fullloop) fullloop ;;
  stop)     stop ;;
  clean)    clean ;;
  label)    label ;;
  workflow) workflow ;;
  env)      env_show ;;
  *) echo "usage: $0 {start|stop|run [phase]|pairs [auto]|fullloop|label|clean|workflow|env}"; exit 1 ;;
esac
