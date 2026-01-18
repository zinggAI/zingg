# Setting Up Zingg Development Environment

The following steps will help you set up the Zingg Development Environment. While the core steps remain the same across different OS, we have provided detailed instructions for **macOS** and **Ubuntu/WSL2**.

### **Step 0: Initial OS Setup**

#### **For macOS**
Homebrew is required to install system dependencies.
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```
**Note for Apple Silicon:** If `brew` is not found, run:
`echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zshrc && source ~/.zshrc`

#### **For Ubuntu (WSL2 on Windows)**

Make sure to update your Ubuntu installation:

`sudo apt update`

**Step 0: Install Ubuntu on WSL2 on Windows**

* Install **wsl**: Type the following command in **Windows PowerShell**.

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

**Step 1: Clone The Zingg Repository**

#### **For macOS**
```bash
brew install git
```

#### **For Ubuntu**
* Install and SetUp Git: **sudo apt install git**
* Verify : **git --version**
* Set up Git by following the [tutorial](https://www.digitalocean.com/community/tutorials/how-to-install-git-on-ubuntu-20-04).
* Clone the Zingg Repository: **git clone https://github.com/zinggAI/zingg.git**

**Note:** It is suggested to fork the repository to your account and then clone the repository.

**Step 2: Install JDK 11 (Java Development Kit)**

#### **For macOS**
* Install OpenJDK 11: `brew install openjdk@11`
* Link Java 11:
  * **Apple Silicon:** `sudo ln -sfn /opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk`
  * **Intel:** `sudo ln -sfn /usr/local/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk`

#### **For Ubuntu**
* Follow this [tutorial](https://linuxize.com/post/install-java-on-ubuntu-20-04/) to install Java 11 JDK 11 in Ubuntu.
* For example:

```
sudo apt install openjdk-11-jdk openjdk-11-jre
javac -version
java -version
```

**Step 3: Install Apache Spark**

#### **Common Steps**
* Download Apache Spark - from the [Apache Spark Official Website](https://spark.apache.org/downloads.html).
* For example for 3.5.0:
```bash
curl -O https://archive.apache.org/dist/spark/spark-3.5.0/spark-3.5.0-bin-hadoop3.tgz
tar -xvf spark-3.5.0-bin-hadoop3.tgz
sudo mv spark-3.5.0-bin-hadoop3 /opt/spark
rm spark-3.5.0-bin-hadoop3.tgz
```

#### **Original Ubuntu Instructions (Manual Wget)**
* Install downloaded Apache Spark - on your Ubuntu by following [this tutorial](https://computingforgeeks.com/how-to-install-apache-spark-on-ubuntu-debian/).
* For example for 3.5.0:

```
wget https://www.apache.org/dyn/closer.lua/spark/spark-3.5.0/spark-3.5.0-bin-hadoop3.tgz
tar -xvf spark-3.5.0-bin-hadoop3.tgz
rm -rf spark-3.5.0-bin-hadoop3.tgz
sudo mv spark-3.5.0-bin-hadoop3 /opt/spark
```

Make sure that Spark version you have installed is compatible with Java you have installed, and Zingg is supporting those versions.

**Note**: Zingg supports Spark 3.5 and the corresponding Java version.

**Step 4: Install Apache Maven**

#### **For macOS**
```bash
brew install maven
```

#### **For Ubuntu**
* Install the latest **maven** package.
* For example for 3.8.8:

```
wget https://dlcdn.apache.org/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.tar.gz
tar -xvf apache-maven-3.8.8-bin.tar.gz 
rm -rf apache-maven-3.8.8-bin.tar.gz 
cd apache-maven-3.8.8/
cd bin
./mvn --version

Make sure that mvn -version should display correct java version as well(JAVA 11)
Apache Maven 3.8.7
Maven home: /usr/share/maven
Java version: 11.0.23, vendor: Ubuntu, runtime: /usr/lib/jvm/java-11-openjdk-amd64
```

**Step 5: Update Environment Variables**

#### **For macOS (~/.zshrc)**
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-11.jdk/Contents/Home
export SPARK_HOME=/opt/spark
export SPARK_MASTER=local[*]
export ZINGG_HOME=<path_to_zingg>/assembly/target
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin:$JAVA_HOME/bin
```

#### **For Ubuntu (~/.bashrc)**
Open `.bashrc` and add env variables at the end of the file.

```
vim ~/.bashrc
export SPARK_HOME=/opt/spark
export SPARK_MASTER=local[*]
export MAVEN_HOME=/home/ubuntu/apache-maven-3.8.8
export ZINGG_HOME=<path_to_zingg>/assembly/target
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin:$JAVA_HOME/bin

```
\<path\_to\_zingg> will be a directory where you clone the repository of the Zingg. Similarly, if you have installed spark on a different directory you can set **SPARK\_HOME** accordingly.

**Note :-** Skip exporting MAVEN_HOME if multiple maven version are not required

* Save/exit and do source .bashrc so that they reflect
```
source ~/.bashrc
```

* Verify:
```
echo $PATH
mvn --version

```

**Note:** If you have already set up **JAVA\_HOME** and **SPARK\_HOME** in the steps before you don't need to do this again.

**Step 6: Compile The Zingg Repository**

* Make sure you are executing the following commands in the same terminal window where you saved the bashrc. Run the following to compile the Zingg Repository -

```
git branch

```

* Run the following to Compile the Zingg Repository
```
mvn initialize
mvn clean compile package -Dspark=sparkVer
```

* Run the following to Compile while skipping tests
```
mvn initialize
mvn clean compile package -Dspark=sparkVer -Dmaven.test.skip=true
```

**Note:** Replace the `sparkVer` with the version of Spark you installed. \
\
For example, **-Dspark=3.5** you still face an error, include **-Dmaven.test.skip=true** with the above command.


**Step 7: If you have any issue with 'SPARK\_LOCAL\_IP'**

#### **For macOS**
1. **Find your IP:** `ipconfig getifaddr en0`
2. **Find your Hostname:** `hostname`
3. **Update hosts:** `sudo nano /etc/hosts` and add `[your-ip] [your-hostname]` at the bottom.

#### **For Ubuntu**
* Install **net-tools** using **sudo apt-get install -y net-tools**
* Run `ifconfig` in the terminal, find the **IP address** and paste the same in **/opt/hosts** IP address of your Pc-Name

**Step 8: Run Zingg To Find Training Data**

* Run this script in the terminal opened in Zingg clones directory `./scripts/zingg.sh --phase findTrainingData --conf examples/febrl/config.json`

**If everything is right, it should show Zingg banner.**

**Step 9: Run Zingg To label Data**

* Run this script in the terminal opened in Zingg clones directory `./scripts/zingg.sh --phase label --conf examples/febrl/config.json --properties-file config/zingg.conf`

**Step 10: Run Zingg To train model based on labelling**

* Run this script in the terminal opened in Zingg clones directory `./scripts/zingg.sh --phase train --conf examples/febrl/config.json --properties-file config/zingg.conf`

**Step 11: Run Zingg To prepare final output data**

* Run this script in the terminal opened in Zingg clones directory `./scripts/zingg.sh --phase match --conf examples/febrl/config.json --properties-file config/zingg.conf`
* change directory `cd /tmp/zinggOutput` (path provided in config file) to see the output files.

