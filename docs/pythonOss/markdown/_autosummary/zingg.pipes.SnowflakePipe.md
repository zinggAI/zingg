# zingg.pipes.SnowflakePipe

### *class* zingg.pipes.SnowflakePipe(name)

Bases: [`Pipe`](zingg.pipes.Pipe.md#zingg.pipes.Pipe)

Pipe Class for working with Snowflake pipeline

* **Parameters:**
  **name** (*String*) – name of the pipe

### Methods

| [`__init__`](#zingg.pipes.SnowflakePipe.__init__)         |                                                |
|-----------------------------------------------------------|------------------------------------------------|
| [`addProperty`](#zingg.pipes.SnowflakePipe.addProperty)   | Method for adding different properties of pipe |
| [`getPipe`](#zingg.pipes.SnowflakePipe.getPipe)           | Method to get Pipe                             |
| [`setDatabase`](#zingg.pipes.SnowflakePipe.setDatabase)   | Method to set Database to the pipe             |
| [`setDbTable`](#zingg.pipes.SnowflakePipe.setDbTable)     | description                                    |
| [`setPassword`](#zingg.pipes.SnowflakePipe.setPassword)   | Method to set Password to the pipe             |
| [`setSFSchema`](#zingg.pipes.SnowflakePipe.setSFSchema)   | Method to set Schema to the pipe               |
| [`setSchema`](#zingg.pipes.SnowflakePipe.setSchema)       | Method to set pipe schema value                |
| [`setURL`](#zingg.pipes.SnowflakePipe.setURL)             | Method to set url to the pipe                  |
| [`setUser`](#zingg.pipes.SnowflakePipe.setUser)           | Method to set User to the pipe                 |
| [`setWarehouse`](#zingg.pipes.SnowflakePipe.setWarehouse) | Method to set warehouse parameter to the pipe  |
| [`toString`](#zingg.pipes.SnowflakePipe.toString)         | Method to get pipe parameter values            |

### Attributes

| `DATABASE`   |    |
|--------------|----|
| `DBTABLE`    |    |
| `PASSWORD`   |    |
| `SCHEMA`     |    |
| `URL`        |    |
| `USER`       |    |
| `WAREHOUSE`  |    |

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

#### setDatabase(db)

Method to set Database to the pipe

* **Parameters:**
  **db** (*Database*) – provide Database parameter.

#### setDbTable(dbtable)

description

* **Parameters:**
  **dbtable** (*String*) – provide bucket parameter.

#### setPassword(passwd)

Method to set Password to the pipe

* **Parameters:**
  **passwd** (*String*) – provide Password parameter.

#### setSFSchema(schema)

Method to set Schema to the pipe

* **Parameters:**
  **schema** (*Schema*) – provide schema parameter.

#### setSchema(s)

Method to set pipe schema value

* **Parameters:**
  **s** (*Schema*) – json schema for the pipe

#### setURL(url)

Method to set url to the pipe

* **Parameters:**
  **url** (*String*) – provide url for this pipe

#### setUser(user)

Method to set User to the pipe

* **Parameters:**
  **user** (*String*) – provide User parameter.

#### setWarehouse(warehouse)

Method to set warehouse parameter to the pipe

* **Parameters:**
  **warehouse** (*String*) – provide warehouse parameter.

#### toString()

Method to get pipe parameter values

* **Returns:**
  pipe information in list format
* **Return type:**
  List[String]
