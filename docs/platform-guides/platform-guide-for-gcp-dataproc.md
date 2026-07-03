---
description: >-
  End-to-end guide to running Zingg on Google Cloud Dataproc from GCS storage
  setup and cluster creation to your first match result. Covers Community (open
  source) and Enterprise.
---

# Platform Guide for GCP Dataproc

Combining Zingg with Google Cloud gives you elastic Spark scale via Dataproc, flexible storage via GCS, and a managed JupyterLab workspace via the Component Gateway. Spin up a cluster when you need it and shut it down when the job is done.

{% hint style="success" icon="right-long" %}
Tested with Dataproc image version 2.2-debian12 (Spark 3.5). The `n2-standard-4` machine type with 16GB RAM per node is the recommended minimum for Zingg's training phases.
{% endhint %}

{% tabs %}
{% tab title="Community (OS)" %}
Uses `Arguments`, `FieldDefinition`, `CsvPipe`, and `ZinggWithSpark`. Runs on a standard Dataproc cluster with JupyterLab.

### Step 1: Download JARs and prepare your GCS bucket

Zingg on GCP requires three JARs to bridge Spark with Google Cloud services. Download these to your local machine before creating the cluster.

<table><thead><tr><th valign="top">JAR</th><th valign="top">Purpose</th><th valign="top">Download</th></tr></thead><tbody><tr><td valign="top"><code>zingg-0.6.0.jar</code></td><td valign="top">The Zingg engine</td><td valign="top"><code>github.com/zinggAI/zingg/releases</code></td></tr><tr><td valign="top"><code>spark-3.5-bigquery-0.44.1.jar</code></td><td valign="top">BigQuery connector</td><td valign="top"><code>github.com/GoogleCloudDataproc/spark-bigquery-connector</code></td></tr><tr><td valign="top"><code>gcs-connector-hadoop3-latest.jar</code></td><td valign="top">GCS connector</td><td valign="top"><code>docs.cloud.google.com/dataproc/docs/concepts/connectors/cloud-storage</code></td></tr></tbody></table>

Create a GCS bucket and upload the JARs and your dataset. You can do this from the Cloud Console or the `gcloud` CLI.

#### **Cloud Console**

1. Navigate to **Cloud Storage → Buckets → Create**.
2. Name your bucket with a globally unique identifier (for example `zingg-production-storage`).
3. Set the region to match where your Dataproc cluster will run (for example `us-central1`).
4. Upload the three JARs and your data file (for example `customers.csv`) to the bucket.

_**IMAGE TO BE ADDED - GCS bucket creation screen in the Google Cloud Console showing bucket name, region selector, and upload interface. Tanwi to check with team for screenshot from a live GCS console.**_&#x20;

#### gcloud CLI

```bash
BUCKET = "zingg-production-storage"

         gcloud storage buckets create gs
    :  //$BUCKET \
    --location=us-central1

       gsutil cp *
           .jar gs :                               //$BUCKET/
                      gsutil cp customers.csv gs:  //$BUCKET/
```

### Step 2: Create a Dataproc cluster

The cluster must be created with the three JARs injected via `spark.jars`. This is the critical configuration step; without it, Zingg cannot run on the cluster.

#### **Cloud Console**

1. Search for **Dataproc** and select **Create Cluster on Compute Engine**.
2. Under **Optional components**, check **Jupyter Notebook**.
3. Under **Component Gateway**, check **Enable component gateway**.
4. Set **Master** and **Worker** nodes to `n2-standard-4` with 100GB boot disk.
5. Scroll to **Properties** and add:
   * **Key:** `spark.jars`
   * **Value:** `gs://YOUR_BUCKET/zingg-0.6.0.jar,gs://YOUR_BUCKET/spark-3.5-bigquery-0.44.1.jar,gs://YOUR_BUCKET/gcs-connector-hadoop3-latest.jar`

_**IMAGE TO BE ADDED — Dataproc cluster creation screen showing the Properties section with `spark.jars` key and the three JAR paths as the value. Tanwi to check with team for screenshot from a live Dataproc console. This is the most important screenshot on the page — the `spark.jars` Properties field is not obvious to find and a screenshot here prevents the most common setup error.**_&#x20;

