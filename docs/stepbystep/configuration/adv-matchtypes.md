---
description: >-
  Defining match types for enterprise
---

# Advanced Match Types

## Defining match types to support generic mappings as well as get domain knowledge

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

**Advanced matchType**

We come across many cases where multiple entities all map to a single entity or a single entity can be used in place of them. This may be in cases such as nicknames, company abbreviations and gender among others. For example, we have multiple records with nicknames such as ["Will", "Bill", "William"], ["John", "Johnny", "Jack"] where each set of map links to one person. So in this case, Will will map to William, Bill will map to William and all of them will be assigned to the same cluster given the rest of the fields also match. So. we can support generic mappings and help increasing domain knowledge as well.

The way to match the given field on multiple criteria such as nicknames and abbreviations. Multiple match types, separated by commas, can also be used. For example **FUZZY_MAPPING**, **EXACT_MAPPING**.  

Here, a json containing all mappings such as [“Will”, “Bill”, “William”], [“IBM", "International Business Machine”], ["0", "M", "Male"] needs to be created and stored according to user's requirement. For example, we make a json for company abbreviations and store is as `companies.json`. They will be added in the config as **EXACT_MAPPING_COMPANIES** along with other match types for the required field.

```json
"fieldDefinition":[
   	{
   		"fieldName" : "name",
   		"matchType" : "exact_mapping_companies",
   		"fields" : "name",
   		"dataType": "string"
   	},
```

Here are the different types supported:

| Match Type             | Description                                                                | Applicable To                 |
| ---------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------- |
| FUZZY_MAPPING                  | Broad matches with typos, abbreviations, and other variations.                                                                           | string                 |
| EXACT_MAPPING                  | No tolerance with variations, Preferable for gender and other categorical variables where you expect no variations.                                                             | string                       |

### The mapping match type can be integrated as follows:

`export ZINGG_CONF_HOME=<path to json with abbreviations>`

### Example nicknames_test.json:

```json
[  
  ["Will", "Bill", "William"],
  ["John", "Johnny", "Jack"],
  ["Robert", "Rob", "Bob", "Bobby"],
  ["Charles", "Charlie", "Chuck"],
  ["James", "Jim", "Jimmy"],
  ["Thomas", "Tom", "Tommy"]
]   
```

### Snowflake:



### Databricks:

