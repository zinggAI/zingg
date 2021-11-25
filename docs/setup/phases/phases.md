---
layout: default
title: Zingg Command Line Phases
parent: Step By Step Guide
nav_order: 6
has_children: true
---
## Zingg Command Line Phases
Zingg runs Spark jobs for building training data(findTrainingData and label), building actual models(train) and applying these models on the data to get mastered entities(match). If you need to match records in one dataset against other, you can run the link phase. The phase to be run is passed as a command line argument. Here are more details about the phases and how they can be invoked.




