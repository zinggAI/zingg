# Field Definitions

#### fieldDefinition

Which fields from the source data are to be used for matching, and what kind of matching do they need?

**FUZZY**

Broad matches with typos, abbreviations, and other variations.

**EXACT**

Less tolerant with variations, but would still match inexact strings to some degree. Preferable for country codes, pin codes, and other categorical variables where you expect fewer variations.

**"DONT\_USE"**

The name says it :-) Appears in the output but no computation is done on these. Helpful for fields like ids that are required in the output.

```python
"fieldDefinition" : [ {
    "matchType" : "DONT_USE",
    "fieldName" : "id",
    "fields" : "id"
  },
  { 
    "matchType" : "FUZZY",
    "fieldName" : "firstName",
    "fields" : "firstName"
  }
  ]
```

In the above example, the field id from the input is present in the output but not used for comparisons. Also, these fields may not be shown to the user while labeling, if [showConcise](field-definitions.md#showconcise) is set to true.

####

#### showConcise

When this flag is set to true, during [Label](../../setup/training/label.md) and [updateLabel](../../updatingLabels.md), only those fields are displayed on the console which helps build the model. In other words, fields that have matchType as "DONT\_USE", are not displayed to the user. Default is false.

####
