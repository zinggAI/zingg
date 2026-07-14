# IncrementalArguments

## zinggEC.enterprise.common.IncrementalArguments

This module is to set up the incremental feature

### *class* zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments

Bases: `object`

#### getArgs()

#### getDeleteAction()

Method to get Delete Action

* **Returns:**
  DeleteAction parameter value
* **Return type:**
  DeleteAction

#### getDeletedData()

Method to get Deleted Data

* **Returns:**
  data that needs to be deleted from incremental run
* **Return type:**
  Array[[EPipe](epipes.md#zinggEC.enterprise.common.epipes.EPipe)]

#### getIncrementalData()

Method to get Incremental Data

* **Returns:**
  data that needs to be passed for incremental run
* **Return type:**
  Array[[EPipe](epipes.md#zinggEC.enterprise.common.epipes.EPipe)]

#### getOutputTmp()

Method to get temporary output path

* **Returns:**
  data that needs to be passed for incremental output
* **Return type:**
  [EPipe](epipes.md#zinggEC.enterprise.common.epipes.EPipe)

#### getParentArgs()

Method to get EArguments

* **Returns:**
  EArguments parameter value
* **Return type:**
  [EArguments](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments)

#### setArgs(argumentsObj)

Method to set IncrementalArguments

* **Parameters:**
  **argumentsObj** ([*IncrementalArguments*](#zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments)) – IncrementalArguments object

#### setDeleteAction(deleteAction)

Method to set Delete Action

* **Parameters:**
  **deleteAction** (*DeleteAction*) – DeleteAction object

#### setDeletedData(\*pipes)

Method to set Deleted Data

* **Parameters:**
  **pipes** (*EPipes*) – EPipes object

#### setIncrementalData(\*pipes)

Method to set Incremental Data

* **Parameters:**
  **pipes** (*EPipes*) – EPipes object

#### setOutputTmp(pipe)

Method to set path for temporary output

param pipes: EPipe object
:type pipes: EPipe

#### setParentArgs(argumentsObj)

Method to set EArguments

* **Parameters:**
  **argumentsObj** ([*EArguments*](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments)) – EArguments object
