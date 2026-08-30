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

| Value A | Value B | Match? |
|---|---|---|
| Tower A, Flat 12 | Tower A, Flat 7 | Yes - both strip to "Tower A Flat", exact match |
| Tower A | Tower B | No - "Tower A" vs "Tower B" after stripping, different |
| Block 3, Wing East | Block 7, Wing East | Yes - both strip to "Block Wing East", exact match |
| Block 3, Wing East | Block 3, Wing West | No - "Wing East" vs "Wing West" |
| 42nd Floor, Suite A | 18th Floor, Suite A | No - strips to "nd Floor, Suite A" vs "th Floor, Suite A"; the ordinal suffix letters survive stripping and differ |
| [null] | Tower A, Flat 12 | Yes - a null/blank value on either side is an automatic match; add `NULL_OR_BLANK` if you want nulls excluded |

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

{% hint style="success" icon="book-open" %}
**Related types**:

* `ONLY_ALPHABETS_FUZZY` - use when alphabetic parts also have variation
* `NUMERIC` - use for numeric fields

**Read more**: [Match types](./)
{% endhint %}
