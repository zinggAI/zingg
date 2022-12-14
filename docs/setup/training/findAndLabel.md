---
parent: Creating training data
title: Find training data and labelling
grand_parent: Step By Step Guide
nav_order: 2
---

# Find And Label

This phase is composed of two phases namely [findTrainingData](findTrainingData.md) and [label](label.md). This will help experienced users to quicken the process of creating Training data.

`./zingg.sh --phase findAndLabel --conf config.json`

As this is phase runs findTrainingData and label together, it should be run only for small datasets where findTrainingData takes a short time to run, else the the user will have to wait long for the console for labeling.&#x20;
