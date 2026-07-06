---
description: >-
  End-to-end guide to running Zingg on AWS Glue from S3 bucket setup and IAM
  configuration to your first match result. Covers Community (open source) and
  Enterprise.
---

# Platform Guide for AWS GLUE

AWS Glue provides serverless Spark on AWS. There are no servers to manage; you define your session configuration, Glue provisions the workers, and you pay only for what you use. Zingg runs on Glue Interactive Sessions using the standard Python API. S3 is used for all data input, output, model storage, and checkpointing.

{% hint style="success" icon="right-long" %}
* Tested with AWS Glue version 5.0, worker type G.1X, Spark 3.5.
* AWS Glue Interactive Sessions restrict standard Jupyter widgets. Zingg's usual labeling widget does not render in Glue notebooks. This guide uses a CSV-based review workflow instead - candidate pairs are exported to S3 for offline labeling and read back into the session. This is covered in detail in Step 14.
{% endhint %}

{% tabs %}
{% tab title="Community" %}
Uses `Arguments`, `FieldDefinition`, `CsvPipe`, and `ZinggWithSpark`. The full workflow runs in a single Glue Studio notebook.

### AWS setup

Complete these steps in the AWS Console before opening any notebook.

#### **Step 1: Create an S3 bucket and upload JARs**

Zingg on AWS Glue requires six JAR files uploaded to S3 before the session starts. These are injected into the Glue workers at session startup via the `%%configure` magic cell.

Create an S3 bucket (for example `zingg-production-storage`) and upload all six JARs into a `/jars/` folder inside it. Also upload your data file (for example `test.csv`) directly to the bucket.

**AWS Console:**

1. Navigate to **S3 → Buckets → Create bucket**.
2. Give the bucket a globally unique name (for example `zingg-production-storage`).
3. Set the region to match where your Glue jobs will run (for example `us-east-1`).
4. Once created, upload the following JAR files into a `/jars/` folder:

<table><thead><tr><th valign="top">JAR</th><th valign="top">Purpose</th></tr></thead><tbody><tr><td valign="top"><code>zingg-0.6.0.jar</code></td><td valign="top">The Zingg engine</td></tr><tr><td valign="top"><code>zingg-common-client-0.6.0.jar</code></td><td valign="top">Common client</td></tr><tr><td valign="top"><code>zingg-common-core-0.6.0.jar</code></td><td valign="top">Common core</td></tr><tr><td valign="top"><code>zingg-spark-client-0.6.0.jar</code></td><td valign="top">Spark client</td></tr><tr><td valign="top"><code>zingg-spark-core-0.6.0.jar</code></td><td valign="top">Spark core</td></tr><tr><td valign="top"><code>zingg-common-infra-0.6.0.jar</code></td><td valign="top">Infrastructure link</td></tr></tbody></table>

Download all JARs from `github.com/zinggAI/zingg/releases`. Also upload your data file to the bucket root.

_**IMAGE TO BE ADDED — S3 bucket view showing the\*\*\*\*****&#x20;****`/jars/`****&#x20;****\*\*\*\*folder with all six Zingg JAR files listed. Tanwi to check with team for the screenshot.**_

#### **Step 2: Create an IAM role for Glue**

AWS Glue Interactive Sessions require a dedicated IAM Role that allows the notebook to read and write S3, run Glue jobs, and pass the role to the Glue service.

**Step 2a: Create the role**

1. Navigate to **IAM Console → Roles → Create role**.
2. **Trusted entity type:** AWS service.
3. **Service or use case:** Glue.
4. Name the role (for example `zingg-glue-role`).

**Step 2b: Attach managed policies**

Search for and attach these four AWS managed policies:

<table><thead><tr><th valign="top">Policy</th><th valign="top">Purpose</th></tr></thead><tbody><tr><td valign="top"><code>AmazonS3FullAccess</code></td><td valign="top">Read and write data and models in S3</td></tr><tr><td valign="top"><code>AWSGlueConsoleFullAccess</code></td><td valign="top">Manage Glue jobs via the Studio UI</td></tr><tr><td valign="top"><code>AWSGlueServiceRole</code></td><td valign="top">Required for Glue to run worker nodes</td></tr><tr><td valign="top"><code>AmazonQDeveloperAccess</code></td><td valign="top">Enables AI coding assistance (optional)</td></tr></tbody></table>

