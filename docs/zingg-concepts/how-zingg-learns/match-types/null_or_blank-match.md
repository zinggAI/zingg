---
description: >-
  Changes how Zingg handles null and blank values in a field. Always combined
  with another match type, never used alone.
---

# NULL\_OR\_BLANK Match

### What `NULL_OR_BLANK` does

By default, most Zingg match types treat a null or blank value as automatically matching anything.

`NULL_OR_BLANK` adds a separate feature flagging whether both values were actually present, so the model can learn to discount matches that were really just two missing values.

**Exception:** `NUMERIC` does not follow the "null matches anything" default - a null paired with any value scores as a non-match under `NUMERIC`, the opposite of every other match type covered here.

Use `NULL_OR_BLANK` combined with another match type, in the `matchType` string with a comma-separated combination.

### What **`NULL_OR_BLANK`** matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top"><code>NULL_OR_BLANK</code> feature</th><th valign="top">Notes</th></tr></thead><tbody><tr><td valign="top">null</td><td valign="top">John Smith</td><td valign="top">0.0</td><td valign="top">One side null</td></tr><tr><td valign="top">[empty string]</td><td valign="top">John Smith</td><td valign="top">0.0</td><td valign="top">Empty string treated exactly like null</td></tr><tr><td valign="top">null</td><td valign="top">null</td><td valign="top">0.0</td><td valign="top">No distinction between one-sided and both-sided nulls</td></tr><tr><td valign="top">null</td><td valign="top">[empty string]</td><td valign="top">0.0</td><td valign="top">Null and blank are interchangeable</td></tr><tr><td valign="top">John Smith</td><td valign="top">John Smith</td><td valign="top">1.0</td><td valign="top">Both values present</td></tr></tbody></table>

There is no distinction between "both null" and "one null, one populated" - all four null/blank rows above score identically.

### When to use **`NULL_OR_BLANK`**

<details>

<summary><strong>Fields that are frequently null across source systems</strong></summary>

Middle name, maiden name, suffix, secondary address line, company name on a personal record, any field that is commonly absent in some source systems but present in others.

Without `NULL_OR_BLANK`, a null middle name in one record, any middle name in another record will match. This is often not what you want - a null middle name should not help two records match.

Add `NULL_OR_BLANK` alongside `FUZZY` on these fields so the model can learn to treat nulls appropriately from your labeled examples.

</details>

<details>

<summary><strong>When data quality varies significantly across source systems</strong></summary>

If one source system reliably populates a field and another consistently leaves it blank, `NULL_OR_BLANK` prevents the blank-vs-populated pattern from inflating match scores across source system pairs.

</details>

### When not to use **`NULL_OR_BLANK`**

<details>

<summary><strong>As a standalone match type</strong></summary>

On its own, `NULL_OR_BLANK` only contributes a "both present?" signal, with no similarity information - which isn't useful for distinguishing matches. Nothing currently validates or blocks this configuration, but it won't do anything meaningful by itself.

Always pair it with another match type: `FUZZY`, `NULL_OR_BLANK` or `EXACT`, `NULL_OR_BLANK` or `PINCODE`, `NULL_OR_BLANK`.

</details>

<details>

<summary><strong>On fields where null means the same thing across all records</strong></summary>

If a field is universally null across all records with no non-null values to contrast against, `NULL_OR_BLANK` adds nothing. Use `DONT_USE` for those fields instead.

</details>

{% tabs %}
{% tab title="Python" %}
### **Community**

```python
from zingg.client import *

company = FieldDefinition("company", "string", "FUZZY,NULL_OR_BLANK")
```

### **Enterprise**

```python
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zingg.client import *

company = EFieldDefinition("company", "string", "FUZZY,NULL_OR_BLANK")
```
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block is identical for Community and Enterprise. Only the Python class differs between editions — `FieldDefinition` (Community) vs `EFieldDefinition` (Enterprise). 
{% endhint %}

```json
{
  "fieldDefinition" : [ {
    "fieldName" : "company",
    "matchType" : "fuzzy,null_or_blank",
    "fields" : "company",
    "dataType" : "string"
  } ]
}
```

{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
`NULL_OR_BLANK` common combinations:

* `FUZZY`, `NULL_OR_BLANK` - name and address fields often null
* `EXACT`, `NULL_OR_BLANK` - identifier fields that may be absent
* `PINCODE`, `NULL_OR_BLANK` - postal codes missing in some systems
* `DONT_USE` - if the field should be excluded from matching entirely

**Read more**: [Match Types](README.md)
{% endhint %}
