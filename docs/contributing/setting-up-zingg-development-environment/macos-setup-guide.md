---
description: >-
  Step-by-step instructions for setting up the Zingg development environment on
  macOS. Install Homebrew, Java 11, Spark 3.5, Maven, then build Zingg from
  source and run the full match workflow.
---

# macOS Setup Guide

This guide provides specific instructions for setting up the Zingg development environment on macOS.

### **Step 0: Initial OS Setup** <a href="#step-0-initial-os-setup" id="step-0-initial-os-setup"></a>

Homebrew is the preferred package manager for macOS and makes installing system dependencies much easier. While you can install dependencies manually, this guide uses Homebrew for simplicity.

#### **Install Homebrew**

```bash
/ bin / bash - c
    "$(curl -fsSL "
    "https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)")"
```

{% hint style="success" icon="right-long" %}
**For Apple Silicon:** If `brew` is not found, run: `echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zshrc && source ~/.zshrc`
{% endhint %}

### **Step 1: Clone The Zingg Repository** <a href="#step-1-clone-the-zingg-repository" id="step-1-clone-the-zingg-repository"></a>

```bash
brew install git
```

{% hint style="success" icon="right-long" %}
It is suggested to fork the repository to your account and then clone the repository.
{% endhint %}

```bash
git clone https://github.com/zinggAI/zingg.git
```

### **Step 2: Install JDK 11 (Java Development Kit)** <a href="#step-2-install-jdk-11-java-development-kit" id="step-2-install-jdk-11-java-development-kit"></a>

1. Install OpenJDK 11: `brew install openjdk@11`
2. Link Java 11:

* **Apple Silicon:** `sudo ln -sfn /opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk`
* **Intel:** `sudo ln -sfn /usr/local/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk`

### **Step 3: Install Apache Spark** <a href="#step-3-install-apache-spark" id="step-3-install-apache-spark"></a>

Download Apache Spark from the [Apache Spark Official Website](https://spark.apache.org/downloads.html).

For example for 3.5.0:

```bash
curl -
    O https
    :  // archive.apache.org/dist/spark/spark-3.5.0/spark-3.5.0-bin-hadoop3.tgz
       tar -
       xvf spark - 3.5.0 - bin - hadoop3.tgz sudo mv spark - 3.5.0 - bin -
       hadoop3 / opt / spark rm spark - 3.5.0 - bin - hadoop3.tgz
```

Make sure that Spark version you have installed is compatible with Java you have installed, and Zingg is supporting those versions.

{% hint style="success" icon="right-long" %}
Zingg supports Spark 3.5 and the corresponding Java version.
{% endhint %}

### **Step 4: Install Apache Maven** <a href="#step-4-install-apache-maven" id="step-4-install-apache-maven"></a>

```bash
brew install maven
```

### **Step 5: Update Environment Variables (\~/.zshrc)** <a href="#step-5-update-environment-variables-.zshrc" id="step-5-update-environment-variables-.zshrc"></a>

```bash
export JAVA_HOME =
    / Library / Java / JavaVirtualMachines / openjdk -
    11.jdk / Contents / Home export SPARK_HOME =
        / opt / spark export SPARK_MASTER = local[*] export ZINGG_HOME =
            <path_to_zingg> / assembly / target export PATH =
                $PATH : $SPARK_HOME / bin : $SPARK_HOME / sbin : $JAVA_HOME /
                                                                 bin
```

`<path_to_zingg>` will be a directory where you clone the repository of the Zingg. Similarly, if you have installed spark on a different directory you can set **SPARK\_HOME** accordingly.

**Save/exit and do source `.zshrc` so that they reflect**

```bash
source ~/.zshrc
```

**Verify**

```bash
echo $PATH mvn-- version
```

### **Step 6: Compile The Zingg Repository** <a href="#step-6-compile-the-zingg-repository" id="step-6-compile-the-zingg-repository"></a>

1. Make sure you are executing the following commands in the same terminal window where you saved the .zshrc. Run the following to compile the Zingg Repository:

```bash
git branch
```

2. Run the following to Compile the Zingg Repository:

```bash
mvn initialize mvn clean compile package - Dspark = sparkVer
```

3. Run the following to Compile while skipping tests:

```bash
mvn initialize mvn clean compile package - Dspark =
    sparkVer - Dmaven.test.skip = true
```

{% hint style="success" icon="right-long" %}
Replace the `sparkVer` with the version of Spark you installed. For example, **-Dspark=3.5**. If you still face an error, include **-Dmaven.test.skip=true** with the above command.
{% endhint %}

### **Step 7: If you have any issue with 'SPARK\_LOCAL\_IP'** <a href="#step-7-if-you-have-any-issue-with-spark_local_ip" id="step-7-if-you-have-any-issue-with-spark_local_ip"></a>

1. **Find your IP:** `ipconfig getifaddr en0`
2. **Find your Hostname:** `hostname`
3. **Update hosts:** `sudo nano /etc/hosts` and add `[your-ip] [your-hostname]` at the bottom.

### **Step 8: Run Zingg To Find Training Data** <a href="#step-8-run-zingg-to-find-training-data" id="step-8-run-zingg-to-find-training-data"></a>

Run this script in the terminal opened in Zingg clones directory:

```bash
./ scripts / zingg.sh-- phase findTrainingData-- conf examples / febrl /
    config.json
```

_If everything is right, it should show Zingg banner._

### **Step 9: Run Zingg To Label Data** <a href="#step-9-run-zingg-to-label-data" id="step-9-run-zingg-to-label-data"></a>

Run this script in the terminal opened in Zingg clones directory:

```bash
./ scripts / zingg.sh-- phase label-- conf examples / febrl /
        config.json-- properties -
    file config / zingg.conf
```

### **Step 10: Run Zingg To Train Model Based On Labeling** <a href="#step-10-run-zingg-to-train-model-based-on-labeling" id="step-10-run-zingg-to-train-model-based-on-labeling"></a>

Run this script in the terminal opened in Zingg clones directory:

```bash
./ scripts / zingg.sh-- phase train-- conf examples / febrl /
        config.json-- properties -
    file config / zingg.conf
```

### **Step 11: Run Zingg To Prepare Final Output Data** <a href="#step-11-run-zingg-to-prepare-final-output-data" id="step-11-run-zingg-to-prepare-final-output-data"></a>

Run this script in the terminal opened in Zingg clones directory:

```bash
./ scripts / zingg.sh-- phase match-- conf examples / febrl /
        config.json-- properties -
    file config / zingg.conf
```

Change directory `cd /tmp/zinggOutput` (or the path provided in your config file) to see the output files.