**Step 2c: Attach an inline policy**

Create an inline policy named `ZinggSessionPermissions` with the following JSON. Replace the `Resource` ARN in `AllowIAMPassRole` with your own role ARN.

```json
{
  "Version" : "2012-10-17",
  "Statement" : [
    {
      "Sid" : "AllowIAMPassRole",
      "Effect" : "Allow",
      "Action" : "iam:PassRole",
      "Resource" : "arn:aws:iam::<account-id>:role/zingg-glue-role",
      "Condition" : {
        "StringLike" : {
          "iam:PassedToService" : "glue.amazonaws.com"
        }
      }
    },
    {
      "Sid" : "ZinggBucketAccess",
      "Effect" : "Allow",
      "Action" : [
        "s3:GetObject", "s3:PutObject", "s3:ListBucket", "kms:Decrypt",
        "kms:GenerateDataKey"
      ],
      "Resource" : [
        "arn:aws:s3:::your-bucket-name",
        "arn:aws:s3:::your-bucket-name/*"
      ]
    },
    {
      "Sid" : "GlueMetadata",
      "Effect" : "Allow",
      "Action" : [ "glue:Get*", "codewhisperer:GenerateRecommendations" ],
      "Resource" : "*"
    }
  ]
}
```

_**IMAGE TO BE ADDED — IAM Role creation screen in the AWS Console showing the role name, attached managed policies, and the inline policy editor. Tanwi to check with team for the screenshot.**_

#### **Step 3: Create a Glue notebook and attach the IAM role**

1. Navigate to **AWS Glue Studio → Notebooks**.
2. Choose **Jupyter Notebook** → **Interactive Session**.
3. Select the IAM role created in Step 2 (`zingg-glue-role`) from the dropdown.
4. Open the notebook. Confirm the kernel in the top right shows **Glue PySpark**.

_**IMAGE TO BE ADDED — AWS Glue Studio Notebooks screen showing the IAM role dropdown with the\*\*\*\*****&#x20;****`zingg-glue-role`****&#x20;****\*\*\*\*selected. Tanwi to check with team for the screenshot.**_

### Notebook 01: Set up Zingg

Everything in this guide runs in a single Glue Studio notebook. The steps below follow the cell-by-cell execution order.

#### **Step 4: Configure the Glue session (run this cell first)**

This is the most important step for Glue. The `%%configure` magic cell must be **the very first cell you run** in a fresh notebook. It tells Glue to inject the Zingg JARs from S3 into every worker node and install the Zingg Python package before the Spark session starts. If you run any other cell first, the JARs will not be available.

```python
% glue_version 5.0
% worker_type G.1X
% number_of_workers 2
% idle_timeout 2880
%% configure {
  "--extra-jars": "s3://your-bucket/jars/zingg-0.6.0.jar,s3://your-bucket/jars/zingg-common-client-0.6.0.jar,s3://your-bucket/jars/zingg-common-core-0.6.0.jar,s3://your-bucket/jars/zingg-spark-client-0.6.0.jar,s3://your-bucket/jars/zingg-spark-core-0.6.0.jar,s3://your-bucket/jars/zingg-common-infra-0.6.0.jar",
    "--additional-python-modules": "zingg==0.6.0,tabulate,ipywidgets",
    "--conf": "spark.serializer=org.apache.spark.serializer.KryoSerializer"
}
```

Replace `your-bucket` with your actual S3 bucket name throughout.

{% hint style="success" icon="right-long" %}
`%%configure` must be the first cell executed. Running any other cell before this, including imports, starts the Glue session without the JARs. If this happens, stop the session (`%stop_session`) and start a fresh notebook.
{% endhint %}

#### **Step 5: Initialise Spark and Glue contexts**

