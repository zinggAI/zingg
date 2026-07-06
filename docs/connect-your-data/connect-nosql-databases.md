---
description: >-
  Connect Zingg to Cassandra and MongoDB  as data sources and output
  destinations.
---

# Connect NoSQL Databases

{% hint style="success" icon="right-long" %}
New to Zingg pipes? Understand how pipes work before configuring them - [Pipes and data connections](pipes-and-data-connections.md).
{% endhint %}

{% hint style="success" icon="right-long" %}
This page covers NoSQL database connections.

* For relational databases see [Connect Relational Databases](connect-relational-databases.md).
* For cloud warehouses see [Connect Cloud Warehouses](connect-cloud-warehouses/).

All NoSQL connectors are available in Community and Enterprise.
{% endhint %}

{% tabs %}
{% tab title="Cassandra" %}
{% hint style="success" icon="right-long" %}
The config below shows Cassandra as a write destination (output).

To read FROM Cassandra, use the same props under "`data`" with `args.setData()`.
{% endhint %}

### Python API

```python
from zingg.client import*
from zingg.pipes import*

cassandraPipe =
Pipe("sampleTest", "CASSANDRA")
cassandraPipe.addProperty("table", "dataschematest")
cassandraPipe.addProperty("keyspace", "zingg")
cassandraPipe.addProperty("cluster", "zingg")
cassandraPipe.addProperty("spark.cassandra.connection.host", "192.168.0.6")
args.setOutput(cassandraPipe)
```

### **JSON Config**

```json
{
  "output" : [ {
    "name" : "sampleTest",
    "format" : "CASSANDRA",
    "props" : {
      "table" : "dataschematest",
      "keyspace" : "zingg",
      "cluster" : "zingg",
      "spark.cassandra.connection.host" : "192.168.0.6"
    },
    "sparkProps" : {"spark.cassandra.connection.host" : "127.0.0.1"},
    "mode" : "Append"
  } ]
}
```
{% endtab %}

{% tab title="MongoDB" %}
### Python API

```python
from zingg.client import*
from zingg.pipes import*

mongoPipe =
Pipe("mongodb", "mongo")
mongoPipe.addProperty("uri", "mongodb://127.0.0.1/people.contacts")
args.setData(mongoPipe)
```

### JSON Config

```json
{
  "data" : [ {
    "name" : "mongodb",
    "format" : "mongo",
    "props" : {"uri" : "mongodb://127.0.0.1/people.contacts"}
  } ]
}
```

{% hint style="danger" icon="right-long" %}
* URI format: `mongodb://host:port/database.collection`
* Authenticated: `mongodb://user:password@host:port/database.collection`
{% endhint %}

{% hint style="success" icon="right-long" %}
**Read more**:

* For relational databases - [Connect Relational Databases](connect-relational-databases.md)
* For cloud warehouses - [Connect Cloud Warehouses](connect-cloud-warehouses/)
{% endhint %}
{% endtab %}
{% endtabs %}
