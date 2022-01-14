---
layout: default
parent: Creating training data
nav_order: 3
title: Using preexisting training data
grand_parent: Step By Step Guide
---

### Using preexisting training data
#### Supplementing Zingg with existing training data

If you alredy have some training data that you want to start with, you can use that as well with Zingg. Add an attribute trainingSamples to the config and define the training pairs. 

The training data supplied to Zingg should have z_cluster column which groups the records together. It also needs z_isMatch column which is 1 if the pairs match or 0 if they do not match.

An example is provided at [Github training data](https://github.com/zinggAI/zingg/blob/main/examples/febrl/training.csv)

The above training data can be specified using [trainingSamples attribute in the configuration.](https://github.com/zinggAI/zingg/blob/main/examples/febrl/configWithTrainingSamples.json)

In addition, labelled data of one model can also be exported and used as training data for another model. For details, check out [exporting labelled data](exportLabeledData.md).

Please note: It is advisable to still run [findTrainingData](./findTrainingData.html) and [label](./label.html) a few rounds to tune Zingg with the supplied training data as well as patterns it needs to learn independently. 
