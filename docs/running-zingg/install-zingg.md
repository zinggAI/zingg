---
description: >-
  Install Zingg on your platform - Community and Enterprise editions across
  Spark and notebook environments
---

# Install Zingg

Zingg runs on Spark (all editions) or Snowflake (Enterprise only). Select your platform from the tabs below. Each tab covers both Community and Enterprise where applicable.

### Prerequisites

The following prerequisites apply to local and self-managed Spark installations only. Managed Spark services such as Databricks, Fabric, EMR, and Synapse handle the Spark runtime for you - you only need to install the Zingg library on those platforms.

* Java: JDK version 11.0.23 or compatible
* Spark: version 3.5.0 or compatible

{% tabs %}
{% tab title="Azure Databricks" %}
### **Step 1: Install Zingg on your Databricks cluster**

Go to **Compute → your cluster → Libraries → Install new → PyPI**.

Install the Zingg Python package matching your edition.

#### **Community**

```bash
%pip install zingg
```

#### Enterprise Lite or Enterprise

```bash
%pip install zinggEC
```

#### Enterprise Plus

```bash
%pip install zinggES
```

Install the required `tabulate` dependency (all editions):

```bash
%pip install tabulate
```

Restart the Python kernel: **Runtime → Restart Python**.

#### **Verify the installation**

```bash
%pip show zingg
```
{% endtab %}

{% tab title="Microsoft Fabric" %}
Zingg on Microsoft Fabric uses the Fabric notebook interface and a custom environment.\
Follow these steps before opening the Zingg notebook.

### **Step 1: Create a Fabric workspace and session**

1. Sign in to Microsoft Fabric and create a new workspace.
2. Name it something like `Zingg-Fabric` .
3. When prompted for a session cluster, choose **New Standard Session.**

### **Step 2: Download the Zingg example notebook**

1. Download `ExampleNotebook.ipynb` from `github.com/zinggAI/zingg/blob/main/examples/fabric/ExampleNotebook.ipynb` .
2. Upload the notebook to your Fabric workspace.

### **Step 3: Create a new environment and install the Zingg JAR**

1. Go to the **Environment** tab in your workspace and click **New Environment**. Name it `Zingg Environment`.
2. Download the latest Zingg release `tar` file from `github.com/zinggAI/zingg/releases` .
3. Extract the `tar` file and locate the Zingg JAR file inside it.
4. In your `Zingg Environment`, go to **Custom Library** and upload the JAR file.
5. **Save** and **Publish** the Environment.

### **Step 4: Install the Zingg Python package**

Install the Zingg Python package matching your edition.

#### **Community:**

```bash
pip install zingg
```

#### Enterprise Lite or Enterprise:

```bash
pip install zinggEC
```

#### Enterprise Plus:

```bash
pip install zinggES
```

Verify the installation:

```bash
pip show zingg
```

### **Step 5: Create a Lakehouse and upload your data**

1. In your workspace, click **New Item** and select **Lakehouse**. Name it and go inside it.
2. Click **Get Data** and upload your data file as CSV or Parquet.

{% hint style="success" icon="right-long" %}
Note the `abfss` path of your Lakehouse for use in the notebook. It follows this format: `abfss://<workspace-id>@onelake.dfs.fabric.microsoft.com/<lakehouse-id>/Files`

For the full step-by-step Fabric guide including screenshots → [Run on Microsoft Fabric](../platform-guides/platform-guide-for-microsoft-fabric.md)
{% endhint %}
{% endtab %}

{% tab title="AWS EMR" %}
Two options for running Zingg on AWS EMR.

### Option A - spark-submit with Zingg JAR

Use the `spark-submit` option passing the Zingg JAR, phase name, and config file. `config.json` must be available locally on the driver.

{% code title="spark-submit example" expandable="true" %}
```bash
aws emr create-cluster \
  --name "Add Spark Step Cluster" \
  --release-label emr-6.2.0 \
  --applications Name=Zingg \
  --ec2-attributes KeyName=myKey \
  --instance-type <instance type> \
  --instance-count <num instances> \
  --steps Type=Spark,Name="Zingg",ActionOnFailure=CONTINUE,Args=[--class,zingg.spark.client.SparkClient,<s3 location of zingg.jar>,--phase,<findTrainingData or match etc>,--conf,<local location of config.json>] \
  --use-default-roles
```
{% endcode %}

### Option B - AWS EMR Notebooks

Run Zingg Python code directly in AWS EMR Notebooks using the Python API. Install the Zingg Python package matching your edition.

#### Community

```bash
%pip install zingg
```

#### Enterprise Lite or Enterprise

```bash
%pip install zinggEC
```

#### Enterprise Plus

```bash
%pip install zinggES
```

#### **Verify the installation**

```bash
%pip show zingg
```
{% endtab %}

{% tab title="AWS Glue" %}
**AWS Glue install steps are not documented on any live docs page. CHECK WITH SONAL before publishing this tab.**
{% endtab %}

{% tab title="GCP Dataproc" %}
_**CHECK WITH SONAL - - NEED TEAMS HELP TO CHECK WHAT EXACTLY FROM THE GCS GUIDE TO BE ADDED HERE**_
{% endtab %}

{% tab title="Azure Synapse" %}
**CHECK WITH SONAL - if "Zingg on Azure Synapse" is the same as "Databricks installation instructions" above. Are the install method and library setup are the same.**
{% endtab %}

