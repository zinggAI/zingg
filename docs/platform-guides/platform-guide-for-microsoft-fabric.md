---
description: >-
  End-to-end guide to running Zingg on Microsoft Fabric from workspace setup and
  Lakehouse creation to your first match result. Covers Community (open source)
  and Enterprise.
---

# Platform Guide for Microsoft Fabric

Microsoft Fabric paired with Zingg gives you entity resolution with built-in data governance via Microsoft Purview. Fabric's OneLake provides a single storage layer for all your data. Zingg runs on Fabric Spark pools using the same Python API as on other platforms.

{% hint style="success" icon="right-long" %}
* Tested with Fabric Runtime 1.3 (Spark 3.5).
* Download the sample Fabric notebook from `github.com/zinggAI/zingg/blob/main/examples/fabric/ExampleNotebook.ipynb` and upload it to your workspace to follow this guide.
{% endhint %}

{% tabs %}
{% tab title="Community" %}
Uses `Arguments`, `FieldDefinition`, `CsvPipe`, and `ZinggWithSpark`. The workflow runs across four notebooks.

### Fabric workspace setup

Complete these four steps in the Fabric UI before opening any notebook.

#### **Step 1: Create a Fabric workspace**

If you are new to Fabric, sign up for a free trial at `microsoft.com/fabric`.

1. Go to **Workspaces** and click **New workspace**.
2. Name it something like `Zingg-Fabric`.
3. When prompted for a session cluster, choose **New Standard Session**.

#### **Step 2: Create a Zingg Environment**

Fabric Environments let you install JARs that persist across notebook sessions. Zingg requires its JAR to be installed in an Environment before any notebook runs.

1. Inside your workspace, go to the **Environment** tab and click **New Environment**.
2. Name it `Zingg Environment`.

#### **Step 3: Install the Zingg JAR in the Environment**

The Zingg JAR must be installed as a Custom Library in your Environment so Fabric's Spark runtime can find it.

1. Go to `github.com/zinggAI/zingg/releases` and download the latest release `tar` file.
2. Extract the `tar` file and locate the JAR file inside it.
3. Open your `Zingg Environment`, go to **Custom Library**, and upload the JAR file.
4. Click **Save** and then **Publish** the Environment.

#### **Step 4: Create a Lakehouse and upload your data**

Zingg reads from and writes to OneLake. Create a Lakehouse to give Zingg a storage location for your data, model files, and output.

1. Inside your workspace, click **New Item** → **Lakehouse**.
2. Give the Lakehouse a name (for example `ZinggLakehouse`).
3. Go inside the Lakehouse, click **Get Data**, and upload your CSV file.

{% hint style="success" icon="right-long" %}
Sample data for testing: `github.com/zinggAI/zingg/blob/main/examples/febrl/test.csv`

Zingg also supports Delta Lake tables, Parquet, and JSON - change the format in the input pipe configuration.

For all connector formats → [Connect Microsoft Fabric](../connect-your-data/connect-cloud-warehouses/connect-microsoft-fabric.md)
{% endhint %}

### Notebook 01: Set up Zingg

Create a new notebook in your workspace, attach it to the `Zingg Environment`, and select **PySpark** as the kernel. All four notebooks in this workflow call `%run 01-setting_up_zingg` at the top — so everything configured here is inherited by Notebooks 02, 03, and 04 automatically.

#### **Step 5: Verify Spark is configured correctly**

Confirm Fabric's Spark runtime is running before proceeding.

```python
spark.sparkContext.getConf().get('spark.hadoop.trident.workspace.id')
```

#### **Step 6: Set the checkpoint directory and install Zingg**

Zingg uses a checkpoint directory to store intermediate Spark computation state. Set this to a path inside your OneLake `Files` directory.

```python
spark.sparkContext.setCheckpointDir("Files")
```

Install the Zingg Python package:

```python
pip install zingg
```

Verify the installation:

```python
!pip show zingg
```

#### **Step 7: Set the model ID and storage paths**

`zinggDir` is the root path for all Zingg model files, training data, and output. `modelId` is the unique name for this model run. Use the same values across all four notebooks.

Replace `<workspace-id>` and `<lakehouse-id>` with the actual IDs from your Fabric workspace and Lakehouse. You can find them in the browser address bar when viewing your Lakehouse.

```python
zinggDir = (
    "abfss://<workspace-id>@onelake"
    ".dfs.fabric.microsoft.com/"
    "<lakehouse-id>/Files/models"
)
modelId = "oss_model"
```

`MARKED_DIR` and `UNMARKED_DIR` are derived automatically. Zingg writes labeled training pairs to `MARKED_DIR` during the `label` phase and reads them back during `train`.

