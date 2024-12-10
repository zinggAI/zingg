# Using Databricks Interface

***

Cleaning, matching, and linking data is essential when working with data from different systems or sources. [Zingg.ai](http://zingg.ai/), an open-source entity resolution tool, can simplify this job. When paired with Databricks, you get a powerful combination for handling even the largest datasets. Here’s how you can get started!

***

#### Step 1: Set Up a Databricks Account

If you’re new to Databricks, the first step is creating an account. Don’t worry; it’s straightforward!

1. Head over to [Databricks](https://databricks.com/try-databricks) and sign up for a free trial.
2. Once inside, take a quick tour of the interface. The **Workspace** tab is where you’ll organize your work, and the **Clusters** tab is for creating the compute resources you’ll need.

_Tip:_ If you’re unsure about cluster settings, choose a smaller size to start — Databricks will automatically scale it up if needed!

_This guide is tested with Databricks Runtime 15.4, but newer versions should work unless otherwise noted._

***

#### Step 2: Create a Cluster and Install Zingg

A cluster is essentially a group of computers working together to process your data. Creating one on Databricks is as simple as clicking a button:

1. Go to the **Clusters** tab, hit **Create Cluster**, and give it a name like “Zingg-Demo.”
2. Set the runtime version to a current LTS (Long-Term Support) version for compatibility.
3. Next, you’ll need to install Zingg. Start by downloading the latest JAR file from [Zingg’s GitHub](https://github.com/zinggAI/zingg/releases).
4. Upload the file: Open the cluster details, navigate to the **Libraries** section, and click **Install New** > **Upload JAR**. Done!

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*Qz_ssAzilBDEivgYCq65Vg.png" alt="Uploading Zingg to Databricks"><figcaption><p>Uploading Zingg to Databricks. Image by Author</p></figcaption></figure>

***

#### Step 3: Install Zingg Python Package

To ensure Zingg runs smoothly, you also need the Python package. Open a Databricks notebook and type this:

```python
!pip install zingg
```

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*dlgZLCD2qqGgAaFUhmBHUQ.png" alt=""><figcaption><p>Installing Zingg on Databricks. Image by Author</p></figcaption></figure>

This command fetches and installs the Zingg Python library. Also, double-check that Zingg uses the tabulate library, which should already be installed on your machine. If not, install it and verify that all dependencies are in place.&#x20;

Use the following command to install the required dependencies, especially tabulate and restart the Python kernel:

***

```python
!pip install tabulate
```

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*hYy7qrWXpGA3dl_OvZSncQ.png" alt=""><figcaption><p>Installing tabulate on Databricks. Image by Author</p></figcaption></figure>

***

#### Step 4: Organize Your Workspace

It’s a good idea to keep things neat, especially when you’re working with multiple datasets and training models. Zingg requires specific folders for its labeled and unlabeled training data. Set up directory paths in your notebook:

```python
##you can change these to the locations of your choice
##these are the only two settings that need to change
zinggDir = "/models"
modelId = "zinggTrial26Nov_1"
```

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*L5YNJKYEDQgm7fKrZUWIPg.png" alt=""><figcaption><p>Setting up directory paths in the Notebook. Image by Author</p></figcaption></figure>

```python

##please leave the following unchanged
MARKED_DIR = zinggDir + "/" + modelId + "/trainingData/marked/"
UNMARKED_DIR = zinggDir + "/" + modelId + "/trainingData/unmarked/"

MARKED_DIR_DBFS = "/dbfs" + MARKED_DIR
UNMARKED_DIR_DBFS = "/dbfs" + UNMARKED_DIR  


import pandas as pd
import numpy as np
 
import time
import uuid
 
from tabulate import tabulate
from ipywidgets import widgets, interact, GridspecLayout
import base64

import pyspark.sql.functions as fn

##this code sets up the Zingg Python interface
from zingg.client import *
from zingg.pipes import *

def cleanModel():
    dbutils.fs.rm(MARKED_DIR, recurse=True)
    # drop unmarked data
    dbutils.fs.rm(UNMARKED_DIR, recurse=True)
    return

# assign label to candidate pair
def assign_label(candidate_pairs_pd, z_cluster, label):
  '''
  The purpose of this function is to assign a label to a candidate pair
  identified by its z_cluster value.  Valid labels include:
     0 - not matched
     1 - matched
     2 - uncertain
  '''
  
  # assign label
  candidate_pairs_pd.loc[ candidate_pairs_pd['z_cluster']==z_cluster, 'z_isMatch'] = label
  
  return
 
def count_labeled_pairs(marked_pd):
  '''
  The purpose of this function is to count the labeled pairs in the marked folder.
  '''

  n_total = len(np.unique(marked_pd['z_cluster']))
  n_positive = len(np.unique(marked_pd[marked_pd['z_isMatch']==1]['z_cluster']))
  n_negative = len(np.unique(marked_pd[marked_pd['z_isMatch']==0]['z_cluster']))
  
  return n_positive, n_negative, n_total

# setup widget 
available_labels = {
    'No Match':0,
    'Match':1,
    'Uncertain':2
    }
#dbutils.widgets.dropdown('label', 'Uncertain', available_labels.keys(), 'Is this pair a match?')


```

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*W1Rf7w2KSp6EyMGWh3emnw.png" alt=""><figcaption><p>Image by Author</p></figcaption></figure>

If you’re unsure what this means, think of these folders as “buckets” where Zingg will store its work.

***

#### **Step 5: Now lets start building out our Zingg Arguments.**

```python

#build the arguments for zingg
args = Arguments()
# Set the modelid and the zingg dir. You can use this as is
args.setModelId(modelId)
args.setZinggDir(zinggDir)
```

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*W1byUQwYEzH5QaYeU6z6Uw.png" alt=""><figcaption><p>Building arguments for Zingg. Image by Author</p></figcaption></figure>

***

#### Step 6: Load Your Data

Zingg supports multiple file formats like CSV, Parquet, or JSON. For this example, let’s use a CSV file. Upload your file to Databricks by dragging it into the **Data** tab. Define its schema (i.e., column names and types):

```python
schema = "rec_id string, fname string, lname string, stNo string, add1 string, add2 string, city string, state string, dob string, ssn string"
inputPipe = CsvPipe("testFebrl", "/FileStore/tables/data.csv", schema)

args.setData(inputPipe)
```

Replace `/FileStore/tables/data.csv` with your actual file path.

So our data look something like this:

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*h6kSKTd4fURzx2cuwjwe9Q.gif" alt=""><figcaption><p>Loading data on Zingg. Image by Author</p></figcaption></figure>

Now lets Configure the output, similarly as loading the data, output can be a CSV , Parquet ,Delta Tables, etc.

```python
#setting outputpipe in 'args'
outputPipe = CsvPipe("resultOutput", "/tmp/output26Nov_1")
args.setOutput(outputPipe)
```

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*fJeS0jGVsg9SPL2ky1dH6g.png" alt=""><figcaption><p>Configuring the Output. Image by Author</p></figcaption></figure>

***

#### Step 7: Define Matching Rules

Here’s where Zingg starts to shine. It uses your rules to decide how to compare records. Let’s say you want to match people based on first name, last name, and city. Define these fields in your notebook:

```python
# Set field definitions
rec_id = FieldDefinition("rec_id", "string", MatchType.DONT_USE)
fname = FieldDefinition("fname", "string", MatchType.FUZZY)  # First Name
lname = FieldDefinition("lname", "string", MatchType.FUZZY)  # Last Name
stNo = FieldDefinition("stNo", "string", MatchType.FUZZY)    # Street Number
add1 = FieldDefinition("add1", "string", MatchType.FUZZY)    # Address Line 1
add2 = FieldDefinition("add2", "string", MatchType.FUZZY)    # Address Line 2
city = FieldDefinition("city", "string", MatchType.FUZZY)    # City
state = FieldDefinition("state", "string", MatchType.FUZZY)  # State
dob = FieldDefinition("dob", "string", MatchType.EXACT)      # Date of Birth (prefer exact match)
ssn = FieldDefinition("ssn", "string", MatchType.EXACT)      # SSN (should use exact match)

# Create the field definitions list
fieldDefs = [rec_id, fname, lname, stNo, add1, add2, city, state, dob, ssn]

# Set field definitions in args
args.setFieldDefinition(fieldDefs)
```

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*MZ2LufmLJOwyEdYFQ9l-9A.png" alt=""><figcaption><p>Defining the rules for matching particular fields. Image by Author</p></figcaption></figure>

**Field Definitions is defining which fields should appear in the output and whether and how they need to be used in matching.**

Some match types are:

* **EXACT** means records must match perfectly.
* **FUZZY** allows for slight differences, like “Jon” and “John.”

You can get creative here depending on your data!

***

#### Step 8: Lets tune the Zingg Performance

The numPartitions define how data is split across the cluster. Please change this as per your data and cluster size by referring to the performance section of the Zingg docs. The labelDataSampleSize is used for sampling in findTrainingData. It lets Zingg select pairs for labeling in a reasonable amount of time. If the findTrainingData phase is taking too much time, please reduce this by atleast 1/10th of its previous value and try again.

```python
# The numPartitions define how data is split across the cluster. 
# Please change the fllowing as per your data and cluster size by referring to the docs.

args.setNumPartitions(4)
args.setLabelDataSampleSize(0.5)
```

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*nzAlr28aqFPPSGKFx7IPww.png" alt=""><figcaption><p>Tuning Zingg performance and finding training data. Image by Author</p></figcaption></figure>

***

#### Step 9: Label Data for Training

Zingg can’t magically know how to match your data — it needs your guidance! It generates candidate pairs, which are potential matches, for you to review:

```python
options = ClientOptions([ClientOptions.PHASE,"findTrainingData"])

#Zingg execution for the given phase
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()
```

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*Kjt40uAhDGt3LynBbogJgA.png" alt=""><figcaption><p>Image by Author</p></figcaption></figure>

Review these pairs in Databricks and manually label them as matches or non-matches or uncertain. Think of it as teaching Zingg what’s right and wrong.

***

#### Step 10: Lets prepare for user labelling and see if we have records for labeling.

```python
options = ClientOptions([ClientOptions.PHASE,"label"])

#Zingg execution for the given phase
zingg = ZinggWithSpark(args, options)
zingg.init()
```

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*Kjt40uAhDGt3LynBbogJgA.png" alt=""><figcaption><p>Prepare for user Labeling. Image by Author</p></figcaption></figure>

```python
# get candidate pairs
candidate_pairs_pd = getPandasDfFromDs(zingg.getUnmarkedRecords())
 
# if no candidate pairs, run job and wait
if candidate_pairs_pd.shape[0] == 0:
  print('No unlabeled candidate pairs found.  Run findTraining job ...')

else:
    # get list of pairs (as identified by z_cluster) to label 
    z_clusters = list(np.unique(candidate_pairs_pd['z_cluster'])) 

    # identify last reviewed cluster
    last_z_cluster = '' # none yet

    # print candidate pair stats
    print('{0} candidate pairs found for labeling'.format(len(z_clusters)))
```

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*-eHNKUruyaIEL-ZFgnxutg.png" alt=""><figcaption><p>Fetching records for Labeling. Image by Author</p></figcaption></figure>

***

#### Step 11: Start labeling to make Zingg learn how we want to match our data

```python
# Label Training Set

# define variable to avoid duplicate saves
ready_for_save = False
print(candidate_pairs_pd)

# user-friendly labels and corresponding zingg numerical value
# (the order in the dictionary affects how displayed below)
LABELS = {
  'Uncertain':2,
  'Match':1,
  'No Match':0  
  }

# GET CANDIDATE PAIRS
# ========================================================
#candidate_pairs_pd = get_candidate_pairs()
n_pairs = int(candidate_pairs_pd.shape[0]/2)
# ========================================================

# DEFINE IPYWIDGET DISPLAY
# ========================================================
display_pd = candidate_pairs_pd.drop(
  labels=[
    'z_zid', 'z_prediction', 'z_score', 'z_isMatch', 'z_zsource'
    ], 
  axis=1)

# define header to be used with each displayed pair
html_prefix = "<p><span style='font-family:Courier New,Courier,monospace'>"
html_suffix = "</p></span>"
header = widgets.HTML(value=f"{html_prefix}<b>" + "<br />".join([str(i)+"&nbsp;&nbsp;" for i in display_pd.columns.to_list()]) + f"</b>{html_suffix}")

# initialize display
vContainers = []
vContainers.append(widgets.HTML(value=f'<h2>Indicate if each of the {n_pairs} record pairs is a match or not</h2></p>'))

# for each set of pairs
for n in range(n_pairs):

  # get candidate records
  candidate_left = display_pd.loc[2*n].to_list()
  print(candidate_left)
  candidate_right = display_pd.loc[(2*n)+1].to_list()
  print(candidate_right)

  # define grid to hold values
  html = ''

  for i in range(display_pd.shape[1]):

    # get column name
    column_name = display_pd.columns[i]

    # if field is image
    if column_name == 'image_path':

      # define row header
      html += '<tr>'
      html += '<td><b>image</b></td>'

      # read left image to encoded string
      l_endcode = ''
      if candidate_left[i] != '':
        with open(candidate_left[i], "rb") as l_file:
          l_encode = base64.b64encode( l_file.read() ).decode()

      # read right image to encoded string
      r_encode = ''
      if candidate_right[i] != '':
        with open(candidate_right[i], "rb") as r_file:
          r_encode = base64.b64encode( r_file.read() ).decode()      

      # present images
      html += f'<td><img src="data:image/png;base64,{l_encode}"></td>'
      html += f'<td><img src="data:image/png;base64,{r_encode}"></td>'
      html += '</tr>'

    elif column_name != 'image_path':  # display text values

      if column_name == 'z_cluster': z_cluster = candidate_left[i]

      html += '<tr>'
      html += f'<td style="width:10%"><b>{column_name}</b></td>'
      html += f'<td style="width:45%">{str(candidate_left[i])}</td>'
      html += f'<td style="width:45%">{str(candidate_right[i])}</td>'
      html += '</tr>'

  # insert data table
  table = widgets.HTML(value=f'<table data-title="{z_cluster}" style="width:100%;border-collapse:collapse" border="1">'+html+'</table>')
  z_cluster = None

  # assign label options to pair
  label = widgets.ToggleButtons(
    options=LABELS.keys(), 
    button_style='info'
    )

  # define blank line between displayed pair and next
  blankLine=widgets.HTML(value='<br>')

  # append pair, label and blank line to widget structure
  vContainers.append(widgets.VBox(children=[table, label, blankLine]))

# present widget
display(widgets.VBox(children=vContainers))
# ========================================================

# mark flag to allow save 
ready_for_save = True
```

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*TfGYRWRKTUHTOcKuYNMypg.gif" alt=""><figcaption><p>User labeling. Image by Author</p></figcaption></figure>

Then , we just have to save the labels provided from the above code.

```python
if not ready_for_save:
  print('No labels have been assigned. Run the previous cell to create candidate pairs and assign labels to them before re-running this cell.')

else:

  # ASSIGN LABEL VALUE TO CANDIDATE PAIRS IN DATAFRAME
  # ========================================================
  # for each pair in displayed widget
  for pair in vContainers[1:]:

    # get pair and assigned label
    html_content = pair.children[1].get_interact_value() # the displayed pair as html
    user_assigned_label = pair.children[1].get_interact_value() # the assigned label

    # extract candidate pair id from html pair content
    start = pair.children[0].value.find('data-title="')
    if start > 0: 
      start += len('data-title="') 
      end = pair.children[0].value.find('"', start+2)
    pair_id = pair.children[0].value[start:end]



    # assign label to candidate pair entry in dataframe
    candidate_pairs_pd.loc[candidate_pairs_pd['z_cluster']==pair_id, 'z_isMatch'] = LABELS.get(user_assigned_label)
  # ========================================================

  # SAVE LABELED DATA TO ZINGG FOLDER
  # ========================================================
  # make target directory if needed
  dbutils.fs.mkdirs(MARKED_DIR)
  
  # save label assignments
  # save labels
  zingg.writeLabelledOutputFromPandas(candidate_pairs_pd,args)

  # count labels accumulated
  marked_pd_df = getPandasDfFromDs(zingg.getMarkedRecords())
  n_pos, n_neg, n_tot = count_labeled_pairs(marked_pd_df)
  print(f'You have accumulated {n_pos} pairs labeled as positive matches.')
  print(f'You have accumulated {n_neg} pairs labeled as not matches.')
  print("If you need more pairs to label, re-run the cell for 'findTrainingData'")
  # ========================================================  

  # save completed
  ready_for_save = False
```

***

#### Step 12: Train the Model

After labeling, it’s time to let Zingg do the heavy lifting. Training adjusts its algorithms to your specific dataset. Start training with this command:

```python
options = ClientOptions([ClientOptions.PHASE,"trainMatch"])

#Zingg execution for the given phase
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()
```

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*gWSb81ifiMXC-ufzRheZQQ.gif" alt=""><figcaption><p>Trining the Zingg model. Image by Author</p></figcaption></figure>

Sit back and relax — Zingg will process the data and build your model.

***

#### Step 13: Predict Matches

Once the model is ready, you can run predictions to see which records are likely matches. The output will be stored in the folder you specified earlier:

```python
outputDF = spark.read.csv("/tmp/output26Nov_1")

colNames = ["z_minScore", "z_maxScore", "z_cluster", "rec_id", "fname", "lname", "stNo", "add1", "add2", "city", "state", "dob", "ssn"]
outputDF.toDF(*colNames).show(100)
```

**z\_minScore:** It represents the **minimum similarity score threshold** for two records to be considered a potential match.

**z\_maxScore:** It represents the **maximum similarity score threshold** for two records to be considered a match or part of the same cluster.

**z\_cluster:** It refers to the clustering logic or grouping of records based on similarity scores. It typically defines how groups of similar entities are formed after applying Z\_MIN and Z\_MAX thresholds.

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*GA3FABFY9rsPj5ZIbA8fqw.gif" alt=""><figcaption><p>Output with a confidence score of matched pairs. Image by Author</p></figcaption></figure>

This will display the matched pairs, along with a confidence score. Review these to ensure accuracy.

***

#### Step 14: Visualize Results

Zingg creates detailed documentation to help you understand how it makes decisions. Generate and view the docs with:

```python
options = ClientOptions([ClientOptions.PHASE,"generateDocs"])

#Zingg execution for the given phase
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()
```

```python
DOCS_DIR = zinggDir + "/" + modelId + "/docs/"
dbutils.fs.ls('file:'+DOCS_DIR)
```

**Check Your Labeling**

Use to below code to check the the number of matches and non-matches you have performed, in the label phase.

```python
displayHTML(open(DOCS_DIR+"model.html", 'r').read())
```

<figure><img src="https://cdn-images-1.medium.com/max/1000/1*ETeXvrwY-97h5N_RP5_prA.gif" alt=""><figcaption><p>Visual of labeling done by the user. Image by Author</p></figcaption></figure>

The below code will display your metadata such as, Field Name as name of the field, Field Type and Nullable.

```python
displayHTML(open(DOCS_DIR+"data.html", 'r').read())
```

The report includes visual insights into the matching process, such as which fields contributed most to the decisions.

***

#### Conclusion !

At last, you’ve just implemented your first entity resolution pipeline on Databricks with Zingg!

Zingg can help in whether you’re deduplicating customer data or linking disparate datasets the above process can save hours of manual effort. When you get more comfortable, we would love if you explore [Zingg’s advanced features](https://docs.zingg.ai/zingg/zmodels) like blocking strategies and custom match algorithms.
