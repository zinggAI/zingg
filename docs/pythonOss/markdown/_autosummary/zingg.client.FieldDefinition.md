# zingg.client.FieldDefinition

### *class* zingg.client.FieldDefinition(name, dataType, \*matchType)

Bases: `object`

This class defines each field that we use in matching We can use this to configure the properties of each field we use for matching in Zingg.

* **Parameters:**
  * **name** (*String*) – name of the field
  * **dataType** (*String*) – type of the data e.g. string, float, etc.
  * **matchType** (*MatchType*) – match type of this field e.g. FUSSY, EXACT, etc.

### Methods

| [`__init__`](#zingg.client.FieldDefinition.__init__)                     |                                                                                |
|--------------------------------------------------------------------------|--------------------------------------------------------------------------------|
| [`getFieldDefinition`](#zingg.client.FieldDefinition.getFieldDefinition) | Method to get  pointer address of this class                                   |
| `getMatchTypeArray`                                                      |                                                                                |
| [`setStopWords`](#zingg.client.FieldDefinition.setStopWords)             | Method to add stopwords to this class object                                   |
| [`stringify`](#zingg.client.FieldDefinition.stringify)                   | Method to stringify'ed the dataType before it is set in FieldDefinition object |

#### \_\_init_\_(name, dataType, \*matchType)

#### getFieldDefinition()

Method to get  pointer address of this class

* **Returns:**
  The pointer containing the address of this class object
* **Return type:**
  pointer([FieldDefinition](#zingg.client.FieldDefinition))

#### setStopWords(stopWords)

Method to add stopwords to this class object

* **Parameters:**
  **stopWords** (*String*) – The stop Words containing csv file’s location

#### stringify(str)

Method to stringify’ed the dataType before it is set in FieldDefinition object

* **Parameters:**
  **str** (*String*) – dataType of the FieldDefinition
* **Returns:**
  The stringify’ed value of the dataType
* **Return type:**
  String
