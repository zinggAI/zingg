---
layout: default
title: Installation
parent: Step By Step Guide
nav_order: 3
---
## Running from Docker image from Docker hub
The easiest way to get started is pulling the Docker image with the last release of Zingg.
```
docker pull zingg/zingg:0.3.2
docker run -it zingg/zingg:0.3.2 bash
```

## Installation

Zingg runs on [Spark](https://spark.apache.org) and can be used on all major Spark distributions. Zingg can run on all major Linux flavours.

Zingg is prebuilt for common Spark versions so you can use those directly. The following document assumes that we are installing Zingg 0.3 on Spark 3.1.2, but you can follow the same process for other versions too

- TOC
{:toc}

### Prerequisites 
A) Java JDK - version "1.8.0_131" 

B) Apache Spark - version spark-3.1.2-bin-hadoop3.2

#### Prerequisites for running Zingg on single machine without setting up a Spark cluster
(Good for a few million records)
A) Install the specified JDK version

B) Apache Spark - Download the specified version from spark.apache.org and unzip it at a folder under home 

Please add the following entries to ~/.bash_aliases 

>export JAVA_HOME=path to jdk

>export SPARK_HOME=path to location of Apache Spark

>export SPARK_MASTER=local[*]

C) Correct entry of host under /etc/hosts 

Run ifconfig to find the ip of the machine and make sure it is added to the /etc/hosts for localhost 

#### Prerequisites for running Zingg on a Spark cluster
If you have a ready Spark cluster, you can run Zingg by configuring the following environment on your driver machine
>export JAVA_HOME=path to jdk

>export SPARK_HOME=path to Apache Spark

>export SPARK_MASTER=spark://master-host:master-port


### Installation Steps 

Download the tar zingg-version.tar.gz to a folder of your choice and run the following 

>gzip -d zingg-0.3.2-SNAPSHOT-bin.tar.gz ; tar xvf zingg-0.3.2-SNAPSHOT-bin.tar 

This will create a folder zingg-0.3.1-SNAPSHOT under the chosen folder. 
 
Move the above folder to zingg. 

>mv zingg-0.3.2-SNAPSHOT-bin ~/zingg 

>export ZINGG_HOME=path to zingg

>export PATH=$PATH:$JAVA_HOME/bin:$SPARK_HOME/bin:$SPARK_HOME/sbin:ZINGG_HOME/scripts 
 
Run bash and print the aliases to ensure that they are set correctly. 

>bash 

>echo $SPARK_HOME 

>echo $JAVA_HOME 

>java --version 

>echo $ZINGG_HOME 

 
Let us now run a sample program to ensure that our installation is correct 

>cd zingg 

>./scripts/zingg.sh --phase match --conf examples/febrl/config.json  

The above will find duplicates in the examples/febl/test.csv file. You will see Zingg logs on the console and once the job finishes, you will see some files under /tmp/zinggOutput with matching records sharing the same cluster id.


Congratulations, Zingg has been installed! 

## Compiling from sources
If you need to compile the latest code or build for a different Spark version, you can clone this repo and 

- Install maven (We are on version 3.3.9)
- Install JDK 1.8
- Set JAVA_HOME to JDK base directory
- Run the following

`mvn clean compile package -Dspark=sparkVer` 

where sparkVer is one of 2.4, 3.0 or 3.1