AWS Glue requires a `GlueContext` alongside the standard Spark context. This is specific to Glue—other platforms do not need this.

```python
import sys
from awsglue.transforms import *
from awsglue.context import GlueContext
from pyspark.context import SparkContext
from awsglue.job import Job

sc = SparkContext.getOrCreate()
glueContext = GlueContext(sc)
spark = glueContext.spark_session
job = Job(glueContext)

print("Spark and Glue contexts initialized.")
```

Verify Java is available:

```python
print("Java Version:",
      sc._gateway.jvm.java.lang.System.getProperty("java.version"))
```

#### **Step 6: Set the S3 checkpoint directory**

Zingg uses a checkpoint directory to store intermediate Spark computation state. On Glue, this must be an S3 path - Glue workers are ephemeral and have no persistent local storage.

```python
checkpoint_path = "s3://your-bucket/zingg_checkpoint/"
sc.setCheckpointDir(checkpoint_path)

print(f"Spark checkpoint directory set to: {checkpoint_path}")
```

Verify it is set:

```python
spark.sparkContext.getCheckpointDir()
```

{% hint style="success" icon="right-long" %}
Because Glue workers are temporary, storing checkpoints in S3 ensures Zingg can recover from any worker interruption during long-running training or match phases. Without this, a worker restart causes the entire job to fail.
{% endhint %}

#### **Step 7: Set the model ID and S3 storage paths**

`BUCKET` is your S3 bucket name. `modelId` is the unique name for this model run. `zinggDir` is where Zingg writes model files and training data. Use the same values for every step in this notebook.

```python
BUCKET = "your-bucket-name"
modelId = "testModelFebrl"
zinggDir = f"s3://{BUCKET}/models"

MARKED_DIR = f"s3://{BUCKET}/models/{modelId}/trainingData/marked/"
UNMARKED_DIR = f"s3://{BUCKET}/models/{modelId}/trainingData/unmarked/"

print("S3 Paths Configured:")
print(f"Project Root:  {zinggDir}")
print(f"Marked Data:   {MARKED_DIR}")
print(f"Unmarked Data: {UNMARKED_DIR}")
```

`MARKED_DIR` and `UNMARKED_DIR` are derived automatically. Zingg writes candidate pairs to `UNMARKED_DIR` during `findTrainingData` and reads labeled pairs from `MARKED_DIR` during `train`.

#### Step 8: Import libraries and set up helper functions

```python
import pandas as pd
import numpy as np
import os, time, uuid, base64
from tabulate import tabulate
from ipywidgets import widgets, interact, GridspecLayout
import pyspark.sql.functions as fn

import boto3
from botocore.exceptions import ClientError

import zingg
from zingg.client import *
from zingg.pipes import *

s3_client = boto3.client('s3')

print("AWS and Zingg libraries imported successfully.")
```

Helper functions for managing training data and labels:

```python
def cleanModel():
    """Clears previous training data from S3
    to restart model learning from scratch."""
    try:
        prefixes = [
            f"models/{modelId}/trainingData/marked/",
            f"models/{modelId}/trainingData/unmarked/"
        ]
        for prefix in prefixes:
            paginator = s3_client.get_paginator('list_objects_v2')
            for page in paginator.paginate(Bucket=BUCKET, Prefix=prefix):
                if 'Contents' in page:
                    delete_keys = [
                        {'Key': obj['Key']}
                        for obj in page['Contents']]
                    s3_client.delete_objects(
                        Bucket=BUCKET,
                        Delete={'Objects': delete_keys})
        print("Model directories cleaned.")
    except Exception as e:
        print(f"Error cleaning model: {str(e)}")

def count_labeled_pairs(marked_pd):
    """Returns positive, negative, and total
    labeled pair counts."""
    if marked_pd.empty:
        return 0, 0, 0
    n_total    = len(np.unique(marked_pd['z_cluster']))
    n_positive = len(np.unique(
        marked_pd[marked_pd['z_isMatch'] == 1]['z_cluster']))
    n_negative = len(np.unique(
        marked_pd[marked_pd['z_isMatch'] == 0]['z_cluster']))
    return n_positive, n_negative, n_total
```