#### gcloud CLI

```bash
gcloud dataproc clusters create zingg - cluster-- region =
    us - central1-- image - version =
        2.2 - debian12-- master - machine - type =
            n2 - standard - 4 --worker - machine - type =
                n2 - standard - 4 --num - workers =
                    2 --optional - components =
                        JUPYTER-- enable - component - gateway-- properties =
                            "^#^spark:spark.jars=\
gs://$BUCKET/zingg-0.6.0.jar,\
gs://$BUCKET/spark-3.5-bigquery-0.44.1.jar,\
gs://$BUCKET/gcs-connector-hadoop3-latest.jar"
```

{% hint style="success" icon="right-long" %}
`numPartitions` should be set to approximately 20–30× your worker vCPU count. For a 2-worker `n2-standard-4` cluster (8 vCPUs each), start with 4–8.
{% endhint %}

### Step 3: Open JupyterLab

Once your cluster status shows **Running**, access the managed JupyterLab environment through the Component Gateway - no SSH or firewall configuration needed.

1. Navigate to **Dataproc → Clusters**.
2. Click your cluster name.
3. Click the **Web Interfaces** tab.
4. Under **Component Gateway**, click the **JupyterLab** link.
5. Create a new notebook and select the **PySpark** kernel.

_**IMAGE TO BE ADDED— Dataproc cluster Web Interfaces tab showing the Component Gateway section with the JupyterLab link highlighted. Tanwi to check with team for screenshot from a live Dataproc cluster.**_&#x20;

### Step 4: Set a checkpoint directory and install Zingg

Zingg uses a GCS checkpoint path as a persistent safety net for intermediate Spark computation state. Set this before running any phase.

```python
checkpoint_path =
    f "gs://{BUCKET}/zingg_checkpoint" spark.sparkContext.setCheckpointDir(
        checkpoint_path)
```

Install the Zingg Python package on the cluster:

```python
checkpoint_path =
    f "gs://{BUCKET}/zingg_checkpoint" spark.sparkContext.setCheckpointDir(
        checkpoint_path)
```

### Step 5: Set the model ID, storage paths, and import libraries

`BUCKET` is your GCS bucket name. `modelId` is a unique name for this model run - Zingg uses it as the folder name under `zinggDir`. Use the same values across all steps in this workflow.

```python
BUCKET = "your-bucket-name" modelId = "testModelFebrl" zinggDir =
    f "gs://{BUCKET}/models"

    MARKED_DIR = (f "gs://{BUCKET}/models/" f
                    "{modelId}/trainingData/marked/")UNMARKED_DIR =
        (f "gs://{BUCKET}/models/" f "{modelId}/trainingData/unmarked/")
```

`MARKED_DIR` and `UNMARKED_DIR` are derived automatically. Zingg writes labeled training pairs to `MARKED_DIR` during the `label` phase and reads them back during `train`.

```python
import pandas as pd import numpy as np import os, time, uuid from tabulate import tabulate from ipywidgets import widgets, interact, GridspecLayout import base64 import pyspark.sql.functions as fn from google.cloud import storage from zingg.client import * from zingg.pipes import *

                                                                                                                                     client = storage.Client()

                                                                                                                                         def cleanModel() : ""
                                                                                                                                                            "Clears previous training data to restart model learning from scratch."
                                                                                                                                                            "" try :bucket = client.get_bucket(BUCKET) for prefix in[f "models/{modelId}/trainingData/marked/", f "models/{modelId}/trainingData/unmarked/"] : for blob in bucket.list_blobs(prefix = prefix) :blob.delete() print("Model cleaned.") except Exception as e:print(f "Error: {str(e)}")

                                                                                                                                                                                                                                                                                                                                                 def count_labeled_pairs(marked_pd) : ""
                                                                                                                                                                                                                                                                                                                                                                                      "Returns positive, negative, and total labeled pair counts."
                                                                                                                                                                                                                                                                                                                                                                                      "" if marked_pd.empty: return 0, 0, 0 n_total = len(np.unique(marked_pd['z_cluster'])) n_positive = len(np.unique(marked_pd[marked_pd['z_isMatch'] == 1]['z_cluster'])) n_negative = len(np.unique(marked_pd[marked_pd['z_isMatch'] == 0]['z_cluster'])) return n_positive, n_negative, n_total
```

