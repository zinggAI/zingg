---
description: >-
  Understanding how blocking is working before running match or link
---

# Verification of Blocking Data

Sometimes Zingg jobs are slow or fail due to a poorly learnt blocking tree. This can happen due to a variety of reasons. It can happen when a user adds significantly larger training samples compared to the labelling learnt by Zingg or due to a natural bias in the data with lots of null columns used in matching. If we have an understanding of how blocking is working before deciding to run a match or link job, we get a better idea whether we need to add more training data for getting better results.

### The verifyBlocking phase is run as follows:

`./scripts/zingg.sh --phase verifyBlocking --conf <path to conf> <optional --zinggDir <location of model>>`

The output contains two directories - zinggDir/modelId/blocks/timestamp/counts and zinggDir/modelId/blocks/timestamp/blockSamples. We can see the counts per block and the top 10% records associated with the top 3 blocks by counts in the directories respectively.