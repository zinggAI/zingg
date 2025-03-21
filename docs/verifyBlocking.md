---
title: Verifying the blocked data
parent: Step By Step Guide
nav_order: 7
description: Understanding how blocking is working before running match or link
---

# Verification of Blocking Model

The Blocking Model ensures that Zingg stays performant. Column spread and values are learnt for the Blocking Model through the training data. Sometimes Zingg jobs are slow or fail due to a poorly learnt blocking model. This can happen due to a variety of reasons like:

* A user adds significantly larger training samples compared to the labelling learnt by Zingg. The manually added training samples may have the same type of columns and blocking rules learnt are not generic enough. For example, providing California state only training data when the matching is using the State column and data has multiple states.
* When there is a natural bias in the data with lots of null columns used in matching.
* When sufficient labeling has not been done.
* When there a lot of non differentiating columns.

Matching is computationally expensive, and If we can have an understanding of how blocking is working, we can decide whether we need to add more training data.

### The verifyBlocking phase is run as follows:

`./scripts/zingg.sh --phase verifyBlocking --conf <path to conf> <optional --zinggDir <location of model>>`

The output contains two directories -&#x20;

`zinggDir/modelId/blocks/timestamp/counts`  `zinggDir/modelId/blocks/timestamp/blockSamples`

We can see the counts per block and the top 10% records associated with the top 3 blocks by counts in the directories respectively.


In the **Zingg Enterprise** version, all the blocks are persisted and we get the complete details.

For  **Zingg Enterprise for Snowflake**, verifyBlocking generates tables with the names:

`zingg_modelId_blocks_timestamp_counts` where we can see the counts per block and `zingg_modelId_blocks_timestamp_blockSamples_hash` where we can see the records associated with the blocks.
