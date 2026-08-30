---
description: >-
  Build and run Zingg entity resolution programs in Python. Available in
  Community, Enterprise (ZinggEC), and Enterprise Plus (ZinggES).
---

# Working with Python

Instead of configuring Zingg using JSON, you can use Python to build and run Zingg entity and identity resolution programs. This is the recommended approach when you are running Zingg on an existing Spark cluster like Databricks, Microsoft Fabric, AWS EMR, or Glue.

The Python API is available in three editions, each with its own package and class names. The pattern is the same across all three: set up `args`, set `options` with the phase name, create the Zingg object, call `initAndExecute()`.

{% hint style="success" icon="right-long" %}
To run on a local machine, install Zingg from the release before running Zingg Python programs. The Python package can be installed via `pip install zingg`. Detailed auto-generated API documentation is available at [readthedocs.org/projects/zingg](https://readthedocs.org/projects/zingg/).
{% endhint %}

#### Choose your edition

| Edition | Package | Use when |
|---|---|---|
| **Community (Open Source)** | `zingg` | Free, open-source Zingg for any Spark deployment |
| **Enterprise (ZinggEC)** | `zinggEC` | Enterprise Lite or Enterprise — adds deterministic matching, blocking strategy, primary key, pass-through, mapping match types, incremental, output stats |
| **Enterprise Plus (ZinggES)** | `zinggES` | Adds the Spark client (`EZingg`, `EZinggWithSpark`) used by Enterprise Plus distributions |

{% hint style="success" icon="right-long" %}
**Read more**: Pick your edition and follow the sub-page below:

* [Community Python API](community-python-api.md)
* [Enterprise ZinggEC Python API](enterprise-zinggec-python-api.md)
* [Enterprise ZinggES Python API](enterprise-zingges-python-api.md)
{% endhint %}

### Common pattern across all editions

Every Zingg Python program regardless of edition, follows the same five-step pattern:

1. **Imports** - import Zingg client and pipes modules
2. **Build `args`** - create the arguments object (`Arguments` in Community, `EArguments` in Enterprise)
3. **Set fields** - create field definitions and register with `args.setFieldDefinition(...)`
4. **Set input and output pipes** - configure data sources and sinks
5. **Set options and execute** - create `ClientOptions` with the phase name, instantiate the Zingg object, call `initAndExecute()`

{% hint style="success" icon="right-long" %}
**Read more**:

* [Configure Zingg](../running-zingg/configure-zingg.md) - the configuration that feeds into every Python API call
* [Run Matches or Link](../running-zingg/run-the-match-phase.md) - running the match and link phases
* [Zingg Command Line](../reference/zingg-command-line.md) - CLI alternative to the Python API
{% endhint %}