{% tab title="Local Spark" %}
{% hint style="success" icon="right-long" %}
Docker is the fastest way to get started locally. Use installing from release if you need a specific Spark version or want to integrate with an existing Spark installation.
{% endhint %}

### Option A - Docker (recommended)

```bash
docker pull zingg/zingg:0.5.0
docker run -it zingg/zingg:0.5.0 bash
```

If permission denied:

```bash
docker run -v /tmp:/tmp -it zingg/zingg:0.5.0 bash
```

### Option B - Installing from Release

Download the latest release from GitHub: `github.com/zinggAI/zingg/releases`

**Example for Zingg 0.5.0 on Spark 3.5.0**

Assumes Zingg 0.5.0 on Spark 3.5.0

**Prerequisites**: Java JDK 11.0.23, Spark 3.5.0

```bash
wget https://github.com/zinggAI/zingg/releases/download/v0.5.0/zingg-0.5.0-spark_3.5.tar.gz

tar -xvf zingg-0.5.0-spark_3.5.tar.gz
```

### Set up environment variables

Add the following to `~/.bash_aliases` (Linux) or `~/.zshrc` (macOS):

```bash
export JAVA_HOME=<path to jdk>
export SPARK_HOME=<path to Apache Spark>
export SPARK_MASTER=local[*]
export ZINGG_HOME=<path to zingg>
export PATH=$PATH:$JAVA_HOME/bin:$SPARK_HOME/bin:$SPARK_HOME/sbin:$ZINGG_HOME/scripts
```

Also verify that your machine's IP is added to `/etc/hosts` for localhost. Run `ifconfig` to find the IP and add it.

#### Verify your installation

Run bash and print the aliases to confirm they are set correctly:

```bash
echo $SPARK_HOME
echo $JAVA_HOME
java --version
echo $ZINGG_HOME
```

Then run a sample program to confirm the installation works:

```bash
cd zingg
./scripts/zingg.sh --phase trainMatch --conf examples/febrl/config.json
```

This builds Zingg models and finds duplicates in `examples/febrl/test.csv`. You will see Zingg logs on the console and output files under `/tmp/zinggOutput` with matching records sharing the same cluster ID. If you see this, Zingg is correctly installed.
{% endtab %}

{% tab title="Snowflake (ENT)" %}
{% hint style="info" icon="right-long" %}
Enterprise only. Zingg on Snowflake uses the Enterprise Snowflake package and runs natively inside Snowflake using Snowpark — no Spark cluster required.
{% endhint %}

**CHECK WITH SONAL** - Snowflake Enterprise install is referenced at\
docs.zingg.ai/latest/stepbystep/installation/installing-zingg-enterprise-snowflake

### **Prerequisites**

* Java JDK 11
* A Snowflake account with `ZINGG_STAGE` already created
* A Zingg Enterprise licence

### **Install Zingg Enterprise for Snowflake**

Extract the Enterprise Snowflake tar file and configure environment variables.

{% code title="Decompress archive" overflow="wrap" %}
```bash
gzip -d zingg-enterprise-snowflake-<version>.tar.gz
```
{% endcode %}

{% code title="Extract archive" overflow="wrap" %}
```bash
tar xvf zingg-enterprise-snowflake-<version>.tar
```
{% endcode %}

Set the environment variables:

{% code title="Set environment variables" overflow="wrap" %}
```bash
export ZINGG_SNOW_HOME=~/zingg-enterprise-snowflake-<version>
export ZINGG_SNOW_JAR=~/zingg-enterprise-snowflake-<version>
```
{% endcode %}

Move your licence file into the install directory:

{% code title="Move licence file" overflow="wrap" %}
```bash
mv ~/zingg.license .
```
{% endcode %}

{% hint style="info" icon="right-long" %}
Add `ZINGG_SNOW_JAR` and `ZINGG_SNOW_HOME` to `.bashrc` so they persist across sessions. For the full end-to-end Snowflake guide → [Run on Snowflake](../platform-guides/platform-guide-for-snowflake.md)
{% endhint %}

### **Create the Snowflake properties file (`snowEnv.txt`)**

{% code title="Create snowEnv.txt" overflow="wrap" %}
```bash
touch snowEnv.txt
```
{% endcode %}

**Contents of `snowEnv.txt`**

{% code title="snowEnv.txt" overflow="wrap" %}
```bash
URL={snowflake_url}
USER={snowflake_user_name}
PASSWORD={snowflake_password}
ROLE={role}
WAREHOUSE={warehouse}
DB={database_name}
SCHEMA={schema}
CLIENT_SESSION_KEEP_ALIVE_HEARTBEAT_FREQUENCY=900
```
{% endcode %}

`CLIENT_SESSION_KEEP_ALIVE_HEARTBEAT_FREQUENCY` is the number of seconds between client attempts to update the session token. Valid range: 900 to 3600.

### **Verify the installation**

{% code title="Verify installation" overflow="wrap" expandable="true" %}
```bash
./scripts/zingg.sh \
  --properties-file snowEnv.txt \
  --phase findTrainingData \
  --conf examples/febrl/configSnow.json
```
{% endcode %}

This will run Zingg models and produce tables named `UNIFIED_CUSTOMERS_MODELID` with matching records sharing the same cluster ID. Congratulations, Zingg Enterprise for Snowflake is installed.
{% endtab %}
{% endtabs %}
