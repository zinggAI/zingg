# epipes

## zinggEC.enterprise.common.epipes

This module is submodule of zingg to work with different types of Pipes supported in Enterprise. Classes of this module inherit the EPipe class, and use that class to create many different types of pipes.

### *class* zinggEC.enterprise.common.epipes.ECsvPipe(name, location=None, schema=None)

Bases: [`EPipe`](#zinggEC.enterprise.common.epipes.EPipe)

Class CsvPipe: used for working with text files which uses a pipe symbol to separate units of text that belong in different columns.

* **Parameters:**
  * **name** (*String*) – name of the pipe.
  * **location** (*String* *or* *None*) – (optional) location from where we read data
  * **schema** (*Schema* *or* *None*) – (optional) json schema for the pipe

#### setDelimiter(delimiter)

This method is used to define delimiter of CsvPipe

* **Parameters:**
  **delimiter** (*String*) – a sequence of one or more characters for specifying the boundary between separate, independent regions in data streams

#### setHeader(header)

Method to set header property of pipe

* **Parameters:**
  **header** (*String* *(**'true' / 'false'**)*) – true if pipe have header, false otherwise

#### setLocation(location)

Method to set location of pipe

* **Parameters:**
  **location** (*String*) – location from where we read data

### *class* zinggEC.enterprise.common.epipes.EPipe(name, format)

Bases: `Pipe`

EPipe class for working with different data-pipelines. Actual pipe def in the args. One pipe can be used at multiple places with different tables, locations, queries, etc

* **Parameters:**
  * **name** (*String*) – name of the pipe
  * **format** (*Format*) – format of pipe e.g. inMemory, delta, etc.

#### getPassthroughExpr()

Method to get pass through condition

* **Returns:**
  pass through conditions in string format
* **Return type:**
  String

#### getPassthruData()

Method to get records which satisfy pass through condition

return: pandas or spark dataframe containing records which satisfy pass through condition
:rtype: DataFrame

#### getUsableData()

Method to get records which do not satisfy pass through condition

return: pandas or spark dataframe containing records which do not satisfy pass through condition
:rtype: DataFrame

#### hasPassThru()

Method to check if there is a pass through condition

* **Returns:**
  whether pass through condition is present or not
* **Return type:**
  boolean

#### setPassthroughExpr(passthroughExpr)

Method to set pass through condition

* **Parameters:**
  **passthroughExpr** (*String*) – String condition for records to not be considered

### *class* zinggEC.enterprise.common.epipes.InMemoryPipe(name, df=None)

Bases: [`EPipe`](#zinggEC.enterprise.common.epipes.EPipe)

Pipe Class for working with InMemory pipeline
:param name: name of the pipe
:type name: String
:param df: provide dataset for this pipe (optional)
:type df: Dataset or None

#### getDataset()

Method to get Dataset from pipe

* **Returns:**
  dataset of the pipe in the format of spark dataset
* **Return type:**
  Dataset<Row>

#### setDataset(df)

Method to set DataFrame of the pipe

* **Parameters:**
  **df** (*DataFrame*) – pandas or spark dataframe for the pipe

### *class* zinggEC.enterprise.common.epipes.UCPipe(name, table)

Bases: [`EPipe`](#zinggEC.enterprise.common.epipes.EPipe)

Pipe Class for working with Delta tables in Unity Catalog of Databricks

* **Parameters:**
  * **name** (*String*) – name of the pipe
  * **table** (*String*) – table from where we read data in the Catalog Volumes

#### setTable(table)

Method to set table in pipe

* **Parameters:**
  **table** (*String*) – table from where we read data
