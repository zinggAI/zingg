---
title: Hardware Sizing
parent: Step By Step Guide
nav_order: 4
description: Hardware required for different sizes of data
---

# Hardware Sizing

Zingg has been built to scale. Performance is dependent on:

* The number of records to be matched.
* The number of fields to be compared against each other.
* The actual number of duplicates.

Here are some performance numbers you can use to determine the appropriate hardware for your data.

* 120k records of examples/febrl120k/test.csv take 5 minutes to run on a 4 core, 10 GB RAM local Spark cluster.
* 5m records of [North Carolina Voters](https://github.com/zinggAI/zingg/tree/main/examples/ncVoters5M) take \~4 hours on a 4 core, 10 GB RAM local Spark cluster.
* 9m records with 3 fields - first name, last name, email take 45 minutes to run on AWS m5.24xlarge instance with 96 cores, 384 GB RAM

If you have up to a few million records, it may be easier to run Zingg on a single machine in Spark local mode.
