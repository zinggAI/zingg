# zingg.pipes.BigQueryPipe

### *class* zingg.pipes.BigQueryPipe(name)

Bases: [`Pipe`](zingg.pipes.Pipe.md#zingg.pipes.Pipe)

Pipe Class for working with BigQuery pipeline

* **Parameters:**
  **name** (*String*) – name of the pipe.

### Methods

| [`__init__`](#zingg.pipes.BigQueryPipe.__init__)                           |                                                           |
|----------------------------------------------------------------------------|-----------------------------------------------------------|
| [`addProperty`](#zingg.pipes.BigQueryPipe.addProperty)                     | Method for adding different properties of pipe            |
| [`getPipe`](#zingg.pipes.BigQueryPipe.getPipe)                             | Method to get Pipe                                        |
| [`setCredentialFile`](#zingg.pipes.BigQueryPipe.setCredentialFile)         | Method to set Credential file to the pipe                 |
| [`setSchema`](#zingg.pipes.BigQueryPipe.setSchema)                         | Method to set pipe schema value                           |
| [`setTable`](#zingg.pipes.BigQueryPipe.setTable)                           | Method to set Table to the pipe                           |
| [`setTemporaryGcsBucket`](#zingg.pipes.BigQueryPipe.setTemporaryGcsBucket) | Method to set TemporaryGcsBucket to the pipe              |
| [`setViewsEnabled`](#zingg.pipes.BigQueryPipe.setViewsEnabled)             | Method to set if viewsEnabled parameter is Enabled or not |
| [`toString`](#zingg.pipes.BigQueryPipe.toString)                           | Method to get pipe parameter values                       |

### Attributes

| `CREDENTIAL_FILE`   |    |
|---------------------|----|
| `TABLE`             |    |
| `TEMP_GCS_BUCKET`   |    |
| `VIEWS_ENABLED`     |    |

#### \_\_init_\_(name)

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

#### setCredentialFile(file)

Method to set Credential file to the pipe

* **Parameters:**
  **file** (*String*) – credential file name

#### setSchema(s)

Method to set pipe schema value

* **Parameters:**
  **s** (*Schema*) – json schema for the pipe

#### setTable(table)

Method to set Table to the pipe

* **Parameters:**
  **table** (*String*) – provide table parameter

#### setTemporaryGcsBucket(bucket)

Method to set TemporaryGcsBucket to the pipe

* **Parameters:**
  **bucket** (*String*) – provide bucket parameter

#### setViewsEnabled(isEnabled)

Method to set if viewsEnabled parameter is Enabled or not

* **Parameters:**
  **isEnabled** (*Bool*) – provide boolean parameter which defines if viewsEnabled option is enable or not

#### toString()

Method to get pipe parameter values

* **Returns:**
  pipe information in list format
* **Return type:**
  List[String]
