#!/bin/bash

# Debug launcher for the Spark-based Zingg client.
# Attaches a JDWP debugger on port 5005 (suspend=y by default — change to
# suspend=n to let the process start without waiting for the debugger).
#
# Prerequisites: ZINGG_HOME and SPARK_HOME must be set (or export them below).
# The assembled JAR is expected at $ZINGG_HOME/zingg-0.7.0.jar.
# Override JAR_PATH to point at a different build output.

# ---------- paths ----------------------------------------------------------

JAR_PATH="${JAR_PATH:-$ZINGG_HOME/zingg-0.7.0.jar}"
ZINGG_JARS="$JAR_PATH:$ZINGG_HOME/thirdParty/lib/secondstring.jar:$ZINGG_HOME/thirdParty/lib/py4j0.10.9.jar"

EMAIL=zingg@zingg.ai
LICENSE=zinggLicense.txt

log4j_setting="-Dlog4j2.configurationFile=file:log4j2.properties"

# JDWP debug port — suspend=y means spark-submit blocks until the debugger connects
DEBUG_PORT="${DEBUG_PORT:-5005}"
DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:${DEBUG_PORT}"

# ---------- arg parsing (same contract as zingg.sh) ------------------------

export SESSION_TYPE="PY4J"
POSITIONAL_ARGS=()

while [[ $# -gt 0 ]]; do
    case $1 in
        --properties-file)
            PROPERTIES_FILE="$2"
            PROPERTIES="--properties-file $PROPERTIES_FILE"
            shift; shift
            ;;
        --run)
            export SESSION_TYPE="CLUSTER"
            RUN_PYTHON_PHASE=1
            PYTHON_SCRIPT="$2"
            shift; shift
            ;;
        --run-databricks)
            RUN_PYTHON_DB_CONNECT_PHASE=1
            PYTHON_SCRIPT_DB_CONNECT="$2"
            shift; shift
            ;;
        --log)
            LOG_FILE=$2
            LOGGING="--files $LOG_FILE"
            shift; shift
            ;;
        --debug-port)
            DEBUG_PORT="$2"
            DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:${DEBUG_PORT}"
            shift; shift
            ;;
        --no-suspend)
            DEBUG_OPTS="${DEBUG_OPTS/suspend=y/suspend=n}"
            shift
            ;;
        *)
            POSITIONAL_ARGS+=("$1")
            shift
            ;;
    esac
done

set -- "${POSITIONAL_ARGS[@]}"

# ---------- executable selection -------------------------------------------

if [[ $RUN_PYTHON_PHASE -eq 1 ]]; then
    EXECUTABLE="$PYTHON_SCRIPT"
elif [[ $RUN_PYTHON_DB_CONNECT_PHASE -eq 1 ]]; then
    EXECUTABLE="$PYTHON_SCRIPT_DB_CONNECT"
else
    EXECUTABLE="--class zingg.spark.client.SparkClient $JAR_PATH"
fi

# ---------- launch ---------------------------------------------------------

echo "=== Zingg debug launcher ==="
echo "JAR      : $JAR_PATH"
echo "SPARK    : $SPARK_HOME"
echo "Debug    : $DEBUG_OPTS"
echo "Waiting for debugger on port ${DEBUG_PORT} ..."
echo ""

if [[ $RUN_PYTHON_DB_CONNECT_PHASE -eq 1 ]]; then
    unset SPARK_MASTER
    unset SPARK_HOME
    export DATABRICKS_CONNECT=Y
    python $EXECUTABLE
else
    $SPARK_HOME/bin/spark-submit \
        --master $SPARK_MASTER \
        $PROPERTIES \
        --files "./log4j2.properties" \
        --conf "spark.executor.extraJavaOptions=$log4j_setting -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -Xloggc:/tmp/memLog.txt -XX:+UseCompressedOops" \
        --conf "spark.driver.extraJavaOptions=$log4j_setting $DEBUG_OPTS" \
        $LOGGING \
        --driver-class-path "$ZINGG_JARS" \
        $EXECUTABLE "$@" \
        --email $EMAIL --license $LICENSE
fi
