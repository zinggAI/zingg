# macOS Setup for Zingg

This guide provides specific instructions for setting up the Zingg development environment on macOS.

### **Step 0: Initial OS Setup**

Homebrew is the preferred package manager for macOS and makes installing system dependencies much easier. While you can install dependencies manually, this guide uses Homebrew for simplicity.

Install Homebrew:
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```
**Note for Apple Silicon:** If `brew` is not found, run:
`echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zshrc && source ~/.zshrc`

---

### **Step 1: Clone The Zingg Repository**

```bash
brew install git
```

**Note:** It is suggested to fork the repository to your account and then clone the repository.

```bash
git clone https://github.com/zinggAI/zingg.git
```

---

### **Step 2: Install JDK 11 (Java Development Kit)**

* Install OpenJDK 11: `brew install openjdk@11`
* Link Java 11:
  * **Apple Silicon:** `sudo ln -sfn /opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk`
  * **Intel:** `sudo ln -sfn /usr/local/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk`

---

### **Step 3: Install Apache Spark**

* Download Apache Spark from the [Apache Spark Official Website](https://spark.apache.org/downloads.html).
* For example for 3.5.0:
```bash
curl -O https://archive.apache.org/dist/spark/spark-3.5.0/spark-3.5.0-bin-hadoop3.tgz
tar -xvf spark-3.5.0-bin-hadoop3.tgz
sudo mv spark-3.5.0-bin-hadoop3 /opt/spark
rm spark-3.5.0-bin-hadoop3.tgz
```

Make sure that Spark version you have installed is compatible with Java you have installed, and Zingg is supporting those versions.

**Note**: Zingg supports Spark 3.5 and the corresponding Java version.

---

### **Step 4: Install Apache Maven**

```bash
brew install maven
```

---

### **Step 5: Update Environment Variables (~/.zshrc)**

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-11.jdk/Contents/Home
export SPARK_HOME=/opt/spark
export SPARK_MASTER=local[*]
export ZINGG_HOME=<path_to_zingg>/assembly/target
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin:$JAVA_HOME/bin
```

`<path_to_zingg>` will be a directory where you clone the repository of the Zingg. Similarly, if you have installed spark on a different directory you can set **SPARK_HOME** accordingly.

* Save/exit and do source .zshrc so that they reflect:
```bash
source ~/.zshrc
```

* Verify:
```bash
echo $PATH
mvn --version
```

---

### **Step 6: Compile The Zingg Repository**

* Make sure you are executing the following commands in the same terminal window where you saved the .zshrc. Run the following to compile the Zingg Repository:

```bash
git branch
```

* Run the following to Compile the Zingg Repository:
```bash
mvn initialize
mvn clean compile package -Dspark=sparkVer
```

* Run the following to Compile while skipping tests:
```bash
mvn initialize
mvn clean compile package -Dspark=sparkVer -Dmaven.test.skip=true
```

**Note:** Replace the `sparkVer` with the version of Spark you installed. For example, **-Dspark=3.5**. If you still face an error, include **-Dmaven.test.skip=true** with the above command.

---

### **Step 7: If you have any issue with 'SPARK_LOCAL_IP'**

1. **Find your IP:** `ipconfig getifaddr en0`
2. **Find your Hostname:** `hostname`
3. **Update hosts:** `sudo nano /etc/hosts` and add `[your-ip] [your-hostname]` at the bottom.

---

---

### **Next Steps**

After completing the macOS-specific setup above, refer to the [main setup guide](./settingUpZingg.md) for Steps 8-11 to run Zingg (findTrainingData, label, train, and match phases).
