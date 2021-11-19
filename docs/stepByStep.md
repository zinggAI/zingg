## Step by step guide to running Zingg
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}
### Step 1: Install
Installation instructions [here](installation.md). If you need to build from the sources or compile for a different flavor of Spark, check [compiling](compiling.md)

## Step 2: Build the config for your data
Zingg needs a configuration file which defines the data and what kind of matching is needed. You can create the configuration file by following the instructions [here](configuration.md)

## Step 3: Build the training data
Zingg builds a new set of models(blocking and similarity) for every new schema definition(columns and match types). This means running the *findTrainingData* and *label* phases multiple times to build the training dataset form which Zingg will learn. You can read more [here](running.md)

## Step 4: Build and save the model
The training data in Step 3 above is used to train Zingg and build and save the models. This is done by running the *train* phase. Read more [here](running.md)

## Step 5: Voila, lets match!
Its now time to apply the model above on our data. This si done by running the *match* or the *link* phases depending on whether you are matching within a single source or linking multiple sources respectively. You can read more [here](running.md)

As long as your input columns and the field types are not changing, the same model should work and you do not need to build a new model. 
