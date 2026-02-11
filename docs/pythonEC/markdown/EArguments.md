# EArguments

## zinggEC.enterprise.common.EArguments

This module is to work with different types of features supported in Zingg Enterprise.

### *class* zinggEC.enterprise.common.EArguments.DeterministicMatching(\*matchCond)

Bases: `object`

#### getDeterministicMatching()

Method to get DeterministicMatching criteria

* **Returns:**
  DeterministicMatching parameter value
* **Return type:**
  [DeterministicMatching](#zinggEC.enterprise.common.EArguments.DeterministicMatching)

### *class* zinggEC.enterprise.common.EArguments.EArguments

Bases: `Arguments`

#### getArgs()

Method to get EArguments

* **Returns:**
  EArguments parameter value
* **Return type:**
  [EArguments](#zinggEC.enterprise.common.EArguments.EArguments)

#### getData()

Method to get data from multiple pipes

* **Returns:**
  Array of data contained in EPipes
* **Return type:**
  Array[[EPipe](epipes.md#zinggEC.enterprise.common.epipes.EPipe)]

#### getDeterministicMatching()

Method to get DeterministicMatching criteria

* **Returns:**
  DeterministicMatching parameter value
* **Return type:**
  [DeterministicMatching](#zinggEC.enterprise.common.EArguments.DeterministicMatching)

#### getFieldDefinition()

Method to get all field definitions to be used for matching

* **Returns:**
  all field definitions in list format
* **Return type:**
  List[[EFieldDefinition](EFieldDefinition.md#zinggEC.enterprise.common.EFieldDefinition.EFieldDefinition)]

#### getOutputStats()

Method to get Output Stats pipe
:return: output stats pipe
:rtype: EPipe

#### getPassthroughExpr()

Method to get pass through condition

* **Returns:**
  pass through conditions in string format
* **Return type:**
  String

#### getPrimaryKey()

Method to get the fields containing the primary keys

* **Returns:**
  all primary keys defined for field definitions in list format
* **Return type:**
  List[[EFieldDefinition](EFieldDefinition.md#zinggEC.enterprise.common.EFieldDefinition.EFieldDefinition)]

#### setArgs(argumentsObj)

Method to set EArguments

* **Parameters:**
  **argumentsObj** ([*EArguments*](#zinggEC.enterprise.common.EArguments.EArguments)) – EArguments object

#### setBlockingModel(blockingModel)

Method to set the Blocking Model used for creating model

* **Parameters:**
  **blockingModel** (*String*) – value as DEFAULT or WIDER

#### setData(\*pipes)

Method to set the multiple pipes for data

* **Parameters:**
  **pipes** (*EPipes*) – EPipes object

#### setDeterministicMatchingCondition(\*detMatchConds)

Method to set the DeterministicMatchingCondition used for matching

* **Parameters:**
  **detMatchConds** ([*DeterministicMatching*](#zinggEC.enterprise.common.EArguments.DeterministicMatching)) – DeterministicMatching object

#### setFieldDefinition(fieldDef)

Method to set the field definitions

* **Parameters:**
  **fieldDef** ([*EFieldDefinition*](EFieldDefinition.md#zinggEC.enterprise.common.EFieldDefinition.EFieldDefinition)) – EFieldDefiniton object

#### setOutputStats(pipe)

Method to set the Output Stats pipe
:param pipe: EPipe object for output stats
:type pipe: EPipe

#### setPassthroughExpr(passthroughExpr)

Method to set pass through condition

* **Parameters:**
  **passthroughExpr** (*String*) – String condition for records to not be considered
