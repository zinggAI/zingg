# Standardise Postprocessor

## Overview

The Standardise Postprocessor standardises field values in matched output using predefined mappings. It addresses inconsistent values that commonly appear across systems (for example, job titles such as "VP of Ops", "Vice President Operations", or "V.P. Operations").

Key points:
- The postprocessor runs after matching and before writing output.
- It does not affect matching behavior; it transforms output values only.
- Matching for mapping lookup is case-insensitive by default.
- Primary key fields are never postprocessed.

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
- Rows must be disjoint: a string must not appear in two different canonical groups.
- The processor does not normalize extra whitespace, punctuation, or partial matches. Those are not handled automatically.

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

Place mapping files from where you are executing. Use the base filename (without `.json`) when referencing the mapping.

---

## Configure postprocessing
Here is how to configure the `StandardisePostprocessor` for a field in Python:
```python
fname = EFieldDefinition("fname", "string", MatchType.FUZZY)
fname.setPostProcessors([StandardisePostprocessorType("STANDARDISE", "nicknames_test")])
```

## Python example

```python
from zingg.client import *
from zinggEC.enterprise.common.EArguments import *
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggEC.enterprise.common.StandardisePostprocessorType import StandardisePostprocessorType
from zinggEC.enterprise.common.MappingMatchType import MappingMatchType
from zinggEC.enterprise.common.epipes import *
from zinggES.enterprise.spark.ESparkClient import *
from zinggEC.enterprise.common.EClientOptions import *

args = EArguments()

id = EFieldDefinition("id", "string", MatchType.DONT_USE)
id.setPrimaryKey(True)

# Example: Here we are using postprocessing for job_title
job_title = EFieldDefinition("job_title", "string", MatchType.FUZZY)
job_title.setPostProcessors([StandardisePostprocessorType("STANDARDISE", "jobtitles")])

fieldDefs = [id, job_title]
args.setFieldDefinition(fieldDefs)

# configure input/output pipes and run as usual
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
      "postProcessors": "STANDARDISE_jobtitles"
    }
  ]
}
```
