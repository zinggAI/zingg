---
description: Instructions on how to install and use Zingg
---

# Step-By-Step Guide

## Step 1: Install

Installation instructions for docker, as well as GitHub release, are [here](setup/installation.md). If you need to build from the sources or compile for a different flavor of Spark, check [compiling](setup/installation.md#compiling-from-sources).

## Step 2: Plan For Hardware

Decide your hardware based on the [performance numbers](setup/hardwareSizing.md).

## Step 3: Build The Config For Your Data

Zingg needs a configuration file that defines the data and what kind of matching is needed. You can create the configuration file by following the instructions [here](stepbystep/configuration/).

## Step 4: Create Training Data

Zingg builds a new set of models(blocking and similarity) for every new schema definition(columns and match types). This means running the _findTrainingData_ and _label_ phases multiple times to build the training dataset from which Zingg will learn. You can read more [here](setup/training/createTrainingData.md).

## Step 5: Build & Save The Model

The training data in Step 4 above is used to train Zingg and build and save the models. This is done by running the _train_ phase. Read more [here](setup/train.md).

## Step 6: Voila, Let's Match!

It's now time to apply the model to our data. This is done by running the _match_ or the _link_ phases depending on whether you are matching within a single source or linking multiple sources respectively. You can read more about [matching](setup/match.md) and [linking](setup/link.md).

As long as your input columns and the field types are not changing, the same model should work and you do not need to build a new model. If you change the match type, you can continue to use the training data and add more labeled pairs on top of it.