### Step 6: Build the arguments object

`Arguments` is the central configuration object. Every phase in this workflow reads from the same `args` instance.

```python
args = Arguments() args.setModelId(modelId) args.setZinggDir(zinggDir)
```

### Step 7: Preview your data

Read your dataset from GCS and preview it before configuring the pipes. This is for your own verification; the actual Zingg input pipe is configured in Step 8.

```python
csv_path = f "gs://{BUCKET}/test.csv" spark_df =
    spark.read.csv(csv_path, header = True, inferSchema = True) schema_list =
        [
          "id", "fname", "lname", "stNo", "add1", "add2", "city", "state",
          "areacode", "dob", "ssn"
        ] spark_df = spark_df
                         .toDF(*schema_list)

                             spark_df.limit(10)
                         .toPandas()
                         .head()
```

_**IMAGE TO BE ADDED — Jupyter notebook cell showing the preview output table with sample FEBRL data — the same customer appearing multiple times with field variations across rows. Source: not in the GCP docx (text-only guide). Tanwi to screenshot from a live notebook run. Same principle as the Databricks guide — this image shows readers the exact problem Zingg is solving before they configure anything. Place: below the****&#x20;****`spark_df.limit(10).toPandas().head()`****&#x20;****line.**_

### Step 8: Configure input and output pipes

`CsvPipe` connects Zingg to your GCS data. The schema string must match your dataset column names exactly.

```python
schema =
    ("id string, fname string, "
     "lname string, stNo string, "
     "add1 string, add2 string, "
     "city string, state string, "
     "areacode string, dob string, "
     "ssn string")

        inputPipe = CsvPipe("testFebrl", f "gs://{BUCKET}/test.csv", schema)
                        args.setData(inputPipe)

                            output_path = f "gs://{BUCKET}/results" outputPipe =
            CsvPipe("resultOutput", output_path) args.setOutput(outputPipe)
```

{% hint style="success" icon="right-long" %}
**Read more**: Zingg supports CSV, Parquet, and JSON output on GCS. To push results directly to BigQuery after matching, use the BigQuery Spark connector.&#x20;

For connector config → [Connect BigQuery](../connect-your-data/connect-cloud-warehouses/connect-bigquery.md)
{% endhint %}

### Step 9: Define fields and match types

Every field in your input schema must appear in `fieldDefinition`. List the most important fields first as field order affects blocking quality.

```python
id = FieldDefinition("id", "string", MatchType.EXACT) fname = FieldDefinition(
    "fname", "string", MatchType.FUZZY) lname =
    FieldDefinition("lname", "string", MatchType.FUZZY) stNo = FieldDefinition(
        "stNo", "string",
        MatchType.FUZZY) add1 = FieldDefinition("add1", "string",
                                                MatchType.FUZZY) add2 =
        FieldDefinition("add2", "string", MatchType.FUZZY) city =
            FieldDefinition("city", "string", MatchType.FUZZY) state =
                FieldDefinition("state", "string", MatchType.FUZZY) areacode =
                    FieldDefinition("areacode", "string", MatchType.FUZZY) dob =
                        FieldDefinition("dob", "string", MatchType.EXACT) ssn =
                            FieldDefinition("ssn", "string", MatchType.EXACT)

                                fieldDefs = [
                                  id, fname, lname, stNo, add1, add2, city,
                                  state, areacode, dob,
                                  ssn
                                ] args.setFieldDefinition(fieldDefs)
```

{% hint style="success" icon="right-long" %}
`FUZZY` handles variations like 'Jon' vs 'John' or 'St' vs 'Street'. `EXACT` requires a character-for-character match. `DONT_USE` excludes a field from matching but keeps it in output.&#x20;

For all match types → [Match Types](../zingg-concepts/how-zingg-learns/match-types/)
{% endhint %}

### Step 10: Configure performance settings

