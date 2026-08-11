---
description: >-
  End-to-end guide to running Zingg on Azure Databricks from cluster setup to
  your first match result. Covers Community (open source) and Enterprise
---

# Platform Guide for Azure Databricks

Databricks is a fully managed Spark environment that integrates seamlessly with Zingg. Both Community and Enterprise run on the same Databricks cluster. The difference is the libraries, class names, and the additional phases available in Enterprise.

{% hint style="success" icon="right-long" %}
Tested with Databricks Runtime 16.4 LTS (Spark 3.5.2, Scala 2.12). Newer LTS versions with Spark 3.5 are compatible.
{% endhint %}

{% hint style="success" icon="right-long" %}
**Read more**: For the Databricks connector config including Delta format, see [Connect Azure Databricks](../connect-your-data/connect-cloud-warehouses/connect-azure-databricks.md).
{% endhint %}

{% tabs %}
{% tab title="Community" %}
Uses `Arguments`, `FieldDefinition`, `CsvPipe`, and `ZinggWithSpark`. The workflow runs across three notebooks.

### Notebook 01: Set up Zingg

#### Step 1: Create a cluster and install the Zingg JAR

1. Go to **Compute** → **Create Cluster**. Name it `Zingg-Community`.
2. Set the runtime to a current LTS version for compatibility.
3. Download the latest Zingg JAR from `github.com/zinggAI/zingg/releases`.
4. Open the cluster → **Libraries** → **Install New** → **Upload JAR** → upload the file.

#### **Step 2: Install the Zingg Python package**

Open a notebook attached to the cluster and run:

```python
%pip install zingg==0.6.0
dbutils.library.restartPython()
```

**Verify the installation**

```python
%pip show zingg
```

#### **Step 3: Set the model ID and storage path**

`zinggDir` is where Zingg writes model files and training data. `modelId` is a unique name for this model run - Zingg uses it as the folder name under `zinggDir`. Use the same values across all notebooks in this workflow.

```python
zinggDir = "/models"
modelId = "zinggTrial"
```

Update `zinggDir` to an `abfss://` or `s3a://` path if you want model files stored in cloud storage. `dbfs:/` paths are deprecated in Databricks—use Unity Catalog storage paths or external cloud storage instead.

```python
MARKED_DIR = zinggDir + "/" + modelId + "/trainingData/marked/"
```

`MARKED_DIR` and `UNMARKED_DIR` are derived from your `zinggDir` and `modelId`. Zingg writes labeled training pairs to these paths during the `label` phase and reads them back during `train`.

#### Step 4: Import libraries and set up helper functions

```python
import pandas as pd
import numpy as np
import time
import uuid
from ipywidgets import widgets, interact, GridspecLayout
import base64
import pyspark.sql.functions as fn

from zingg.client import *
from zingg.pipes import *

def count_labeled_pairs(marked_pd):
    n_total = len(np.unique(marked_pd['z_cluster']))
    n_positive = len(np.unique(marked_pd[marked_pd['z_isMatch'] == 1]['z_cluster']))
    n_negative = len(np.unique(marked_pd[marked_pd['z_isMatch'] == 0]['z_cluster']))
    n_uncertain = len(np.unique(marked_pd[marked_pd['z_isMatch'] == 2]['z_cluster']))
    return n_positive, n_negative, n_uncertain, n_total
```

#### Step 5: Build the arguments object

`Arguments` is the central configuration object. Every subsequent phase reads from the same `args` instance.

```python
args = Arguments()
args.setModelId(modelId)
args.setZinggDir(zinggDir)
```

#### **Step 6: Configure performance settings**

`numPartitions` controls how data is distributed across Spark workers. Set it to approximately 20–30× your worker vCPU count. `labelDataSampleSize` controls what fraction of the data is scanned when finding candidate pairs - reduce it if `findTrainingData` is slow.

```python
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)

spark.conf.set("spark.sql.adaptive.enabled", False)
```

