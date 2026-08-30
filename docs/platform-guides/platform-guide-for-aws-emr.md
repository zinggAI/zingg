---
description: >-
  Run Zingg on AWS Elastic MapReduce for distributed Spark-based entity
  resolution on AWS infrastructure.
---

# Platform Guide for AWS EMR

AWS EMR provides managed Spark on AWS infrastructure. Zingg runs on EMR using the standard Python API for Community and EZingg for Enterprise. S3 is used for data storage and model persistence using the `s3a://` path format.

{% tabs %}
{% tab title="Community" %}
{% hint style="success" icon="right-long" %}
Tested with EMR 7.3.0 (Spark 3.5.1). Recommended instance type: `m6g.xlarge` or larger for worker nodes.
{% endhint %}

### Prerequisites

* AWS account with EMR and S3 access
* EC2 key pair for SSH access
* S3 bucket for data and model storage
* IAM role with EMR, S3, and EC2 permissions

### Step 1: Create an S3 bucket

Create a bucket for Zingg data, models, and JARs:

```bash
aws s3 mb s3://your-zingg-bucket --region us-east-1
```

Upload the Zingg JAR and your dataset:

```bash
aws s3 cp zingg-0.7.0.jar s3://your-zingg-bucket/jars/
aws s3 cp your-data.csv s3://your-zingg-bucket/data/
```

### Step 2: Create an EMR cluster

Create a cluster with the required configurations:

```bash
aws emr create-cluster \
  --name "Zingg-EMR" \
  --release-label emr-7.3.0 \
  --applications Name=Spark Name=JupyterHub \
  --ec2-attributes KeyName=your-key-pair,InstanceProfile=EMR_EC2_DefaultRole \
  --instance-groups \
    InstanceGroupType=MASTER,InstanceCount=1,InstanceType=m6g.xlarge \
    InstanceGroupType=CORE,InstanceCount=2,InstanceType=m6g.xlarge \
  --configurations '[{"Classification":"spark-defaults","Properties":{"spark.jars":"s3://your-zingg-bucket/jars/zingg-0.7.0.jar"}}]' \
  --service-role EMR_DefaultRole \
  --region us-east-1
```

### Step 3: Connect to the cluster

Once the cluster is running, connect via SSH:

```bash
ssh -i your-key.pem hadoop@<master-public-dns>
```

Or use JupyterHub at `https://<master-public-dns>:9443`

### Step 4: Install Zingg Python package

In a notebook or terminal on the cluster:

```bash
pip install zingg==0.7.0
```

### Step 5: Configure and run Zingg

Create your `config.json` with S3 paths:

```json
{
  "data": [{
    "name": "inputData",
    "format": "csv",
    "props": {
      "path": "s3a://your-zingg-bucket/data/your-data.csv",
      "header": "true",
      "inferSchema": "true"
    }
  }],
  "output": [{
    "name": "outputData",
    "format": "csv",
    "props": {
      "path": "s3a://your-zingg-bucket/output/"
    }
  }],
  "modelId": "100",
  "zinggDir": "s3a://your-zingg-bucket/models",
  "numPartitions": 16,
  "labelDataSampleSize": 0.5,
  "fieldDefinition": [...]
}
```

Run Zingg phases:

```bash
# On the master node
zingg.sh --phase findTrainingData --conf config.json
zingg.sh --phase label --conf config.json
zingg.sh --phase train --conf config.json
zingg.sh --phase match --conf config.json
```

### Step 6: Monitor and iterate

Check logs in `/mnt/var/log/spark/` or via the Spark UI at `http://<master-public-dns>:18080`
{% endtab %}

