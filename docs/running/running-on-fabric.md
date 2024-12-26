---
description: Step by Step Identity Resolution with Zingg on Microsoft Fabric
---

# Running on Fabric

#### **Implementing Zingg on Fabric: A Step-by-Step Guide**

When working with data from different systems or sources, cleaning, matching, and linking data is crucial. These processes can be challenging, especially with large datasets or complex matching rules. That’s where [**Zingg.ai**](http://zingg.ai/), an open-source entity resolution tool, comes in handy. When paired with **Microsoft Fabric** and enhanced by the data governance capabilities of **Microsoft Purview**, we have a powerful combination that simplifies the data integration tasks. Let’s explore how we can use these tools together to achieve seamless entity resolution and data governance.

***

#### **Integrating Zingg with Purview**

Microsoft Purview is a unified data governance solution that helps us manage and discover data assets. By combining Zingg’s entity resolution capabilities with Purview, we can:

* Maintain a clear lineage of matched and merged data.
* Ensure compliance with governance standards.
* Enhance the trustworthiness of our data.

#### **Benefits of Combining Purview with Zingg**

* Purview’s cataloging features allow us to track every step of the data matching and resolution process.
* With Purview, we can secure sensitive data while performing matching and linking operations.
* We can keep a detailed record of transformations and resolutions, making compliance audits seamless.

#### **Step 1: Set Up Fabric Account**

If you’re new to Fabric, the first step is creating an account. Don’t worry; it’s straightforward!

1. Head over to Fabric and sign up for a free trial.
2. Once inside, take a quick tour of the interface. The **Workspace** tab is where you’ll organize your work. Lets give it a name like “_Zingg-Fabric”._

_Tip:_ If you’re unsure about session cluster settings, choose a New Standard Session

\


<figure><img src="https://cdn-images-1.medium.com/max/800/1*ho-cPkI3NmnnNktDYszttw.png" alt=""><figcaption><p>Creating a Session on Fabric. Image by Author.</p></figcaption></figure>

***

#### **Step 2: Create a Environment and Install Zingg**

Go the Environment tab, here we will be having a Default workspace environment.\


<figure><img src="https://cdn-images-1.medium.com/max/800/1*Awd2ECMLeCqdU3qBGJsZQw.png" alt=""><figcaption><p>Selecting the Environment for running the Notebook. Image by Author.</p></figcaption></figure>

We have click on the New Environment button so that we can install the Zingg Jar file (zingg-0.4.0.jar) in the custom libraries

We can find the Zingg jar file from [Zingg-Github](https://github.com/zinggAI/zingg/releases) . then navigate to custom libraries and upload the Jar file

By this we have successfully installed the jar file. We are ready to move to the next step

***

#### Step 3: Setup Spark Checkpoint Directory and Install Zingg Python Package

Below code sets the checkpoint directory for Spark to a specified path in Azure Data Lake Storage Gen2, enabling fault-tolerant processing by storing intermediate RDD states.

```
spark.sparkContext.setCheckpointDir("abfss://<container-name>@<storage-account-name>.dfs.core.windows.net/<directory-path>")
```

To ensure Zingg runs smoothly, we also need the Python package. Create a Fabric notebook in our workspace and type this:

```python
!pip install zingg
```

<figure><img src="https://cdn-images-1.medium.com/max/800/1*PLhGFD2EPz8-ulFD3yWgbg.png" alt=""><figcaption><p>Installing Zingg. Image by Author.</p></figcaption></figure>

This command fetches and installs the Zingg Python library and restart the Python kernel so that you can use updated package. Also, double-check that Zingg uses the `tabulate` library, which should already be installed on your machine. If not, install it and verify that all dependencies are in place.

Use the following command to install the required dependencies, especially `tabulate` and restart the Python kernel:

```python
!pip install tabulate
```

<figure><img src="https://cdn-images-1.medium.com/max/800/1*X5j_QiuWHpkVwwpIsenjJQ.png" alt=""><figcaption><p>Installing Tabulate. Image by Author.</p></figcaption></figure>

***

#### Step 4: Organize Your Workspace

It’s a good idea to keep things neat, especially when you’re working with multiple datasets and training models. Zingg requires specific folders for its labeled and unlabeled training data. Set up directory paths in your notebook:

```python
##you can change these to the locations of your choice
##these are the only two settings that need to change
zinggDir = "abfss://<container-name>@<storage-account-name>.dfs.core.windows.net/<directory-path>"
modelId = "testModelFabric"
```

<figure><img src="https://cdn-images-1.medium.com/max/800/1*yKMrz04ULIu_W-UNZnj1pw.png" alt=""><figcaption><p>Setting up the Directory. Image by Author.</p></figcaption></figure>

```python
## Define constants
MARKED_DIR = zinggDir + "/" + modelId + "/trainingData/marked/"
UNMARKED_DIR = zinggDir + "/" + modelId + "/trainingData/unmarked/"

# Fill these with your specific details
storage_account = "# Replace with your storage account ID"  

fabric_url = "dfs.fabric.microsoft.com"

# Updated paths for Microsoft Fabric
MARKED_DIR_DBFS = f"abfss://{storage_account}@{fabric_url}{MARKED_DIR}"
UNMARKED_DIR_DBFS = f"abfss://{storage_account}@{fabric_url}{UNMARKED_DIR}"

## Import necessary libraries
import pandas as pd
import numpy as np
import os
import time
import uuid
from tabulate import tabulate
from ipywidgets import widgets, interact, GridspecLayout
import base64
import pyspark.sql.functions as fn

# Import Azure libraries for Fabric
from azure.identity import DefaultAzureCredential
from azure.storage.filedatalake import DataLakeServiceClient

# Zingg libraries
from zingg.client import *
from zingg.pipes import *

# Setup Fabric authentication
def get_service_client():
    credential = DefaultAzureCredential()
    service_client = DataLakeServiceClient(
        account_url=f"https://{storage_account}.dfs.fabric.microsoft.com",
        credential=credential,
    )
    return service_client

service_client = get_service_client()

# Function to clean model directories in Fabric
def cleanModel():
    try:
        # Access the file system
        file_system_client = service_client.get_file_system_client(file_system=storage_account)
        
        # Remove marked directory
        if file_system_client.get_directory_client(MARKED_DIR).exists():
            file_system_client.get_directory_client(MARKED_DIR).delete_directory()
        
        # Remove unmarked directory
        if file_system_client.get_directory_client(UNMARKED_DIR).exists():
            file_system_client.get_directory_client(UNMARKED_DIR).delete_directory()
        
        print("Model cleaned successfully.")
    except Exception as e:
        print(f"Error cleaning model: {str(e)}")
    return

# Function to assign label to a candidate pair
def assign_label(candidate_pairs_pd, z_cluster, label):
    '''
    The purpose of this function is to assign a label to a candidate pair
    identified by its z_cluster value. Valid labels include:
       0 - not matched
       1 - matched
       2 - uncertain
    '''
    # Assign label
    candidate_pairs_pd.loc[candidate_pairs_pd['z_cluster'] == z_cluster, 'z_isMatch'] = label
    return

# Function to count labeled pairs
def count_labeled_pairs(marked_pd):
    '''
    The purpose of this function is to count the labeled pairs in the marked folder.
    '''
    n_total = len(np.unique(marked_pd['z_cluster']))
    n_positive = len(np.unique(marked_pd[marked_pd['z_isMatch'] == 1]['z_cluster']))
    n_negative = len(np.unique(marked_pd[marked_pd['z_isMatch'] == 0]['z_cluster']))

    return n_positive, n_negative, n_total

# Setup interactive widget
available_labels = {
    'No Match': 0,
    'Match': 1,
    'Uncertain': 2
}
```

<figure><img src="https://cdn-images-1.medium.com/max/800/1*W1Rf7w2KSp6EyMGWh3emnw.png" alt=""><figcaption><p>Defining Functions and Directories. Image by Author.</p></figcaption></figure>

If you’re unsure what this means, think of these folders as “buckets” where Zingg will store its work.

***

#### Step 5: Now lets start building out our Zingg Arguments.

```python
#build the arguments for zingg
args = Arguments()
# Set the modelid and the zingg dir. You can use this as is
args.setModelId(modelId)
args.setZinggDir(zinggDir)
print(args)
```

<figure><img src="https://cdn-images-1.medium.com/max/800/1*5rwSBCKIjeXiWrwSQYIV2Q.png" alt=""><figcaption><p>Building Zingg Arguments. Image by Author.</p></figcaption></figure>

#### Step 6: Load Your Data

Zingg supports multiple file formats like CSV, Parquet, or JSON. For this example, let’s use a CSV file. Upload your file to Fabric.

Define its schema (i.e., column names and types):

```python
schema = "rec_id string, fname string, lname string, stNo string, add1 string, add2 string, city string, state string, dob string, ssn string"
inputPipe = CsvPipe("<pipe-name>", "abfss://<container-name>@<storage-account-name>.dfs.core.windows.net/<directory-path>/<file-name>.csv", <schema>)
args.setData(inputPipe)
```

Replace `"<pipe-name>", "abfss://<container-name>@<storage-account-name>.dfs.core.windows.net/<directory-path>/<file-name>.csv"` with your actual file path.

So our data look something like this:

<figure><img src="https://cdn-images-1.medium.com/max/800/1*JB1Mf_-qyimtXRmtnVVgOg.png" alt=""><figcaption><p>Loading Data from the Onelake. Image by Author.</p></figcaption></figure>

Now lets Configure the output, similarly as loading the data, output can be a CSV , Parquet ,Delta Tables, etc.

```python
#setting outputpipe in 'args'
outputPipe = CsvPipe("resultOutput", "abfss://<container-name>@<storage-account-name>.dfs.core.windows.net/<directory-path>")
args.setOutput(outputPipe)
```

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

<figure><img src="https://cdn-images-1.medium.com/max/800/1*s1WCuMaFVesfj_M4KfHJ2Q.png" alt=""><figcaption><p>Setting Field Definitions. Image by Author.</p></figcaption></figure>

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

<figure><img src="https://cdn-images-1.medium.com/max/800/1*EEEEeoC9n86oFTW7CsRjPg.png" alt=""><figcaption><p>Setting Sample Size. Image by Author.</p></figcaption></figure>

#### Step 9: Label Data for Training

Zingg can’t magically know how to match your data — it needs your guidance! It generates candidate pairs, which are potential matches, for you to review:

```python
options = ClientOptions([ClientOptions.PHASE,"findTrainingData"])
#Zingg execution for the given phase
zingg = ZinggWithSpark(args, options)
print(args)
print(options)
print(zingg)
zingg.initAndExecute()
```

<figure><img src="https://cdn-images-1.medium.com/max/800/1*RqAmKemfR1eH9Ndj7CquMg.png" alt=""><figcaption><p>Running the FindTrainingData Phase. Image by Author.</p></figcaption></figure>

Review these pairs in Fabric and manually label them as matches or non-matches or uncertain. Think of it as teaching Zingg what’s right and wrong.

***

#### Step 10: Lets prepare for user labeling and see if we have records for labeling.

```python
options = ClientOptions([ClientOptions.PHASE,"label"])
#Zingg execution for the given phase
zingg = ZinggWithSpark(args, options)
zingg.init()
```

<figure><img src="https://cdn-images-1.medium.com/max/800/1*bsDugxq58aKFGuXuBJZcZQ.png" alt=""><figcaption><p>Preparing for the Labeling Phase. Image by Author.</p></figcaption></figure>

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

<figure><img src="https://cdn-images-1.medium.com/max/800/1*8maOsURCaII0YmblWRy7Cg.png" alt=""><figcaption><p>Getting Candidate Pairs for User Labeling. Image by Author.</p></figcaption></figure>

#### Step 11: Start labeling to make Zingg learn how you want to match our data

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

<figure><img src="https://cdn-images-1.medium.com/max/800/1*gkXaA0-naOyKgKxkxlEvhA.png" alt=""><figcaption><p>User Labeling. Image by Author.</p></figcaption></figure>

Then , you just have to save the labels provided from the above code.

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
  notebookutils.fs.mkdirs(MARKED_DIR)
  
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

<figure><img src="https://cdn-images-1.medium.com/max/800/1*VX3ayQUNXncc_emmZOA5Mg.png" alt=""><figcaption><p>Saving the User Labeling. Image by Author.</p></figcaption></figure>

#### Step 12: Train the Model

After labeling, it’s time to let Zingg do the heavy lifting. Training adjusts its algorithms to your specific dataset. Start training with this command:

```python
options = ClientOptions([ClientOptions.PHASE,"trainMatch"])
#Zingg execution for the given phase
zingg = ZinggWithSpark(args, options)
zingg.initAndExecute()
```

<figure><img src="https://cdn-images-1.medium.com/max/800/1*gUTpMpMSuNg_IfOp3lyOtQ.png" alt=""><figcaption><p>Running the TrainMatch Phase. Image by Author.</p></figcaption></figure>

Sit back and relax — Zingg will process the data and build your model.

***

#### Step 13: Predict Matches

Once the model is ready, you can run predictions to see which records are likely matches. The output will be stored in the folder you specified earlier and select one file:

```python
outputDF = spark.read.csv("abfss://<container-name>@<storage-account-name>.dfs.core.windows.net/<directory-path>/part-00000-63e9bd54-844b-401c-8033-f10282e7105d-c000.csv")

colNames = ["z_minScore", "z_maxScore", "z_cluster", "rec_id", "fname", "lname", "stNo", "add1", "add2", "city", "state", "dob", "ssn"]
outputDF.toDF(*colNames).show(100)
```

**z\_minScore: It r**epresents the **minimum similarity score threshold** for two records to be considered a potential match.

**z\_maxScore: It r**epresents the **maximum similarity score threshold** for two records to be considered a match or part of the same cluster.

**z\_cluster: It r**efers to the clustering logic or grouping of records based on similarity scores. It typically defines how groups of similar entities are formed after applying **z\_minScore** and **z\_maxScore** thresholds.

<figure><img src="https://cdn-images-1.medium.com/max/800/1*QhJoDcTYjXf-TCyQPiQVOg.png" alt=""><figcaption><p>Reading the Matches. Image by Author.</p></figcaption></figure>

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

#### Check Your Labeling

Use to below code to check the the number of matches and non-matches you have performed, in the label phase.

```python
displayHTML(open(DOCS_DIR+"model.html", 'r').read())
```

<figure><img src="https://cdn-images-1.medium.com/max/800/1*SUYs9Hkcxwyo6jUBgsvhJA.png" alt=""><figcaption><p>Check Your Labeling. Image by Author.</p></figcaption></figure>

The below code will display your metadata such as, Field Name as name of the field, Field Type and Nullable.

```python
displayHTML(open(DOCS_DIR+"data.html", 'r').read())
```

The report includes visual insights into the matching process.

***

#### Conclusion !

At last, you’ve just implemented your first entity resolution pipeline on Microsoft Fabric with Zingg!

By combining Zingg’s entity resolution capabilities with Microsoft Fabric’s analytics power and Purview’s governance features, we can handle even the most complex data challenges. This integration not only simplifies our workflows but also ensures that our data is clean, compliant, and ready for analysis.

When you get more comfortable, we would love if you explore [Zingg’s advanced features](https://docs.zingg.ai/zingg/zmodels) like blocking strategies and custom match algorithms.