```python
MARKED_DIR = zinggDir + "/" + modelId + "/trainingData/marked/"
UNMARKED_DIR = zinggDir + "/" + modelId + "/trainingData/unmarked/"
```

#### Step 8: Import libraries and set up helper functions

```python
import pandas as pd
import numpy as np
from ipywidgets import widgets, interact, GridspecLayout
import base64
import pyspark.sql.functions as fn

from zingg.client import *
from zingg.pipes import *

def count_labeled_pairs(marked_pd):
    """
    Returns positive, negative, uncertain,
    and total labeled pair counts.
    """
    n_total = len(np.unique(marked_pd['z_cluster']))
    n_positive = len(np.unique(marked_pd[marked_pd['z_isMatch'] == 1]['z_cluster']))
    n_negative = len(np.unique(marked_pd[marked_pd['z_isMatch'] == 0]['z_cluster']))
    n_uncertain = len(np.unique(marked_pd[marked_pd['z_isMatch'] == 2]['z_cluster']))
    return n_positive, n_negative, n_uncertain, n_total

def fix_void_columns(df):
    """
    Handles columns where all values are None
    to prevent save errors.
    """
    for col in df.columns:
        if df[col].apply(lambda x: x is None).all():
            df[col] = df[col].astype(str)
    return df
```

#### **Step 9: Build the arguments object**

`Arguments` is the central configuration object. Every phase in this workflow reads from the same `args` instance.

```python
args = Arguments()
args.setModelId(modelId)
args.setZinggDir(zinggDir)
```

#### **Step 10: Configure performance settings**

`numPartitions` controls how data is distributed across Spark workers. Disabling Adaptive Query Execution gives Zingg more predictable Spark behavior.

```python
args.setNumPartitions(32)
spark.conf.set("spark.sql.adaptive.enabled", False)
```

{% hint style="info" icon="right-long" %}
Set `numPartitions` to approximately 20–30× your worker vCPU count. `labelDataSampleSize` is set in Notebook 02 where the labeling loop runs — see Step 13.
{% endhint %}

#### **Step 11: Preview your data**

Read your CSV from OneLake and preview it before configuring the Zingg pipes. This is for your own verification—the Zingg input pipe is configured in Step 12.

```python
import pandas as pd

schema = ["rec_id", "fname", "lname", "stNo", "add1", "add2", "city", "areacode", "state", "dob", "ssn"]

data = pd.read_csv(
    "abfss://<workspace-id>@onelake"
    ".dfs.fabric.microsoft.com/"
    "<lakehouse-id>/Files/test.csv",
    header=None
)
data.columns = schema
data.head()
```

_**IMAGE TO BE ADDED — Fabric notebook cell showing the data preview output table with sample FEBRL records — the same entity appearing multiple times with field variations across rows. Tanwi to check with team for screenshot from a live Fabric notebook run.**_

#### **Step 12: Configure input and output pipes**

`CsvPipe` connects Zingg to your OneLake data. The schema string must match your dataset column names exactly.

```python
schema = (
    "rec_id string, fname string, "
    "lname string, stNo string, "
    "add1 string, add2 string, "
    "city string, areacode string, "
    "state string, dob string, "
    "ssn string"
)

inputPipe = CsvPipe(
    "inputpipe",
    "abfss://<workspace-id>@onelake"
    ".dfs.fabric.microsoft.com/"
    "<lakehouse-id>/Files/test.csv",
    schema
)
args.setData(inputPipe)

output_path = (
    "abfss://<workspace-id>@onelake"
    ".dfs.fabric.microsoft.com/"
    "<lakehouse-id>/Files/Output" +
    modelId
)
outputPipe = CsvPipe("resultOutput", output_path)
args.setOutput(outputPipe)
```

{% hint style="success" icon="right-long" %}
**Read more**: To use Delta tables instead of CSV, change the format to `delta` and point to your Lakehouse Tables directory. For all connector formats → [Connect Microsoft Fabric](../connect-your-data/connect-cloud-warehouses/connect-microsoft-fabric.md)
{% endhint %}

#### **Step 13: Define fields and match types**

Every field in your input schema must appear in `fieldDefinition`. List the most important fields first as field order affects blocking quality.

