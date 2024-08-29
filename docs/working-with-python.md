---
description: A whole new way to work with Zingg!
---

# Working With Python

Instead of configuring Zingg using JSON, we can now use Python to build and run Zingg entity and identity resolution programs. This is handy when you want to run Zingg on an existing Spark cluster. To run on a local machine, please install from the release before running Zingg Python programs.

The Zingg Python package can be installed by invoking:

`python -m pip install zingg`

Detailed documentation of the Python API is available at:[https://readthedocs.org/projects/zingg/](https://readthedocs.org/projects/zingg/)

Example programs for Python exist under [examples](https://github.com/zinggAI/zingg/tree/main/examples/febrl). Please check [examples/febrl/FebrlExample.py](https://github.com/zinggAI/zingg/blob/main/examples/febrl/FebrlExample.py) to get started.

Please refer to the [command line guide](stepbystep/zingg-command-line.md) for running Python programs. Please note that Zingg Python programs are PySpark programs and hence need the Zingg CLI to execute.

