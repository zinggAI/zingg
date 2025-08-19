# Zingg Enterpise ES Entity Resolution Package

Zingg Enterprise Python APIs for entity resolution, record linkage, data mastering and deduplication using ML
([https://www.zingg.ai](https://www.zingg.ai))

requires python 3.6+; spark 3.5.0 Otherwise,
[`zinggES.enterprise.spark.ESparkClient.EZingg()`](#zinggES.enterprise.spark.ESparkClient.EZingg) cannot be executed

<a id="module-zinggES"></a>

<a id="module-zinggES.enterprise.spark.ESparkClient"></a>

## zinggES.enterprise.spark.ESparkClient

This module is the main entry point of the Zingg Enterprise Python API

### *class* zinggES.enterprise.spark.ESparkClient.EZingg(args ,options)

Bases: `object`

This class is the main point of interface with the Zingg Enterprise matching
product. Construct a client to Zingg using provided arguments and spark
master. If running locally, set the master to local. This creates a new
session.

* **Parameters:**
  * **args** ([*Arguments*](#zinggEC.enterprise.common.EArguments)) – arguments for training and matching
  * **options** ([*ClientOptions*](#zingg.client.ClientOptions)) – client option for this class object

### *class* zinggES.enterprise.spark.ESparkClient.EZinggWithSpark(args ,options)

Bases: `object`

This class is the main point of interface with the Zingg Enterprise matching
product. Construct a client to Zingg using provided arguments and spark
master. If running locally, set the master to local and this uses the current
spark session.

* **Parameters:**
  * **args** ([*Arguments*](#zinggEC.enterprise.common.EArguments)) – arguments for training and matching
  * **options** ([*ClientOptions*](#zingg.client.ClientOptions)) – client option for this class object


