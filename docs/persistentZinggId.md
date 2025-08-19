---
title: peristent ZINGG ID
parent: Step By Step Guide
nav_order: 16
description: Reassign the ZINGG IDs for clusters from the original production model
---

# Persistent ZINGG ID

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

Let's say we have a production flow already deployed, and there is a new release which introduces some additional features (e.g., nicknames, a better blocking tree, or additional hash functions). Or maybe there are changes to the classifiers which are expected to change the output of Zingg. 

Now, in that case, we already have existing ZINGG IDs which have gone into the operational systems. So, we want to reassign the ZINGG IDs for clusters from the original production model as far as possible. 

We want to provide a user-friendly and easy way for any Zingg user to be able to achieve this.

### The reassign phase is run as follows:

`./scripts/zingg.sh --phase reassignZinggId --conf <path to new model conf> --originalZinggId <path to original conf>`

The output will be updating the output of the new run with the ZINGG_ID in NEW_OUTPUT and keeping a backup of the new output as NEW_OUTPUT_timestamp.

[^1]: Zingg Enterprise is an advance version of Zingg Community with production grade features
