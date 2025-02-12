---
parent: Creating training data
nav_order: 1
grand_parent: Step By Step Guide
description: Pairs of records that match or dont so as to train Zingg
---

# Finding Records For Training Set Creation

The **findTrainingData** phase prompts Zingg to search for edge cases in the data which can be labeled by the user and used for learning. During this phase, Zingg combs through the data samples and judiciously selects limited representative pairs that can be labelled by the user. Zingg is very frugal about the training so that user effort is minimized and models can be built and deployed quickly.

This **findTrainingData** job writes the edge cases to the folder configured through `zinggDir/modelId` in the config:

`./zingg.sh --phase findTrainingData --conf config.json`

The **findTrainingData** phase is run first and then the label phase is run and this cycle is repeated so that the Zingg models get smarter from user feedback.