```python
rec_id = FieldDefinition("rec_id", "string", MatchType.DONT_USE)
fname = FieldDefinition("fname", "string", MatchType.FUZZY)
lname = FieldDefinition("lname", "string", MatchType.FUZZY)
stNo = FieldDefinition("stNo", "string", MatchType.FUZZY)
add1 = FieldDefinition("add1", "string", MatchType.FUZZY)
add2 = FieldDefinition("add2", "string", MatchType.FUZZY)
city = FieldDefinition("city", "string", MatchType.FUZZY)
areacode = FieldDefinition("areacode", "string", MatchType.FUZZY)
state = FieldDefinition("state", "string", MatchType.FUZZY)
dob = FieldDefinition("dob", "string", MatchType.EXACT)
ssn = FieldDefinition("ssn", "string", MatchType.EXACT)

fieldDefs = [
    rec_id, fname, lname, stNo, add1,
    add2, city, areacode, state, dob,
    ssn
]
args.setFieldDefinition(fieldDefs)
```

{% hint style="success" icon="right-long" %}
`FUZZY` handles variations like 'Jon' vs 'John' or 'St' vs 'Street'. `EXACT` requires a character-for-character match. `DONT_USE` excludes a field from matching but keeps it in output — use this for record identifiers. For all match types → [Match Types](../zingg-concepts/how-zingg-learns/match-types/)
{% endhint %}

### Notebook 02: Find training data and label pairs

This notebook runs `findTrainingData` and `label`. It calls `%run 01-setting_up_zingg` at the top so all configuration from Notebook 01 is inherited automatically.

#### **Step 14: Set `labelDataSampleSize`**

`labelDataSampleSize` controls how much of your dataset is scanned when finding candidate pairs.

```python
args.setNumPartitions(4)
args.setLabelDataSampleSize(0.4)
```

{% hint style="success" icon="right-long" %}
For 100k records use `labelDataSampleSize` between 0.1 and 0.5. For 1M+ records use 0.01 to 0.05. If `findTrainingData` is slow, reduce by approximately 10× and try again.
{% endhint %}

#### **Step 15: Find candidate pairs**

Zingg scans your dataset using the field rules defined in Step 13 and selects the most informative pairs for labeling — edge cases where the model has the most to learn. Candidate pairs are saved to `UNMARKED_DIR` in your OneLake Lakehouse.

```python
options = ClientOptions([ ClientOptions.PHASE, "findTrainingData" ])
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()
```

#### Step 16: Load pairs for labeling

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

#### **Step 17: Label pairs in the widget**

A widget displays each candidate pair side by side. For each pair, select Match, No Match, or Uncertain.

```python
ready_for_save = False
LABELS = {'Uncertain': 2, 'Match': 1, 'No Match': 0}

n_pairs    = int(candidate_pairs_pd.shape[0] / 2)
display_pd = candidate_pairs_pd.drop(
    labels=['z_zid', 'z_prediction',
        'z_score', 'z_isMatch', 'z_zsource'],
    axis=1)

vContainers = []
vContainers.append(widgets.HTML(
    value=f'<h2>Indicate if each of the '
          f'{n_pairs} record pairs is a match or not</h2>'))

for n in range(n_pairs):
    candidate_left  = display_pd.loc[2*n].to_list()
    candidate_right = display_pd.loc[(2*n)+1].to_list()
    html = ''
    z_cluster = None
    for i in range(display_pd.shape[1]):
        column_name = display_pd.columns[i]
        if column_name == 'z_cluster':
            z_cluster = candidate_left[i]
        html += '<tr>'
        html += f'<td style="width:10%"><b>{column_name}</b></td>'
        html += f'<td style="width:45%">{str(candidate_left[i])}</td>'
        html += f'<td style="width:45%">{str(candidate_right[i])}</td>'
        html += '</tr>'
    table = widgets.HTML(
        value=f'<table data-title="{z_cluster}" '
              f'style="width:100%;border-collapse:collapse" '
              f'border="1">{html}</table>')
    label = widgets.ToggleButtons(
        options=LABELS.keys(), button_style='info')
    vContainers.append(widgets.VBox(
        children=[table, label,
            widgets.HTML(value='<br>')]))

display(widgets.VBox(children=vContainers))
ready_for_save = True
```

_**IMAGE TO BE ADDED — Zingg labeling widget running inside a Fabric notebook showing two candidate records side by side with Match / No Match / Uncertain toggle buttons. Tanwi to check with team for screenshot from a live Fabric notebook run.**_

{% hint style="success" icon="right-long" %}
Target 30–40 match pairs and 30–40 non-match pairs before training. Repeat Steps 15–18 in a loop until you reach this target. Label until all field types and data variation patterns in your schema are covered. If accuracy needs improvement after the first match run, return to labeling and focus on patterns that are missing or underrepresented.
{% endhint %}

#### **Step 18: Save labeled pairs**

After labeling pairs in the widget, run this cell to save your labels to the training data folder in OneLake.

