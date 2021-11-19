---
layout: default
title: Zingg Command Line Phases
parent: Running Zingg
nav_order: 3
has_children: true
has_toc: false
---
## Zingg Command Line Phases
Zingg runs Spark jobs for building training data(findTrainingData and label), building actual models(train) and applying these models on the data to get mastered entities(match). If you need to match records in one dataset against other, you can run the link phase. The phase to be run is passed as a command line argument. Here are more details about the phases and how they can be invoked.

- TOC
{:toc}

The findTrainingData phase is run first and then the label phase is run and this cycle repeated so that the Zingg models get smarter from user feedback.This generates edge cases for labelling and the user marks them. At each stage, the user will get different variations of attributes across the records. 

Proceed till you have at least 30-40 positives, or when you see the predictions by Zingg converging with the output you want. Zingg performs pretty well with even small number of training, as the samples to be labelled are chosen by the algorithm itself. 


