# Tuning Label, Match And Link Jobs

#### numPartitions

The number of Spark partitions over which the input data is distributed. Keep it equal to 20-30 times the number of cores. This is an important configuration for performance.

#### labelDataSampleSize

Fraction of the data to be used for training the models. Adjust it between 0.0001 and 0.1 to keep the sample size small enough so that it finds enough edge cases fast. If the size is bigger, the findTrainingData job will spend more time combing through samples. If the size is too small, Zingg may not find the right edge cases.
