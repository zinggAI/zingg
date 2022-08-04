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

| Match Type             | Description                                                                                                                                                                                                                                           | Can be applied to             |
| ---------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------- |
| FUZZY                  | Broad matches with typos, abbreviations, and other variations.                                                                                                                                                                                        | string, integer, double, date |
| EXACT                  | No tolerance with variations, Preferable for country codes, pin codes, and other categorical variables where you expect no variations.                                                                                                                | string                        |
| DONT\_USE              | Appears in the output but no computation is done on these. Helpful for fields like ids that are required in the output. DONT\_USE fields are not shown to the user while labeling, if [showConcise](field-definitions.md#showconcise) is set to true. | any                           |
| EMAIL                  | Matches only the id part of the email before the @ character                                                                                                                                                                                          | any                           |
| PINCODE                | Matches pin codes like xxxxx-xxxx with xxxxx                                                                                                                                                                                                          | string                        |
| NULL\_OR_\__BLANK      | By default Zingg marks matches as                                                                                                                                                                                                                     | string                        |
| TEXT                   | Compares words overlap between two strings.                                                                                                                                                                                                           | string                        |
| NUMERIC                | extracts numbers from strings and compares how many of them are same across both strings                                                                                                                                                              |                               |
| NUMERIC\_WITH\_UNITS   | extracts product codes or numbers with units, for example 16gb from strings and compares how many are same across both strings                                                                                                                        | string                        |
| ONLY\_ALPHABETS\_EXACT | only looks at the alphabetical characters and compares if they are exactly the same                                                                                                                                                                   | string                        |
| ONLY\_ALPHABETS\_FUZZY | ignores any numbers in the strings and then does a fuzzy comparison                                                                                                                                                                                   | string                        |

####
