---
description: >-
  Defining match types for enterprise
---

# Advanced Match Types

## Defining match types to support generic mappings as well as get domain knowledge

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

**Advanced matchType**

The way to match the given field on multiple criteria such as nicknames and abbreviations. Multiple match types, separated by commas, can also be used. For example **FUZZY_MAPPING**, **EXACT_MAPPING**.  

Here, a json containing all mappings such as [“Will”, “Bill”, “William”] and [“IBM", "International Business Machine”] needs to be created and stored according to user's requirement. For example, we make a json for company abbreviations and store is as `companies.json`. They will be added in the config as **EXACT_MAPPING_COMPANIES** along with other match types.

Here are the different types supported:

| Match Type             | Description                                                                                                                                                                                                                                           | Applicable To                 |
| ---------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------- |
| FUZZY_MAPPING                  | Broad matches with typos, abbreviations, and other variations.                                                                                                                          | string                 |
| EXACT_MAPPING                  | No tolerance with variations, Preferable for country codes, pin codes, and other categorical variables where you expect no variations.                                                                                                                | string                       |