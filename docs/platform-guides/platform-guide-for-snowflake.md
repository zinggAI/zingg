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
For the Snowflake connector JSON config → [Connect Snowflake](../connect-your-data/connect-cloud-warehouses/connect-snowflake.md) Sample Snowflake config file: `github.com/zinggAI/zingg/blob/main/examples/febrl/configSnow.json`
{% endhint %}

This guide requires an AWS account with EC2 and Lambda access, and a Snowflake account. Ensure your EC2 security group allows outbound HTTPS (port 443) for Snowflake connectivity. A `t3.medium` or larger EC2 instance is sufficient for labeling and training phases. For large datasets (1M+ records) use `m5.xlarge` or above.

### **Step 1: Set up Zingg on AWS EC2**

Create or use an existing EC2 instance. Connect to it using VS Code Remote SSH:

1. Install the VS Code Remote SSH extension.
2. Add your EC2 host to the SSH config file:

```bash
Host <IP address of EC2 instance>
  HostName <hostname of EC2 instance>
  User ec2-user
  IdentityFile <path to.pem key file>
  PreferredAuthentications publickey
```

3. Click **Open a Remote Window** in VS Code, select your EC2 host, and connect. 4. Pull the Zingg Docker image and start a bash session inside the container:

```bash
docker pull zingg/zingg:0.6.0
docker run -it zingg/zingg:0.6.0 bash
```

Inside the container, your container ID is the alphanumeric string between `@` and `:` in the terminal prompt. For example in `root@fab997383957:/zingg#`, the container ID is `fab997383957`. Note this value—you will need it in Step 3.

Before connecting to Snowflake, download the Snowflake Spark connector JAR and the Snowflake JDBC driver and add them to `zingg.conf` inside the container:

```bash
spark.jars=snowflake-jdbc-3.13.19.jar,spark-snowflake_2.12-2.10.0-spark_3.1.jar
```

{% hint style="success" icon="right-long" %}
**Read more**: For JAR download links and full `zingg.conf` setup → [Connect Snowflake](../connect-your-data/connect-cloud-warehouses/connect-snowflake.md)
{% endhint %}

### **Step 2: Connect Zingg to Snowflake**

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
Replace `your-customer-table` with your source Snowflake table name and `your-output-table` with the table Zingg will create for resolved output. For all `fieldDefinition` parameters → [Configuration Schema](../reference/configuration-schema.md)

Set `numPartitions` to approximately 20–30× your Snowflake warehouse vCPU count. For a standard XS warehouse (1 node), start with 4. For a LARGE warehouse (4 nodes), use 16–32. Reduce `labelDataSampleSize` to 0.05–0.1 for tables with more than 1M rows.
{% endhint %}

### **Step 3: Create an AWS Lambda function to trigger Zingg phases**

The Lambda function receives a Zingg phase name, SSHes into EC2, and executes that phase inside the Docker container. `findTrainingData`, `train`, and `match` run via Lambda. The `label` phase runs interactively on EC2 directly.

Create a file called `lambda_function.py` with the following code. Replace all placeholder values before deploying:

```python
import json
import paramiko

def lambda_handler(event, context):
    status_code = 200
    try:
        event_body = event["body"]
        payload = json.loads(event_body)
        row = payload["data"]
        row_number = row[0][0]
        phase = row[0][1]

        if phase not in ['findTrainingData', 'match', 'train', 'checklog']:
            raise ValueError

        ssh = paramiko.SSHClient()
        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        ssh.connect(
            '<Your EC2 IP>',
            username='ec2-user',
            key_filename=('/var/task/your-key.pem')
        )

        if phase != 'checklog':
            command = (
                "docker exec "
                "<your container ID> "
                "bash -c 'zingg.sh "
                "--properties-file "
                "config/zingg.conf "
                "--phase " + phase + " --conf examples/febrl/"
                "configSnow.json'"
                " > logfile.txt"
            )
            stdin, stdout, stderr = ssh.exec_command(command)
            result = json.dumps({"data" : [[0, "Started phase: " + phase]]})
        else:
            command = "cat logfile.txt"
            stdin, stdout, stderr = ssh.exec_command(command)
            output = stdout.readlines()
            result = json.dumps({"data" : [[0, output]]})

        return {"statusCode" : status_code, "body" : result}

    except ValueError:
        return {"statusCode" : 400, "body" : json.dumps({"data" : [[0, "Invalid parameters"]]})}
    except Exception as e:
        return {"statusCode" : 400, "body" : json.dumps({"data" : [[0, str(e)]]})}
```

{% hint style="success" icon="right-long" %}
The `checklog` option lets you read the Zingg log file directly from Snowflake to monitor phase progress without logging back into EC2.
{% endhint %}

### **Step 4: Deploy the Lambda function**

1. Inside EC2, create a virtual environment and install dependencies:

