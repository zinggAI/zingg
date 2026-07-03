---
description: >-
  Step-by-step instructions for setting up the Zingg development environment on
  Ubuntu and WSL2. Install Java 11, Spark 3.5, Maven, then build Zingg from
  source and run the full match workflow.
---

# Ubuntu/WSL2 Setup Guide

The following steps will help you set up the Zingg Development Environment on **Ubuntu/WSL2**.

### **Step 0: Initial OS Setup (Ubuntu/WSL2)** <a href="#step-0-initial-os-setup-ubuntu-wsl2" id="step-0-initial-os-setup-ubuntu-wsl2"></a>

Make sure to update your Ubuntu installation:

`sudo apt update`

#### **Install Ubuntu on WSL2 on Windows**

1. Install **wsl**: Type the following command in **Windows PowerShell**.

```bash
wsl --install
```

2. Download Ubuntu from **Microsoft Store**, **Ubuntu 20.04 LTS**
3. Configure Ubuntu with a **username** and **password**
4. Open **Ubuntu 20.04 LTS** and start working

```bash
sudo apt update
```

{% hint style="success" icon="right-long" %}
Follow this [tutorial](https://ubuntu.com/tutorials/install-ubuntu-on-wsl2-on-windows-10#1-overview) for more information.
{% endhint %}

### **Step 1: Clone The Zingg Repository (Ubuntu)**

1. Install and SetUp Git: **sudo apt install git**
2. Verify : **git --version**
3. Set up Git by following the [tutorial](https://www.digitalocean.com/community/tutorials/how-to-install-git-on-ubuntu-20-04).
4. Clone the Zingg Repository: `git clone https://github.com/zinggAI/zingg.git`

{% hint style="success" icon="right-long" %}
It is suggested to fork the repository to your account and then clone the repository.
{% endhint %}

### **Step 2: Install JDK 11 (Ubuntu)**

Follow this [tutorial](https://linuxize.com/post/install-java-on-ubuntu-20-04/) to install Java 11 JDK 11 in Ubuntu.

For example:

```bash
sudo apt install openjdk - 11 - jdk openjdk - 11 - jre javac - version java -
    version
```

### **Step 3: Install Apache Spark**

#### **Common Steps**

Download Apache Spark - from the [Apache Spark Official Website](https://spark.apache.org/downloads.html).

* For example, 3.5.0:

```bash
curl -
    O https
    :  // archive.apache.org/dist/spark/spark-3.5.0/spark-3.5.0-bin-hadoop3.tgz
       tar -
       xvf spark - 3.5.0 - bin - hadoop3.tgz sudo mv spark - 3.5.0 - bin -
       hadoop3 / opt / spark rm spark - 3.5.0 - bin - hadoop3.tgz
```

#### **Original Ubuntu Instructions (Manual Wget)**

Install the downloaded Apache Spark - on your Ubuntu by following [this tutorial](https://computingforgeeks.com/how-to-install-apache-spark-on-ubuntu-debian/).

* For example, 3.5.0:

```bash
wget https
    :  // www.apache.org/dyn/closer.lua/spark/spark-3.5.0/spark-3.5.0-bin-hadoop3.tgz
       tar -
       xvf spark - 3.5.0 - bin - hadoop3.tgz rm - rf spark - 3.5.0 - bin -
       hadoop3.tgz sudo mv spark - 3.5.0 - bin - hadoop3 / opt / spark
```

Make sure that Spark version you have installed is compatible with Java you have installed, and Zingg is supporting those versions.

{% hint style="success" icon="right-long" %}
Zingg supports Spark 3.5 and the corresponding Java version.
{% endhint %}

### **Step 4: Install Apache Maven (Ubuntu)**

Install the latest **maven** package.

* For example, 3.8.8:

```bash
wget https
    :  // dlcdn.apache.org/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.tar.gz
       tar -
       xvf apache - maven - 3.8.8 - bin.tar.gz rm - rf apache - maven - 3.8.8 -
       bin.tar.gz cd apache - maven - 3.8.8 / cd bin./ mvn-- version
```

{% hint style="success" icon="right-long" %}
Make sure that mvn -version should display correct java version as well(JAVA 11)

Apache Maven 3.8.7

* Maven home: `/usr/share/maven`
* Java version: 11.0.23
* Vendor: Ubuntu, runtime: /usr/lib/jvm/java-11-openjdk-amd64
{% endhint %}

### **Step 5: Update Environment Variables (Ubuntu - \~/.bashrc)**

1. Open `.bashrc` and add env variables at the end of the file.

```bash
vim ~ /.bashrc export SPARK_HOME =
    / opt / spark export SPARK_MASTER = local[*] export MAVEN_HOME =
        / home / ubuntu / apache - maven - 3.8.8 export ZINGG_HOME =
            <path_to_zingg> / assembly / target export JAVA_HOME =
                / usr / lib / jvm / java - 11 - openjdk - amd64 export PATH =
                    $PATH : $SPARK_HOME / bin : $SPARK_HOME /
                                                sbin : $JAVA_HOME / bin
```

`<path_to_zingg>` will be a directory where you clone the repository of the Zingg. Similarly, if you have installed spark on a different directory you can set **SPARK\_HOME** accordingly.

{% hint style="success" icon="right-long" %}
Skip exporting `MAVEN_HOME` if multiple maven version are not required.
{% endhint %}

2. Save/exit and do source .bashrc so that they reflect.

```bash
source ~/.bashrc
```

3. Verify:

```bash
echo $PATH mvn-- version
```

{% hint style="success" icon="right-long" %}
If you have already set up **JAVA\_HOME** and **SPARK\_HOME** in the steps before you don't need to do this again.
{% endhint %}

### **Step 6: Compile The Zingg Repository**

1. Make sure you are executing the following commands in the same terminal window where you saved the bashrc. Run the following to compile the Zingg Repository.

```bash
git branch
```

2. Run the following to Compile the Zingg Repository

```bash
mvn initialize mvn clean compile package - Dspark = sparkVer
```

3. Run the following to Compile while skipping tests.

```bash
mvn initialize mvn clean compile package - Dspark =
    sparkVer - Dmaven.test.skip = true
```

{% hint style="success" icon="right-long" %}
Replace the `sparkVer` with the version of Spark you installed. For example, **-Dspark=3.5** you still face an error, include **-Dmaven.test.skip=true** with the above command.
{% endhint %}

### **Step 7: If you have any issue with 'SPARK\_LOCAL\_IP' (Ubuntu)**

1. Install **net-tools** using **sudo apt-get install -y net-tools**
2. Run `ifconfig` in the terminal, find the **IP address** and paste the same in **/opt/hosts** IP address of your Pc-Name

### **Step 8: Run Zingg To Find Training Data**

Run this script in the terminal opened in Zingg clones directory `./scripts/zingg.sh --phase findTrainingData --conf examples/febrl/config.json`

_If everything is right, it should show Zingg banner._

### **Step 9: Run Zingg To label Data**

Run this script in the terminal opened in Zingg clones directory `./scripts/zingg.sh --phase label --conf examples/febrl/config.json --properties-file config/zingg.conf`

### **Step 10: Run Zingg To train model based on labeling**

Run this script in the terminal opened in Zingg clones directory `./scripts/zingg.sh --phase train --conf examples/febrl/config.json --properties-file config/zingg.conf`

### **Step 11: Run Zingg To prepare final output data**

Run this script in the terminal opened in Zingg clones directory `./scripts/zingg.sh --phase match --conf examples/febrl/config.json --properties-file config/zingg.conf`

Change the directory `cd /tmp/zinggOutput` (path provided in the configconfig file) to see the output files.
