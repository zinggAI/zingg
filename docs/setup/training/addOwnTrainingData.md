---
parent: Creating training data
nav_order: 3
title: Using preexisting training data
grand_parent: Step By Step Guide
description: Instructions on using existing training data with Zingg
---

# Using pre-existing training data

## Supplementing Zingg with existing training data

If you already have some training data that you want to start with, you can use that as well with Zingg. Add an attribute trainingSamples to the config and define the training pairs.

The training data supplied to Zingg should have a z\_cluster column that groups the records together. It also needs the z\_isMatch column which is 1 if the pairs match or 0 if they do not match.

An example is provided in [Github training data](../../../examples/febrl/training.csv).

The above training data can be specified using [trainingSamples attribute in the configuration.](../../../examples/febrl/configWithTrainingSamples.json)

In addition, labeled data of one model can also be exported and used as training data for another model. For details, check out [exporting labeled data](exportLabeledData.md).

Please note: It is advisable to still run [findTrainingData](findTrainingData.md) and [label](label.md) a few rounds to tune Zingg with the supplied training data as well as patterns it needs to learn independently.
