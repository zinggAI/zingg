---
title: match statistics
parent: Step By Step Guide
nav_order: 17
description: Reassign the ZINGG IDs for clusters from the original production model
---

# Match Statistics

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

Currently, the Zingg output consists of the original data along with the Zingg ID and the match probabilities. If we could surface information about the linkages we found in the records within the cluster, we could help users with matching internals and anomalies. When our users are dealing with millions of records, finding the needle in the haystack is critical. 

This information about changing clusters would be a good first step to observe the entity resolution pipeline. While running Zingg incrementally, the Match Statistics would expose how the clusters numbers change as records get inserted and updated into the identity graph. If the number of clusters changes disproportionately to the number of records updated and added, an alert could be triggered.

Whenever we run phases like match or incremental, the statistics about the changes in clusters will be written.

The output will be the statistics being written which are of three types - *SUMMARY, CLUSTER AND RECORD*

The output is written in the directory -&#x20;

`zinggDir/modelId/stats/type`

[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to https://www.zingg.ai/product/zingg-entity-resolution-compare-versions for individual tier features.