{% tab title="Enterprise" %}
{% hint style="info" icon="right-long" %}
Enterprise requires a Zingg licence and the Enterprise EMR package. [Contact Zingg to get access](https://www.zingg.ai/company/contact/contact).
{% endhint %}

### Prerequisites

* AWS account with EMR and S3 access
* Zingg Enterprise license
* Enterprise JARs: `zingg-enterprise-spark-0.7.0.jar`, `zingg_license.jar`
* Enterprise Python packages: `zinggEC`, `zinggES`

### Step 1: Create an S3 bucket and upload Enterprise artifacts

```bash
aws s3 mb s3://your-zingg-enterprise-bucket --region us-east-1
aws s3 cp zingg-enterprise-spark-0.7.0.jar s3://your-zingg-enterprise-bucket/jars/
aws s3 cp zingg_license.jar s3://your-zingg-enterprise-bucket/jars/
aws s3 cp your-data.csv s3://your-zingg-enterprise-bucket/data/
```

Upload Enterprise Python wheels:

```bash
aws s3 cp zinggEC-0.7.0-py3-none-any.whl s3://your-zingg-enterprise-bucket/wheels/
aws s3 cp zinggES-0.7.0-py3-none-any.whl s3://your-zingg-enterprise-bucket/wheels/
```

### Step 2: Create an EMR cluster with Enterprise JARs

```bash
aws emr create-cluster \
  --name "Zingg-Enterprise-EMR" \
  --release-label emr-7.3.0 \
  --applications Name=Spark Name=JupyterHub \
  --ec2-attributes KeyName=your-key-pair,InstanceProfile=EMR_EC2_DefaultRole \
  --instance-groups \
    InstanceGroupType=MASTER,InstanceCount=1,InstanceType=m6g.xlarge \
    InstanceGroupType=CORE,InstanceCount=4,InstanceType=m6g.xlarge \
  --configurations '[{"Classification":"spark-defaults","Properties":{"spark.jars":"s3://your-zingg-enterprise-bucket/jars/zingg-enterprise-spark-0.7.0.jar,s3://your-zingg-enterprise-bucket/jars/zingg_license.jar"}}]' \
  --service-role EMR_DefaultRole \
  --region us-east-1
```

### Step 3: Install Enterprise Python packages

On the cluster master node:

```bash
pip install s3://your-zingg-enterprise-bucket/wheels/zinggEC-0.7.0-py3-none-any.whl
pip install s3://your-zingg-enterprise-bucket/wheels/zinggES-0.7.0-py3-none-any.whl
pip install zingg==0.7.0
```

### Step 4: Configure and run Zingg Enterprise

Create `config.json` with Enterprise settings:

```json
{
  "data": [{
    "name": "inputData",
    "format": "csv",
    "props": {
      "path": "s3a://your-zingg-enterprise-bucket/data/your-data.csv",
      "header": "true",
      "inferSchema": "true"
    }
  }],
  "output": [{
    "name": "outputData",
    "format": "csv",
    "props": {
      "path": "s3a://your-zingg-enterprise-bucket/output/"
    }
  }],
  "modelId": "100",
  "zinggDir": "s3a://your-zingg-enterprise-bucket/models",
  "numPartitions": 32,
  "labelDataSampleSize": 0.5,
  "blockingModel": "DEFAULT",
  "fieldDefinition": [...]
}
```

Run Enterprise phases:

```bash
# On the master node
zingg.sh --phase findTrainingData --conf config.json
zingg.sh --phase label --conf config.json
zingg.sh --phase train --conf config.json
zingg.sh --phase match --conf config.json

# Enterprise-only phases
zingg.sh --phase explain --conf config.json
zingg.sh --phase runIncremental --conf config.json
```

### Step 5: Monitor and iterate

Check logs in `/mnt/var/log/spark/` or via the Spark UI at `http://<master-public-dns>:18080`

Use `explain` phase output for detailed match analysis.
{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
**Read more**:

* Tune accuracy → [Improve Accuracy](../tuning/improve-accuracy/)
* Understand scores and set thresholds → [Interpret Output Scores](../interpreting-results/interpret-output-scores.md)
* Set up incremental for production → [Run Incremental Matching](../running-zingg/run-incremental-matching.md)
{% endhint %}