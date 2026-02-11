# MappingMatchType

## zinggEC.enterprise.common.MappingMatchType

This module is to work with the mapping match type which is used for leveraging domain expertise to push matching accuracy. Also saves time massaging data before matching.

### *class* zinggEC.enterprise.common.MappingMatchType.MappingMatchType(name, value)

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
  [MappingMatchType](#zinggEC.enterprise.common.MappingMatchType.MappingMatchType)
