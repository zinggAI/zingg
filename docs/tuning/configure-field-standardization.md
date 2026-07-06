---
description: >-
  Normalise field values in your Zingg match output to a canonical form using
  predefined value mappings.
tags:
  - ent
---

# Configure Field Standardization

{% hint style="info" icon="right-long" %}
**Enterprise** only. The Standardise Postprocessor is available in ZinggEC (Enterprise) and ZinggES (Enterprise Plus). Not available in Community.
{% endhint %}

After matching, field values across resolved records often contain inconsistent variations. For example, a job title field may contain `VP of Ops`, `Vice President Operations`, and `V.P. Operations` , all representing the same role but appearing differently across source systems.

The standardized postprocessor normalizes these to a canonical form before writing output. This is the final step between matched data and production-ready golden records.

### Mapping file format and rules

Create a JSON file where each element is an array of equivalent values. The first value in each array is the canonical value. All other values in that array map to it in the output.

**Example: `jobtitles.json`**

```json
[
  ["Chief Executive Officer", "CEO",
   "Executive Director",
   "Managing Director", "President"],
  ["Vice President of Operations",
   "VP of Ops",
   "Vice President Operations",
   "V.P. Operations", "VP Ops"],
  ["Software Engineer", "SWE",
   "Software Developer",
   "Developer", "Programmer"],
  ["Data Scientist",
   "Data Science Engineer",
   "DS Engineer", "ML Engineer"],
  ["Senior Manager", "Sr Manager",
   "Sr. Manager", "Senior Mgr"]
]
```

#### **Three rules your mapping file must follow**

<table><thead><tr><th width="236.6796875">Rule</th><th>Why it matters</th></tr></thead><tbody><tr><td>First value is canonical</td><td>All variants in the array map to the first value in the output. <code>["Chief Executive Officer", "CEO"]</code> → output will always say "Chief Executive Officer". Order your arrays with the canonical form first.</td></tr><tr><td>Lookup is case-insensitive</td><td>"CEO", "ceo", and "Ceo" all match the same entry. You do not need separate entries for casing variants.</td></tr><tr><td>Rows must be disjoint</td><td>A string must not appear in two different arrays. If "CEO" appears in two separate canonical groups, Zingg's behaviour is undefined. Each value belongs to exactly one canonical group.</td></tr></tbody></table>

{% hint style="success" icon="right-long" %}
Place your mapping JSON file in the directory from which you are executing Zingg. Reference it by base filename (without the `.json` extension) in your configuration. For example, a file named `jobtitles.json` is referenced as `jobtitles`.
{% endhint %}

{% tabs %}
{% tab title="Enterprise" %}
### Configure postprocessing

Use `STANDARDISE_<basename>` to reference a mapping file named `<basename>.json`. The `STANDARDISE` prefix tells Zingg which postprocessor type to use, and the basename without `.json` is the filename of your mapping file. For the example mapping above, the file is `jobtitles.json` and the reference is `STANDARDISE_jobtitles`.

#### Python

```python
from zingg.client import *
from zinggEC.enterprise.common.EArguments import *
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggEC.enterprise.common.StandardisePostprocessorType import StandardisePostprocessorType
from zinggEC.enterprise.common.epipes import *
from zinggES.enterprise.spark.ESparkClient import *
from zinggEC.enterprise.common.EClientOptions import *

args = EArguments()

id = EFieldDefinition("id", "string", MatchType.DONT_USE)
id.setPrimaryKey(True)

job_title = EFieldDefinition("job_title", "string", MatchType.FUZZY)
job_title.setPostProcessors([
    StandardisePostprocessorType("STANDARDISE", "jobtitles")
])

fieldDefs = [id, job_title]
args.setFieldDefinition(fieldDefs)
```

Configure input and output pipes, then run `match` or `runIncremental` as usual.

#### JSON

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

{% hint style="success" icon="right-long" %}
Use `STANDARDISE_<basename>` to reference a mapping file named `<basename>.json`. The `STANDARDISE` prefix tells Zingg which postprocessor type to use. The `<basename>` (without `.json`) is the filename of your mapping file. For the example above, the file is `jobtitles.json` and the reference is `STANDARDISE_jobtitles`.
{% endhint %}
{% endtab %}

{% tab title="Enterprise Snowflake" %}
**CONTENT FOR THIS SECTION TO BE PROVIDED BY SONAL LATER**
{% endtab %}
{% endtabs %}

### **Verify the standardisation in your output**

After running `match`, inspect a field that has the postprocessor configured. Values previously appearing as "CEO" or "Executive Director" in your input should now appear as "Chief Executive Officer" in the output, matching the canonical value in your mapping file.

If the output still shows raw variants:

* Confirm the mapping file is in the correct directory (the one from which Zingg is being executed)
* Confirm the `postProcessors` JSON value or the `StandardisePostprocessorType` Python argument exactly matches `STANDARDISE_<basename>` or `("STANDARDISE", "<basename>")`
* Confirm the field is not set as `primaryKey: true` - primary keys are never postprocessed

{% hint style="success" icon="right-long" %}
**Read more**:

* [Standardize Fields and Results](../zingg-concepts/how-zingg-learns/zingg-models/standardize-fields-and-results.md) - concept and where it is useful
* [Configure Zingg](../running-zingg/configure-zingg.md) - full field definition configuration including all `EFieldDefinition` methods
* [Match Types](../zingg-concepts/how-zingg-learns/match-types/) - for the MAPPING match type, which uses a similar mapping file approach for matching
{% endhint %}
