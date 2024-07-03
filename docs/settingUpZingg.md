# Setting Zingg Development Environment

The following steps will help you set up the Zingg Development Environment. While the steps remain the same across different OS, we have provided detailed instructions for Ubuntu OS. Below examples have been created using Ubuntu 22.04.2 LTS

Make sure to update your ubutu installation

sudo apt update

****

_**Step 0 :  Install Ubuntu on WSL2 on Windows**_

* Install wsl: Type the following command in **Windows PowerShell**.
```
wsl --install
```
* Download Ubuntu from **Microsoft Store**, **Ubuntu 20.04 LTS**
* Configure Ubuntu with a **username** and **password**
* Open **Ubuntu 20.04 LTS** and start working
```
sudo apt update
```
* Follow this [tutorial](https://ubuntu.com/tutorials/install-ubuntu-on-wsl2-on-windows-10#1-overview) for more information.

****

_**Step 1 :  Clone the Zingg Repository**_

* Install and SetUp Git: **sudo apt install git**
* Verify : **git --version**
* Set up Git by following the [tutorial](https://www.digitalocean.com/community/tutorials/how-to-install-git-on-ubuntu-20-04).
* Clone the Zingg Repository: **git clone https://github.com/zinggAI/zingg.git**

_**Note :-**_ It is suggested to fork the repository to your account and then clone the repository.

****

_**Step 2 :  Install JDK 1.8 (Java Development Kit)**_

* Follow this [tutorial](https://linuxize.com/post/install-java-on-ubuntu-20-04/) to install Java8 JDK1.8 in Ubuntu.&#x20;

* For example:
```
sudo apt install openjdk-8-jdk openjdk-8-jre
javac -version
java -version
```

****

_**Step 3 :  Install Apache Spark -**_

* Download Apache Spark - from the [Apache Spark Official Website](https://spark.apache.org/downloads.html).
* Install downloaded Apache Spark - on your Ubuntu by following [this tutorial](https://computingforgeeks.com/how-to-install-apache-spark-on-ubuntu-debian/).

* For example for 3.5.0:
```
wget https://www.apache.org/dyn/closer.lua/spark/spark-3.5.0/spark-3.5.0-bin-hadoop3.tgz
tar -xvf spark-3.5.0-bin-hadoop3.tgz
rm -rf spark-3.5.0-bin-hadoop3.tgz
sudo mv spark-3.5.0-bin-hadoop3 /opt/spark
```

Make sure that spark version you have installed is compatible with java you have installed, and Zingg is supporting those versions.

_**Note :-**_ Zingg supports Spark 3.5 and the corresponding Java version.

****

_**Step 4 :  Install Apache Maven**_

* Install the latest maven package.

* For example for 3.8.8:
```
wget https://dlcdn.apache.org/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.tar.gz
tar -xvf apache-maven-3.8.8-bin.tar.gz 
rm -rf apache-maven-3.8.8-bin.tar.gz 
cd apache-maven-3.8.8/
cd bin
./mvn --version
```

****

_**Step 5 :  Update Env Variables**_

Open .bashrc and add env variables at end of file
```
vim ~/.bashrc

export SPARK_HOME=/opt/spark
export SPARK_MASTER=local[\*]
export MAVEN_HOME=/home/ubuntu/apache-maven-3.8.8
export ZINGG_HOME=/home/administrator/Zingg/zingg-Nitish/assembly/target
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin:$JAVA_HOME/bin

Skip adding export MAVEN_HOME=/home/ubuntu/apache-maven-3.8.8, if multiple maven version are not required 

Save/exit and do source .bashrc so that they reflect

source ~/.bashrc

Verify:
echo $PATH
mvn --version
```

where \<path\_to\_zingg> will be a directory where you clone the repository of the Zingg. Similarly, if you have installed spark on a different directory you can set **SPARK\_HOME** accordingly.

_**Note :-**_  If you have already set up **JAVA\_HOME** and **SPARK\_HOME** in the steps before you don't need to do this again.

****

_**Step 6 :  Compile the Zingg Repository**_

* Run the following to Compile the Zingg Repository -
```
git branch
(Ensure you are on main branch)
mvn initialize
* Run the following to Compile the Zingg Repository - **mvn initialize** and
* **mvn clean compile package -Dspark=sparkVer**
```

_**Note :-**_	Replace the **sparkVer** with the version of spark you installed, For example, **-Dspark=3.5** and if still facing error, include **-Dmaven.test.skip=true** with the above command.


_**Note :-**_ substitute 3.3 with profile of the spark version you have installed. This is based on profiles specified in pom.xml
****

_**Step 7 :  If had any issue with 'SPARK\_LOCAL\_IP'**_

* Install **net-tools** using **sudo apt-get install -y net-tools**
* Run command in the terminal **ifconfig**, find the **IP address** and paste the same in **/opt/hosts** IP address of your Pc-Name

****

_**Step 8 :  Run Zingg to Find Training Data**_

* Run this Script in terminal opened in zingg clones directory - **./scripts/zingg.sh --phase findTrainingData --conf examples/febrl/config.json**

****

**If everything is right, it should show Zingg Icon.**
