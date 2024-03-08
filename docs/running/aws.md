---
title: Running on AWS
parent: Running Zingg on Cloud
nav_order: 5
---

# Running on AWS

One option is to use the spark-submit option with the Zingg config and phase. Please note that the config.json should be available locally at the driver for Zingg to use it.&#x20;

`````
aws emr create-cluster --name "Add Spark Step Cluster" --release-label emr-6.2.0 --applications Name=Zingg \
--ec2-attributes KeyName=myKey --instance-type <instance type> --instance-count <num instances> \
--steps Type=Spark,Name="Zingg",ActionOnFailure=CONTINUE,Args=[--class,zingg.spark.client.SparkClient,<s3 location of zingg.jar>,--phase,<name of phase - findTrainingData,match etc>,--conf,<local location of config.json>] --use-default-roles````
`````

A step by step s provided at the following [link](https://blog.infostrux.com/identity-resolution-with-zingg-ai-snowflake-and-aws-emr-for-the-canadian-football-league-22cf0850ab53). The guide mentions training locally using Zingg docker, but the findTrainingData and label phases can be executed on EMR directly.&#x20;



A second option is to run Zingg Python code in [AWS EMR Notebooks](https://aws.amazon.com/emr/features/notebooks/)
