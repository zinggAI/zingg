---
description: >-
  Defining which fields should appear in the output and whether and how they
  need to be used in matching
---

# Field Definitions

#### fieldDefinition

This is a JSON array representing the fields from the source data to be used for matching, and the kind of matching they need.

Each field denotes a **column** from the input. Fields have the following JSON attributes:

**fieldName**

The **name** of the field from the input data schema

**fields**

To be defined later. For now, please keep this as the `fieldName`

**dataType**

Type of the column - `string, integer, double`, etc.

**matchType**

The way to match the given field. Multiple match types, separated by commas, can also be used. For example **FUZZY**, **NUMERIC**. Here are the different types supported:

#### showConcise

| Match Type             | Description                                                                                                                                                                                                                                           | Applicable To                 |
| ---------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------- |
| FUZZY                  | Broad matches with typos, abbreviations, and other variations.                                                                                                                                                                                        | string, integer, long, double, date   |
| EXACT                  | No tolerance with variations, Preferable for country codes, pin codes, and other categorical variables where you expect no variations.                                                                                                                | string, integer, long, date, boolean                        |
| DONT\_USE              | Appears in the output but no computation is done on these. Helpful for fields like ids that are required in the output. DONT\_USE fields are not shown to the user while labeling, if [showConcise](field-definitions.md#showconcise) is set to true. | any                           |
| EMAIL                  | Matches only the id part of the email before the @ character                                                                                                                                                                                          | any                           |
| PINCODE                | Matches pin codes like xxxxx-xxxx with xxxxx                                                                                                                                                                                                          | string                        |
| NULL\_OR\_BLANK      | By default Zingg treats nulls as matches, but if we add this to a field which has other match type like FUZZY, Zingg will build a feature for null values and learn                                                                                                                                                                                                           | string, integer, long, date, boolean                        |
| TEXT                   | Compares words overlap between two strings. Good for descriptive fields without much typos                                                                                                                               | string                        |
| NUMERIC                | extracts numbers from strings and compares how many of them are same across both strings, for example apartment numbers.                                                                                                                                                              | string                        | 
| NUMERIC\_WITH\_UNITS   | extracts product codes or numbers with units, for example 16gb from strings and compares how many are same across both strings                                                                                                                        | string                        | 
| ONLY\_ALPHABETS\_EXACT | only looks at the alphabetical characters and compares if they are exactly the same. when the numbers inside strings do not matter, for example if you are looking at buildings but want to ignore flat numbers                                                                                                                                                                  | string                        | 
| ONLY\_ALPHABETS\_FUZZY | ignores any numbers in the strings and then does a fuzzy comparison, useful for fields like addresses with typos where you want to look at street number separately using 
NUMERIC                                                                                                                                                                                 | string                        | 

####
