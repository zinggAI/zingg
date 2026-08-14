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

{% hint style="success" icon="right-long" %}
`DONT_USE` performs no computation. There is no similarity algorithm to describe - the field is passed through to output unchanged.
{% endhint %}

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

If a field has information that should influence whether two records represent the same entity, even as a weak signal do not use `DONT_USE`. Use `FUZZY`.

`DONT_USE` is for fields that carry zero matching information.

</details>

<details>

<summary><strong>When the field is a decent blocking key, even if it's a poor similarity signal</strong></summary>

`DONT_USE` removes a field from blocking as well as similarity scoring - there's no way to keep a field eligible for blocking while excluding it from the match score. If a field would narrow down candidate pairs well (a good blocking key) but you don't want its raw value driving the similarity score, `DONT_USE` is the wrong choice - it throws away the blocking value too. Give it a real match type instead, even a low-signal one, so it stays available to blocking.

For example, a `region_code` field cheaply splits records into non-overlapping candidate sets - a good blocking key - but two records sharing the same region says almost nothing about whether they're the same entity, so you don't want it driving the match score. Marking it `DONT_USE` strips it from blocking too, forcing Zingg to compare far more candidate pairs than necessary. Use `EXACT` on it instead - its similarity contribution will be weak, but blocking can still use it.

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
from zingg.client import *

rec_id = EFieldDefinition("rec_id", "string", MatchType.DONT_USE)
```
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block is identical for Community and Enterprise. Only the Python class differs between editions — `FieldDefinition` (Community) vs `EFieldDefinition` (Enterprise). 
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

{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
**Related types**:

* `NULL_OR_BLANK` - for fields that should participate in matching but have frequent nulls
* `EXACT` - for fields that should contribute an exact match signal

**Read more**: [Match types](./)
{% endhint %}
