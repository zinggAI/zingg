---
description: >-
  Strips all numeric characters first, then requires an exact match on the
  remaining alphabetic characters. Built for building & location names where
  unit numbers vary but  name should be identicals.
---

# ONLY\_ALPHABETS\_EXACT Match

### What `ONLY_ALPHABETS_EXACT` does

`ONLY_ALPHABETS_EXACT` removes all numeric characters from both field values and then compares the remaining alphabetic characters exactly. Two values match if their alphabetic portions are identical after numbers are stripped.

This is designed for situations where the numeric component of a value varies across records but the alphabetic component should be consistent - building names where flat or unit numbers differ, or wing identifiers where floor numbers change.

### How the algorithm works

_**COMMENT FOR TEAM — Algorithm detail for ONLY\_ALPHABETS\_EXACT to be added here.**_

### What **`ONLY_ALPHABETS_EXACT`** matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Match?</th></tr></thead><tbody><tr><td valign="top">Tower A, Flat 12</td><td valign="top">Tower A, Flat 7</td><td valign="top">Yes - both strip to "Tower A Flat", exact match</td></tr><tr><td valign="top">Tower A</td><td valign="top">Tower B</td><td valign="top">"No - "Tower A" vs "Tower B" after stripping, different</td></tr><tr><td valign="top">Block 3, Wing East</td><td valign="top">Block 7, Wing East</td><td valign="top">Yes - both strip to "Block Wing East",<br>exact match</td></tr><tr><td valign="top">Block 3, Wing East</td><td valign="top">Block 3, Wing West</td><td valign="top">No - "Wing East" vs "Wing West"</td></tr><tr><td valign="top">42nd Floor, Suite A</td><td valign="top">18th Floor, Suite A</td><td valign="top">Yes - both strip to "nd Floor Suite A" <em><strong>(confirm with team - ordinal suffixes)</strong></em></td></tr></tbody></table>

### When to use **`ONLY_ALPHABETS_EXACT`**

<details>

<summary><strong>Building names and location identifiers</strong></summary>

Any address field where the building or block name is consistent but unit, flat, or floor numbers vary. The name is the matching signal; the number is noise.

Combine `ONLY_ALPHABETS_EXACT` with `NUMERIC` on the same field to match both the name (alphabetically) and the number (numerically) independently.

</details>

### When not to use **`ONLY_ALPHABETS_EXACT`**

<details>

<summary><strong>When the alphabetic portion also has variation</strong></summary>

`ONLY_ALPHABETS_EXACT` requires a perfect alphabetic match after stripping. "Saint James House" and "St James House" will not match - the alphabetic portions are different.

Use `ONLY_ALPHABETS_FUZZY` instead when the alphabetic component also has spelling or abbreviation variation.

</details>

{% tabs %}
{% tab title="Python" %}
### **Community**

```python
from zingg.client import *
    street = FieldDefinition("street", "string", MatchType.ONLY_ALPHABETS_EXACT)
```

### **Enterprise**

```python
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
    street = EFieldDefinition("street", "string", MatchType.ONLY_ALPHABETS_EXACT)
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
    "matchType" : "only_alphabets_exact",
    "fields" : "street",
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

* `ONLY_ALPHABETS_FUZZY` - use when alphabetic parts also have variation
* `NUMERIC` - combine to also match the number component

**Read more**: [Match types](./)
{% endhint %}
