# ESparkClient

## zinggES.enterprise.spark.ESparkClient

This module is the main entry point of the Zingg Enterprise Python API

### *class* zinggES.enterprise.spark.ESparkClient.EZingg(args, options)

Bases: `Zingg`

This class is the main point of interface with the Zingg Enterprise matching product. Construct a client to Zingg using provided arguments and spark master.
If running locally, set the master to local. This creates a new session.

* **Parameters:**
  * **args** (*EArguments*) – arguments for training and matching
  * **options** (*ClientOptions*) – client option for this class object

### *class* zinggES.enterprise.spark.ESparkClient.EZinggWithSpark(args, options)

Bases: [`EZingg`](#zinggES.enterprise.spark.ESparkClient.EZingg)

This class is the main point of interface with the Zingg Enterprise matching product. Construct a client to Zingg using provided arguments and spark master.
If running locally, set the master to local and this uses the current spark session.

* **Parameters:**
  * **args** (*EArguments*) – arguments for training and matching
  * **options** (*ClientOptions*) – client option for this class object
