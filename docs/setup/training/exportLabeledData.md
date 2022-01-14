---
layout: default
parent: Creating training data
title: Exporting Labeled data as Training data
grand_parent: Step By Step Guide
nav_order: 4
---
## Export labeled data from zingg learner and import it to new zingg instance/model

Labelled data of one model can be exported and used as training data for another model.

Please follow the below instructions to do the same.

pyspark can be run from the below location. Please run it from $ZINGG_HOME
```
$ cd $ZINGG_HOME
$ $SPARK_HOME/bin/pyspark
```

The pyspark command will open a pspark shell. The below commands shall be run in pyspark shell.

### Getting Training data
The labelled data can be read into spark as follows. Pass appropriate \<modelId\>.
```
labelledData = spark.read.parquet("models/<modelId>/trainingData/marked")
```
The fields that are added in labelled data by the Zingg. Some of them even become part of final output (match/link).
```
baseCols = ['z_cluster', 'z_zid', 'z_prediction', 'z_score', 'z_source', 'z_isMatch']
```
The below columns are needed for sample training data.
```
additionalTrainingColumns = ['z_cluster','z_isMatch']
```
The below command will filter out all the zingg added columns. sourceDataColumns will contain the original columns which define the user data.
```
sourceDataColumns =  [c for c in labelledData.columns if c not in  baseCols]
```

The list of columns needed for training samples in desired sequence.
```
trainingSampleColumns = [*additionalTrainingColumns, *sourceDataColumns]
```
Select all the required columns from the Labelled data. This data will be our training data.
```
trainingSamples = labelledData.select(trainingSampleColumns)
```
Save the samples in a single csv file. Inside the location folder, a file named like part***.csv will be created. This file may be renamed appropriately and be referred to in zingg config file.
```
trainingSamples.coalesce(1).write.csv("<file-location>")
```
### Getting Schema
The below command will produce schema of training sample data. This will also go into Zingg config file.
```
trainingSamples.schema.jsonValue()
```
Anytime, you can view the list of columns or actual labelled data, print columns etc.

```
trainingSamples.show()
trainingSamples.columns
print(trainingSampleColumns)
```