{% hint style="success" icon="right-long" %}
For 100k records start with `labelDataSampleSize` between 0.1 and 0.5. For 1M+ records use 0.01 to 0.05. If `findTrainingData` takes too long, reduce by approximately 10× and try again. Disabling Adaptive Query Execution gives Zingg more predictable Spark behavior.
{% endhint %}

#### Step 7: Connect your data

The OS notebook uses `UCPipe` to connect to a Unity Catalog table. Replace `table` with your own `catalog.schema.tablename`. If your data is in a CSV file, use `CsvPipe` instead — see the hint below.

```python
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)

spark.conf.set("spark.sql.adaptive.enabled", False)
table = "zingg_catalog.input.test"
inputPipe = UCPipe("testFebrl65", table)
args.setData(inputPipe)
```

**Preview the Data**

```python
df = spark.table(table)
display(df)
```

If your data is in a CSV file, use `CsvPipe` instead of `UCPipe`:

```python
schema = (
    "rec_id string, fname string, "
    "lname string, stNo string, "
    "add1 string, add2 string, "
    "city string, state string, "
    "dob string, ssn string"
)

inputPipe = CsvPipe("testFebrl", "/FileStore/tables/data.csv", schema)
args.setData(inputPipe)
```

Sample data to test with: `github.com/zinggAI/zingg/blob/main/examples/febrl120k/test.csv`

#### Step 8: Configure output

```python
outputTable = "zingg_catalog.output.febrlOutput"
outputPipe = UCPipe("resultFebrl", outputTable)
args.setOutput(outputPipe)
```

Output can also be a CSV or Parquet file. For all supported output formats → [Connect Azure Databricks](../connect-your-data/connect-cloud-warehouses/connect-azure-databricks.md).

#### **Step 9: Define fields and match types**

The order in which you list fields matters—put the most important fields first. Every field in your input schema must appear in `fieldDefinition`, either with a match type or as `DONT_USE`.

```python
recId = FieldDefinition("recId", "STRING", MatchType.DONT_USE)
fName = FieldDefinition("fName", "STRING", MatchType.FUZZY)
lName = FieldDefinition("lName", "STRING", MatchType.FUZZY)
streetId = FieldDefinition("streetId", "STRING", MatchType.DONT_USE)
street = FieldDefinition("street", "STRING", MatchType.FUZZY)
locality = FieldDefinition("locality", "STRING", MatchType.FUZZY)
area = FieldDefinition("area", "STRING", MatchType.FUZZY)
areaCode = FieldDefinition("areaCode", "STRING", MatchType.FUZZY)
state = FieldDefinition("state", "STRING", MatchType.FUZZY)
dob = FieldDefinition("dob", "STRING", MatchType.FUZZY)
ssn = FieldDefinition("ssn", "STRING", MatchType.EXACT)

args.setFieldDefinition([
    recId, fName, lName, streetId, street,
    locality, area, areaCode, state, dob,
    ssn
])
```

{% hint style="success" icon="right-long" %}
`FUZZY` handles variations like 'Jon' vs 'John' or 'St' vs 'Street'. `EXACT` requires a character-for-character match. `DONT_USE` excludes a field from matching but keeps it in the output - use this for identifiers like record IDs. For all match types → [Match Types](../zingg-concepts/how-zingg-learns/match-types/)
{% endhint %}

### Notebook 02: Find training data and label pairs

#### **Step 10: Find candidate pairs**

Zingg scans your data and selects the most informative pairs for labeling—edge cases where the model has the most to learn. Run this before labeling.

```python
options = ClientOptions([ ClientOptions.PHASE, "findTrainingData" ])
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()
```

#### Step 11: Load pairs for labeling

