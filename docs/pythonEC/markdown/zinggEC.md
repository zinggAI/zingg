# Zingg Enterpise EC Entity Resolution Package

Zingg Enterprise Python APIs for entity resolution, record linkage, data mastering and deduplication using ML
([https://www.zingg.ai](https://www.zingg.ai))

requires python 3.6+; spark 3.5.0 Otherwise,
[`zinggES.enterprise.spark.ESparkClient.EZingg()`](#zinggES.enterprise.spark.ESparkClient.EZingg) cannot be executed

<a id="module-zinggEC"></a>

<a id="module-zinggEC.enterprise.common.ApproverArguments"></a>

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

Method to get ApproverArguments

* **Returns:**
  ApproverArguments parameter value
* **Return type:**
  ApproverArguments

#### getDestination()

Method to get the destination of output

* **Returns:**
  Array of data contained in EPipes
* **Return type:**
  Array[EPipe]

#### getParentArgs()

Method to get EArguments

* **Returns:**  
  EArguments parameter value
* **Return type:**
  EArguments

#### setApprovalQuery(approval_query)

Method to set query for approval

* **Parameters:**  
  **approval_query** (*String*) – setting a query in string format for approval condition

#### setArgs(arguments_obj)

Method to set ApproverArguments

* **Parameters:**  
  **argumentsObj** (*ApproverArguments*) – ApproverArguments object

#### setDestination(*pipes)
    
Method to set the multiple pipes for output destination

* **Parameters:**   
  **pipes** (*EPipes*) – EPipes object

#### setParentArgs(argumentsObj)

Method to set EArguments

* **Parameters:**
  **argumentsObj** (*EArguments*) – EArguments object

<a id="module-zinggEC.enterprise.common.IncrementalArguments"></a>  

## zinggEC.enterprise.common.IncrementalArguments

This module is to set up the incremental feature

### *class* zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments

Bases: `object`

#### getArgs()

Method to get IncrementalArguments

* **Returns:**
  IncrementalArguments parameter value
* **Return type:**
  IncrementalArguments

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
  Array[EPipe]

#### getIncrementalData()

Method to get Incremental Data

* **Returns:**
  data that needs to be passed for incremental run
* **Return type:** 
  Array[EPipe]

#### getParentArgs()

Method to get EArguments

* **Returns:**  
  EArguments parameter value
* **Return type:**
  EArguments

#### setArgs(argumentsObj)

Method to set IncrementalArguments

* **Parameters:**
  **argumentsObj** (*IncrementalArguments*) – IncrementalArguments object

#### setDeleteAction(deleteAction)

Method to set Delete Action

* **Parameters:** 
  **deleteAction** (*DeleteAction*) – DeleteAction object

#### setDeletedData(*pipes)

Method to set Deleted Data

* **Parameters:** 
  **pipes** (*EPipes*) – EPipes object

#### setIncrementalData(*pipes)

Method to set Incremental Data

* **Parameters:** 
  **pipes** (*EPipes*) – EPipes object

#### setParentArgs(argumentsObj)

Method to set EArguments

* **Parameters:**
  **argumentsObj** (*EArguments*) – EArguments object

<a id="module-zinggEC.enterprise.common.MappingMatchType"></a>

## zinggEC.enterprise.common.MappingMatchType

This module is to work with the mapping match type which is used for
leveraging domain expertise to push matching accuracy. Also saves time
massaging data before matching.

### *class* zinggEC.enterprise.common.MappingMatchType.MappingMatchType(name ,value)

Bases: `object`

MappingMatchType class for defining mappings required for matching on a field

* **Parameters:**
  * **name** (*String*) – name of the match type - MAPPING
  * **format** (*String*) – name of the json containing mappings

#### getMappingMatchType()

Method to get mapping match type

* **Returns:**    
  mapping match type containg name and value
* **Return type:** 
  MappingMatchType

<a id="module-zinggEC.enterprise.common.epipes"></a>

## zinggEC.enterprise.common.epipes

This module is submodule of zingg to work with different types of Pipes
supported in Enterprise. Classes of this module inherit the EPipe class, and
use that class to create many different types of pipes.

### *class* zinggEC.enterprise.common.epipes.ECsvPipe(name , location =None, schema =None)

Bases: `EPipe`

Class CsvPipe: used for working with text files which uses a pipe symbol to
separate units of text that belong in different columns.

* **Parameters:**
  * **name** (*String*) – name of the pipe.
  * **location** (*String_or_None*) – (optional) location from where we read data
  * **schema** (*Schema_or_None*) – (optional) json schema for the pipe

#### setDelimiter(*delimiter*)

This method is used to define delimiter of CsvPipe

* **Parameters:**  
  **delimiter** (*String*) – a sequence of one or more characters for specifying
the boundary between separate, independent regions in data streams

#### setHeader(*header*)

Method to set header property of pipe

* **Parameters:**
  **header** (*String('true' / 'false')*) – true if pipe have header, false otherwise

#### setLocation(*location*)

Method to set location of pipe

* **Parameters:**
  **location** (*String*) – location from where we read data

### *class* zinggEC.enterprise.common.epipes.EPipe(name ,format)

Bases: `Pipe`

EPipe class for working with different data-pipelines. Actual pipe def in the
args. One pipe can be used at multiple places with different tables,
locations, queries, etc

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

* **Returns:**
  pandas or spark dataframe containing records which satisfy pass through condition
* **Return type:**  
  DataFrame

#### getUsableData()

Method to get records which do not satisfy pass through condition

* **Returns:**
  pandas or spark dataframe containing records which do not satisfy pass through condition
* **Return type:**  
  DataFrame

#### hasPassThru()

Method to check if there is a pass through condition

* **Returns:**   
  whether pass through condition is present or not
* **Return type:**  
  boolean

#### setPassthroughExpr(*passthroughExpr*)

Method to set pass through condition

* **Parameters:**
  **passthroughExpr** (*String*) – String condition for records to not be considered

### *class* zinggEC.enterprise.common.epipes.InMemoryPipe(name , df=None)

Bases: `EPipe`

Pipe Class for working with InMemory pipeline


* **Parameters:**
  * **name** (*String*) – name of the pipe
  * **df** (*Dataset or None*) – provide dataset for this pipe(optional)

#### getDataset()

Method to get Dataset from pipe

* **Returns:**
  dataset of the pipe in the format of spark dataset
* **Return type:**   
  Dataset<Row>

#### setDataset(*df*)

Method to set DataFrame of the pipe

* **Parameters:**
  **df** (*DataFrame*) – pandas or spark dataframe for the pipe

### *class* zinggEC.enterprise.common.epipes.UCPipe(name ,table)  

Bases: `EPipe`

Pipe Class for working with Delta tables in Unity Catalog of Databricks

* **Parameters:**
  * **name** (*String*) – name of the pipe
  * **table** (*String*) – table from where we read data in the Catalog Volumes

#### setTable(table)

Method to set table in pipe

* **Parameters:**
  **table** (*String*) – table from where we read data

<a id="module-zinggEC.enterprise.common.EArguments"></a>  

## zinggEC.enterprise.common.EArguments

This module is to work with different types of features supported in Zingg
Enterprise.

### *class* zinggEC.enterprise.common.EArguments.DeterministicMatching(*matchCond)

Bases: `object`

#### getDeterministicMatching()

Method to get DeterministicMatching criteria

* **Returns:**
  DeterministicMatching parameter value
* **Return type:**  
  DeterministicMatching

### *class* zinggEC.enterprise.common.EArguments.EArguments

Bases: `Arguments`

#### getArgs()

Method to get EArguments

* **Returns:** 
  EArguments parameter value
* **Return type:**
  EArguments

#### getData()

Method to get data from multiple pipes

* **Returns:** 
  Array of data contained in EPipes
* **Return type:**
  Array[EPipe]

#### getDeterministicMatching()

Method to get DeterministicMatching criteria

* **Returns:**
  DeterministicMatching parameter value
* **Return type:**
  DeterministicMatching

#### getFieldDefinition()

Method to get all field definitions to be used for matching

* **Returns:**
  all field definitions in list format
* **Return type:**
  List[EFieldDefinition]

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
  List[EFieldDefinition]

#### setArgs(argumentsObj)

Method to set EArguments

* **Parameters:**   
  **argumentsObj** (*EArguments*) – EArguments object

#### setBlockingModel(blockingModel)

Method to set the Blocking Model used for creating model

* **Parameters:**
  **blockingModel** (*String*) – value as DEFAULT or WIDER

#### setData(*pipes)

Method to set the multiple pipes for data

* **Parameters:**
  **pipes** (*EPipes*) – EPipes object

#### setDeterministicMatchingCondition(*detMatchConds)

Method to set the DeterministicMatchingCondition used for matching

* **Parameters:**
  **detMatchConds** (*DeterministicMatching*) – DeterministicMatching object

#### setFieldDefinition(fieldDef)

Method to set the field definitions

* **Parameters:**
  **fieldDef** (*EFieldDefinition*) – EFieldDefiniton object

#### setPassthroughExpr(passthroughExpr)

Method to set pass through condition

* **Parameters:**
  **passthroughExpr** (*String*) – String condition for records to not be considered

<a id="module-zinggEC.enterprise.common.EFieldDefinition"></a>

## zinggEC.enterprise.common.EFieldDefinition

This module is to work with the extended functionality of field definitions

### *class* zinggEC.enterprise.common.EFieldDefinition.EFieldDefinition(name ,dataType , *matchType) 

Bases: `FieldDefinition`

This class defines each field that we use in matching. We can use this to
configure the properties of each field we use for matching in Zingg.

* **Parameters:**
  * **name** (*String*) – name of the field
  * **dataType** (*String*) – type of the data e.g. string, float, etc.
  * **matchType** (*MatchType*) – match type of this field e.g. FUSSY, EXACT, etc. including user-defined mapping match types

#### getMatchTypeArray(matchType) 

Method to get the match types associated with a field

* **Parameters:**   
  **matchType** (*List[IMatchType]*) – list of match types associated with a field

#### getPrimaryKey()

Method to check if the field contains the primary key

* **Returns:**
  true or false depending on if the field contains the primary key
* **Return type:**
  boolean

#### setPrimaryKey(primaryKey)
 
Method to set the field containing the primary key

* **Parameters:**   
  **primaryKey** (*boolean*) – true or false depending on if the field contains the primary key
