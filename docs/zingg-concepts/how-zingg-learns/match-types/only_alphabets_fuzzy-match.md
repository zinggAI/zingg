---
description: >-
  Strips all numeric characters first, then applies fuzzy matching to the
  remaining alphabetic characters. Built for address fields where street names
  vary & numbers are handled separately.
---

# ONLY\_ALPHABETS\_FUZZY Match

### What ONLY\_ALPHABETS\_FUZZY does

`ONLY_ALPHABETS_FUZZY` removes all numeric characters from both field values and then applies fuzzy string similarity to the remaining alphabetic characters. Two values match based on how similar their alphabetic portions are with tolerance for typos, abbreviations, and spelling variants.

This is the standard choice for address street name fields where the street name needs fuzzy comparison and the street number needs to be handled separately. Combine `ONLY_ALPHABETS_FUZZY` with NUMERIC on the same field, or split the field before matching.

### How the algorithm works

_**COMMENT FOR TEAM — Algorithm detail for ONLY\_ALPHABETS\_FUZZY to be added here.**_

### What **`ONLY_ALPHABETS_FUZZY`** matches and what it does not

### When to use **`ONLY_ALPHABETS_FUZZY`**

<details>

<summary><strong>Street address lines where number and name are in the same field</strong></summary>

The most common use case: an address line like "42 Main Street" where you want to match "Main Street" fuzzily but not use the number 42 in that comparison.

Combine with `NUMERIC` on the same field so both components are compared:

"42 Main St" and "42 Main Street" - `NUMERIC` matches 42, `ONLY_ALPHABETS_FUZZY` matches "Main St" to "Main Street".

</details>

### When not to use **`ONLY_ALPHABETS_FUZZY`**

<details>

<summary><strong>When the alphabetic portions must match exactly</strong></summary>

Use `ONLY_ALPHABETS_EXACT` when you need the alphabetic component to be identical - for example, building name codes where any alphabetic variation means a different location.

</details>

<details>

<summary><strong>When you want a single match type covering the whole string</strong></summary>

If your address data is structured enough that `FUZZY` on the full string gives good results, use `FUZZY`. `ONLY_ALPHABETS_FUZZY` adds complexity and is most valuable when the number component is causing false positives in a `FUZZY` match.

</details>

{% tabs %}
{% tab title="Python" %}
### **Community**

```python
from zingg.client import *
    street = FieldDefinition("street", "string", MatchType.ONLY_ALPHABETS_FUZZY)
```

### **Enterprise**

```python
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
    street = EFieldDefinition("street", "string", MatchType.ONLY_ALPHABETS_FUZZY)
```
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block is identical for Community and Enterprise. Only the Python class differs between editions — `FieldDefinition` (Community) vs `EFieldDefinition` (Enterprise). The CLI command is identical for both editions.
{% endhint %}

```json
{
  "fieldDefinition" : [ {
    "fieldName" : "street",
    "matchType" : "only_alphabets_fuzzy",
    "fields" : "street",
    "dataType" : "string"
  } ]
}
```

### **CLI**

```bash
./scripts/zingg.sh --phase findTrainingData --conf config.json
```
{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
**Related types**:

* `NUMERIC` - always combine with this for full address fields
* `ONLY_ALPHABETS_EXACT` - use when the name must match exactly (no abbreviation tolerance)
* `ONLY_ALPHABETS_FUZZY_OPTIMISED` - same match type, faster at scale (ENT)
* `FUZZY` - simpler alternative for the full string

**Read more**: [Match Types](./)
{% endhint %}
