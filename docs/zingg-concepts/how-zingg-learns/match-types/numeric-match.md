---
description: >-
  Extracts numbers from strings and compares how many are shared across both
  values. Built for street numbers, apartment numbers, and building numbers
  embedded in address strings.
---

# NUMERIC Match

### What `NUMERIC` does

`NUMERIC` extracts all numbers from a string field value and scores the two values using Similarity functions - which gives a continuous score in \[0, 1].

Numbers are extracted into a set, so a number repeated within one value is only counted once - "10 Main St, Apt 10" contributes the number 10 a single time, not twice.

This makes it useful for address fields where a number is embedded in a string - "42 Main St" and "42 Main Street" share the number 42, which `NUMERIC` extracts and matches. The alphabetic parts are not compared by `NUMERIC` - use `ONLY_ALPHABETS_FUZZY` or `FUZZY` for those.

**Exception:** every other match type in this reference treats a null/blank value as an automatic match. `NUMERIC` does not - a null or blank value extracts no numbers, so it has nothing to share with the other side and scores a deterministic non-match (0.0).

### What `NUMERIC` matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Score</th><th valign="top">Notes</th></tr></thead><tbody><tr><td valign="top">42 Main St</td><td valign="top">42 Main Street</td><td valign="top">1.0 (Yes)</td><td valign="top">42 extracted from both</td></tr><tr><td valign="top">42B Main St</td><td valign="top">42 Main St</td><td valign="top">1.0 (Yes)</td><td valign="top">42 extracted from both - "B" isn't a digit, so it's dropped</td></tr><tr><td valign="top">42 Main St</td><td valign="top">43 Main St</td><td valign="top">0.0 (No)</td><td valign="top">{42} vs {43} share nothing</td></tr><tr><td valign="top">Suite 12, Floor 3</td><td valign="top">Suite 12, Floor 4</td><td valign="top">0.33</td><td valign="top">{12,3} vs {12,4} share one of three distinct numbers</td></tr><tr><td valign="top">10 Main St, Apt 10</td><td valign="top">10 Main St</td><td valign="top">1.0 (Yes)</td><td valign="top">"10" appears twice on the left but dedupes to the set {10}, same as the right side</td></tr><tr><td valign="top">[no numbers]</td><td valign="top">42 Main St</td><td valign="top">0.0 (No)</td><td valign="top">One side extracts no numbers, so there's nothing to intersect</td></tr><tr><td valign="top">[null]</td><td valign="top">42 Main St</td><td valign="top">0.0 (No)</td><td valign="top">The null-matches-anything default every other match type uses does not apply here</td></tr></tbody></table>

### When to use `NUMERIC`

<details>

<summary><strong>Street numbers and apartment numbers in address fields</strong></summary>

When your address data has the street number embedded in the same field as the street name - "42 Main Street"; use `NUMERIC` to match the number component.

</details>

<details>

<summary><strong>Building identifiers, floor numbers, unit numbers</strong></summary>

Any field where a number is the primary discriminating element and it is embedded in a string with non-numeric context.

</details>

### When not to use `NUMERIC`

<details>

<summary><strong>Fields where the full string matters, not just the numbers</strong></summary>

`NUMERIC` ignores alphabetic characters. "42 Main Street" and "42 Oak Avenue" both contain "42" - `NUMERIC` would score them as matching. The street name difference is invisible to NUMERIC.

Use `FUZZY` so both the number and the text are compared.

</details>

<details>

<summary><strong>Product codes with units</strong></summary>

For "16gb", "500ml", and similar specifications where the unit matters as much as the number, use `NUMERIC_WITH_UNITS` instead.

</details>

{% hint style="success" icon="right-long" %}
**Related matches:**

* `NUMERIC_WITH_UNITS` - for product codes that include units (16gb, 500ml)
* `FUZZY` - if you want a single match type covering the whole string

**Read more**: [Match Types](./)
{% endhint %}
