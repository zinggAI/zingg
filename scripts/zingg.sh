#!/bin/bash
#ZINGG_HOME=./assembly/target
ZINGG_JARS=$ZINGG_HOME/zingg-0.3.3-SNAPSHOT.jar
EMAIL=xxx@yyy.com
LICENSE="test"

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

$SPARK_HOME/bin/spark-submit --master $SPARK_MASTER $OPTION_JARS $OPTION_SPARK_CONF --conf spark.serializer=org.apache.spark.serializer.KryoSerializer --conf spark.default.parallelism="8" --conf spark.executor.extraJavaOptions="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -Xloggc:/tmp/memLog.txt -XX:+UseCompressedOops" --conf spark.executor.memory=10g --conf spark.debug.maxToStringFields=200 --driver-class-path $ZINGG_JARS --class zingg.client.Client $ZINGG_JARS $@ --email $EMAIL --license $LICENSE 
