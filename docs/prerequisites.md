---
description: Instructions on how to install prerequisite software needed to compile/run Zingg
---

# Step-By-Step Guide

## Step 1: Operating System
Ubuntu or any other similar linux system
Below steps have been created using Ubuntu 22.04.2 LTS

Update your ubutu installation

sudo apt update


## Step 2: Install java
sudo apt install openjdk-8-jdk openjdk-8-jre
javac -version
java -version

## Step 3: Install git
sudo apt install git
git --version

## Step 4: Install spark
wget https://dlcdn.apache.org/spark/spark-3.3.2/spark-3.3.2-bin-hadoop3.tgz
tar -xvf spark-3.3.2-bin-hadoop3.tgz
rm -rf spark-3.3.2-bin-hadoop3.tgz
sudo mv spark-3.3.2-bin-hadoop3 /opt/spark

## Step 5: Install mvn
wget https://dlcdn.apache.org/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.tar.gz
tar -xvf apache-maven-3.8.8-bin.tar.gz 
rm -rf apache-maven-3.8.8-bin.tar.gz 
cd apache-maven-3.8.8/
cd bin
./mvn --version

## Step 5: Clone zingg git repo
git clone https://github.com/zinggAI/zingg.git

## Step 6: Update env variables
Open .bashrc and env variables at end of file

vim ~/.bashrc

export SPARK_HOME=/opt/spark
export SPARK_MASTER=local[\*]
export MAVEN_HOME=/home/ubuntu/apache-maven-3.8.8
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin:$MAVEN_HOME/bin
export ZINGG_HOME=/home/ubuntu/zingg/assembly/target
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

Save/exit do source .bashrc so that they reflect

source ~/.bashrc

Verify:
echo $PATH
mvn --version

## Step 7: Compile Zingg
git branch
(Ensure you are on main branch)
mvn initialize

Now switch to latest dev branch e.g. 0.4.0
git checkout 0.4.0
mvn initialize clean compile package -Dspark=3.3 -Dmaven.test.skip=true
mvn clean compile package -Dspark=3.3 -Dmaven.test.skip=true