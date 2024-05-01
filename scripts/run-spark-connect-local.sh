#!/usr/bin/bash

 spark-3.5.1-bin-hadoop3/sbin/start-connect-server.sh --wait \
   --verbose \
   --jars assembly/target/zingg-0.4.0.jar \
   --conf spark.connect.extensions.relation.classes=zingg.spark.connect.ZinggConnectPlugin \
   --packages org.apache.spark:spark-connect_2.12:3.5.1
