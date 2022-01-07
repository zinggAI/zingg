---
layout: default
parent: Creating Training Data
nav_order: 3
---

### Supplementing Zingg with existing training data

If you alredy have some training data that you want to start with, you can use that as well with Zingg. Add an attribute trainingSamples to the config and define the training pairs. 

The training data supplied to Zingg should have z_cluster column which groups th records together. It also needs z_isMatch column which is 1 if the pairs match or 0 if they do not match.

An example is provided at [Github training data](https://github.com/zinggAI/zingg/blob/main/examples/febrl/training.csv)

The above training data can be specified using [trainingSamples attribute in the configuration.](https://github.com/zinggAI/zingg/blob/main/examples/febrl/configWithTrainingSamples.json)

Please note: It is advisable to still run [findTrainingData](./findTrainingData.html) and [label](./label.html) a few rounds to tune Zingg with the supplied training data as well as patterns it needs to learn independently. 