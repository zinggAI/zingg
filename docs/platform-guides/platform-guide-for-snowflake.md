---
description: >-
  End-to-end guide to running Zingg on Snowflake using Snowflake as your data
  source on AWS EC2 with Docker (Community) or running Zingg natively inside
  Snowflake with Enterprise.
tags:
  - ent
---

# Platform Guide for Snowflake

{% hint style="success" icon="right-long" %}
Run Zingg with Snowflake as your data source using the Snowflake Spark connector on EC2, or run Zingg natively inside Snowflake with Enterprise.
{% endhint %}

{% tabs %}
{% tab title="Community" %}
{% hint style="success" icon="right-long" %}
For the Snowflake connector JSON config → [Connect Snowflake](../connect-your-data/connect-cloud-warehouses/connect-snowflake.md). Sample Snowflake config file: `github.com/zinggAI/zingg/blob/main/examples/febrl/configSnow.json`
{% endhint %}

Community Zingg runs on an **AWS EC2** instance using the **Zingg Docker image**, reading input from Snowflake and writing resolved output back to Snowflake through the Snowflake Spark connector. You run the Zingg phases directly with the Zingg CLI (`zingg.sh`) or the Python API — no Lambda, API Gateway, or external functions required.

A `t3.medium` or larger EC2 instance is sufficient for labeling and training. For large datasets (1M+ records) use `m5.xlarge` or above. Ensure your EC2 security group allows outbound HTTPS (port 443) for Snowflake connectivity.

### **Step 1: Set up Zingg on AWS EC2**

Create or use an existing EC2 instance. Connect to it using VS Code Remote SSH:

1. Install the VS Code Remote SSH extension.
2. Add your EC2 host to the SSH config file:

```bash
Host <IP address of EC2 instance>
  HostName <hostname of EC2 instance>
  User ec2-user
  IdentityFile <path to .pem key file>
  PreferredAuthentications publickey
```

3. Click **Open a Remote Window** in VS Code, select your EC2 host, and connect.
4. Pull the Zingg Docker image and start a bash session inside the container:

```bash
docker pull zingg/zingg:0.7.0
docker run -it zingg/zingg:0.7.0 bash
```

Before connecting to Snowflake, add the Snowflake Spark connector and JDBC driver to `zingg.conf` inside the container. **Use the connector build that matches Zingg 0.7.0's Spark 3.5 runtime** — an older `spark_3.1`/`spark_3.4` build will not load:

```properties
spark.jars=snowflake-jdbc-<version>.jar,spark-snowflake_2.12-<version>-spark_3.5.jar
```

{% hint style="success" icon="right-long" %}
**Read more**: For JAR download links (matched to Spark 3.5) and full `zingg.conf` setup → [Connect Snowflake](../connect-your-data/connect-cloud-warehouses/connect-snowflake.md)
{% endhint %}

### **Step 2: Configure the Snowflake connection**

Configure your `config.json` with your Snowflake connection details, field definitions, and performance settings. Use `examples/febrl/configSnow.json` as a starting point.

```json
{
  "data" : [ {
    "name" : "identityResolution",
    "format" : "net.snowflake.spark.snowflake",
    "props" : {
      "sfUrl" : "your-account.snowflakecomputing.com",
      "sfUser" : "your-username",
      "sfPassword" : "your-password",
      "sfDatabase" : "your-database",
      "sfSchema" : "MYSCHEMA",
      "sfWarehouse" : "COMPUTE_WH",
      "dbtable" : "your-customer-table",
      "application" : "zingg_zingg"
    }
  } ],
  "output" : [ {
    "name" : "unifiedCustomers",
    "format" : "net.snowflake.spark.snowflake",
    "props" : {
      "sfUrl" : "your-account.snowflakecomputing.com",
      "sfUser" : "your-username",
      "sfPassword" : "your-password",
      "sfDatabase" : "your-database",
      "sfSchema" : "MYSCHEMA",
      "sfWarehouse" : "COMPUTE_WH",
      "dbtable" : "your-output-table",
      "application" : "zingg_zingg"
    }
  } ],
  "modelId" : "100",
  "zinggDir" : "models",
  "numPartitions" : 4,
  "labelDataSampleSize" : 0.5,
  "fieldDefinition" : [
    {
      "fieldName" : "fname",
      "matchType" : "FUZZY",
      "fields" : "fname",
      "dataType" : "string"
    },
    {
      "fieldName" : "lname",
      "matchType" : "FUZZY",
      "fields" : "lname",
      "dataType" : "string"
    }
  ]
}
```

{% hint style="success" icon="right-long" %}
Replace `your-customer-table` with your source Snowflake table and `your-output-table` with the table Zingg writes resolved output to. For all `fieldDefinition` parameters → [Configuration Schema](../reference/configuration-schema.md)

Set `numPartitions` to approximately 20–30× your Snowflake warehouse vCPU count. For a standard XS warehouse (1 node), start with 4. For a LARGE warehouse (4 nodes), use 16–32. Reduce `labelDataSampleSize` to 0.05–0.1 for tables with more than 1M rows.
{% endhint %}

### **Step 3: Run the Zingg phases**

Run the standard Zingg workflow — `findTrainingData` → `label` → `train` → `match` — from inside the container. Two options: the Zingg CLI or the Python API.

#### Option A — Zingg CLI

