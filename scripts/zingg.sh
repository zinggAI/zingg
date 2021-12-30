#!/bin/bash
#ZINGG_HOME=./assembly/target
ZINGG_JARS=$ZINGG_HOME/zingg-0.3.1-SNAPSHOT.jar
EMAIL=xxx@yyy.com
LICENSE="test"
##for local
export SPARK_MEM=10g

$SPARK_HOME/bin/spark-submit --master $SPARK_MASTER --conf spark.serializer=org.apache.spark.serializer.KryoSerializer --conf spark.es.nodes="127.0.0.1" --conf spark.es.port="9200" --conf spark.es.resource="cluster/cluster1" --conf spark.default.parallelism="8" --conf spark.executor.extraJavaOptions="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -Xloggc:/tmp/memLog.txt -XX:+UseCompressedOops" --conf spark.executor.memory=10g --conf spark.debug.maxToStringFields=200 --driver-class-path $ZINGG_JARS --class zingg.client.Client $ZINGG_JARS $@ --email $EMAIL --license $LICENSE 
