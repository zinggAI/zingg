#!/bin/bash
#ZINGG_HOME=./assembly/target
ZINGG_JARS=$ZINGG_HOME/zingg-0.3.3-SNAPSHOT.jar


# Set the ZINGG environment variables
ZINGG_ENV="$(dirname "$0")"/load-zingg-env.sh
if [[ -f "${ZINGG_ENV}" ]]; then
  source ${ZINGG_ENV}
fi

if [[ -z "${ZINGG_EXTRA_JARS}" ]]; then
  OPTION_JARS=""
else
  OPTION_JARS="--jars ${ZINGG_EXTRA_JARS}"
fi

if [[ -z "${ZINGG_EXTRA_SPARK_CONF}" ]]; then
  OPTION_SPARK_CONF=""
else
  OPTION_SPARK_CONF="${ZINGG_EXTRA_SPARK_CONF}"
fi

function read_zingg_conf() {
    local CONF_PROPS=""

    ZINGG_CONF_DIR="$(cd "`dirname "$0"`"/../config; pwd)"

    file="${ZINGG_CONF_DIR}/zingg-defaults.conf"
    # Leading blanks removed; comment Lines, blank lines removed
    PROPERTIES=$(sed 's/^[[:blank:]]*//;s/#.*//;/^[[:space:]]*$/d' $file)
 
    while IFS='=' read -r key value; do
      # Trim leading and trailing spaces
      key=$(echo $key | sed 's/^[[:blank:]]*//;s/[[:blank:]]*$//;')
      value=$(echo $value | sed 's/^[[:blank:]]*//;s/[[:blank:]]*$//;')
      # Append to conf variable
      CONF_PROPS+=" --conf ${key}=${value}"
    done <<< "$(echo -e "$PROPERTIES")"
 
    echo $CONF_PROPS
}

OPTION_SPARK_CONF+=$(read_zingg_conf)
# All the additional options must be added here
ALL_OPTIONS=" ${OPTION_JARS} ${OPTION_SPARK_CONF} "
$SPARK_HOME/bin/spark-submit --master $SPARK_MASTER ${ALL_OPTIONS}  --conf spark.executor.extraJavaOptions="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -Xloggc:/tmp/memLog.txt -XX:+UseCompressedOops" --driver-class-path $ZINGG_JARS --class zingg.client.Client $ZINGG_JARS $@ --email $EMAIL --license $LICENSE 
