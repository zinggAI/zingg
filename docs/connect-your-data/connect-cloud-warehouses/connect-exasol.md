---
description: >-
  Connect Zingg to Exasol using the Exasol Spark connector. Available in all
  editions.
---

# Connect Exasol

### Prerequisites

1. Download the Exasol Spark connector assembled JAR (look for -assembly suffix).

`github.com/exasol/spark-connector/releases`

2. Add to `zingg.conf`.

```
spark.jars = spark - connector_2 .12 - 1.3.0 - spark - 3.3.2 - assembly.jar
```

### Python API <a href="#json-config-4" id="json-config-4"></a>

```python
from zingg.client import *
from zingg.pipes import *

exasolInput = Pipe("input", "com.exasol.spark")
exasolInput.addProperty("host", "10.11.0.2")
exasolInput.addProperty("port", "8563")
exasolInput.addProperty("username", "sys")
exasolInput.addProperty("password", "exasol")
exasolInput.addProperty("query", "SELECT * FROM DB_SCHEMA.CUSTOMERS")
args.setData(exasolInput)

exasolOutput = Pipe("output", "com.exasol.spark")
exasolOutput.addProperty("host", "10.11.0.2")
exasolOutput.addProperty("port", "8563")
exasolOutput.addProperty("username", "sys")
exasolOutput.addProperty("password", "exasol")
exasolOutput.addProperty("create_table", "true")
exasolOutput.addProperty("table", "DB_SCHEMA.ENTITY_RESOLUTION")
args.setOutput(exasolOutput)
```

### JSON Config <a href="#json-config-4" id="json-config-4"></a>

{% hint style="success" icon="right-long" %}
The host parameter must be the first internal node IPv4 address. Full Exasol connector options: `github.com/exasol/spark-connector/blob/main/doc/user_guide/user_guide.md`
{% endhint %}

```json
{
  "data" : [ {
    "name" : "input",
    "format" : "com.exasol.spark",
    "props" : {
      "host" : "10.11.0.2",
      "port" : "8563",
      "username" : "sys",
      "password" : "exasol",
      "query" : "SELECT * FROM DB_SCHEMA.CUSTOMERS"
    }
  } ],
  "output" : [ {
    "name" : "output",
    "format" : "com.exasol.spark",
    "props" : {
      "host" : "10.11.0.2",
      "port" : "8563",
      "username" : "sys",
      "password" : "exasol",
      "create_table" : "true",
      "table" : "DB_SCHEMA.ENTITY_RESOLUTION"
    },
    "mode" : "Append"
  } ]
}
```
