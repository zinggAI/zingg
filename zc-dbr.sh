#!/usr/bin/env bash
# Run the Zingg Spark Connect client against a Databricks cluster.
#
# The cluster is the Connect server here, so there is no start/stop: the
# plugins must already be installed on it (see "Cluster setup" below). This
# script only drives the same example clients that zc.sh runs locally.
#
# Usage:
#   export DATABRICKS_PROFILE=zingg-dbr          # from ~/.databrickscfg
#   # ...or set DATABRICKS_HOST / _TOKEN / _CLUSTER_ID explicitly
#
# The dataset is described entirely by a Zingg conf JSON -- schema,
# fieldDefinition, input/output pipes, zinggDir and modelId. Defaults to
# spark-connect/python/examples/databricks.json; override with CONF=<file>.
# Every path in it resolves ON THE CLUSTER, so use /Volumes or DBFS.
#   ./zc-dbr.sh env
#   ./zc-dbr.sh run findTrainingData
#   ./zc-dbr.sh label auto
#
# Cluster setup (once), on Classic compute in Dedicated access mode:
#   1. Install as cluster libraries:
#        zingg-0.7.0.jar
#        zingg-spark-connect-proto-0.7.0.jar
#        zingg-spark-connect-server-plugin-0.7.0-dbr.jar   <-- the shaded one
#        protobuf-java-3.23.4.jar
#      Build them with:  mvn -Dspark=dbr clean install -DskipTests
#   2. Cluster Spark config:
#        spark.connect.extensions.command.classes zingg.spark.connect.server.ZinggCommandPlugin
#        spark.connect.extensions.relation.classes zingg.spark.connect.server.ZinggRelationPlugin
#   3. Restart the cluster.
#
# Every path below is resolved ON THE CLUSTER, not locally. Use UC Volumes or
# DBFS; a local /tmp path will silently write to the driver's disk.

set -euo pipefail

WORKTREE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
EXAMPLES="$WORKTREE/spark-connect/python/examples"
# pyspark 3.5.x matches DBR's Spark 3.5.0; no databricks-connect needed.
VENV_PY="${VENV_PY:-$HOME/Desktop/zingg-dbr-jars/.venv/bin/python}"

# Auth: a Databricks CLI profile (recommended -- works with OAuth, which has no
# PAT to embed in a URL) or an explicit host/token/cluster for PAT auth.
PROFILE="${DATABRICKS_PROFILE:-}"
if [ -n "$PROFILE" ]; then
  CONN=(--profile "$PROFILE")
  DESC="profile $PROFILE"
else
  HOST="${DATABRICKS_HOST:?set DATABRICKS_PROFILE, or DATABRICKS_HOST/_TOKEN/_CLUSTER_ID}"
  TOKEN="${DATABRICKS_TOKEN:?set DATABRICKS_TOKEN}"
  CLUSTER="${DATABRICKS_CLUSTER_ID:?set DATABRICKS_CLUSTER_ID}"
  HOST="${HOST#https://}"; HOST="${HOST#http://}"; HOST="${HOST%/}"
  CONN=(--remote "sc://${HOST}:443/;token=${TOKEN};x-databricks-cluster-id=${CLUSTER}")
  DESC="$HOST (cluster $CLUSTER, PAT)"
fi

CONF="${CONF:-$WORKTREE/spark-connect/python/examples/databricks.json}"
[ -f "$CONF" ] || { echo "no such conf file: $CONF" >&2; exit 1; }

ARGS=("${CONN[@]}" --conf "$CONF")

preflight() {
  [ -x "$VENV_PY" ] || { echo "MISSING python: $VENV_PY" >&2; exit 1; }
  "$VENV_PY" -c 'import zingg_connect' 2>/dev/null || {
    echo "zingg_connect not importable by $VENV_PY" >&2
    echo "  pip install --no-deps -e $WORKTREE/spark-connect/python" >&2; exit 1; }
}

case "${1:-}" in
  env)
    echo "connection $DESC"
    echo "python     $VENV_PY"
    echo "conf       $CONF"
    "$VENV_PY" - "$CONF" <<'EOF'
import json, sys
d = json.load(open(sys.argv[1]))
print("  zinggDir %s (model %s)" % (d.get("zinggDir"), d.get("modelId")))
print("  data     %s" % d["data"][0]["props"]["path"])
print("  output   %s" % d["output"][0]["props"]["path"])
print("  fields   %d" % len(d.get("fieldDefinition", [])))
EOF
    ;;
  run)   preflight; "$VENV_PY" "$EXAMPLES/run_over_connect.py" "${2:-findTrainingData}" "${ARGS[@]}" ;;
  label)
    preflight
    a=("${ARGS[@]}"); [ "${2:-}" = "auto" ] && a+=(--auto)
    "$VENV_PY" "$EXAMPLES/label_over_connect.py" "${a[@]}"
    ;;
  *) echo "usage: $0 {env|run [phase]|label [auto]}"; exit 1 ;;
esac
