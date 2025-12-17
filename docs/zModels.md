---
title: Zingg Models
nav_order: 5
has_children: true
---

# Zingg Models

Zingg learns two models from the data.

## 1. Blocking Model

One fundamental problem with scaling data mastering is that the number of comparisons increases **quadratically** as the number of input records increases.

![Data Mastering At Scale](.gitbook/assets/fuzzymatchingcomparisons.jpg)

Zingg learns a clustering/blocking model which indexes near similar records. This means that Zingg does not compare every record with every other record. Typical Zingg comparisons are **0.05-1%** of the possible problem space.

## 2. Similarity Model

The similarity model helps Zingg to predict which record pairs match. The similarity is run only on records within the same block/cluster to scale the problem to larger datasets. The similarity model is a classifier that predicts the similarity of records that are not exactly the same but could belong together.

![Fuzzy matching comparisons](.gitbook/assets/dataMatching.jpg)

To build these models, training data is needed. Zingg comes with an interactive learner to rapidly build training sets.

![Shows records and asks user to mark yes, no, cant say on the cli.](.gitbook/assets/label2.gif)