`numPartitions` controls how data is distributed across cluster nodes. `labelDataSampleSize` controls how much of the dataset is scanned when finding candidate pairs.

For a 2-worker `n2-standard-4` cluster (8 vCPUs each), start with 4-8

```python
args.setNumPartitions(4)
```

Reduce to 0.1 if `findTrainingData` is slow on large datasets

```python
args.setLabelDataSampleSize(0.5)
```

{% hint style="success" icon="right-long" %}
For 100k records use `labelDataSampleSize` between 0.1 and 0.5. For 1M+ records use 0.01 to 0.05. If `findTrainingData` takes too long, reduce by approximately 10× and try again.
{% endhint %}

### Step 11: Find candidate pairs

Zingg scans your dataset using the field rules defined in Step 9 and identifies pairs of records the model is uncertain about — edge cases where human input is most valuable. Candidate pairs are saved to `UNMARKED_DIR` in your GCS bucket.

```python
options = ClientOptions([ ClientOptions.PHASE, "findTrainingData" ]) zingg =
    ZinggWithSpark(args, options) zingg.initAndExecute()
```

### Step 12: Load pairs for labeling

```python
options = ClientOptions([ ClientOptions.PHASE, "label" ]) zingg =
    ZinggWithSpark(args, options) zingg.init()

        candidate_pairs_pd =
        getPandasDfFromDs(zingg.getUnmarkedRecords())

            if candidate_pairs_pd.shape[0] == 0
    : print("No pairs found. Run findTrainingData first.") else : z_clusters =
            list(np.unique(candidate_pairs_pd['z_cluster']))
                print(f "{len(z_clusters)} candidate pairs found for labeling")
```

### Step 13: Label pairs in the widget

A widget displays each candidate pair side by side. For each pair select Match, No Match, or Uncertain.

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
    candidate_left  = display_pd.iloc[2*n].to_list()
    candidate_right = display_pd.iloc[(2*n)+1].to_list()
    html = ''
    z_cluster = None
    for i in range(display_pd.shape[1]):
        col = display_pd.columns[i]
        if col == 'z_cluster':
            z_cluster = candidate_left[i]
        html += '<tr>'
        html += f'<td style="width:20%"><b>{col}</b></td>'
        html += f'<td style="width:40%">{str(candidate_left[i])}</td>'
        html += f'<td style="width:40%">{str(candidate_right[i])}</td>'
        html += '</tr>'
    table = widgets.HTML(
        value=f'<table data-title="{z_cluster}" '
              f'style="width:100%;border-collapse:collapse" '
              f'border="1">{html}</table>')
    label = widgets.ToggleButtons(
        options=LABELS.keys(), button_style='info')
    vContainers.append(widgets.VBox(
        children=[table, label, widgets.HTML(value='<br>')]))

display(widgets.VBox(children=vContainers))
ready_for_save = True
```

_**IMAGE TO BE ADDED — Zingg labeling widget rendered in JupyterLab on Dataproc, showing two candidate records side by side with Match / No Match / Uncertain toggle buttons. Tanwi to check with team for screenshot from a live Dataproc notebook run.**_&#x20;

{% hint style="success" icon="right-long" %}
Target 30–40 match pairs and 30–40 non-match pairs before training. Repeat Steps 11–14 in a loop until you reach this target. Label until all field types and data variation patterns in your schema are covered. If accuracy needs improvement after the first match run, return to labeling and focus on patterns that are underrepresented.
{% endhint %}

### Step 14: Save labeled pairs

```python
if not ready_for_save:
    print("Run the widget cell first.")
else:
    for pair in vContainers[1:]:
        user_label = pair.children[1].get_interact_value()
        start = pair.children[0].value.find('data-title="')
        if start > 0:
            start += len('data-title="')
            end = pair.children[0].value.find('"', start+2)
            pair_id = pair.children[0].value[start:end]
            candidate_pairs_pd.loc[
                candidate_pairs_pd['z_cluster'] == pair_id,
                'z_isMatch'] = LABELS.get(user_label)

    zingg.writeLabelledOutputFromPandas(
        candidate_pairs_pd, args)

    marked_pd = getPandasDfFromDs(
        zingg.getMarkedRecords())
    n_pos, n_neg, n_tot = \
        count_labeled_pairs(marked_pd)
    print(f"Total pairs labeled: {n_tot}")
    print(f"Positive matches: {n_pos}")
    print(f"Non-matches: {n_neg}")
    print("Run Steps 11-14 again if you need more pairs.")
    ready_for_save = False
