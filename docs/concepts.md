## Key Zingg Concepts

Zingg learns 2 models on the data. 

1. Blocking Model

One fundamental problem with scaling data mastering is that the number of comparisons increase quadratically as the number of input record increases. 

![Data Mastering At Scale](/assets/fuzzymatchingcomparisons.jpg)


Zingg learns a clustering/blocking model which indexes near similar records. This means that Zingg does not compare every record with every other record. Typical Zingg comparisons are 0.05-1% of the possible problem space.

2. Similarity Model 

The similarity model helps Zingg to predict which record pairs match. Similarity is run only on records within the same block/cluster to scale the problem to larger datasets. The similarity model is a classifier which predicts similarity of records which are not exactly same, but could belong together.

![Fuzzy matching comparisons](/assets/dataMatching.jpg) 

To build these models, training data is needed. Zingg comes with an interactive learner to rapidly build training sets. 

![Shows records and asks user to mark yes, no, cant say on the cli.](assets/label2.gif) 