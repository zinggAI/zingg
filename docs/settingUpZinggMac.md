# Setting Up Zingg Development Environment (macOS)

The following steps will help you set up the Zingg Development Environment on macOS. \
\
These instructions are tested on macOS (Apple Silicon & Intel) and require Java 11.

**Step 0: Install Homebrew**

Homebrew is required to install system dependencies.

```
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

* Verify installation:

```
brew --version
```

**Step 1: Install Git**

```
brew install git
```

* Verify:

```
git --version
```

**Step 2: Clone The Zingg Repository**

```
git clone https://github.com/zinggAI/zingg.git
cd zingg
```

**Note:** It is recommended to fork the repository first if you plan to contribute.

**Step 3: Install JDK 11 (Required)**

**Note:** Zingg requires **Java 11**. Do **not** use Java 8, 17, or newer versions.

* Install OpenJDK 11:

```
brew install openjdk@11
```

* Link Java 11:

```
sudo ln -sfn /opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk
```

* Verify:

```
java -version
javac -version
```

Both should display **Java 11**.

**Step 4: Install Apache Spark**

**Note:** Apache Spark **must NOT** be installed via Homebrew.

* Download Spark from the official Apache site. Example for **Spark 3.5.0**:

```
curl -O https://archive.apache.org/dist/spark/spark-3.5.0/spark-3.5.0-bin-hadoop3.tgz
tar -xvf spark-3.5.0-bin-hadoop3.tgz
rm -rf spark-3.5.0-bin-hadoop3.tgz
sudo mv spark-3.5.0-bin-hadoop3 /opt/spark
```

**Note:** Zingg supports **Spark 3.5.x** with **Java 11**.

**Step 5: Install Apache Maven**

**Note:** Java **must be installed before Maven**.

* Install Maven:

```
brew install maven
```

* Verify:

```
mvn --version
```

* Ensure Maven is using **Java 11**:

```
Java version: 11.x
```

**Step 6: Update Environment Variables**

* Open your shell config file (e.g., `.zshrc`):

```
vim ~/.zshrc
```

* Add the following:

```
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-11.jdk/Contents/Home
export SPARK_HOME=/opt/spark
export SPARK_MASTER=local[*]
export ZINGG_HOME=<path_to_zingg>/assembly/target
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin:$JAVA_HOME/bin
```

Replace `<path_to_zingg>` with the absolute path where the Zingg repo is cloned.

* Reload:

```
source ~/.zshrc
```

* Verify:

```
java -version
spark-shell --version
mvn --version
```

**Step 7: Compile The Zingg Repository**

* Initialize Maven:

```
mvn initialize
```

* Compile:

```
mvn clean compile package -Dspark=sparkVer
```

* To skip tests:

```
mvn clean compile package -Dspark=sparkVer -Dmaven.test.skip=true
```

**Note:** Replace `sparkVer` with your Spark version (Example: **-Dspark=3.5**).

**Step 8: Run Zingg To Find Training Data**

* Run this script in the terminal opened in Zingg cloned directory:

```
./scripts/zingg.sh --phase findTrainingData --conf examples/febrl/config.json
```

**If everything is right, it should show Zingg banner.**