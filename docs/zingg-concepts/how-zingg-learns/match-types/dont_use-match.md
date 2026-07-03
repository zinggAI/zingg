---
description: >-
  Includes a field in Zingg output but excludes it from matching entirely. No
  similarity computation is run on DONT_USE fields.
---

# DONT\_USE Match

### What `DONT_USE` does

`DONT_USE` tells Zingg to carry a field through to the output without using it in any similarity computation. No matching algorithm is applied. The field appears in every output record exactly as it appeared in the input.

`DONT_USE` fields are also hidden from the labeller when the `showConcise` flag is set to `true` —\
so your domain experts see only the fields that are actually being used for matching when they label pairs.

### How the algorithm works

_**COMMENT FOR TEAM — Algorithm detail for DONT\_USE to be added here.**_

{% hint style="success" icon="right-long" %}
`DONT_USE` performs no computation. There is no similarity algorithm to describe - the field is passed through to output unchanged.
{% endhint %}

### What **`DONT_USE`** matches and what it does not

<table><thead><tr><th valign="top">Scenario</th><th valign="top">With DONT_USE</th><th valign="top">Without DONT_USE</th></tr></thead><tbody><tr><td valign="top">customer_id field in output</td><td valign="top">Appears in every output row with original value.<br>Not used in matching.</td><td valign="top">Would need to be in field<br>definitions for output -<br>but would also contribute<br>to match scoring.</td></tr><tr><td valign="top">record_id = "REC-00123"</td><td valign="top">Carried to output as "REC-00123". Not compared against other records.</td><td valign="top">"REC-00123" would be compared fuzzily or exactly against other record IDs - likely producing false positives or false negatives.</td></tr><tr><td valign="top">Shown in labeller</td><td valign="top">Hidden when <code>showConcise=true</code> <br>reduces visual noise for labellers.</td><td valign="top">Would appear in every labeller row, distracting from the fields that actually drive matching.</td></tr></tbody></table>

### When to use `DONT_USE`

<details>

<summary><strong>Record IDs and internal keys</strong></summary>

Any field that uniquely identifies a record in a source system `customer_id`, `account_id`, `case_id`, `transaction_id` should use `DONT_USE`.

These fields must appear in output so you can trace resolved clusters back to source records. But they should never influence matching - different source systems use different ID schemes for the same entity.

Using `FUZZY` on a customer\_id field would cause records with similar-looking IDs from different systems to match, which is almost certainly wrong.

</details>

<details>

<summary><strong>Audit and traceability fields</strong></summary>

Timestamps, data source identifiers, record creation dates, batch IDs - any field needed in output for audit or traceability purposes but irrelevant to entity identity.

</details>

<details>

<summary><strong>Reducing labeller noise with <code>showConcise</code></strong></summary>

When your schema has many fields but only 4-6 are actually used for matching, setting `DONT_USE` on the rest and using `--showConcise=true` in the CLI makes the labelling\
interface cleaner. Labellers see only the fields that drive decisions.

</details>

### When not to use `DONT_USE`

<details>

<summary><strong>When the field carries a matching signal</strong></summary>

If a field has information that should influence whether two records represent the same entity, even as a weak signal do not use `DONT_USE`. Use `FUZZY` with low weight, or include it in a deterministic matching condition (Enterprise).

`DONT_USE` is for fields that carry zero matching information.

</details>

{% tabs %}
{% tab title="Python" %}
### **Community**

```python
from zingg.client import *

    rec_id = FieldDefinition("rec_id", "string", MatchType.DONT_USE)
```

### **Enterprise**

```python
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition

    rec_id = EFieldDefinition("rec_id", "string", MatchType.DONT_USE)
```
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block is identical for Community and Enterprise. Only the Python class differs between editions — `FieldDefinition` (Community) vs `EFieldDefinition` (Enterprise). The CLI command is identical for both editions.
{% endhint %}

```json
{
  "fieldDefinition" : [ {
    "fieldName" : "rec_id",
    "matchType" : "dont_use",
    "fields" : "rec_id",
    "dataType" : "string"
  } ]
}
```

### **CLI**

```bash
./ scripts / zingg.sh-- phase findTrainingData-- conf config.json
```
{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
**Related types**:

* `NULL_OR_BLANK` - for fields that should participate in matching but have frequent nulls
* `EXACT` - for fields that should contribute an exact match signal
* `showConcise` flag - hide `DONT_USE` fields from the labeller (CLI command reference)&#x20;

**Read more**: [Match types](./)
{% endhint %}
