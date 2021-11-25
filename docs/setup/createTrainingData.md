---
layout: default
parent: Step By Step Guide
nav_order: 6
---
## Creating training data
Zingg builds models to predict similarity. Training data is needed to build these models. 


### findTrainingData - finding pairs of records which could be similar to train Zingg
Zingg builds models to predict similarity. Training data is needed to build these models. The findTrainingData phase prompts Zingg to search for edge cases in the data. During findTrainingData, Zingg combs through the data samples and judiciously selects limited pairs for the user to mark. Zingg is very frugal about the training so that user effort is minimized and models can be built and deployed quickly.

This findTrainingData job writes the edge cases to the folder configured throgh zinggDir/modelId in the config. 

`./zingg.sh --phase findTrainingData --conf config.json`


The findTrainingData phase is run first and then the label phase is run and this cycle repeated so that the Zingg models get smarter from user feedback.

### label - user feedback on the training pairs 
This phase opens an interactive learner where the user can mark the pairs found by findTrainingData phase as matches or non matches. The findTrainingData phase generates edge cases for labelling and the label phase helps the user to mark them. 

`./zingg.sh --phase label --conf config.json`

![Shows records and asks user to mark yes, no, cant say on the cli.](/assets/label.gif) 



Proceed running findTrainingData followed by label phases till you have at least 30-40 positives, or when you see the predictions by Zingg converging with the output you want. At each stage, the user will get different variations of attributes across the records. Zingg performs pretty well with even small number of training, as the samples to be labelled are chosen by the algorithm itself. 

