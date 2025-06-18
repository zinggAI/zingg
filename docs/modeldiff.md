---
title: Comparing two models
parent: Step By Step Guide
nav_order: 15
description: Comparison of two outputs with different models
---

# Model Difference

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

Let us take the case where we have an existing model where we have marked some fields as fuzzy and we then build a model and look at its match output. Now, we train another model where we've marked some of these attributes as exact or maybe added more match types or even change some field types, etc. Here, the primary key remains the same. 

We want to understand how those changes are translating into either a better or worse model. Also, what other changes that we could make to get the model to the kind of accuracy that we are looking for.

Comparison of the two outputs becomes important in such a case and understanding which model is working better for us.

### The model difference phase is run as follows:

`./scripts/zingg.sh --phase diff –conf <path to new model conf> --compareTo <path to original conf>`

The output will be as follows -&#x20;

`zingg_modelDiff_originalModelId_newModelId` in case of snowflake
`zinggDir/newModelId/modeldiff/originalModelId_newModelId` in case of spark

The output will contain records that have been impacted due to changes in clusters as a result of the new model trained. 

[^1]: Zingg Enterprise is an advance version of Zingg Community with production grade features