{% hint style="success" icon="right-long" %}
`boto3` is the AWS SDK for Python. It is used here for low-level S3 operations - scanning folders, reading labeled files, and deleting training data, that Spark cannot handle directly. `cleanModel()` uses a paginator to ensure all files are found even when a folder contains more than 1,000 objects.
{% endhint %}

#### **Step 9: Build the arguments object**

`Arguments` is the central configuration object. Every phase in this workflow reads from the same `args` instance.

```python
args = Arguments()
args.setModelId(modelId)
args.setZinggDir(zinggDir)
print(args)
```

#### **Step 10: Preview your data**

Read your CSV from S3 and preview it before configuring the Zingg pipes.

```python
csv_path = f"s3://{BUCKET}/test.csv"
spark_df = spark.read.csv(csv_path, header=True, inferSchema=True)
schema_list = [
  "id", "fname", "lname", "stNo", "add1", "add2", "city", "state",
  "areacode", "dob", "ssn"
]
spark_df = spark_df.toDF(*schema_list)

print(f"Previewing data from {csv_path}:")
spark_df.show(10)
```

_**IMAGE TO BE ADDED - Glue notebook cell showing the\*\*\*\*****&#x20;****`spark_df.show(10)`****&#x20;****\*\*\*\*output table with sample FEBRL records — the same entity appearing multiple times with field variations across rows. Tanwi to check with team for the screenshot.**_

#### **Step 11: Configure input and output pipes**

`CsvPipe` connects Zingg to your S3 data. The schema string must match your dataset column names exactly.

```python
schema = (
    "id string, fname string, "
    "lname string, stNo string, "
    "add1 string, add2 string, "
    "city string, state string, "
    "areacode string, dob string, "
    "ssn string"
)

inputPipe = CsvPipe("testFebrl", f"s3://{BUCKET}/test.csv", schema)
args.setData(inputPipe)

output_path = f"s3://{BUCKET}/results/"
outputPipe = CsvPipe("resultOutput", output_path)
outputPipe.addProperty("header", "true")
args.setOutput(outputPipe)

print("Input and output pipes configured.")
```

{% hint style="success" icon="right-long" %}
Zingg also supports Parquet and JSON output on S3. To push results downstream to Amazon Redshift, use the Redshift connector.

For all connector formats → [Connect Relational Databases](../connect-your-data/connect-relational-databases.md)
{% endhint %}

#### **Step 12: Define fields and match types**

Every field in your input schema must appear in `fieldDefinition`. List the most important fields first — field order affects blocking quality.

```python
id = FieldDefinition("id", "string", MatchType.EXACT)
fname = FieldDefinition("fname", "string", MatchType.FUZZY)
lname = FieldDefinition("lname", "string", MatchType.FUZZY)
stNo = FieldDefinition("stNo", "string", MatchType.FUZZY)
add1 = FieldDefinition("add1", "string", MatchType.FUZZY)
add2 = FieldDefinition("add2", "string", MatchType.FUZZY)
city = FieldDefinition("city", "string", MatchType.FUZZY)
state = FieldDefinition("state", "string", MatchType.FUZZY)
areacode = FieldDefinition("areacode", "string", MatchType.FUZZY)
dob = FieldDefinition("dob", "string", MatchType.EXACT)
ssn = FieldDefinition("ssn", "string", MatchType.EXACT)

fieldDefs = [
  id, fname, lname, stNo, add1, add2, city,
  state, areacode, dob,
  ssn
]
args.setFieldDefinition(fieldDefs)
```

{% hint style="success" icon="right-long" %}
`FUZZY` handles variations like 'Jon' vs 'John' or 'St' vs 'Street'. `EXACT` requires a character-for-character match. `DONT_USE` excludes a field from matching but keeps it in output.

**Read more**: For all match types → [Match Types](../zingg-concepts/how-zingg-learns/match-types/)
{% endhint %}

#### **Step 13: Configure performance settings**

