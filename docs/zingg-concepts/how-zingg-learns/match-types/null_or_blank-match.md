---
description: >-
  Changes how Zingg handles null and blank values in a field. Always combined
  with another match type, never used alone.
---

# NULL\_OR\_BLANK Match

### What `NULL_OR_BLANK` does

By default, Zingg treats null values as matching anything. If a field is null in one record and has a value in another, the default behavior scores them as matching on that field.

`NULL_OR_BLANK` changes this. When added to a field alongside another match type, it tells Zingg to build an explicit feature for null and blank values so the ML model can learn from them rather than defaulting to a match. The model then decides whether nulls should contribute to a match based on your labeled training pairs.

Use `NULL_OR_BLANK` combined with another match type. It tells Zingg how to treat null and empty values for that field. Used in the `matchType` string with a comma-separated combination.

### How the algorithm works

_**COMMENT FOR TEAM -- Algorithm detail for NULL\_OR\_BLANK to be added here.**_

### What **`NULL_OR_BLANK`** matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Match?</th></tr></thead><tbody><tr><td valign="top">null</td><td valign="top">John Smith</td><td valign="top"><p>Without <code>NULL_OR_BLANK</code>: treated as matching (default).</p><p>With <code>NULL_OR_BLANK</code>: model learns from labeled examples - likely not a match</p></td></tr><tr><td valign="top">[empty string]</td><td valign="top">John Smith</td><td valign="top">Same as above</td></tr><tr><td valign="top">null</td><td valign="top">null</td><td valign="top">Confirm with team - both null behavior</td></tr><tr><td valign="top">John Smith</td><td valign="top">John Smith</td><td valign="top">No change - <code>NULL_OR_BLANK</code> only affects null/blank values</td></tr><tr><td valign="top">null</td><td valign="top">[empty string</td><td valign="top">Confirm with team - null vs empty string</td></tr></tbody></table>

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

`NULL_OR_BLANK` cannot be used alone. It only modifies the null behaviour of another match type on the same field.

Always combine: `FUZZY`, `NULL_OR_BLANK` or `EXACT`, `NULL_OR_BLANK` or `PINCODE`, `NULL_OR_BLANK`.

</details>

<details>

<summary><strong>On fields where null means the same thing across all records</strong></summary>

If a field is null in almost all records across all source systems, `NULL_OR_BLANK` adds little value - there are no non-null values for the model to contrast against. Consider `DONT_USE` instead.

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
    company = EFieldDefinition("company", "string", "FUZZY,NULL_OR_BLANK")
```
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block is identical for Community and Enterprise. Only the Python class differs between editions — `FieldDefinition` (Community) vs `EFieldDefinition` (Enterprise). The CLI command is identical for both editions.
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

### **CLI**

```bash
./scripts/zingg.sh --phase findTrainingData --conf config.json
```
{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
`NULL_OR_BLANK` always combines with another match type. Common combinations:

* `FUZZY`, `NULL_OR_BLANK` - name and address fields often null
* `EXACT`, `NULL_OR_BLANK` - identifier fields that may be absent
* `PINCODE`, `NULL_OR_BLANK` - postal codes missing in some systems
* `DONT_USE` - if the field should be excluded from matching entirely

**Read more**: [Match Types](./)
{% endhint %}
