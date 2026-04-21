# Transform

## Overview
The Transform phase in Zingg allows you to apply various transformations to your data before it is used for matching or other downstream processes. This phase is crucial for cleaning and standardising data, ensuring consistency, and improving the quality of matches.
The Standardise Transformer standardises field values during the Transform phase using predefined mappings. It addresses inconsistent values that commonly appear across systems (for example, job titles such as "VP of Ops", "Vice President Operations", or "V.P. Operations").

Key points:
- The transformer runs during the Transform phase, independently of matching.
- It can be used to clean and standardise data before matching or as a standalone data transformation step.
- Matching for mapping lookup is case-insensitive by default.
- Primary key fields are never transformed.

Industry usage and benefits:
- Human resources: standardise job titles and roles across multiple systems to improve headcount and reporting accuracy.
- Sales and CRM: normalize company names for consistent account consolidation and analytics.
- Product and catalog management: unify legacy and new product codes for inventory and billing reconciliation.
- Compliance and reporting: enforce canonical values to simplify regulatory reporting and downstream processing.

---

## Mapping file format and rules

Create a JSON file where each element is an array of equivalent values. The **first value** in each array is the canonical value and is used as the replacement for all other values in that row.

Important rules:
- First value is canonical: e.g. `["Chief Executive Officer", "CEO", "Executive Director"]` — all variants map to `Chief Executive Officer`.
- Lookup is case-insensitive: `CEO`, `ceo`, `Ceo` will match.
- Rows must be disjoint: a string must not appear in two different canonical groups. Since lookup is case-insensitive, ensure that strings are disjoint even when ignoring case. For example, if you have `"CEO"` in one group, you cannot have `"ceo"` or `"Ceo"` in another group.
- The transformer does not normalize extra whitespace, punctuation, or partial matches. Those are not handled automatically.

Example: `jobtitles.json`

```json
[
  ["Chief Executive Officer", "CEO", "Executive Director", "Managing Director", "President"],
  ["Vice President of Operations", "VP of Ops", "Vice President Operations", "V.P. Operations", "VP Ops"],
  ["Software Engineer", "SWE", "Software Developer", "Developer", "Programmer"],
  ["Data Scientist", "Data Science Engineer", "DS Engineer", "ML Engineer"],
  ["Senior Manager", "Sr Manager", "Sr. Manager", "Senior Mgr"]
]
```

Place mapping files in the directory from where you are executing Zingg. Use the base filename (without `.json`) when referencing the mapping.

---

## Configure transformation

Here is how to configure the `StandardiseTransformer` for a field in Python:

```python
fname = EFieldDefinition("fname", "string", MatchType.FUZZY)
fname.setTransformers([StandardiseTransformerType("STANDARDISE", "nicknames_test")])
```

## Python example

```python
from zingg.client import *
from zinggEC.enterprise.common.EArguments import *
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggEC.enterprise.common.StandardiseTransformerType import StandardiseTransformerType
from zinggEC.enterprise.common.MappingMatchType import MappingMatchType
from zinggEC.enterprise.common.epipes import *
from zinggES.enterprise.spark.ESparkClient import *
from zinggEC.enterprise.common.EClientOptions import *

args = EArguments()

id = EFieldDefinition("id", "string", MatchType.DONT_USE)
id.setPrimaryKey(True)

# Example: Here we are using transformation for job_title
job_title = EFieldDefinition("job_title", "string", MatchType.FUZZY)
job_title.setTransformers([StandardiseTransformerType("STANDARDISE", "jobtitles")])

fieldDefs = [id, job_title]
args.setFieldDefinition(fieldDefs)

# Configure input and output pipes
inputPipe = CsvPipe("input", "examples/febrl/input.csv")
outputPipe = CsvPipe("output", "examples/febrl/transformed_output.csv")

args.setData(inputPipe)
args.setOutput(outputPipe)

# Set the zingg directory
args.setZinggDir("models")

# Create the client and run transform phase
sparkClient = ESparkClient(args, EClientOptions.TRANSFORM)
sparkClient.init()
sparkClient.execute()
```

---

## JSON configuration example

Use `STANDARDISE_<basename>` to reference a mapping file named `<basename>.json`.

```json
{
  "fieldDefinition": [
    {
      "fieldName": "id",
      "matchType": "dont_use",
      "dataType": "string",
      "primaryKey": true
    },
    {
      "fieldName": "job_title",
      "matchType": "fuzzy",
      "dataType": "string",
      "transformers": "STANDARDISE_jobtitles"
    }
  ],
  "data": [
    {
      "name": "input",
      "format": "csv",
      "props": {
        "location": "examples/febrl/input.csv",
        "header": "true"
      }
    }
  ],
  "output": [
    {
      "name": "output",
      "format": "csv",
      "props": {
        "location": "examples/febrl/transformed_output.csv",
        "header": "true"
      }
    }
  ],
  "zinggDir": "models",
  "modelId": "100",
  "numPartitions": 4
}
```

---

## Command-line usage

Run the Transform phase with the following command:

```bash
scripts/zingg.sh --phase transform --conf examples/febrl/config.json --properties-file config/zingg.conf
```

This will:
1. Read data from the configured input source
2. Apply all configured transformers (including standardisation)
3. Write the transformed data to the configured output

---