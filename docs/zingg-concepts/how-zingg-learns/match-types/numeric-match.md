---
description: >-
  Extracts numbers from strings and compares how many are shared across both
  values. Built for street numbers, apartment numbers, and building numbers
  embedded in address strings.
---

# NUMERIC Match

### What `NUMERIC` does

`NUMERIC` extracts all numeric characters from a string field value and compares the numbers found in both records. It scores based on how many extracted numbers are the same across the two values.

This makes it useful for address fields where a number is embedded in a string - "42 Main St" and "42 Main Street" share the number 42, which `NUMERIC` extracts and matches. The alphabetic parts are not compared by `NUMERIC` - use `ONLY_ALPHABETS_FUZZY` or `FUZZY` for those.

### How the algorithm works

_**COMMENT FOR TEAM — Algorithm detail for NUMERIC to be added here.**_

### What `NUMERIC` matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Match?</th></tr></thead><tbody><tr><td valign="top">42 Main St</td><td valign="top">42 Main Street</td><td valign="top">Yes - 42 extracted from both</td></tr><tr><td valign="top">42B Main St</td><td valign="top">42 Main St</td><td valign="top">Yes - 42 extracted from both (B ignored)</td></tr><tr><td valign="top">42 Main St</td><td valign="top">43 Main St</td><td valign="top">No - different numbers (42 vs 43)</td></tr><tr><td valign="top">Suite 12, Floor 3</td><td valign="top">Suite 12, Floor 4</td><td valign="top">Partial - 12 matches, 3 vs 4 differs. Score reflects partial overlap.</td></tr><tr><td valign="top">Flat 4, Tower A</td><td valign="top">Flat 4, Tower A</td><td valign="top">Yes - 4 matches</td></tr><tr><td valign="top">[no numbers]</td><td valign="top">42 Main St</td><td valign="top"><em><strong>Confirm with team —</strong></em><br><em><strong>what score when one side has no numbers?</strong></em></td></tr></tbody></table>

### When to use `NUMERIC`

<details>

<summary><strong>Street numbers and apartment numbers in address fields</strong></summary>

When your address data has the street number embedded in the same field as the street name - "42 Main Street"; use `NUMERIC` to match the number component.

For best results on address fields, combine `NUMERIC` with `ONLY_ALPHABETS_FUZZY` on the same field (or split the field into number and name before matching)

</details>

<details>

<summary><strong>Building identifiers, floor numbers, unit numbers</strong></summary>

Any field where a number is the primary discriminating element and it is embedded in a string with non-numeric context.

</details>

### When not to use `NUMERIC`

<details>

<summary><strong>Fields where the full string matters, not just the numbers</strong></summary>

`NUMERIC` ignores alphabetic characters. "42 Main Street" and "42 Oak Avenue" both contain "42" - `NUMERIC` would score them as matching. The street name difference is invisible to NUMERIC.

Combine with `ONLY_ALPHABETS_FUZZY` or `FUZZY` so both the number and the text are compared.

</details>

<details>

<summary><strong>Product codes with units</strong></summary>

For "16gb", "500ml", and similar specifications where the unit matters as much as the number, use `NUMERIC_WITH_UNITS` instead.

</details>

{% tabs %}
{% tab title="Python" %}
### **Community**

```python
from zingg.client import *

    amount = FieldDefinition("amount", "double", MatchType.NUMERIC)
```

### **Enterprise**

```python
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition

    amount = EFieldDefinition("amount", "double", MatchType.NUMERIC)
```
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block is identical for Community and Enterprise. Only the Python class differs between editions — `FieldDefinition` (Community) vs `EFieldDefinition` (Enterprise). The CLI command is identical for both editions.
{% endhint %}

```json
{
  "fieldDefinition" : [ {
    "fieldName" : "amount",
    "matchType" : "numeric",
    "fields" : "amount",
    "dataType" : "double"
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
**Related matches:**

* `ONLY_ALPHABETS_FUZZY` - combine with NUMERIC for full address fields
* `NUMERIC_WITH_UNITS` - for product codes that include units (16gb, 500ml)
* `FUZZY` - if you want a single match type covering the whole string

**Read more**: [Match Types](./)
{% endhint %}
