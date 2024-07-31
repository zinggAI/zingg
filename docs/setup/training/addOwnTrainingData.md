---
parent: Creating training data
nav_order: 3
title: Using preexisting training data
grand_parent: Step By Step Guide
---

# Using Pre-existing Training Data

## Supplementing Zingg With Existing Training Data

If you already have some training data that you want to start with, you can use that as well with Zingg. Add an attribute **trainingSamples** to the config and define the training pairs.

The training data supplied to Zingg should have a **z\_cluster** column that groups the records together. The **z\_cluster** uniquely identifies the group. We also need to add the **z\_isMatch** column which is **1** if the pairs _match_ or **0** if they do _not_ match. The **z\_isMatch** value has to be the same for all the records in the **z\_cluster** group. They either match with each other or they don't.

An example is provided in [GitHub training data](../../../examples/febrl/training.csv).

The above training data can be specified using [trainingSamples attribute in the configuration.](../../../examples/febrl/configWithTrainingSamples.json)

In addition, labeled data of one model can also be exported and used as training data for another model. For details, check out [exporting labeled data](exportLabeledData.md).

**Note**: It is advisable to still run [findTrainingData](findTrainingData.md) and [label](label.md) a few rounds to tune Zingg with the supplied training data as well as patterns it needs to learn independently.
