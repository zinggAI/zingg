---
parent: Creating training data
title: Exporting labeled data as csv
grand_parent: Step By Step Guide
nav_order: 4
---

# Exporting Labeled Data

If we need to send our labeled data for a subject matter expert to review or if we want to build another model in a new location and [reuse training effort](addOwnTrainingData.md) from earlier, we can write our labeled data to a csv&#x20;

`./scripts/zingg.sh --phase exportModel --conf <path to conf> --location <folder to save the csv>`
