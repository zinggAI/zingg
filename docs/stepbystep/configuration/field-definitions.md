---
description: >-
  Defining which fields should appear in the output and whether and how they
  need to be used in matching.
---

# Field Definitions

#### fieldDefinition

This is a JSON array representing the fields from the source data to be used for matching, and the kind of matching they need.

Each field denotes a **column** from the input. Fields have the following JSON attributes:

**fieldName**

The **name** of the field from the input data schema.

**fields**

To be defined later. For now, please keep this as the `fieldName`

**dataType**

Type of the column - `string, integer, double`, etc.

**matchType**

- The way to match the given field. Multiple match types can be combined (comma-separated), for example: `FUZZY,NUMERIC`.
- Here are the supported match types and descriptions:

|                     Match Type | Description                                                                                                                                                                                                                                                                                    | Applicable To                                      |
|-------------------------------:|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------|
|                          FUZZY | Broad matches tolerant to typos, abbreviations, and other variations. Uses fuzzy string similarity features.                                                                                                                                                                                   | string, integer, long, double, date                |
|                FUZZY_OPTIMISED | Same semantics as FUZZY but uses an optimized implementation. Provides similar matching quality with significantly lower CPU and memory usage on large datasets. Recommended for production/large-scale matching when FUZZY quality is desired but performance is critical. [Zingg Enterprise Feature](#user-content-fn-1)[^1] | string, integer, long, double, date                                             |
|                          EXACT | Exact match with no tolerance for variation. Preferable for country codes, pin codes, and other categorical variables where you expect no variations.                                                                                                                                          | string, integer, long, date, boolean               |
|                       DONT_USE | Included in the output but no computation is done on this field. Useful for IDs required in the output. DONT_USE fields are not shown to the labeler when [showConcise](label.md) is set to true.                                                                                              | any                                                |
|                          EMAIL | Matches only the local part (before the @) of email addresses.                                                                                                                                                                                                                                 | string                                             |
|                EMAIL_OPTIMISED | Same semantics as EMAIL but uses an optimized implementation for much faster evaluation on large datasets while preserving match behavior. Recommended for large-scale runs where many emails are compared. [Zingg Enterprise Feature](#user-content-fn-1)[^1]                                                                                   | string                                             |
|                        PINCODE | Matches postal / pin codes (supports typical local formats such as `xxxxx` or `xxxxx-xxxx` depending on data).                                                                                                                                                                                 | string                                             |
|                  NULL_OR_BLANK | By default, Zingg treats nulls as matches. If NULL_OR_BLANK is added to a field that also has other match types (e.g., `FUZZY`), Zingg will build an explicit feature for null/blank values so the model can learn their effect.                                                               | string, integer, long, date, boolean               |
|                           TEXT | Compares overlapping words between two strings. Good for descriptive or long-text fields without many typos.                                                                                                                                                                                   | string                                             |
|                        NUMERIC | Extracts numbers from strings and compares how many are the same across both strings (useful for apartment numbers, building numbers, etc.).                                                                                                                                                   | string                                             |
|             NUMERIC_WITH_UNITS | Extracts product codes or numbers with units (e.g., `16gb`) and compares how many are same across both strings                                                                                                                                                                 | string                                             |
|           ONLY_ALPHABETS_EXACT | Compares only the alphabetic characters and requires an exact match. Useful when numeric parts should be ignored (e.g., building name vs flat number).                                                                                                                                         | string                                             |
|           ONLY_ALPHABETS_FUZZY | Compares only the alphabetic characters using a fuzzy comparison; numeric characters are ignored. Useful for addresses when you want to handle street names fuzzily while numeric parts are handled separately (e.g., via NUMERIC).                                                            | string                                             |
| ONLY_ALPHABETS_FUZZY_OPTIMISED | Same semantics as ONLY_ALPHABETS_FUZZY but uses optimized processing. Use this when you need the fuzzy alphabet-only behavior at production scale. [Zingg Enterprise Feature](#user-content-fn-1)[^1]                                                                                                                                            | string                                             |
|             MAPPING_(FILENAME) | Maps input values to canonical values using a mapping file (e.g., nickname maps, company abbreviations, gender codes). Matching is tolerant to common variations defined in the mapping. See [Advanced Match Types](adv-matchtypes.md) for mapping file format and examples.                   | string                                             |
