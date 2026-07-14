# zingg.pipes.Pipe

### *class* zingg.pipes.Pipe(name, format)

Bases: `object`

Pipe class for working with different data-pipelines. Actual pipe def in the args. One pipe can be used at multiple places with different tables, locations, queries, etc

* **Parameters:**
  * **name** (*String*) – name of the pipe
  * **format** (*Format*) – format of pipe e.g. bigquery,csv, etc.

### Methods

| [`__init__`](#zingg.pipes.Pipe.__init__)       |                                                |
|------------------------------------------------|------------------------------------------------|
| [`addProperty`](#zingg.pipes.Pipe.addProperty) | Method for adding different properties of pipe |
| [`getPipe`](#zingg.pipes.Pipe.getPipe)         | Method to get Pipe                             |
| [`setSchema`](#zingg.pipes.Pipe.setSchema)     | Method to set pipe schema value                |
| [`toString`](#zingg.pipes.Pipe.toString)       | Method to get pipe parameter values            |

#### \_\_init_\_(name, format)

#### addProperty(name, value)

Method for adding different properties of pipe

* **Parameters:**
  * **name** (*String*) – name of the property
  * **value** (*String*) – value you want to set for the property

#### getPipe()

Method to get Pipe

* **Returns:**
  pipe parameter values in the format of a list of string
* **Return type:**
  [Pipe](#zingg.pipes.Pipe)

#### setSchema(s)

Method to set pipe schema value

* **Parameters:**
  **s** (*Schema*) – json schema for the pipe

#### toString()

Method to get pipe parameter values

* **Returns:**
  pipe information in list format
* **Return type:**
  List[String]
