# Setting Zingg Development Environment

The following steps will help you set up the Zingg Development Environment. While the steps remain the same across different OS, we have provided detailed instructions for Ubuntu OS.

****

_**Step 1 :  Clone the Zingg Repository**_

* Install and SetUp Git: **sudo apt install git**
* Set up Git by following the [tutorial](https://www.digitalocean.com/community/tutorials/how-to-install-git-on-ubuntu-20-04).
* Clone the Zingg Repository: **git clone https://github.com/zinggAI/zingg.git**

****

_**Step 2**_** :  **_**Install JDK 1.8 (Java Development Kit)**_

* Follow this [tutorial](https://linuxize.com/post/install-java-on-ubuntu-20-04/) to install Java8 JDK1.8 in Ubuntu.&#x20;

****

_**Step 3 :  Install Apache Spark - version spark-3.1.2-bin-hadoop3.2**_

* Download Apache Spark - version spark-3.1.2-bin-hadoop3.2 from the [Apache Spark Official Website](https://spark.apache.org/downloads.html).
* Install downloaded Apache Spark - version spark-3.1.2-bin-hadoop3.2 on your Ubuntu by following [this tutorial](https://computingforgeeks.com/how-to-install-apache-spark-on-ubuntu-debian/).

****

_**Step 4 :  Install Apache Maven**_

* Install the latest maven package using the following Linux command:

```
sudo apt install maven
```

****

_**Step 5 :  Set JAVA\_HOME to JDK base directory**_

* Go to **cd /etc** directory in your Ubuntu system, and open the **‘profile’ file** using gedit. Just run **sudo gedit profile**
* Paste these in the **‘profile’ file.**

```
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export SPARK_HOME=~/spark-3.1.2-bin-hadoop3.2
export SPARK_MASTER=local[\*]
export ZINGG_HOME=<path_to_zingg>/assembly/target
```



where \<path\_to\_zingg> will be a directory where you clone the repository of the Zingg. Similarly, if you have installed spark on a different directory you can set **SPARK\_HOME** accordingly.

_**Note :-**_  If you have already set up **JAVA\_HOME** and **SPARK\_HOME** in the steps before you don't need to do this again.

****

_**Step 6 :  Compile the Zingg Repository**_

* Run the following to Compile the Zingg Repository - **mvn clean compile package -Dspark=sparkVer**

****

_**Step 7 :  If had any issue with 'SPARK\_LOCAL\_IP'**_

* Install **net-tools** using **sudo apt-get install -y net-tools**
* Run command in the terminal **ifconfig**, find the **IP address** and paste the same in **/opt/hosts** IP address of your Pc-Name

****

_**Step 8 :  Run Zingg to Find Training Data**_

* Run this Script in terminal opened in zingg clones directory - **./scripts/zingg.sh --phase findTrainingData --conf examples/febrl/config.json**

****

**If everything is right, it should show Zingg Icon.**