```bash
./scripts/zingg.sh --phase findTrainingData --conf examples/febrl/configSnow.json --properties-file config/zingg.conf
./scripts/zingg.sh --phase label            --conf examples/febrl/configSnow.json --properties-file config/zingg.conf
# repeat findTrainingData + label until you have enough labelled pairs, then:
./scripts/zingg.sh --phase train            --conf examples/febrl/configSnow.json --properties-file config/zingg.conf
./scripts/zingg.sh --phase match            --conf examples/febrl/configSnow.json --properties-file config/zingg.conf
```

#### Option B — Python API

Build the same source/output pipes with `SnowflakePipe` and run each phase by changing the `PHASE` value:

```python
from zingg.client import Arguments, ClientOptions, ZinggWithSpark
from zingg.pipes import SnowflakePipe, FieldDefinition, MatchType

args = Arguments()
args.setFieldDefinition([
    FieldDefinition("fname", "string", MatchType.FUZZY),
    FieldDefinition("lname", "string", MatchType.FUZZY),
])

def snowflake(name, table):
    p = SnowflakePipe(name)
    p.setURL("your-account.snowflakecomputing.com")
    p.setUser("your-username")
    p.setPassword("your-password")
    p.setDatabase("your-database")
    p.setSFSchema("MYSCHEMA")
    p.setWarehouse("COMPUTE_WH")
    p.setDbTable(table)
    return p

args.setData(snowflake("identityResolution", "your-customer-table"))
args.setOutput(snowflake("unifiedCustomers", "your-output-table"))
args.setModelId("100")
args.setZinggDir("models")
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)

# run one phase at a time: findTrainingData, label, train, match
options = ClientOptions([ClientOptions.PHASE, "findTrainingData"])
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()
```

{% hint style="success" icon="right-long" %}
Run `findTrainingData` to generate candidate pairs — Zingg selects the most informative pairs, not random samples. Run `label` interactively to mark those pairs as Match, No Match, or Can't Say. Label until all field types and data-variation patterns in your schema are represented. If accuracy needs improvement after the first match run, return to labeling and focus on patterns that are missing or underrepresented.
{% endhint %}

### **Running phases asynchronously**

For large tables, Zingg phases can run for several hours and an SSH timeout would kill the job. Use `nohup` to run a phase as a background process on EC2:

```bash
nohup ./scripts/zingg.sh --properties-file config/zingg.conf --phase findTrainingData --conf examples/febrl/configSnow.json &
```

Monitor progress:

```bash
tail -f nohup.out
```

{% hint style="success" icon="right-long" %}
Output is written to the Snowflake table configured in the `output` section of your `config.json`. Records with the same `z_cluster` value represent the same real-world entity. For output column definitions → [Interpret Output Scores](../interpreting-results/interpret-output-scores.md)
{% endhint %}

### **Snowflake Iceberg Tables variant**

This variant uses Azure Blob Storage as the Iceberg storage layer with Snowflake managing the Iceberg tables and Zingg running locally via Docker.

#### **Prerequisites:**

* Active Azure Blob Storage account with a container
* Snowflake account with a warehouse and database

#### **Step 1: Create the Iceberg setup in Snowflake**

1. Create an Azure Blob Storage account and container. Note the storage account name and container name.
2. In Snowflake: create a warehouse, database, and external volume pointing to Azure Blob Storage.
3. Run `DESC VOLUME <volume_name>` and click the `AZURE_CONSENT_URL` to authorise Snowflake access to Azure storage.
4. Note the `AZURE_MULTI_TENANT_APP_NAME` value (before the underscore) and add it as a role assignment in your Azure storage account.
5. Verify: `SYSTEM$VERIFY_EXTERNAL_VOLUME('your_volume_name')`.
6. Create your Iceberg table and load your data.

#### **Step 2: Configure Zingg**

Zingg accesses Snowflake Iceberg tables through the same Snowflake Spark connector as standard Snowflake tables. The Iceberg layer is transparent to Zingg — reference the Iceberg table by name in `dbtable` exactly as you would a regular Snowflake table. Ensure Snowflake has been authorised to access your Azure Blob Storage external volume before running any Zingg phase.

```json
{
  "data" : [ {
    "name" : "icebergInput",
    "format" : "net.snowflake.spark.snowflake",
    "props" : {
      "sfUrl" : "your-account.snowflakecomputing.com",
      "sfUser" : "your-username",
      "sfPassword" : "your-password",
      "sfDatabase" : "your-database",
      "sfSchema" : "MYSCHEMA",
      "sfWarehouse" : "COMPUTE_WH",
      "dbtable" : "your-iceberg-table-name",
      "application" : "zingg_zingg"
    }
  } ]
}
```

#### **Step 3: Run Zingg phases**

Same as the main guide above — pull the Zingg Docker image and run the phases with the Zingg CLI or Python API.
{% endtab %}

{% tab title="Enterprise" %}
{% hint style="info" icon="right-long" %}
Enterprise only. Zingg Enterprise runs natively inside Snowflake using Snowpark. No EC2, no Docker, and no Lambda required.

Enterprise requires a Zingg licence and the Enterprise Snowflake package. [Contact Zingg to get access](https://www.zingg.ai/company/contact/contact).
{% endhint %}

**Content for the Enterprise Snowflake platform guide is being prepared. This section will be updated with full step-by-step instructions once confirmed by the team.**
{% endtab %}
{% endtabs %}