```bash
python3 -m venv awspackagelayer
source awspackagelayer/bin/activate
pip install paramiko

#Zip the site - packages
zip -r deploy.zip awspackagelayer/lib/python3.7/site-packages

#Add the script and key file
zip -g deploy.zip lambda_function.py
zip -g deploy.zip your-key.pem
```

{% hint style="success" icon="right-long" %}
When the `.pem` key file is included in the zip, Lambda extracts it to `/var/task/your-key.pem` at runtime. The `key_filename` value in `lambda_function.py` must be set to `/var/task/your-key.pem` before zipping and deploying. If the path is wrong, the SSH connection will fail.
{% endhint %}

2. Go to **AWS Lambda** → **Create Function**. Give it a name and select Python. Create a new role with basic Lambda permissions.
3. Upload the `deploy.zip` via **Upload From** → `.zip` file.
4. Test the function with this Event JSON:

```json
{ "body" : "{\"data\": [[0, \"findTrainingData\"]]}" }
```

{% hint style="success" icon="right-long" %}
A successful test returns `HTTP 200` and `Started phase: findTrainingData`. Monitor progress via `tail -f logfile.txt` on EC2.
{% endhint %}

_**IMAGE TO BE ADDED — AWS Lambda function test screen showing a successful HTTP 200 response with the "Started phase: findTrainingData" body. Tanwi to check with team for screenshot this from a live Lambda test run and add here. Caption: "Lambda function test returning HTTP 200 — Zingg phase started successfully."**_

### **Step 5: Connect Lambda to Snowflake via external function**

1. Create a new IAM role. Entity type: **Another AWS account**. Specify your AWS Account ID.
2. Create an AWS API Gateway: REST API with regional endpoint. Create a resource with a POST method. Integration type: Lambda function with Lambda proxy integration.
3. Deploy the API and note the invoke URL.
4. In Snowflake, get the API integration ARN to update the IAM trust relationship:

```sql
DESC INTEGRATION your_api_integration;
```

Take the `API_AWS_ROLE_ARN` value from the output and add it as a trusted entity in the IAM role trust policy. This authorises Snowflake's AWS account to assume the role and invoke the Lambda function.

5. Create the external function in Snowflake:

```sql
CREATE OR REPLACE EXTERNAL FUNCTION run_zingg(phase VARCHAR) RETURNS VARIANT
    API_INTEGRATION = <your_api_integration> AS '<your API Gateway invoke URL>';
```

### Step 6: Run Zingg phases from Snowflake

**Run findTrainingData**

```sql
SELECT run_zingg('findTrainingData');
```

**Run label interactively on EC2 directly**

```sql
./zingg.sh --phase label --conf examples/febrl/configSnow.json
```

**Run train**

```sql
SELECT run_zingg('train');
```

**Run match**

```sql
SELECT run_zingg('match');
```

**Check logs from Snowflake**

```sql
SELECT run_zingg('checklog');
```

{% hint style="success" icon="right-long" %}
Run `findTrainingData` to generate candidate pairs. Zingg selects the most informative pairs from your data, not random samples. Run `label` interactively on EC2 to label those pairs as Match, No Match, or Uncertain. Label until all field types and data variation patterns in your schema are represented. If accuracy needs improvement after the first match run, return to labeling and focus on patterns that are missing or underrepresented.
{% endhint %}

### **Running phases asynchronously**

For large tables, Zingg phases can run for several hours. The SSH connection will time out and kill the job if you run synchronously. Use `nohup` to run phases as background processes on EC2:

```bash
nohup ./scripts/zingg.sh --properties-file ~/zingg/snowEnv.txt --phase findTrainingData --conf ~/zingg/snowConfigFile.json &
```

Monitor progress from EC2:

```bash
tail -f nohup.out
```

Or from Snowflake:

```bash
SELECT run_zingg('checklog');
```

{% hint style="success" icon="right-long" %}
Output is written to the Snowflake table configured in the `output` section of your `config.json`. Records with the same `Z_CLUSTER` value represent the same real-world entity. For output column definitions → [Interpret Output Scores](../interpreting-results/interpret-output-scores.md)
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

Same as the main guide above; pull the Zingg Docker image, configure Lambda, and invoke phases from Snowflake.
{% endtab %}

{% tab title="Enterprise" %}
{% hint style="info" icon="right-long" %}
Enterprise only. Zingg Enterprise runs natively inside Snowflake using Snowpark. No EC2, no Docker, and no Lambda required.

Enterprise requires a Zingg licence and the Enterprise Snowflake package. [Contact Zingg to get access](https://www.zingg.ai/company/contact/contact).
{% endhint %}

**Content for the Enterprise Snowflake platform guide is being prepared. This section will be updated with full step-by-step instructions once confirmed by the team.**
{% endtab %}
{% endtabs %}