```python
options = ClientOptions([ ClientOptions.PHASE, "label" ])
zingg = ZinggWithSpark(args, options)
zingg.init()

candidate_pairs_pd = getPandasDfFromDs(zingg.getUnmarkedRecords())

if candidate_pairs_pd.shape[0] == 0:
    print("No pairs found. Run findTrainingData first.")
else:
    z_clusters = list(np.unique(candidate_pairs_pd['z_cluster']))
    print(f"{len(z_clusters)} candidate pairs found for labeling")
```

#### Step 12: Label pairs in the widget

A widget displays each candidate pair side by side. For each pair, select:

* **Match**—these records represent the same real-world entity
* **No Match**—these records are different entities
* **Uncertain**—you cannot decide

The widget code handles the display and state management. Run the cell to render it.

_**IMAGE TO BE ADDED — Zingg labeling widget in a Databricks notebook showing two candidate records side by side with Match / No Match / Uncertain toggle buttons. Tanwi to check with team for a screenshot from a live notebook run.**_

{% hint style="success" icon="right-long" %}
Target 30–40 match pairs and 30–40 non-match pairs before training. Repeat Steps 10–12 until you reach this target. Label until all field types and data variation patterns in your schema are covered. If results need improvement after the first match run, return to labeling and focus on patterns that are missing or underrepresented.
{% endhint %}

#### Step 13: Save labeled pairs

After labeling pairs in the widget, run this cell to save your labels to the training data folder.

```python
zingg.writeLabelledOutputFromPandas(candidate_pairs_pd, args)

marked_pd_df = getPandasDfFromDs(zingg.getMarkedRecords())
n_pos, n_neg, n_uncer, n_tot = count_labeled_pairs(marked_pd_df)
print(f"Out of total {n_tot} pairs,")
print(f"You have accumulated {n_pos} pairs labeled as positive matches.")
print(f"You have accumulated {n_neg} pairs labeled as not matches.")
print(f"You have accumulated {n_uncer} pairs labeled as uncertain.")
```

### Notebook 03: Train, match, and view output

#### Step 14: Review labeled pairs before training

Before training, review the pairs you have labeled to verify quality. This uses the `updateLabel` phase to surface your marked records for inspection.

```python
options = ClientOptions([ ClientOptions.PHASE, "updateLabel" ])
zingg = ZinggWithSpark(args, options)
zingg.init()

markedRecords = getPandasDfFromDs(zingg.getMarkedRecords())
display(markedRecords)
```

#### **Step 15: Train the model**

Zingg builds the blocking and similarity models from your labeled pairs and persists them to `zinggDir/modelId`. Once trained, this model can be reused on new data without retraining.

```python
options = ClientOptions([ ClientOptions.PHASE, "train" ])
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()
```

#### **Step 16: Run the match**

Applies the trained model to your full dataset and writes resolved clusters to the output location configured in Step 8.

```python
options = ClientOptions([ ClientOptions.PHASE, "match" ])
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()
```

#### Step 17: View output

```python
df = spark.table(outputTable)
display(df)
print(df.count())
```

_**IMAGE TO BE ADDED — match output table in Databricks showing resolved records with\*\*\*\*****&#x20;****`Z_CLUSTER`****&#x20;****column visible alongside original fields. Ideally highlight two rows sharing the same****&#x20;****`Z_CLUSTER`****&#x20;****\*\*\*\*to show they have been resolved to the same entity. Tanwi to check with team for screenshot from a live notebook run.**_

{% hint style="success" icon="right-long" %}
Records sharing the same `Z_CLUSTER` value have been resolved to the same real-world entity. `Z_MINSCORE` is the weakest match confidence within the cluster. `Z_MAXSCORE` is the strongest. For full output column definitions → [Interpret Output Scores](../interpreting-results/interpret-output-scores.md).
{% endhint %}

### Notebook 04: Generate model documentation (optional)

Run `generateDocs` after labeling to produce readable HTML documentation of your training data—both matched and non-matched pairs. Run it before training to inspect data quality, or share the output with subject matter experts to validate labels before committing to train.

