---
description: >-
  Strips all numeric characters first, then requires an exact match on the
  remaining alphabetic characters. Built for building & location names where
  unit numbers vary but the name should be identical.
---

# ONLY\_ALPHABETS\_EXACT Match

### What `ONLY_ALPHABETS_EXACT` does

`ONLY_ALPHABETS_EXACT` removes all numeric characters from both field values and then compares the remaining alphabetic characters exactly. Two values match if their alphabetic portions are identical after numbers are stripped.

This is designed for situations where the numeric component of a value varies across records but the alphabetic component should be consistent.

### What **`ONLY_ALPHABETS_EXACT`** matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Match?</th></tr></thead><tbody><tr><td valign="top">Tower A, Flat 12</td><td valign="top">Tower A, Flat 7</td><td valign="top">Yes - both strip to "Tower A Flat", exact match</td></tr><tr><td valign="top">Tower A</td><td valign="top">Tower B</td><td valign="top">No - "Tower A" vs "Tower B" after stripping, different</td></tr><tr><td valign="top">Block 3, Wing East</td><td valign="top">Block 7, Wing East</td><td valign="top">Yes - both strip to "Block Wing East",<br>exact match</td></tr><tr><td valign="top">Block 3, Wing East</td><td valign="top">Block 3, Wing West</td><td valign="top">No - "Wing East" vs "Wing West"</td></tr><tr><td valign="top">42nd Floor, Suite A</td><td valign="top">18th Floor, Suite A</td><td valign="top">No - strips to "nd Floor, Suite A" vs "th Floor, Suite A"; the ordinal suffix letters survive stripping and differ</td></tr><tr><td valign="top">[null]</td><td valign="top">Tower A, Flat 12</td><td valign="top">Yes - a null/blank value on either side is an automatic match; add <code>NULL_OR_BLANK</code> if you want nulls excluded</td></tr></tbody></table>

### When to use **`ONLY_ALPHABETS_EXACT`**

<details>

<summary><strong>Building names and location identifiers</strong></summary>

"Tower A, Flat 12" vs "Tower A, Flat 7" - the building/block name is consistent but unit, flat, or floor numbers vary. The name is the matching signal; the number is noise.

</details>

<details>

<summary><strong>Fixed-route public transit</strong></summary>

"Route #4B - Downtown Express" vs "Route #12B - Downtown Express" - strips to "Route #B - Downtown Express" for both. The route variant letter must match exactly since a typo here means a different transit line, and stripping only digits keeps the punctuation intact so the label stays readable.

</details>

<details>

<summary><strong>Legal contract clauses and policy sections</strong></summary>

"Section 4.1(a): Termination for Convenience" vs "Section 12.3(a): Termination for Convenience" - strips to "Section .(a): Termination for Convenience" for both, grouping the same clause across contract templates or versions with no fuzzy tolerance on legal wording.

</details>

<details>

<summary><strong>Medical diagnostic panels and lab test sub-types</strong></summary>

"Hepatitis B Panel - Panel 1" vs "Hepatitis B Panel - Panel 4" - strips to "Hepatitis B Panel - Panel " for both, grouping the same test across internal lab tracking numbers. Fuzzy matching is too risky here - a typo in a diagnostic test name is a patient-safety issue.

</details>

<details>

<summary><strong>Warehouse aisle and rack tracking</strong></summary>

"Aisle-A / Tier-3" vs "Aisle-A / Tier-12" - strips to "Aisle-A / Tier-" for both, grouping records by physical corridor rather than shelf level. The aisle label must match exactly, and stripping only digits keeps the slashes and dashes intact for downstream layout scripts.

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
from zinggEC.enterprise.common.EMatchTypes import *

street = EFieldDefinition("street", "string", MatchType.ONLY_ALPHABETS_EXACT)
```
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block is identical for Community and Enterprise. Only the Python class differs between editions — `FieldDefinition` (Community) vs `EFieldDefinition` (Enterprise). 
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

{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
**Related types**:

* `ONLY_ALPHABETS_FUZZY` - use when alphabetic parts also have variation
* `NUMERIC` - use for numeric fields

**Read more**: [Match types](./)
{% endhint %}
