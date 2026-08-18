---
description: >-
  Same matching behavior as ONLY_ALPHABETS_FUZZY with optimised processing for
  production-scale address matching. Enterprise only.
tags:
  - ent
  - tag: enterprise-only
    primary: true
---

# ONLY\_ALPHABETS\_FUZZY\_OPTIMISED Match

{% hint style="info" icon="right-long" %}
`ONLY_ALPHABETS_FUZZY_OPTIMISED` is Enterprise only. The Community (open source) edition does not support this match type. Available in Enterprise Lite and above.
{% endhint %}

### What `ONLY_ALPHABETS_FUZZY_OPTIMISED` does

`ONLY_ALPHABETS_FUZZY_OPTIMISED` produces the same matching results as `ONLY_ALPHABETS_FUZZY` - strips numbers, then applies fuzzy matching to the alphabetic portion.

The scoring pattern is exactly the same as `ONLY_ALPHABETS_FUZZY` - only the computation time differs. `ONLY_ALPHABETS_FUZZY_OPTIMISED` runs faster on large datasets.

### What **`ONLY_ALPHABETS_FUZZY_OPTIMISED`** matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Match?</th></tr></thead><tbody><tr><td valign="top">42 Main Street</td><td valign="top">44 Main Street</td><td valign="top">Yes - same as <code>ONLY_ALPHABETS_FUZZY</code>, numbers stripped, "Main Street" is identical</td></tr><tr><td valign="top">42 Main St</td><td valign="top">44 Main Street</td><td valign="top">Yes - same as <code>ONLY_ALPHABETS_FUZZY</code>, "Main St" vs "Main Street" scores a high fuzzy similarity</td></tr><tr><td valign="top">42 Main St</td><td valign="top">42 Oak St</td><td valign="top">No - same as <code>ONLY_ALPHABETS_FUZZY</code>, "Main St" vs "Oak St" too different alphabetically</td></tr><tr><td valign="top">[null]</td><td valign="top">Main Street</td><td valign="top">Yes - same as <code>ONLY_ALPHABETS_FUZZY</code>, a null/blank value on either side is an automatic match; add <code>NULL_OR_BLANK</code> if you want nulls excluded</td></tr></tbody></table>

### When to use `ONLY_ALPHABETS_FUZZY_OPTIMISED`

<details>

<summary><strong>Address, catalog, or brand fields at production scale</strong></summary>

Any field where you would use `ONLY_ALPHABETS_FUZZY` - street addresses, product catalogs, store/branch names - once your dataset is large enough that performance matters.

</details>

### When not to use `ONLY_ALPHABETS_FUZZY_OPTIMISED`

<details>

<summary><strong>When the number is what makes the records different</strong></summary>

`ONLY_ALPHABETS_FUZZY_OPTIMISED` carries the same blind spot as `ONLY_ALPHABETS_FUZZY`: identifiers (patent, invoice, flight numbers) and safety- or spec-critical values (dosage, material grade, size) all lose the one value that distinguishes them once the numbers are stripped. Use `EXACT` or `ONLY_ALPHABETS_EXACT` instead.

</details>

{% hint style="info" icon="right-long" %}
`ONLY_ALPHABETS_FUZZY_OPTIMISED` is **Enterprise** only. Use `ONLY_ALPHABETS_FUZZY` in Community.
{% endhint %}

{% hint style="success" icon="right-long" %}
**Related types**:

* `ONLY_ALPHABETS_FUZZY` - same matching quality, but more time
* `FUZZY_OPTIMISED` - same optimisation pattern for name/text fields
* `NUMERIC` - for numeric fields

**Read more**: [Match Types](./)
{% endhint %}
