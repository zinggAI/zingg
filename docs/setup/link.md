---
title: Linking data
parent: Step By Step Guide
nav_order: 11
---

# Linking Across Datasets

In many cases like reference data mastering, enrichment, etc, two individual datasets are duplicate-free but they need to be matched against each other. The link phase is used for such scenarios.

`./zingg.sh --phase link --conf config.json`

Sample configuration file [configLink.json](../../examples/febrl/configLink.json) is defined at [examples/febrl](https://github.com/zinggAI/zingg/tree/main/examples/febrl). In this option, each record from the first source is matched with all the records from the remaining sources.

The sample output is given in the image below. The linked records are given the same **z\_cluster** id. The last column (**z\_source**) in the output tells the source dataset of that record.

![Link Results](../../assets/link.png)
