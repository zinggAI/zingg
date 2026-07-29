#!/usr/bin/env bash
# Helper for running the Zingg Spark Connect end-to-end loop locally.
#
# Usage:
#   ./zc.sh start          # start the Spark Connect server (with Zingg plugin)
#   ./zc.sh stop           # stop the server
#   ./zc.sh run [phase]    # run_over_connect.py for a phase (default findTrainingData)
#   ./zc.sh pairs [auto]   # label_over_connect.py: fetch pairs, mark, write back (over Connect)
#   ./zc.sh label          # SEED pre-labeled data (stand-in; see note below)
#   ./zc.sh clean          # remove previous model/output dirs
#   ./zc.sh fullloop       # full pipeline with REAL labelling over Connect: ftd -> label(auto) -> train -> match
#   ./zc.sh workflow       # full pipeline with SEEDED labels: ftd -> label(seed) -> train -> match
#
# NOTE on 'label' vs 'pairs': real labelling over Spark Connect now works via
# the ZinggRelationPlugin (see `pairs`/label_over_connect.py -- fetch unmarked
# pairs, mark, write back) and is exercised end to end by `fullloop`. The
# `label` step here is a separate, faster stand-in: it just copies pre-labeled
# ("marked") Febrl training data shipped in the repo into the model dir, in
# place of a real labelling session -- `workflow` uses this shortcut.

set -euo pipefail

SPARK=/Users/zingg/spark
WORKTREE=/Users/zingg/Desktop/zingg-spark-connect
VENV_PY=~/zingg-connect-venv/bin/python
M2=$HOME/.m2/repository

MODEL_DIR=/tmp/zinggConnectModels
MODEL_ID=100
MARKED_SRC="$WORKTREE/test/testFebrl/$MODEL_ID/trainingData/marked"
MARKED_DST="$MODEL_DIR/$MODEL_ID/trainingData/marked"

JARS="$WORKTREE/assembly/target/zingg-0.7.0.jar"
JARS="$JARS,$M2/zingg/zingg-spark-connect-proto/0.7.0/zingg-spark-connect-proto-0.7.0.jar"
JARS="$JARS,$M2/zingg/zingg-spark-connect-server-plugin/0.7.0/zingg-spark-connect-server-plugin-0.7.0.jar"
JARS="$JARS,$M2/com/google/protobuf/protobuf-java/3.23.4/protobuf-java-3.23.4.jar"

start() {
  SPARK_LOCAL_IP=127.0.0.1 "$SPARK/sbin/start-connect-server.sh" \
    --packages org.apache.spark:spark-connect_2.12:3.5.1 \
    --jars "$JARS" \
    --conf spark.driver.bindAddress=127.0.0.1 \
    --conf spark.connect.extensions.command.classes=zingg.spark.connect.server.ZinggCommandPlugin \
    --conf spark.connect.extensions.relation.classes=zingg.spark.connect.server.ZinggRelationPlugin
}

wait_up() {
  echo "waiting for server on 15002..."
  for _ in $(seq 1 30); do
    if lsof -tiTCP:15002 -sTCP:LISTEN >/dev/null 2>&1; then echo "server UP"; return 0; fi
    sleep 2
  done
  echo "server did NOT come up in time" >&2; return 1
}

EXAMPLES="$WORKTREE/spark-connect/python/examples"
DATA="$WORKTREE/examples/febrl/test.csv"
CONN_ARGS=(--remote sc://localhost:15002 --data "$DATA" --zingg-dir "$MODEL_DIR" --model-id "$MODEL_ID")

run()   { "$VENV_PY" "$EXAMPLES/run_over_connect.py" "${1:-findTrainingData}" "${CONN_ARGS[@]}"; }
pairs() {
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
  echo "===== full label loop complete ====="
}
stop()  { "$SPARK/sbin/stop-connect-server.sh"; }
clean() { rm -rf "$MODEL_DIR" /tmp/zinggConnectOutput; echo "cleaned"; }

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
  echo "===== workflow complete -- results under /tmp/zinggConnectOutput ====="
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
  *) echo "usage: $0 {start|stop|run [phase]|pairs [auto]|fullloop|label|clean|workflow}"; exit 1 ;;
esac
