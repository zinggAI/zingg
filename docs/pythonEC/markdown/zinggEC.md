# Zingg Enterpise EC Entity Resolution Package

Zingg Enterprise Python APIs for entity resolution, record linkage, data mastering and deduplication using ML
([https://www.zingg.ai](https://www.zingg.ai))

requires python 3.6+; spark 3.5.0 Otherwise,
[`zinggES.enterprise.spark.ESparkClient.EZingg()`](#zinggES.enterprise.spark.ESparkClient.EZingg) cannot be executed

## zinggEC.enterprise.common.ApproverArguments

This module is to set up the approval feature

_class
_zinggEC.enterprise.common.ApproverArguments.ApproverArguments[[source]](_modules/zinggEC/enterprise/common/ApproverArguments.html#ApproverArguments)

    

Bases: `object`

getApprovalQuery()[[source]](_modules/zinggEC/enterprise/common/ApproverArguments.html#ApproverArguments.getApprovalQuery)

    

Method to get query for approval

Returns:

    

query in string format for approval condition

Return type:

    

String

getArgs()[[source]](_modules/zinggEC/enterprise/common/ApproverArguments.html#ApproverArguments.getArgs)

    

Method to get ApproverArguments

Returns:

    

ApproverArguments parameter value

Return type:

    

ApproverArguments

getDestination()[[source]](_modules/zinggEC/enterprise/common/ApproverArguments.html#ApproverArguments.getDestination)

    

Method to get the destination of output

Returns:

    

Array of data contained in EPipes

Return type:

    

Array[EPipe]

getParentArgs()[[source]](_modules/zinggEC/enterprise/common/ApproverArguments.html#ApproverArguments.getParentArgs)

    

Method to get EArguments

Returns:

    

EArguments parameter value

Return type:

    

EArguments

setApprovalQuery(_approval_query_)[[source]](_modules/zinggEC/enterprise/common/ApproverArguments.html#ApproverArguments.setApprovalQuery)

    

Method to set query for approval

Parameters:

    

**approval_query** (_String_) – setting a query in string format for approval
condition

setArgs(_arguments_obj_)[[source]](_modules/zinggEC/enterprise/common/ApproverArguments.html#ApproverArguments.setArgs)

    

Method to set ApproverArguments

Parameters:

    

**argumentsObj** (_ApproverArguments_) – ApproverArguments object

setDestination(_*
pipes_)[[source]](_modules/zinggEC/enterprise/common/ApproverArguments.html#ApproverArguments.setDestination)

    

Method to set the multiple pipes for output destination

Parameters:

    

**pipes** (_EPipes_) – EPipes object

setParentArgs(_argumentsObj_)[[source]](_modules/zinggEC/enterprise/common/ApproverArguments.html#ApproverArguments.setParentArgs)

    

Method to set EArguments

Parameters:

    

**argumentsObj** (_EArguments_) – EArguments object

## zinggEC.enterprise.common.IncrementalArguments

This module is to set up the incremental feature

_class
_zinggEC.enterprise.common.IncrementalArguments.IncrementalArguments[[source]](_modules/zinggEC/enterprise/common/IncrementalArguments.html#IncrementalArguments)

    

Bases: `object`

getArgs()[[source]](_modules/zinggEC/enterprise/common/IncrementalArguments.html#IncrementalArguments.getArgs)

    

Method to get IncrementalArguments

Returns:

    

IncrementalArguments parameter value

Return type:

    

IncrementalArguments

getDeleteAction()[[source]](_modules/zinggEC/enterprise/common/IncrementalArguments.html#IncrementalArguments.getDeleteAction)

    

Method to get Delete Action

Returns:

    

DeleteAction parameter value

Return type:

    

DeleteAction

getDeletedData()[[source]](_modules/zinggEC/enterprise/common/IncrementalArguments.html#IncrementalArguments.getDeletedData)

    

Method to get Deleted Data

Returns:

    

data that needs to be deleted from incremental run

Return type:

    

Array[EPipe]

getIncrementalData()[[source]](_modules/zinggEC/enterprise/common/IncrementalArguments.html#IncrementalArguments.getIncrementalData)

    

Method to get Incremental Data

Returns:

    

data that needs to be passed for incremental run

Return type:

    

Array[EPipe]

getParentArgs()[[source]](_modules/zinggEC/enterprise/common/IncrementalArguments.html#IncrementalArguments.getParentArgs)

    

Method to get EArguments

Returns:

    

EArguments parameter value

Return type:

    

EArguments

setArgs(_argumentsObj_)[[source]](_modules/zinggEC/enterprise/common/IncrementalArguments.html#IncrementalArguments.setArgs)

    

Method to set IncrementalArguments

Parameters:

    

**argumentsObj** (_IncrementalArguments_) – IncrementalArguments object

setDeleteAction(_deleteAction_)[[source]](_modules/zinggEC/enterprise/common/IncrementalArguments.html#IncrementalArguments.setDeleteAction)

    

Method to set Delete Action

Parameters:

    

**deleteAction** (_DeleteAction_) – DeleteAction object

setDeletedData(_*
pipes_)[[source]](_modules/zinggEC/enterprise/common/IncrementalArguments.html#IncrementalArguments.setDeletedData)

    

Method to set Deleted Data

Parameters:

    

**pipes** (_EPipes_) – EPipes object

setIncrementalData(_*
pipes_)[[source]](_modules/zinggEC/enterprise/common/IncrementalArguments.html#IncrementalArguments.setIncrementalData)

    

Method to set Incremental Data

Parameters:

    

**pipes** (_EPipes_) – EPipes object

setParentArgs(_argumentsObj_)[[source]](_modules/zinggEC/enterprise/common/IncrementalArguments.html#IncrementalArguments.setParentArgs)

    

Method to set EArguments

Parameters:

    

**argumentsObj** (_EArguments_) – EArguments object

## zinggEC.enterprise.common.MappingMatchType

This module is to work with the mapping match type which is used for
leveraging domain expertise to push matching accuracy. Also saves time
massaging data before matching.

_class _zinggEC.enterprise.common.MappingMatchType.MappingMatchType(_name_ ,
_value_)[[source]](_modules/zinggEC/enterprise/common/MappingMatchType.html#MappingMatchType)

    

Bases: `object`

MappingMatchType class for defining mappings required for matching on a field

Parameters:

    

  * **name** (_String_) – name of the match type - MAPPING

  * **format** (_String_) – name of the json containing mappings

getMappingMatchType()[[source]](_modules/zinggEC/enterprise/common/MappingMatchType.html#MappingMatchType.getMappingMatchType)

    

Method to get mapping match type

Returns:

    

mapping match type containg name and value

Return type:

    

MappingMatchType

## zinggEC.enterprise.common.epipes

This module is submodule of zingg to work with different types of Pipes
supported in Enterprise. Classes of this module inherit the EPipe class, and
use that class to create many different types of pipes.

_class _zinggEC.enterprise.common.epipes.ECsvPipe(_name_ , _location =None_,
_schema
=None_)[[source]](_modules/zinggEC/enterprise/common/epipes.html#ECsvPipe)

    

Bases: `EPipe`

Class CsvPipe: used for working with text files which uses a pipe symbol to
separate units of text that belong in different columns.

Parameters:

    

  * **name** (_String_) – name of the pipe.

  * **location** (_String_ _or_ _None_) – (optional) location from where we read data

  * **schema** (_Schema_ _or_ _None_) – (optional) json schema for the pipe

setDelimiter(_delimiter_)[[source]](_modules/zinggEC/enterprise/common/epipes.html#ECsvPipe.setDelimiter)

    

This method is used to define delimiter of CsvPipe

Parameters:

    

**delimiter** (_String_) – a sequence of one or more characters for specifying
the boundary between separate, independent regions in data streams

setHeader(_header_)[[source]](_modules/zinggEC/enterprise/common/epipes.html#ECsvPipe.setHeader)

    

Method to set header property of pipe

Parameters:

    

**header** (_String_ _(__'true' / 'false'__)_) – true if pipe have header,
false otherwise

setLocation(_location_)[[source]](_modules/zinggEC/enterprise/common/epipes.html#ECsvPipe.setLocation)

    

Method to set location of pipe

Parameters:

    

**location** (_String_) – location from where we read data

_class _zinggEC.enterprise.common.epipes.EPipe(_name_ ,
_format_)[[source]](_modules/zinggEC/enterprise/common/epipes.html#EPipe)

    

Bases: `Pipe`

EPipe class for working with different data-pipelines. Actual pipe def in the
args. One pipe can be used at multiple places with different tables,
locations, queries, etc

Parameters:

    

  * **name** (_String_) – name of the pipe

  * **format** (_Format_) – format of pipe e.g. inMemory, delta, etc.

getPassthroughExpr()[[source]](_modules/zinggEC/enterprise/common/epipes.html#EPipe.getPassthroughExpr)

    

Method to get pass through condition

Returns:

    

pass through conditions in string format

Return type:

    

String

getPassthruData()[[source]](_modules/zinggEC/enterprise/common/epipes.html#EPipe.getPassthruData)

    

Method to get records which satisfy pass through condition

return: pandas or spark dataframe containing records which satisfy pass
through condition :rtype: DataFrame

getUsableData()[[source]](_modules/zinggEC/enterprise/common/epipes.html#EPipe.getUsableData)

    

Method to get records which do not satisfy pass through condition

return: pandas or spark dataframe containing records which do not satisfy pass
through condition :rtype: DataFrame

hasPassThru()[[source]](_modules/zinggEC/enterprise/common/epipes.html#EPipe.hasPassThru)

    

Method to check if there is a pass through condition

Returns:

    

whether pass through condition is present or not

Return type:

    

boolean

setPassthroughExpr(_passthroughExpr_)[[source]](_modules/zinggEC/enterprise/common/epipes.html#EPipe.setPassthroughExpr)

    

Method to set pass through condition

Parameters:

    

**passthroughExpr** (_String_) – String condition for records to not be
considered

_class _zinggEC.enterprise.common.epipes.InMemoryPipe(_name_ , _df
=None_)[[source]](_modules/zinggEC/enterprise/common/epipes.html#InMemoryPipe)

    

Bases: `EPipe`

Pipe Class for working with InMemory pipeline :param name: name of the pipe
:type name: String :param df: provide dataset for this pipe (optional) :type
df: Dataset or None

getDataset()[[source]](_modules/zinggEC/enterprise/common/epipes.html#InMemoryPipe.getDataset)

    

Method to get Dataset from pipe

Returns:

    

dataset of the pipe in the format of spark dataset

Return type:

    

Dataset<Row>

setDataset(_df_)[[source]](_modules/zinggEC/enterprise/common/epipes.html#InMemoryPipe.setDataset)

    

Method to set DataFrame of the pipe

Parameters:

    

**df** (_DataFrame_) – pandas or spark dataframe for the pipe

_class _zinggEC.enterprise.common.epipes.UCPipe(_name_ ,
_table_)[[source]](_modules/zinggEC/enterprise/common/epipes.html#UCPipe)

    

Bases: `EPipe`

Pipe Class for working with Delta tables in Unity Catalog of Databricks

Parameters:

    

  * **name** (_String_) – name of the pipe

  * **table** (_String_) – table from where we read data in the Catalog Volumes

setTable(_table_)[[source]](_modules/zinggEC/enterprise/common/epipes.html#UCPipe.setTable)

    

Method to set table in pipe

Parameters:

    

**table** (_String_) – table from where we read data

## zinggEC.enterprise.common.EArguments

This module is to work with different types of features supported in Zingg
Enterprise.

_class _zinggEC.enterprise.common.EArguments.DeterministicMatching(_*
matchCond_)[[source]](_modules/zinggEC/enterprise/common/EArguments.html#DeterministicMatching)

    

Bases: `object`

getDeterministicMatching()[[source]](_modules/zinggEC/enterprise/common/EArguments.html#DeterministicMatching.getDeterministicMatching)

    

Method to get DeterministicMatching criteria

Returns:

    

DeterministicMatching parameter value

Return type:

    

DeterministicMatching

_class
_zinggEC.enterprise.common.EArguments.EArguments[[source]](_modules/zinggEC/enterprise/common/EArguments.html#EArguments)

    

Bases: `Arguments`

getArgs()[[source]](_modules/zinggEC/enterprise/common/EArguments.html#EArguments.getArgs)

    

Method to get EArguments

Returns:

    

EArguments parameter value

Return type:

    

EArguments

getData()[[source]](_modules/zinggEC/enterprise/common/EArguments.html#EArguments.getData)

    

Method to get data from multiple pipes

Returns:

    

Array of data contained in EPipes

Return type:

    

Array[EPipe]

getDeterministicMatching()[[source]](_modules/zinggEC/enterprise/common/EArguments.html#EArguments.getDeterministicMatching)

    

Method to get DeterministicMatching criteria

Returns:

    

DeterministicMatching parameter value

Return type:

    

DeterministicMatching

getFieldDefinition()[[source]](_modules/zinggEC/enterprise/common/EArguments.html#EArguments.getFieldDefinition)

    

Method to get all field definitions to be used for matching

Returns:

    

all field definitions in list format

Return type:

    

List[EFieldDefinition]

getPassthroughExpr()[[source]](_modules/zinggEC/enterprise/common/EArguments.html#EArguments.getPassthroughExpr)

    

Method to get pass through condition

Returns:

    

pass through conditions in string format

Return type:

    

String

getPrimaryKey()[[source]](_modules/zinggEC/enterprise/common/EArguments.html#EArguments.getPrimaryKey)

    

Method to get the fields containing the primary keys

Returns:

    

all primary keys defined for field definitions in list format

Return type:

    

List[EFieldDefinition]

setArgs(_argumentsObj_)[[source]](_modules/zinggEC/enterprise/common/EArguments.html#EArguments.setArgs)

    

Method to set EArguments

Parameters:

    

**argumentsObj** (_EArguments_) – EArguments object

setBlockingModel(_blockingModel_)[[source]](_modules/zinggEC/enterprise/common/EArguments.html#EArguments.setBlockingModel)

    

Method to set the Blocking Model used for creating model

Parameters:

    

**blockingModel** (_String_) – value as DEFAULT or WIDER

setData(_*
pipes_)[[source]](_modules/zinggEC/enterprise/common/EArguments.html#EArguments.setData)

    

Method to set the multiple pipes for data

Parameters:

    

**pipes** (_EPipes_) – EPipes object

setDeterministicMatchingCondition(_*
detMatchConds_)[[source]](_modules/zinggEC/enterprise/common/EArguments.html#EArguments.setDeterministicMatchingCondition)

    

Method to set the DeterministicMatchingCondition used for matching

Parameters:

    

**detMatchConds** (_DeterministicMatching_) – DeterministicMatching object

setFieldDefinition(_fieldDef_)[[source]](_modules/zinggEC/enterprise/common/EArguments.html#EArguments.setFieldDefinition)

    

Method to set the field definitions

Parameters:

    

**fieldDef** (_EFieldDefinition_) – EFieldDefiniton object

setPassthroughExpr(_passthroughExpr_)[[source]](_modules/zinggEC/enterprise/common/EArguments.html#EArguments.setPassthroughExpr)

    

Method to set pass through condition

Parameters:

    

**passthroughExpr** (_String_) – String condition for records to not be
considered

## zinggEC.enterprise.common.EFieldDefinition

This module is to work with the extended functionality of field definitions

_class _zinggEC.enterprise.common.EFieldDefinition.EFieldDefinition(_name_ ,
_dataType_ , _*
matchType_)[[source]](_modules/zinggEC/enterprise/common/EFieldDefinition.html#EFieldDefinition)

    

Bases: `FieldDefinition`

This class defines each field that we use in matching. We can use this to
configure the properties of each field we use for matching in Zingg.

Parameters:

    

  * **name** (_String_) – name of the field

  * **dataType** (_String_) – type of the data e.g. string, float, etc.

  * **matchType** (_MatchType_) – match type of this field e.g. FUSSY, EXACT, etc. including user-defined mapping match types

getMatchTypeArray(_matchType_)[[source]](_modules/zinggEC/enterprise/common/EFieldDefinition.html#EFieldDefinition.getMatchTypeArray)

    

Method to get the match types associated with a field

Parameters:

    

**matchType** (_List_ _[__IMatchType_ _]_) – list of match types associated
with a field

getPrimaryKey()[[source]](_modules/zinggEC/enterprise/common/EFieldDefinition.html#EFieldDefinition.getPrimaryKey)

    

Method to check if the field contains the primary key

Returns:

    

true or false depending on if the field contains the primary key

Return type:

    

boolean

setPrimaryKey(_primaryKey_)[[source]](_modules/zinggEC/enterprise/common/EFieldDefinition.html#EFieldDefinition.setPrimaryKey)

    

Method to set the field containing the primary key

Parameters:

    

**primaryKey** (_boolean_) – true or false depending on if the field contains
the primary key

[ Previous](index.html "Zingg Enterpise Entity Resolution Python Package")

* * *

(C) Copyright 2025, Zingg.AI.

Built with [Sphinx](https://www.sphinx-doc.org/) using a
[theme](https://github.com/readthedocs/sphinx_rtd_theme) provided by [Read the
Docs](https://readthedocs.org).

