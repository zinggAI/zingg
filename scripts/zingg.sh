#!/bin/bash
#ZINGG_HOME=./assembly/target
ZINGG_JARS=$ZINGG_HOME/zingg-0.3.3-SNAPSHOT.jar
EMAIL=xxx@yyy.com
LICENSE="test"

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

if [[ -z "${SPARK_EXECUTOR_MEMORY}" ]]; then
  SPARK_EXECUTOR_MEMORY=8g
fi  
OPTION_EXECUTOR_MEMORY="--conf spark.executor.memory=${SPARK_EXECUTOR_MEMORY}"

if [[ -z "${SPARK_DRIVER_MEMORY}" ]]; then
  SPARK_DRIVER_MEMORY=8g
fi
OPTION_DRIVER_MEMORY="--conf spark.driver.memory=${SPARK_DRIVER_MEMORY}"

# All the additional options must be added here
ALL_OPTIONS=" ${OPTION_DRIVER_MEMORY} ${OPTION_EXECUTOR_MEMORY} ${OPTION_JARS} ${OPTION_SPARK_CONF} "
$SPARK_HOME/bin/spark-submit --master $SPARK_MASTER ${ALL_OPTIONS} --conf spark.serializer=org.apache.spark.serializer.KryoSerializer --conf spark.default.parallelism="8" --conf spark.executor.extraJavaOptions="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -Xloggc:/tmp/memLog.txt -XX:+UseCompressedOops" --conf spark.debug.maxToStringFields=200 --driver-class-path $ZINGG_JARS --class zingg.client.Client $ZINGG_JARS $@ --email $EMAIL --license $LICENSE 