`numPartitions` controls how data is distributed across Glue workers. `labelDataSampleSize` controls how much of the dataset is scanned when finding candidate pairs.

```python
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)
```

{% hint style="success" icon="right-long" %}
For a 2-worker G.1X cluster (4 vCPUs each), 4–8 partitions is a good starting point. Set `numPartitions` to approximately 2–3× your total worker vCPU count. For 1M+ records, reduce `labelDataSampleSize` to 0.01–0.05 to prevent the sampling phase from exhausting worker memory.
{% endhint %}

### Notebook 01 continued: Find training data and label pairs

{% hint style="success" icon="right-long" %}
AWS Glue Interactive Sessions restrict standard Jupyter widgets. Zingg's standard `ipywidgets` labeling interface does not render in Glue notebooks. Instead, this guide exports candidate pairs as a CSV to S3 for offline review. You label each pair by entering 0, 1, or 2 in a spreadsheet, upload the file back to S3, and run a sync cell to feed the labels back into Zingg. The process is covered in Steps 15 and 16.
{% endhint %}

#### **Step 14: Find candidate pairs**

Zingg scans your dataset using the field rules defined in Step 12 and selects the most informative pairs for labeling. Candidate pairs are saved to `UNMARKED_DIR` in your S3 bucket.

```python
options = ClientOptions([
    ClientOptions.PHASE, "findTrainingData"])
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()
```

#### **Step 15: Export candidate pairs for offline labeling**

Initialise the label phase and pull candidate pairs into a local Pandas DataFrame:

```python
options = ClientOptions([
    ClientOptions.PHASE, "label"])
zingg = ZinggWithSpark(args, options)
zingg.init()

candidate_pairs_pd = getPandasDfFromDs(
    zingg.getUnmarkedRecords()
)

if candidate_pairs_pd.shape[0] == 0:
    print("No pairs found. Run findTrainingData first.")
else:
    z_clusters = list(np.unique(
        candidate_pairs_pd['z_cluster']
    ))
    print(f"{len(z_clusters)} candidate pairs found for labeling")
```

Because standard Jupyter widgets do not work in Glue, export the candidate pairs as a vertical CSV to S3 for offline review. Each pair appears as a side-by-side block with a `>>> DECISION` row at the bottom where you enter `0` (No Match), `1` (Match), or `2` (Uncertain).

```python
import pandas as pd, base64

ready_for_save = False
LABELS = {'Uncertain': 2, 'Match': 1, 'No Match': 0}

n_pairs    = int(candidate_pairs_pd.shape[0] / 2)
display_pd = candidate_pairs_pd.drop(
    labels=['z_zid', 'z_prediction',
        'z_score', 'z_isMatch', 'z_zsource'],
    axis=1, errors='ignore')

vertical_review_data = []

for n in range(n_pairs):
    candidate_left  = display_pd.loc[2*n].to_list()
    candidate_right = display_pd.loc[(2*n)+1].to_list()

    vertical_review_data.append({
        "Attribute": f"=== PAIR {n} ===",
        "Record_A": "VALUE A",
        "Record_B": "VALUE B"})

    for i in range(display_pd.shape[1]):
        column_name = display_pd.columns[i]
        if column_name == 'z_cluster':
            z_cluster = candidate_left[i]
        vertical_review_data.append({
            "Attribute": column_name,
            "Record_A": str(candidate_left[i]),
            "Record_B": str(candidate_right[i])})

    label_options = ", ".join(
        [f"{k}({v})" for k, v in LABELS.items()])
    vertical_review_data.append({
        "Attribute": ">>> DECISION",
        "Record_A": f"Choose: {label_options}",
        "Record_B": ""})
    vertical_review_data.append({
        "Attribute": "-" * 20,
        "Record_A": "", "Record_B": ""})

review_df    = pd.DataFrame(vertical_review_data)
export_path  = f"s3://{BUCKET}/review/pending_labels.csv"
spark.createDataFrame(review_df).coalesce(1)\
    .write.option("header", "true")\
    .mode("overwrite").csv(export_path)

ready_for_save = True
print(f"Review sheet exported for {n_pairs} pairs to: {export_path}")
```

