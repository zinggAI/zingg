# EFieldDefinition

## zinggEC.enterprise.common.EFieldDefinition

This module is to work with the extended functionality of field definitions

### *class* zinggEC.enterprise.common.EFieldDefinition.EFieldDefinition(name, dataType, \*matchType)

Bases: `FieldDefinition`

This class defines each field that we use in matching. We can use this to configure the properties of each field we use for matching in Zingg.

* **Parameters:**
  * **name** (*String*) – name of the field
  * **dataType** (*String*) – type of the data e.g. string, float, etc.
  * **matchType** (*MatchType*) – match type of this field e.g. FUSSY, EXACT, etc. including user-defined mapping match types

#### getMatchTypeArray(matchType)

Method to get the match types associated with a field

* **Parameters:**
  **matchType** (*List**[**IMatchType**]*) – list of match types associated with a field

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
