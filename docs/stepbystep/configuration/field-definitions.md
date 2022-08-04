---
description: >-
  Defining which fields should appear in the output and whether and how they
  need to be used in matching
---

# Field Definitions

#### fieldDefinition

This is a JSON array representing the fields from the source data to be used for matching, and the kind of matching they need.

Each field denotes a column from the input. Fields have the following JSON attributes

**fieldName**

The name of the field from the input data schema

**fields**

To be defined later. For now, please keep this as the fieldName

**dataType**

Type of the column - string, integer, double etc

**matchType**

&#x20;The way to match the given field. Here are the different types supported.&#x20;



#### FUZZY

Broad matches with typos, abbreviations, and other variations.

**EXACT**

Less tolerant with variations, but would still match inexact strings to some degree. Preferable for country codes, pin codes, and other categorical variables where you expect fewer variations.

**DONT\_USE**

The name says it :-) Appears in the output but no computation is done on these. Helpful for fields like ids that are required in the output.

```python
"fieldDefinition" : [ 
		{
			"fieldName" : "fname",
			"matchType" : "fuzzy",
			"fields" : "fname",
			"dataType": "\"string\"" 
		},
		{
			"fieldName" : "lname",
			"matchType" : "fuzzy",
			"fields" : "lname",
			"dataType": "\"string\"" 
		}
  ]
```

In the above example, the field id from the input is present in the output but not used for comparisons. Also, these fields may not be shown to the user while labeling, if [showConcise](field-definitions.md#showconcise) is set to true.

####

#### showConcise

When this flag is set to true, during [Label](../../setup/training/label.md) and [updateLabel](../../updatingLabels.md), only those fields are displayed on the console which helps build the model. In other words, fields that have matchType as "DONT\_USE", are not displayed to the user. Default is false.

####