```

### Step 15: Generate model documentation (optional)

Run `generateDocs` after labeling to produce readable HTML reports of your training data,  both matched and non-matched pairs. Use this to verify label consistency and share a visual audit with subject matter experts before training.

```python
options = ClientOptions([ ClientOptions.PHASE, "generateDocs" ])
    zingg = ZinggWithSpark(args, options) zingg.initAndExecute()

                DOCS_DIR =
        f "{zinggDir}/{modelId}/docs/" from IPython.core.display import display,
    HTML

        print("--- Model Summary ---") with
    open(DOCS_DIR + "model.html", 'r') as f
    : display(HTML(f.read()))

          print("--- Data Distribution ---") with
      open(DOCS_DIR + "data.html", 'r') as f : display(HTML(f.read()))
```

_**IMAGE TO BE ADDED— `generateDocs` HTML output rendered inside JupyterLab on Dataproc showing labeled pair examples. Source: not in the GCP docx. Tanwi to check with team for screenshot from a live notebook run.**_&#x20;

{% hint style="success" icon="right-long" %}
`generateDocs` is optional. Skip it if you have 30–40 matches and 30–40 non-matches and are confident in your labeling quality.
{% endhint %}

### Step 16: Train and match

`trainMatch` combines the `train` and `match` phases into a single call. Zingg builds a model from your labeled pairs and immediately applies it to the full dataset. This is the most compute-intensive step — it distributes the workload across all Dataproc nodes.

```python
options = ClientOptions([ ClientOptions.PHASE, "trainMatch" ]) zingg =
    ZinggWithSpark(args, options) zingg.initAndExecute()
```

You can also run `train` and `match` as two separate phases if you want to inspect the trained model before running the full dataset match:

Train separately:

```python
options = ClientOptions([ ClientOptions.PHASE, "train" ]) zingg =
    ZinggWithSpark(args, options) zingg.initAndExecute()
```

Then match separately:

```python
options = ClientOptions([ ClientOptions.PHASE, "match" ]) zingg =
    ZinggWithSpark(args, options) zingg.initAndExecute()
```

### Step 17: View output

Match output is written to `output_path` in your GCS bucket as distributed part-files. Spark reads them back and merges them automatically.

```python
outputDF = spark.read.csv(output_path, header = False, inferSchema = True)

               colNames =
    [
      "z_score", "z_cluster", "z_zid", "id", "fname", "lname", "stNo", "add1",
      "add2", "city", "state", "areacode", "dob", "ssn"
    ]

    final_results = outputDF.toDF(*colNames) final_results.show(10)
```

_**IMAGE TO BE ADDED  — match output table in JupyterLab on Dataproc showing resolved records with `z_cluster` column visible. Highlight two rows sharing the same `z_cluster` value to show they have been resolved to the same entity.Tanwi to check with team for screenshot from a live notebook run.**_&#x20;

{% hint style="success" icon="right-long" %}
* `z_cluster`— unique entity ID assigned by Zingg. All records sharing the same `z_cluster` represent the same real-world entity. Group by `z_cluster` to collapse duplicates into a golden record.
* `z_score` — model confidence. Values closer to 1.0 indicate a stronger match.
* `z_zid` — unique internal row identifier assigned during this run.&#x20;

For threshold guidance and full output column definitions → [Interpret Output Scores](../interpreting-results/interpret-output-scores.md)
{% endhint %}
{% endtab %}

{% tab title="Enterprise" %}

{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
**Read more**:

* Tune accuracy → [Improve Accuracy](../tuning/improve-accuracy/)
* Understand scores and set thresholds → [Interpret Output Scores](../interpreting-results/interpret-output-scores.md)
* Push results to BigQuery → [Connect BigQuery](../connect-your-data/connect-cloud-warehouses/connect-bigquery.md)
{% endhint %}