```python
if not ready_for_save:
    print("Run the widget cell first.")
else:
    for pair in vContainers[1:]:
        user_assigned_label = pair.children[1].get_interact_value()
        start = pair.children[0].value.find('data-title="')
        if start > 0:
            start += len('data-title="')
            end = pair.children[0].value.find('"', start+2)
        pair_id = pair.children[0].value[start:end]
        candidate_pairs_pd.loc[
            candidate_pairs_pd['z_cluster'] == pair_id,
            'z_isMatch'] = LABELS.get(user_assigned_label)

    notebookutils.fs.mkdirs(MARKED_DIR)
    zingg.writeLabelledOutputFromPandas(
        candidate_pairs_pd, args)

    marked_pd_df = getPandasDfFromDs(
        zingg.getMarkedRecords())
    n_pos, n_neg, n_uncer, n_tot = \
        count_labeled_pairs(marked_pd_df)
    print(f"Out of total {n_tot} pairs,")
    print(f"You have accumulated {n_pos} pairs labeled as positive matches.")
    print(f"You have accumulated {n_neg} pairs labeled as not matches.")
    print(f"You have accumulated {n_uncer} pairs labeled as uncertain.")
    print("Run Steps 15-18 again if you need more pairs.")
    ready_for_save = False
```

{% hint style="success" icon="right-long" %}
`notebookutils.fs.mkdirs(MARKED_DIR)` creates the target directory in OneLake if it does not already exist. This is a Fabric-specific utility — it is not used on other platforms.
{% endhint %}

### Notebook 03: Generate model documentation (optional)

This notebook runs `generateDocs`. It calls `%run 01-setting_up_zingg` at the top. Run it after labeling and before training to inspect label quality. Skip it if you are confident in your labeling and want to proceed directly to Notebook 04.

#### **Step 19: Generate and view model documentation**

Run `generateDocs` to produce readable HTML reports of your labeled training data — both matched and non-matched pairs. Use this to verify label consistency and share with subject matter experts before committing to training.

```python
options = ClientOptions([ ClientOptions.PHASE, "generateDocs" ])
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()

DOCS_DIR = zinggDir + "/" + modelId + "/docs/"

displayHTML(open(DOCS_DIR + "model.html", 'r').read())
displayHTML(open(DOCS_DIR + "data.html", 'r').read())
```

_**IMAGE TO BE ADDED —****&#x20;****`generateDocs`****\*\*\*\* \*\*\*\*HTML output rendered inside a Fabric notebook showing labeled pair examples in a table. Tanwi to check with team for screenshot from a live Fabric notebook run.**_

### Notebook 04: Train and match

This notebook runs `trainMatch` and displays the output. It calls `%run 01-setting_up_zingg` at the top.

#### **Step 20: Train and match**

`trainMatch` combines `train` and `match` into a single phase. Zingg builds a model from your labeled pairs and immediately applies it to the full dataset. This is the most compute-intensive step; Spark distributes the workload across all Fabric Spark nodes.

```python
options = ClientOptions([ ClientOptions.PHASE, "trainMatch" ])
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()
```

You can also run `train` and `match` as separate phases if you want to inspect the trained model before running the full dataset:

```python
options = ClientOptions([ ClientOptions.PHASE, "train" ])
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()

options = ClientOptions([ ClientOptions.PHASE, "match" ])
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()
```

#### Step 21: View output

Match output is written to `output_path` in your OneLake Lakehouse. Read it back into a Spark DataFrame to inspect the resolved clusters.

```python
colNames = [
    "z_minScore", "z_maxScore", "z_cluster", "rec_id", "fname", "lname",
    "stNo", "add1", "add2", "city", "areacode", "state", "dob", "ssn"
]

outputDF = spark.read.csv(output_path)
outputDF = outputDF.toDF(*colNames)
display(outputDF)
print(outputDF.count())
```

_**IMAGE TO BE ADDED— Match output table in a Fabric notebook showing resolved records with\*\*\*\*****&#x20;****`z_cluster`****&#x20;****\*\*\*\*column visible — two rows sharing the same cluster value highlighted to illustrate entity resolution. Tanwi to check with team for screenshot from a live Fabric notebook run.**_
{% endtab %}

{% tab title="Enterprise" %}

{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
**Read more**:

* Tune accuracy → [Improve Accuracy](../tuning/improve-accuracy/)
* Understand scores and set thresholds → [Interpret Output Scores](../interpreting-results/interpret-output-scores.md)
{% endhint %}

{% hint style="success" icon="right-long" %}
Download the notebooks used in this guide:

* Community notebooks (NB01–04): `github.com/zinggAI/zingg/tree/main/examples/fabric`
* Enterprise notebooks (NB01–07): included in your Zingg Enterprise package
{% endhint %}
