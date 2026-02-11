# zingg.pipes.CsvPipe

### *class* zingg.pipes.CsvPipe(name, location=None, schema=None)

Bases: [`Pipe`](zingg.pipes.Pipe.md#zingg.pipes.Pipe)

Class CsvPipe: used for working with text files which uses a pipe symbol to separate units of text that belong in different columns.

* **Parameters:**
  * **name** (*String*) – name of the pipe.
  * **location** (*String* *or* *None*) – (optional) location from where we read data
  * **schema** (*Schema* *or* *None*) – (optional) json schema for the pipe

### Methods

| [`__init__`](#zingg.pipes.CsvPipe.__init__)         |                                                    |
|-----------------------------------------------------|----------------------------------------------------|
| [`addProperty`](#zingg.pipes.CsvPipe.addProperty)   | Method for adding different properties of pipe     |
| [`getPipe`](#zingg.pipes.CsvPipe.getPipe)           | Method to get Pipe                                 |
| [`setDelimiter`](#zingg.pipes.CsvPipe.setDelimiter) | This method is used to define delimiter of CsvPipe |
| [`setHeader`](#zingg.pipes.CsvPipe.setHeader)       | Method to set header property of pipe              |
| [`setLocation`](#zingg.pipes.CsvPipe.setLocation)   | Method to set location of pipe                     |
| [`setSchema`](#zingg.pipes.CsvPipe.setSchema)       | Method to set pipe schema value                    |
| [`toString`](#zingg.pipes.CsvPipe.toString)         | Method to get pipe parameter values                |

#### \_\_init_\_(name, location=None, schema=None)

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
  [Pipe](zingg.pipes.Pipe.md#zingg.pipes.Pipe)

#### setDelimiter(delimiter)

This method is used to define delimiter of CsvPipe

* **Parameters:**
  **delimiter** (*String*) – a sequence of one or more characters for specifying the boundary between separate, independent regions in data streams

#### setHeader(header)

Method to set header property of pipe

* **Parameters:**
  **header** (*Boolean*) – true if pipe have header, false otherwise

#### setLocation(location)

Method to set location of pipe

* **Parameters:**
  **location** (*String*) – location from where we read data

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
