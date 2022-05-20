# Setting Up Zingg Development Environment
The following steps will help you setup the Zingg Development Environment. While the steps remain the same across different OS, we have provided detailed instructions for Ubuntu OS. 

**Step 1: Clone the Zingg Repository**
* Install and SetUp Git: **sudo apt install git**
* SetUp Git by following the [tutorial](https://www.digitalocean.com/community/tutorials/how-to-install-git-on-ubuntu-20-04)
* Clone the Zingg Repository: **git clone https://github.com/zinggAI/zingg.git**

**Step 2: Install JDK 1.8 (Java Development Kit)**
* Follow this [tutorial](https://linuxize.com/post/install-java-on-ubuntu-20-04/) to install Java8 JDK1.8 in Ubuntu.
**Step 3: Install Apache Spark - version spark-3.1.2-bin-hadoop3.2**
* Download Apache Spark - version spark-3.1.2-bin-hadoop3.2 from the [Apache Spark Official Website](https://spark.apache.org/downloads.html)
* Install Downloaded Apache Spark - version spark-3.1.2-bin-hadoop3.2 on your Ubuntu by following [this tutorial](https://computingforgeeks.com/how-to-install-apache-spark-on-ubuntu-debian/)

**Step 4: Install Apache Maven 3.3.9**
* Download Apache Maven 3.3.9 ‘.tar.gz’ file from the [Apache Maven Official Website](https://maven.apache.org/download.cgi)
* Install the downloaded Apache Maven file using [this tutorial](https://www.linuxhelp.com/how-to-install-apache-maven-3-3-9-on-linux-mint-18-3)

**Step 5: Set JAVA_HOME to JDK base directory**
* Go to **cd /etc** directory in your Ubuntu system, and open the **‘profile’ file** using gedit. Just run **sudo gedit profile**
* Paste these in the **‘profile’ file** 
```
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export SPARK_HOME=~/spark-3.1.2-bin-hadoop3.2
export SPARK_MASTER=local[\*]
export ZINGG_HOME=<path_to_zingg>/assembly/target
```
where <path_to_zingg> will be directory where you clone the repository of the Zingg. Similarly, if you have installed spark on different directory you can set **SPARK_HOME** accordingly.


**Note:-** If you have already setup **JAVA_HOME** and **SPARK_HOME** in the steps before you don't need to do this again.

**Step 6: Compile the Zingg Repository**
* Run the following to Compile the Zingg Repository - **mvn clean compile package -Dspark=sparkVer**

**Step 7: If had any issue with 'SPARK_LOCAL_IP'**
* Install **net-tools** using **sudo apt-get install -y net-tools**
* Run command in the terminal **ifconfig**, and find the **IP address** and paste the same in **/opt/hosts** IP address of your Pc-Name

**Step 8: Run Zingg to Find Training Data**
* Run this Script in terminal opened in zingg clones directory - **./scripts/zingg.sh --phase findTrainingData --conf examples/febrl/config.json**

**If everythings right, it should show Zingg Icon.**

