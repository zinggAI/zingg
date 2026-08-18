---
description: >-
  Strips all numeric characters first, then applies fuzzy matching to the
  remaining alphabetic characters. Built for address fields where street names
  vary & numbers are handled separately.
---

# ONLY\_ALPHABETS\_FUZZY Match

### What ONLY\_ALPHABETS\_FUZZY does

`ONLY_ALPHABETS_FUZZY` removes all numeric characters from both field values and then applies fuzzy string similarity to the remaining alphabetic characters. Two values match based on how similar their alphabetic portions are with tolerance for typos, abbreviations, and spelling variants.

### What **`ONLY_ALPHABETS_FUZZY`** matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Match?</th></tr></thead><tbody><tr><td valign="top">42 Main Street</td><td valign="top">44 Main Street</td><td valign="top">Yes - numbers stripped, "Main Street" is identical</td></tr><tr><td valign="top">42 Main St</td><td valign="top">44 Main Street</td><td valign="top">Yes - "Main St" vs "Main Street" scores a high fuzzy similarity</td></tr><tr><td valign="top">42 Main St</td><td valign="top">42 Oak St</td><td valign="top">No - "Main St" vs "Oak St" too different alphabetically</td></tr><tr><td valign="top">[null]</td><td valign="top">Main Street</td><td valign="top">Yes - a null/blank value on either side is an automatic match; add <code>NULL_OR_BLANK</code> if you want nulls excluded</td></tr></tbody></table>

### When to use **`ONLY_ALPHABETS_FUZZY`**

<details>

<summary><strong>Street address lines where number and name are in the same field</strong></summary>

The most common use case: an address line like "42 Main Street" where you want to match "Main Street" fuzzily but not use the number 42 in that comparison.

"42 Main St" and "42 Main Street" - `ONLY_ALPHABETS_FUZZY` matches "Main St" to "Main Street".

</details>

<details>

<summary><strong>Product model/catalog fields with variant numbers</strong></summary>

"iPhone 15 Pro Max 256GB" vs "iPhone 15 Pro Max 512GB" - stripping the numbers isolates "iPhone Pro Max GB", grouping storage/color variants under the same parent product.

</details>

<details>

<summary><strong>Corporate, brand, and store-location fields</strong></summary>

"Walmart Store #1024" vs "Walmart Store #4412" - stripping the store numbers isolates "Walmart Store", letting branch-level records roll up to the parent brand.

</details>

<details>

<summary><strong>Course or curriculum codes</strong></summary>

"CS 101: Intro to Python" vs "CS 102: Intro to Python" - stripping the course numbers isolates "CS: Intro to Python", letting transcripts map across curriculum revisions.

</details>

<details>

<summary><strong>Drug/chemical name fields, when dosage doesn't matter for the match</strong></summary>

"Amoxicillin 250mg" vs "Amoxicillin 500mg" - stripping the dosage isolates "Amoxicillin mg", useful for flagging an allergy to the drug itself regardless of strength. Only applies when the field represents the drug name, not the prescribed dose - see below for when the number itself is what matters.

</details>

### When not to use **`ONLY_ALPHABETS_FUZZY`**

<details>

<summary><strong>When the alphabetic portions must match exactly</strong></summary>

Use `ONLY_ALPHABETS_EXACT` when you need the alphabetic component to be identical - for example, building name codes where any alphabetic variation means a different location.

</details>

<details>

<summary><strong>When the number is a unique identifier, not a variant</strong></summary>

Patent numbers ("US9123456B2" vs "US9123457B2"), invoice numbers ("Invoice #5001" vs "#5002"), and flight numbers ("Flight AA 402" vs "AA 403") all collapse to the same alphabetic string once stripped ("Patent USB", "Invoice #", "Flight AA"), merging records that must stay distinct.

</details>

<details>

<summary><strong>When the number is a safety- or spec-critical value, not noise</strong></summary>

Drug dosage ("Insulin 10 Units" vs "100 Units"), material grade ("Grade 40" vs "Grade 60" steel rebar), and size ("Nike Air Max Size 6" vs "Size 11") all lose the one value that makes the records different. Stripping the number here risks a dosing error, a structural failure, or shipping the wrong size - not a false match.

</details>

<details>

<summary><strong>When you want a single match type covering the whole string</strong></summary>

If your address data is structured enough that `FUZZY` on the full string gives good results, use `FUZZY`. `ONLY_ALPHABETS_FUZZY` adds complexity and is most valuable when the number component is causing false positives in a `FUZZY` match.

</details>

{% hint style="success" icon="right-long" %}
**Related types**:

* `NUMERIC` - use this for numeric fields
* `ONLY_ALPHABETS_EXACT` - use when the name must match exactly (no abbreviation tolerance)
* `ONLY_ALPHABETS_FUZZY_OPTIMISED` - same match type, faster at scale
* `FUZZY` - simpler alternative for the full string

**Read more**: [Match Types](./)
{% endhint %}
