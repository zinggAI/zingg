---
description: >-
  Install Zingg on your platform - Community and Enterprise editions across
  Spark and notebook environments
---

# Install Zingg

Zingg runs on Spark (all editions) or Snowflake (Enterprise only). Select your platform from the tabs below. Each tab covers both Community and Enterprise where applicable.

### Prerequisites

The following prerequisites apply to local and self-managed Spark installations only. Managed Spark services such as Databricks, Fabric, EMR, and Dataproc handle the Spark runtime for you - you only need to install the Zingg library on those platforms.

* Java: JDK version 11.0.23 or compatible
* Spark: version 3.5.2 or compatible

{% tabs %}
{% tab title="Notebook Environments" %}
### **Step 1: Install Zingg on your Databricks cluster**

Go to **Compute → your cluster → Libraries → Install new → PyPI**.

Install the Zingg Python package matching your edition.

#### **Community**

```bash
%pip install zingg
```

#### Enterprise

```bash
%pip install zinggEC
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

{% tab title="Local Spark" %}
{% hint style="success" icon="right-long" %}
[Docker](quick-start-docker.md) is the fastest way to get started locally. Use installing from release if you need a specific Spark version or want to integrate with an existing Spark installation.
{% endhint %}

### Installing from Release

Download the latest release from GitHub: `github.com/zinggAI/zingg/releases`

**Example for Zingg 0.7.0 on Spark 3.5.2**

**Prerequisites**: Java JDK 11.0.23, Spark 3.5.2

```bash
wget https://github.com/zinggAI/zingg/releases/download/v0.7.0/zingg-0.7.0-spark_3.5.tar.gz

tar -xvf zingg-0.7.0-spark_3.5.tar.gz
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
Enterprise only. Zingg Enterprise on Snowflake uses the Snowflake Container Application package and runs natively inside Snowflake using Snowpark — no Spark cluster required.
{% endhint %}

**Create the connection properties**

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
EXECUTE JOB SERVICE
IN COMPUTE POOL ZINGG_POOL
NAME = ZINGG_MATCH
ASYNC = true
EXTERNAL_ACCESS_INTEGRATIONS = (ALLOW_ALL_EAI)
FROM @specs SPECIFICATION_TEMPLATE_FILE='zingg-job.yaml'
USING (PHASE => 'match', CONFIG => 'febrlConfig.json');
```
{% endcode %}

This will run Zingg febrl example and produce tables named `UNIFIED_CUSTOMERS` with matching records sharing the same Zingg ID.&#x20;

Congratulations, Zingg Enterprise for Snowflake is installed.
{% endtab %}
{% endtabs %}
