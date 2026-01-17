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

**Note for Apple Silicon:** If `brew` is not found after installation, run:
```bash
echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zshrc
source ~/.zshrc
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

  **For Apple Silicon (M1/M2/M3):**
  ```bash
  sudo ln -sfn /opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk
  ```

  **For Intel Macs:**
  ```bash
  sudo ln -sfn /usr/local/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk
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

**Step 8: Handling 'SPARK_LOCAL_IP' Issues**

If you encounter issues with `SPARK_LOCAL_IP`, follow these concise steps to fix it on macOS:

1.  **Find your IP:** `ipconfig getifaddr en0`
2.  **Find your Hostname:** `hostname`
3.  **Update hosts:** `sudo nano /etc/hosts`
    *   Add a line at the bottom with: `your-ip  your-hostname`

**Step 9: Run Zingg To Find Training Data**

Run this script in the terminal opened in the Zingg clones directory:

```bash
./scripts/zingg.sh --phase findTrainingData --conf examples/febrl/config.json
```

If everything is configured correctly, you should see the Zingg banner.

**Step 10: Run Zingg To Label Data**

Run the following command to start the labeling process:

```bash
./scripts/zingg.sh --phase label --conf examples/febrl/config.json --properties-file config/zingg.conf
```

**Step 11: Run Zingg To Train Model**

Once labeling is complete, run this command to train the model:

```bash
./scripts/zingg.sh --phase train --conf examples/febrl/config.json --properties-file config/zingg.conf
```

**Step 12: Run Zingg To Prepare Final Output Data**

Finally, run the match phase to generate the output:

```bash
./scripts/zingg.sh --phase match --conf examples/febrl/config.json --properties-file config/zingg.conf
```

To view the output files, navigate to the output directory (as specified in your config file):

```bash
cd /tmp/zinggOutput
ls -lh
```