# ApproverArguments

## zinggEC.enterprise.common.ApproverArguments

This module is to set up the approval feature

### *class* zinggEC.enterprise.common.ApproverArguments.ApproverArguments

Bases: `object`

#### getApprovalQuery()

Method to get query for approval

* **Returns:**
  query in string format for approval condition
* **Return type:**
  String

#### getArgs()

#### getDestination()

Method to get the destination of output

* **Returns:**
  Array of data contained in EPipes
* **Return type:**
  Array[[EPipe](epipes.md#zinggEC.enterprise.common.epipes.EPipe)]

#### getParentArgs()

Method to get EArguments

* **Returns:**
  EArguments parameter value
* **Return type:**
  [EArguments](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments)

#### setApprovalQuery(approval_query)

Method to set query for approval

* **Parameters:**
  **approval_query** (*String*) – setting a query in string format for approval condition

#### setArgs(arguments_obj)

Method to set ApproverArguments

* **Parameters:**
  **argumentsObj** ([*ApproverArguments*](#zinggEC.enterprise.common.ApproverArguments.ApproverArguments)) – ApproverArguments object

#### setDestination(\*pipes)

Method to set the multiple pipes for output destination

* **Parameters:**
  **pipes** (*EPipes*) – EPipes object

#### setParentArgs(argumentsObj)

Method to set EArguments

* **Parameters:**
  **argumentsObj** ([*EArguments*](EArguments.md#zinggEC.enterprise.common.EArguments.EArguments)) – EArguments object
