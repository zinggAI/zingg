---
title: Running on Databricks
parent: Running Zingg on Cloud
nav_order: 6
---
There are several ways to run Zingg on Databricks. All [file formats and data sources and sinks](../dataSourcesAndSinks) are supported within Databricks. 

# Running directly within Databricks using the Databricks notebook interface
This uses the Zingg Python API and an [example notebook is available here](https://github.com/zinggAI/zingg/blob/main/examples/databricks/FebrlExample.ipynb)

# Running using Databricks Connect from your local machine
1. Configure databricks connect 11.3 and create correspoding workspace/cluster as per the [Databricks docs](https://docs.databricks.com/dev-tools/databricks-connect-legacy.html). Please makre sure that you run `databricks-connect configure`

Ensure to run databricks-connect configure

2. Set env variable ZINGG_HOME to the path where latest zingg release jar is e.g. location of zingg-0.4.0.jar

4. Set env variable DATA_BRICKS_CONNECT to Y

5. pip install zingg

6. Now run zingg using the shell script with -run-databricks option, SPARK session would be made remotely to Databricks and job would run on your Databricks environment
e.g. ./scripts/zingg.sh --run-databricks test/InMemPipeDataBricks.py

Please refer to the [different options](https://docs.zingg.ai/zingg/stepbystep/zingg-command-line) available on the Zingg command line.


# Running on Databricks using Spark Submit Jobs
Zingg is run as a Spark Submit Job along with a python notebook-based labeler specially created to run within the Databricks cloud since the cloud environment does not have the system console for the labeler to work. 

Please refer to the [Databricks Zingg tutorial](https://medium.com/@sonalgoyal/identity-resolution-on-databricks-for-customer-360-591661bcafce) for a detailed tutorial.
