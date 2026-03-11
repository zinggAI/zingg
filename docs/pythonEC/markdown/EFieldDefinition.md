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
  **matchType** (*List*[IMatchType]*) – list of match types associated with a field

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

---

#### setPostProcessors(postProcessors)

Method to set the postprocessors for a field

* **Parameters:**
  **postProcessors** (*List[IPostprocessor]*) – list of postprocessor objects to apply to field output (for example `StandardisePostprocessorType("STANDARDISE", "jobtitles")`)

**Note:** Postprocessors transform output values after matching completes. They are not applied to fields marked as primary keys. The `StandardisePostprocessor` uses mapping JSON files where the first value in each mapping row is the canonical replacement for other values in that row. Lookup is case-insensitive by default; other normalizations (trimming whitespace, punctuation handling) are not performed automatically.

#### getPostProcessors()

Method to get the postprocessors associated with a field

* **Returns:**
  list of postprocessor objects configured for the field
* **Return type:**
  List[IPostprocessor]