_**IMAGE TO BE ADDED — S3 console showing the\*\*\*\*****&#x20;****`/review/`****&#x20;****folder with the exported****&#x20;****`pending_labels.csv`****&#x20;****\*\*\*\*part file ready for download. Tanwi to check with team for the screenshot.**_

{% hint style="success" icon="right-long" %}
How to label the review sheet:

1. Go to **S3 → your-bucket → review/** in the AWS Console and download the `part-00000-*.csv` file.
2. Open it in Excel or Google Sheets.
3. For each pair block, find the `>>> DECISION` row and enter your label in the `Record_B` column: `1` (Match), `0` (No Match), or `2` (Uncertain).
4. Save the file and upload it back to the same S3 path: `s3://your-bucket/review/`
5. Run Step 16 to feed the labels into Zingg.
{% endhint %}

_**IMAGE TO BE ADDED — Example of the exported review CSV open in Excel showing two FEBRL records side by side in a vertical layout, with the\*\*\*\*****&#x20;****`>>> DECISION`****&#x20;****row highlighted and a****&#x20;****`1`****&#x20;****entered in the****&#x20;****`Record_B`****&#x20;****\*\*\*\*column. Tanwi to check with team for the screenshot.**_

{% hint style="success" icon="right-long" %}
Target 30–40 match pairs and 30–40 non-match pairs before training. Repeat Steps 14–16 in a loop until you reach this target. Label until all field types and data variation patterns in your schema are covered. If accuracy needs improvement after the first match run, return to labeling and focus on patterns that are missing or underrepresented.
{% endhint %}

#### **Step 16: Sync labeled pairs back to Zingg**

After labeling the review sheet and uploading it back to S3, run this cell to read the labels and save them to the Zingg training data folder.

```python
import pandas as pd, boto3, io

if not ready_for_save:
    print("Run the export cell first.")
else:
    bucket_name = BUCKET
    prefix      = "review/"
    s3_client   = boto3.client('s3')

    # Auto-locate the CSV part file in the review folder
    response = s3_client.list_objects_v2(
        Bucket=bucket_name, Prefix=prefix)

    csv_key = None
    if 'Contents' in response:
        for obj in response['Contents']:
            if obj['Key'].endswith('.csv'):
                csv_key = obj['Key']
                break

    if not csv_key:
        print(f"No CSV found in s3://{bucket_name}/{prefix}")
    else:
        print(f"Reading labels from: s3://{bucket_name}/{csv_key}")

        file_obj = s3_client.get_object(
            Bucket=bucket_name, Key=csv_key
        )
        labeled_df = pd.read_csv(
            io.BytesIO(file_obj['Body'].read()),
            sep=None, engine='python'
        )
        labeled_df.columns = (
            ['Attribute', 'Record_A', 'Record_B']
            + list(labeled_df.columns[3:])
        )

        print("Mapping labels to Zingg DataFrame...")

        for i in range(len(labeled_df)):
            attr_val = str(labeled_df.iloc[i]['Attribute'])
            if "=== PAIR" in attr_val:
                pair_idx     = int(attr_val.split(" ")[2])
                decision_idx = i + len(display_pd.columns) + 1
                if decision_idx < len(labeled_df):
                    user_input = labeled_df.iloc[
                        decision_idx
                    ]['Record_B']
                    if (pd.notna(user_input)
                            and str(user_input).strip() != ""):
                        label_int = int(float(user_input))
                        target_cluster = candidate_pairs_pd.iloc[
                            2*pair_idx
                        ]['z_cluster']
                        candidate_pairs_pd.loc[
                            candidate_pairs_pd['z_cluster'] == target_cluster,
                            'z_isMatch'] = label_int

        zingg.writeLabelledOutputFromPandas(
            candidate_pairs_pd, args
        )

        marked_pd_df = getPandasDfFromDs(
            zingg.getMarkedRecords()
        )
        n_pos, n_neg, n_tot = count_labeled_pairs(marked_pd_df)

        print(f"Labels synchronized successfully.")
        print(f"Total labeled: {n_tot} | Matches: {n_pos} | Non-matches: {n_neg}")
        print("Run Steps 14-16 again if you need more pairs.")
        ready_for_save = False
```

### Notebook 01 continued: Generate model documentation (optional)

#### **Step 17: Generate model documentation**

Run `generateDocs` after labeling to produce a model report showing field weights, training data quality, and precision and recall estimates. The HTML report is written to your S3 model directory and can be downloaded from the S3 console.

```python
options = ClientOptions([
    ClientOptions.PHASE, "generateDocs"])
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()

DOCS_DIR = zinggDir + "/" + modelId + "/docs/"
s3_key   = f"models/{modelId}/docs/model.html"

s3_client = boto3.client('s3')
try:
    s3_client.head_object(Bucket=BUCKET, Key=s3_key)
    print(f"Model report is ready.")
    print(f"Download from: s3://{BUCKET}/{s3_key}")
except Exception as e:
    print(f"Documentation not found at expected S3 path. {e}")
```

{% hint style="success" icon="right-long" %}
Unlike other platforms, Glue cannot render HTML inline in the notebook. Download `model.html` from the S3 console and open it in a browser to view the documentation. Navigate to **S3 → your-bucket → models → modelId → docs** to find the file.

`generateDocs` is optional. Skip it if you have 30–40 matches and 30–40 non-matches and are confident in your labeling quality.
{% endhint %}

### Notebook 01 continued: Train and match

#### **Step 18: Train and match**

`trainMatch` combines `train` and `match` into a single phase. Zingg builds a model from your labeled pairs and immediately applies it to the full dataset. This is the most compute-intensive step; it distributes the workload across all Glue workers.

```python
options = ClientOptions([
    ClientOptions.PHASE, "trainMatch"])
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()

print(f"Training complete. Results saved to: s3://{BUCKET}/results/")
```

You can also run `train` and `match` as separate phases if you want to inspect the trained model before running the full dataset:

```python
options = ClientOptions([ClientOptions.PHASE, "train"])
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()

options = ClientOptions([ClientOptions.PHASE, "match"])
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()
```

#### Step 19: View output

Match output is written to `output_path` in your S3 bucket as distributed part files. Spark reads the entire folder and merges them automatically.

```python
outputDF = spark.read.option(
    "header", "false"
).csv(output_path)

colNames = [
    "z_score", "z_cluster", "z_zid",
    "id", "fname", "lname", "stNo",
    "add1", "add2", "city", "state",
    "areacode", "dob", "ssn"
]

final_results = outputDF.toDF(*colNames)
final_results.orderBy("z_cluster").show(10, truncate=False)

total_records = final_results.count()
unique_entities = final_results.select(
    'z_cluster'
).distinct().count()

print(f"Total Records Processed: {total_records}")
print(f"Unique Entities Identified: {unique_entities}")
print(f"Redundancy Reduced by: "
    f"{((total_records - unique_entities) / total_records) * 100:.2f}%")
```

_**IMAGE TO BE ADDED — Glue notebook cell showing\*\*\*\*****&#x20;****`final_results.orderBy("z_cluster").show(10)`****&#x20;****output with resolved records grouped by****&#x20;****`z_cluster`****&#x20;****\*\*\*\*— two rows sharing the same cluster value visible in the output. Tanwi to check with team for the screenshot.**_
{% endtab %}

{% tab title="Enterprise" %}

{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
**Read more**:

* Tune accuracy → [Improve Accuracy](../tuning/improve-accuracy/)
* Understand scores and set thresholds → [Interpret Output Scores](../interpreting-results/interpret-output-scores.md)
* Set up incremental for production → [Run Incremental Matching](../running-zingg/run-incremental-matching.md)
{% endhint %}

{% hint style="success" icon="right-long" %}
Download the notebooks used in this guide:

* Community notebooks (NB01–04): Download the notebook used in this guide: `github.com/zinggAI/zingg/tree/main/examples/aws-glue`
* Enterprise notebooks — TO BE ADDED
{% endhint %}
