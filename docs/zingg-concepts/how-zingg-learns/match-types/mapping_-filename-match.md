---
description: >-
  Resolve known variants of the same value using a user-supplied lookup file -
  nicknames, abbreviations, aliases, and company name variants.
tags:
  - ent
  - tag: enterprise-only
    primary: true
---

# MAPPING\_(FILENAME) Match

{% hint style="info" icon="right-long" %}
`MAPPING_(FILENAME)` is Enterprise only. The Community edition does not support mapping-based match types. Available in Enterprise Lite and above.
{% endhint %}

### What `MAPPING_(FileName)` does

`MAPPING` resolves variants of the same value using a lookup file you provide. Use it when records contain the same entity referred to by different names - nicknames like "Jon" and "Jonathan", company abbreviations like "IBM" and "International Business Machines", or country codes like "US" and "United States" - that you want Zingg to treat as equivalent.

The mapping file lists groups of equivalent values. Zingg uses it as a lookup during matching: when comparing two records, if both values for a `MAPPING` field appear in the same group, the field is treated as a match. The lookup is bidirectional and case-insensitive - "Jon" matches "Jonathan", "Jonathan" matches "Jon", "JOHN" matches "john".

`MAPPING` can be combined with another match type using a comma-separated combination in the `matchType` string. Combined with `FUZZY`, Zingg checks the mapping file first and falls back to fuzzy matching for values not in the mapping.

### How the algorithm works

_**COMMENT FOR TEAM — Algorithm detail for MAPPING\_(FileName) to be added here.**_

### What `MAPPING_(FileName)` matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Result with MAPPING only</th><th valign="top">Result with MAPPING + FUZZY</th></tr></thead><tbody><tr><td valign="top">Jon</td><td valign="top">Jonathan</td><td valign="top">Match - both in same mapping row</td><td valign="top">Match</td></tr><tr><td valign="top">Bob</td><td valign="top">Robert</td><td valign="top">Match - both in same mapping row</td><td valign="top">Match</td></tr><tr><td valign="top">John</td><td valign="top">Jonathan</td><td valign="top">Match - both in same mapping row</td><td valign="top">Match</td></tr><tr><td valign="top">Jon</td><td valign="top">Jhon</td><td valign="top">No match - "Jhon" not in mapping</td><td valign="top">Match - caught by FUZZY tolerance</td></tr><tr><td valign="top">Robert</td><td valign="top">William</td><td valign="top">No match - different mapping rows</td><td valign="top">No match - FUZZY also too dissimilar</td></tr><tr><td valign="top">Jon</td><td valign="top">[null]</td><td valign="top">No match - null not in mapping</td><td valign="top">No match - combine with <code>NULL_OR_BLANK</code></td></tr></tbody></table>

### When to use `MAPPING`

<details>

<summary><strong>Names with nicknames</strong></summary>

For datasets where the same person appears under different familiar names: Jonathan / Jon / Johnny / John; Robert / Bob / Rob / Bobby; William / Will / Bill. `FUZZY` alone catches small variations like "Jon" vs "John" but fails on completely different forms like "Robert" vs "Bob". Use `MAPPING` for the known nickname patterns and combine with `FUZZY` for everything else.

</details>

<details>

<summary><strong>Company names with abbreviations and legal suffixes</strong></summary>

"IBM" / "I.B.M." / "International Business Machines" will never be matched by `FUZZY` alone because the strings are too different. Build a mapping file of canonical company name variants and apply `MAPPING` to the company name field.

</details>

<details>

<summary><strong>Country codes vs full names</strong></summary>

"US" / "USA" / "U.S.A." / "United States" / "United States of America" all represent the same country. A mapping file lists each variant in one group.

</details>

<details>

<summary><strong>Product code variants across systems</strong></summary>

When merging records from systems that use different SKU formats - "SKU-1234" vs "1234" vs "PROD\_1234" - list the canonical variants in a mapping file.

</details>

### When not to use `MAPPING`

<details>

<summary><strong>Free-text fields with unbounded variation</strong></summary>

If your field has too many variants to list explicitly - full street addresses, free-text descriptions, product specifications - `MAPPING` is the wrong choice. You cannot enumerate the variants. Use `FUZZY` or `TEXT` instead.

</details>

<details>

<summary><strong>Trusted identifiers like SSN, passport, tax ID</strong></summary>

Trusted identifiers should always use `EXACT`, not `MAPPING`. There is no "variant" of an SSN - either the value matches or it does not. Adding mapping logic on a unique identifier introduces room for incorrect matches.

</details>

<details>

<summary><strong>Date and timestamp fields</strong></summary>

Dates should use `EXACT`. Different formats of the same date (`2024-01-15` vs `01/15/2024`) should be normalised upstream of Zingg, not handled through a mapping file.

</details>

### Configuring `MAPPING`

The mapping file is a JSON array where each element is an array of equivalent values.

{% tabs %}
{% tab title="Python" %}
```python
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
    fname = EFieldDefinition("fname", "string", "MAPPING_nicknames")
```
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block below uses Enterprise-only match type `mapping_<filename>`. Replace `<filename>` with the name of your mapping file without the `.json` extension. The CLI command is identical to other phases.
{% endhint %}

```json
{
  "fieldDefinition" : [ {
    "fieldName" : "fname",
    "matchType" : "mapping_nicknames",
    "fields" : "fname",
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
