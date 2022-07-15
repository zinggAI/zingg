#!/bin/bash
#ZINGG_HOME=./assembly/target
ZINGG_JARS=$ZINGG_HOME/zingg-0.3.4-SNAPSHOT.jar
EMAIL=zingg@zingg.ai
LICENSE=zinggLicense.txt

# All the additional options must be added here
$SPARK_HOME/bin/spark-submit --master $SPARK_MASTER ${ALL_OPTIONS} --conf spark.executor.extraJavaOptions="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -Xloggc:/tmp/memLog.txt -XX:+UseCompressedOops" --driver-class-path $ZINGG_JARS --class zingg.client.Client $ZINGG_JARS $@ --email $EMAIL --license $LICENSE 
