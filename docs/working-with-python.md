---
description: A whole new way to work with Zingg!
---

# Working With Python

Instead of configuring Zingg using the JSON, we can now use Python to build and run Zingg entity and identity resolution programs. This is handy when you want to run Zingg on an existing Spark cluster. To run on local machine, please do the installation of the release before running Zingg python programs.

The Zingg Python package can be installed by invoking

`python -m pip install zingg`

Detailed documentation of the python api is available at [https://readthedocs.org/projects/zingg/](https://readthedocs.org/projects/zingg/)

Example programs for python exist under examples. Please check examples/febrl/FebrlExample.py to get started.

Please refer to the [command line guide](stepbystep/zingg-command-line.md) for running python programs. Please note that Zingg Python programs are PySpark programs and hence need the Zingg cli to execute.

``

