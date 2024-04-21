#!/usr/bin/bash

 spark-3.5.1-bin-hadoop3/sbin/start-connect-server.sh \
   --package org.apache.spark:spark-connect_2.12:3.5.1 \
   --jars zing_jar_path
   --conf spark.connect.extensions.relation.classes=zingg.spark.connect.ZinggSparkConnectPlugin