```python
options = ClientOptions([ ClientOptions.PHASE, "generateDocs" ])
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()

DOCS_DIR = zinggDir + "/" + modelId + "/docs/"
displayHTML(open(DOCS_DIR + "model.html", 'r').read())
```

_**IMAGE TO BE ADDED —****&#x20;****`generateDocs`****&#x20;****HTML output rendered inside a Databricks notebook showing labeled pair examples. Tanwi to check with team for screenshot from a live notebook run. Even a small portion of the rendered HTML is sufficient — it tells the reader what to expect before they run it. Place: below the****&#x20;****`displayHTML`****\*\*\*\*\*\*\*\* \*\*\*\*line.**_
{% endtab %}

{% tab title="Enterprise" %}
Uses `EArguments`, `EFieldDefinition`, `ECsvPipe`, `EZinggWithSpark`. Seven notebooks, each covering one phase.

Enterprise adds blocking model configuration, a primary key for incremental matching, stats output, deterministic matching rules, pass-through expressions, the `runIncremental` phase, and the `explain` phase.

{% hint style="warning" icon="right-long" %}
Enterprise requires a Zingg licence and the `zinggEC` and `zinggES` packages. [Contact Zingg to get access](https://www.zingg.ai/company/contact/contact).
{% endhint %}

### Notebook 01: Set up Zingg

#### Step 1: Create a cluster and install Enterprise JARs

1. Go to **Compute** → **Create Cluster**. Name it `Zingg-Enterprise`.
2. Set the runtime to a current LTS version.
3. Create a managed Volume inside your catalog schema.
4. Upload `zingg-enterprise-spark-0.6.0.jar` and `zingg_license.jar` to the Volume.
5. Open the cluster → **Libraries** → **Install New** → **Volumes** → navigate to: `/Volumes/catalog_name/schema_name/volume_name/zingg-enterprise-spark-0.6.0.jar`
6. Repeat for `zingg_license.jar`.

_**IMAGE TO BE ADDED — Databricks cluster Libraries tab showing the Enterprise JAR files installed from a Volume path. Tanwi to check with team for screenshot from a live Enterprise cluster setup.**_

#### Step 2: Verify all three packages are installed

```python
!pip show zingg
!pip show zinggEC
!pip show zinggES
```

{% hint style="info" icon="right-long" %}
All three must show as installed: `zingg` (Community base), `zinggEC` (Enterprise), `zinggES` (Enterprise Plus). If any show as not found, install the corresponding `.whl` file from the cluster Libraries tab using the wheels provided in your Enterprise package.
{% endhint %}

#### Step 3: Set the checkpoint directory

```python
spark.sparkContext.setCheckpointDir("Files")
```

#### Step 4: Import libraries

```python
import pandas as pd
import numpy as np
import os, time, uuid
from ipywidgets import widgets, interact, GridspecLayout
import base64
import pyspark.sql.functions as fn
from zinggEC.enterprise.common.ApproverArguments import *
from zinggEC.enterprise.common.IncrementalArguments import *
from zinggEC.enterprise.common.epipes import *
from zinggEC.enterprise.common.EArguments import *
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggES.enterprise.spark.ESparkClient import *
from zingg.client import *
from zingg.pipes import *
```

#### Step 5: Set the model ID and storage path

`files_dir` is the root path for all input, output, and model storage. `zingg_dir` is where Zingg writes model files. `model_id` is the unique name for this run.

```python
files_dir = (
    "abfss://<workspace-id>@onelake.dfs.fabric.microsoft.com/<lakehouse-id>/"
    "Files"
)
zingg_dir = files_dir + "/zingg"
model_id = "zinggModel"
MARKED_DIR = zingg_dir + "/" + model_id + "/trainingData/marked/"
UNMARKED_DIR = zingg_dir + "/" + model_id + "/trainingData/unmarked/"
```

Replace `files_dir` with your actual Databricks external storage path (`abfss://` for ADLS or `s3a://` for S3). `dbfs:/` paths are deprecated—use external storage.

#### **Step 6: Build the Enterprise arguments object**

`EArguments` is the Enterprise equivalent of `Arguments`. `setBlockingModel` sets the blocking strategy. `DEFAULT` suits most datasets—use `WIDER` if you know matching pairs are being missed.

```python
args = EArguments()
args.setModelId(model_id)
args.setZinggDir(zingg_dir)
args.setBlockingModel("DEFAULT")
```

#### Step 7: Configure performance settings

```python
args.setNumPartitions(32)
spark.conf.set("spark.sql.adaptive.enabled", False)
```

{% hint style="info" icon="right-long" %}
Set `numPartitions` to approximately 20–30× your worker vCPU count. For a 4-node cluster with 8 vCPUs each, 32 is a good starting point. `labelDataSampleSize` is set in Notebook 03—see that notebook for guidance.
{% endhint %}

#### **Step 8: Connect your data**

Enterprise uses `ECsvPipe` for CSV and Parquet, or `UCPipe` for Unity Catalog tables.

```python
schema = (
    "id string, fname string, lname string, "
    "streetId string, street string, locality string, "
    "area string, areacode string, state string, "
    "dob string, ssn string"
)

input_path = files_dir + "/input/your_data.csv"
inputPipe = ECsvPipe("testFebrl", input_path, schema)
args.setData(inputPipe)
```

**Preview your data**

```python
data = spark.read.csv(input_path, header=True)
display(data)
```

#### Step 9: Configure output

```python
output_path = files_dir + "/output/" + model_id
outputPipe = ECsvPipe("resultOutput", output_path)
outputPipe.addProperty("header", "true")
args.setOutput(outputPipe)
```

#### **Step 10: Configure stats output**

Stats output is an Enterprise feature. Zingg replaces `$ZINGG_DYNAMIC_STAT_NAME` at runtime with `SUMMARY`, `CLUSTER`, or `RECORD`, writing three separate stats files per run.

```python
stats_path = (files_dir + "/statsOutput$ZINGG_DYNAMIC_STAT_NAME")
statsOutputPipe = ECsvPipe("stats", stats_path)
statsOutputPipe.addProperty("header", "true")
args.setOutputStats(statsOutputPipe)
```

{% hint style="success" icon="right-long" %}
**Read more:** If `outputStats` is not configured, Zingg skips stats writing and the run proceeds normally. For stats field definitions → [Output Statistics](../interpreting-results/output-statistics.md).
{% endhint %}

#### **Step 11: Define fields with `EFieldDefinition`**

`EFieldDefinition` is the Enterprise equivalent of `FieldDefinition`. Mark your primary key field with `setPrimaryKey(True)` — this is required for `runIncremental` to identify records across runs. Put the most important fields first.

```python
recId = EFieldDefinition("id", "string", MatchType.DONT_USE)
recId.setPrimaryKey(True)
fname = EFieldDefinition("fname", "string", MatchType.FUZZY)
lname = EFieldDefinition("lname", "string", MatchType.FUZZY)
streetId = EFieldDefinition("streetId", "string", MatchType.FUZZY)
street = EFieldDefinition("street", "string", MatchType.FUZZY)
locality = EFieldDefinition("locality", "string", MatchType.FUZZY)
area = EFieldDefinition("area", "string", MatchType.FUZZY)
areacode = EFieldDefinition("areacode", "string", MatchType.FUZZY)
state = EFieldDefinition("state", "string", MatchType.FUZZY)
dob = EFieldDefinition("dob", "string", MatchType.FUZZY)
ssn = EFieldDefinition("ssn", "string", MatchType.FUZZY)

fieldDefs = [
    recId, fname, lname, streetId, street,
    locality, area, areacode, state, dob,
    ssn
]
args.setFieldDefinition(fieldDefs)
```

#### **Step 12: Deterministic matching and pass-through (optional)**

Deterministic matching short-circuits probabilistic scoring — records matching all fields in a condition are always matched regardless of similarity scores. Pass-through excludes specific records from matching while still including them in output with their own Zingg ID.

**Deterministic conditions**

```python
dm1 = DeterministicMatching('fname', 'streetId', 'area')
dm2 = DeterministicMatching('fname', 'streetId', 'lname')
args.setDeterministicMatchingCondition(dm1, dm2)
```

**Pass-through expression**

```python
args.setPassthroughExpr("fname = 'matilda'")
```

{% hint style="success" icon="right-long" %}
**Read more**:

* For deterministic matching concepts → [Configure Zingg](../running-zingg/configure-zingg.md)
* For pass-through → [Pass Through](../zingg-concepts/pass-through.md)
{% endhint %}

### Notebook 02: Remove stopwords (Optional)

Stopwords are high-frequency words that appear across many records but carry no matching signal — words like `pvt`, `ltd`, `st`, `ave`. Removing them improves blocking and similarity accuracy on address and company name fields.

#### **Step 13: Generate stopword recommendations**

Zingg analyses the specified column and returns a list of high-frequency words it recommends treating as stopwords. Run this once per column you want to clean.

```python
stopwordcolumn = "street"
args.setColumn(stopwordcolumn)
options = ClientOptions([ ClientOptions.PHASE, "recommend" ])
zingg = EZinggWithSpark(args, options)
zingg.initAndExecute()
```

#### Step 14: Review recommendations

```python
stopwordsForStreet = spark.read.csv(
    zingg_dir + "/" + model_id + "/stopWords/" + stopwordcolumn
)
stopwordsForStreet.show()
```

_**IMAGE TO BE ADDED — Databricks notebook showing the stopwords output table with word and frequency columns. Tanwi to check with team for screenshot from a live notebook run. A simple table with 10–15 rows is sufficient that tells the reader what the recommendation output looks like before they run it.**_

#### **Step 15: Apply stopwords to the field definition**

Review the recommendations. You can use them as-is or edit the list — add or remove words that matter for your specific dataset.

```python
street.setStopWords(zingg_dir + "/" + model_id + "/stopWords/" + stopwordcolumn)

print(args.getArgs())
```

{% hint style="success" icon="right-long" %}
**Read more:** Skip this notebook on the first run. Return to it if match accuracy on text-heavy fields needs improvement. For the full stopwords guide → [Remove Stopwords](../tuning/improve-accuracy/remove-stopwords-optional.md)
{% endhint %}

### Notebook 03: Find training data and label pairs

#### **Step 16: Set `labelDataSampleSize`**

`labelDataSampleSize` controls how much of your data is scanned when finding candidate pairs.

```python
args.setLabelDataSampleSize(0.1)
```

{% hint style="info" icon="right-long" %}
For 100k records use 0.1–0.5. For 1M records use 0.01–0.05. If `findTrainingData` takes too long, reduce by approximately 10× and try again.
{% endhint %}

#### Step 17: Find candidate pairs

```python
options = ClientOptions([ ClientOptions.PHASE, "findTrainingData" ])
zingg = EZinggWithSpark(args, options)
zingg.initAndExecute()
```

#### Step 18: Load pairs for labeling

```python
options = ClientOptions([ ClientOptions.PHASE, "label" ])
zingg = EZinggWithSpark(args, options)
zingg.init()

candidate_pairs_pd = getPandasDfFromDs(zingg.getUnmarkedRecords())

if candidate_pairs_pd.shape[0] == 0:
    print("No pairs found. Run findTrainingData first.")
else:
    z_clusters = list(np.unique(candidate_pairs_pd['z_cluster']))
    print(f"{len(z_clusters)} candidate pairs found for labeling")
```

#### Step 19: Label pairs in the widget

The Enterprise widget shows one pair at a time with Prev and Next navigation. For each pair select `Match`, `No Match`, or `Uncertain`. Labels are saved directly to the `DataFrame` as you click.

_**IMAGE TO BE ADDED — Enterprise labeling widget in a Databricks notebook: two records displayed in a table, Match / No Match / Uncertain toggle buttons, Prev and Next navigation. Tanwi to check with team for screenshot from a live Enterprise notebook run. If the OS and Enterprise widgets look identical, the same screenshot can be reused.**_

{% hint style="info" icon="right-long" %}
Target 30–40 match pairs and 30–40 non-match pairs before training. Repeat Steps 17–19 until all field types and data patterns are represented. If accuracy needs improvement after the first match run, return here and focus on patterns that are underrepresented.
{% endhint %}

#### Step 20: Save labeled pairs

```python
zingg.writeLabelledOutputFromPandas(candidate_pairs_pd, args)

marked_pd_df = getPandasDfFromDs(zingg.getMarkedRecords())
n_pos, n_neg, n_uncer, n_tot = count_labeled_pairs(marked_pd_df)
print(f"Out of total {n_tot} pairs,")
print(f"You have accumulated {n_pos} pairs labeled as positive matches.")
print(f"You have accumulated {n_neg} pairs labeled as not matches.")
print(f"You have accumulated {n_uncer} pairs labeled as uncertain.")
```

### Notebook 04: Generate model documentation

Run `generateDocs` after labeling and before training to inspect training data quality. Produces readable HTML documentation of your labeled pairs—both matches and non-matches.

```python
options = ClientOptions([
    ClientOptions.PHASE, "generateDocs"])
zingg = EZinggWithSpark(args, options)
zingg.initAndExecute()

DOCS_DIR = zingg_dir + "/" + model_id + "/docs/"

model_doc = spark.read.text(DOCS_DIR + "model/part-*")
model_html = "\n".join(r.value for r in model_doc.collect())
displayHTML(model_html)

data_doc = spark.read.text(DOCS_DIR + "data/part-*")
data_html = "\n".join(r.value for r in data_doc.collect())
displayHTML(data_html)
```

_**IMAGE TO BE ADDED —****&#x20;****`generateDocs`****\*\*\*\* \*\*\*\*output rendered inside a Databricks notebook showing labeled pair examples in HTML. Tanwi to check with team for screenshot from a live notebook run. Can reuse the OS version if the output looks the same.**_

### Notebook 05: Train and match

#### **Step 21: Train the model**

```python
options = ClientOptions([ ClientOptions.PHASE, "train" ])
zingg = EZinggWithSpark(args, options)
zingg.initAndExecute()
```

#### Step 22: Run the match

```python
options = ClientOptions([ ClientOptions.PHASE, "match" ])
zingg = EZinggWithSpark(args, options)
zingg.initAndExecute()
```

#### Step 23: View output

```python
outputDF = spark.read.csv(output_path, header=True)
display(outputDF)
print(outputDF.count())
```

_**IMAGE TO BE ADDED — Enterprise match output in Databricks showing\*\*\*\*****&#x20;****`ZINGG_ID`****&#x20;****column alongside resolved records. Show two rows with the same****&#x20;****`ZINGG_ID`****&#x20;****\*\*\*\*to illustrate entity resolution. Tanwi to check with team for screenshot from a live notebook run.**_

{% hint style="success" icon="right-long" %}
**Read more**: Enterprise output includes `ZINGG_ID` — a globally unique, persistent identifier for each resolved entity. Unlike `Z_CLUSTER` in Community, `ZINGG_ID` does not change between runs including incremental runs.

* For output column definitions → [Interpret Output Scores](../interpreting-results/interpret-output-scores.md)
* For Zingg ID lifecycle → [Zingg ID](/broken/pages/9QpDFW20AMt0UJ4cEW6b)
{% endhint %}

### Notebook 06: Run incremental matching

After the initial match, use `runIncremental` to update the identity graph with new or changed records without retraining the model. New records that match an existing cluster inherit its `ZINGG_ID`. Records that do not match any existing cluster receive a new `ZINGG_ID`.

#### **Step 24: Configure incremental input**

`IncrementalArguments` wraps your base `args` and adds the incremental data source and a temporary output path.

```python
incrArgs = IncrementalArguments()
incrArgs.setParentArgs(args)

incremental_input_path = files_dir + "/input/incr.csv"
incrPipe = ECsvPipe("testFebrlIncr", incremental_input_path, schema)
incrArgs.setIncrementalData(incrPipe)

tmp_output_path = files_dir + "/output/temp"
outputTmpPipe = ECsvPipe("outputTemp", tmp_output_path)
outputTmpPipe.addProperty("header", "true")
incrArgs.setOutputTmp(outputTmpPipe)
```

{% hint style="info" icon="right-long" %}
`setOutputTmp` specifies a temporary path where Zingg writes intermediate results before merging them into the main output. It must be different from your main output path.
{% endhint %}

#### Step 25: Run incremental

```python
options = ClientOptions([ ClientOptions.PHASE, "runIncremental" ])
zingg = EZinggWithSpark(incrArgs, options)
zingg.initAndExecute()
```

#### Step 26: View updated output

```python
outputDF = spark.read.csv(output_path, header=True)
display(outputDF)
print(outputDF.count())
```

{% hint style="success" icon="right-long" %}
**Read more**: For the full incremental matching guide including cluster merge and reassignment behaviour → [Run Incremental Matching](../running-zingg/run-incremental-matching.md)
{% endhint %}

### Notebook 07: Explain output

The explain phase shows how a specific cluster was formed, which record pairs contributed, their similarity scores, and how transitive matching connected records through intermediate pairs. Use this for governance, model validation, and sharing evidence with domain experts.

#### **Step 27: Import explain libraries**

```python
from zinggEC.enterprise.common.ExplainArguments import*
from zinggEC.enterprise.common.EClientOptions import*
```

#### **Step 28: Set the cluster to explain**

Find a `ZINGG_ID` from your match output that you want to investigate. Copy it into `zingg_id` below.

```python
explain_output = files_dir + "/output/explainOutput"
zingg_id = "7b73c8f1-1b39-4314-bb5f-7f5674183cc3"
```

#### Step 29: Run the explain phase

```python
explainArgs = ExplainArguments()
explainArgs.setParentArgs(args)

explainPipe = ECsvPipe("outputexplain", explain_output)
explainArgs.setExplainOutput(explainPipe)

explainOptions = EClientOptions(
    [ EClientOptions.PHASE, "explain", EClientOptions.ZINGG_ID, zingg_id ]
)
zinggExplain = EZingg(explainArgs, explainOptions)
zinggExplain.initAndExecute()
```

#### Step 30: View explain output

```python
outputDF = spark.read.csv(explain_output, header=True)
display(outputDF)
print(outputDF.count())
```

_**IMAGE TO BE ADDED — explain output table in Databricks showing\*\*\*\*****&#x20;****`pk1`****,****&#x20;****`pk2`****\*\*\*\*, and similarity score columns for matched pairs within the cluster. Tanwi to check with team for screenshot from a live notebook run. A small 5–10 row output table is sufficient.**_

{% hint style="success" icon="right-long" %}
**Read more**: Each row in the output represents a matched record pair within the cluster — `pk1` and `pk2` are the primary keys of the two records, with their similarity score.

For the full explain guide → [Explain a Specific Cluster](../interpreting-results/explain-a-specific-cluster.md)
{% endhint %}
{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
Download the notebooks used in this guide:

* Community notebooks (NB01–04): `github.com/zinggAI/zingg/tree/main/examples/databricks`
* Enterprise notebooks (NB01–07): included in your Zingg Enterprise package
{% endhint %}
