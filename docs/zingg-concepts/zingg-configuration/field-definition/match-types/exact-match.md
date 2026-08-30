---
description: >-
  No tolerance for variation. Two values must match perfectly for EXACT to score
  them as a match. The right choice for trusted identifiers and categorical
  fields.
---

# EXACT Match

### What `EXACT` does

`EXACT` compares two field values and scores them as matching only when they are identical. There is no tolerance for typos, abbreviations, case differences, or format variation.

Use `EXACT` for any field where a difference in value means a definite difference in entity trusted identifiers like SSN, national ID, passport number, tax code and for categorical fields like country codes and boolean flags where variation is not expected.

### What `EXACT` matches and what it does not

| Value A | Value B | Match? |
|---|---|---|
| 123-45-6789 | 123-45-6789 | Yes - identical |
| 123-45-6789 | 123-45-6780 | No - single-digit difference |
| US | US | Yes - identical |
| US | us | Yes - both values are lowercased by a case-normalization preprocessing step before the comparison runs, so "US" and "us" both become "us" and match |
| true | true | Yes |
| true | 1 | No - EXACT does plain equality with no boolean coercion, so "true" and "1" do not match |
| 2024-01-15 | 2024-01-15 | Yes |
| 2024-01-15 | 01/15/2024 | No - no date parsing under EXACT; differing formats are compared as literal strings |
| [null] | 123-45-6789 | Yes - EXACT treats a null on either side as an automatic match; add `NULL_OR_BLANK` if you want nulls excluded |

### When to use `EXACT`

<details>

<summary><strong>National identifiers - SSN, passport, tax ID, national ID</strong></summary>

These fields are reliable unique identifiers. An exact match on SSN means the two records are definitively the same entity. Any tolerance on these fields creates false positives that are very difficult to correct after the fact.

Always use `EXACT` on trusted identifiers. In Zingg Enterprise, also add them as deterministic matching conditions, an exact match on a trusted identifier produces a guaranteed match with score 1 before the probabilistic model runs.

→ [Deterministic vs Probabilistic Matching](../../../zingg-entity-resolution-platform/deterministic-vs-probabilistic-matching.md)

</details>

<details>

<summary><strong>Date of birth, event dates</strong></summary>

Dates should not have fuzzy tolerance. "1985-06-15" and "1985-06-16" are different dates of birth - those are different people.

Use `EXACT` for all date fields. If your date formats vary across source systems, normalise them upstream before matching - do not use `FUZZY` to bridge\
format differences on dates.

</details>

<details>

<summary><strong>Country codes, boolean flags, categorical variables</strong></summary>

Short categorical codes where variation is not expected: "US", "GB", "IN", true/false,\
status codes belong with `EXACT`.

These fields should have no tolerance. If a record says "US" and another says "UK", they are different countries. `FUZZY` would score them as similar (two-character strings\
sharing one character). `EXACT` does not.

</details>

### When not to use `EXACT`

<details>

<summary><strong>Any field with known format variation</strong></summary>

Email addresses entered with and without dots, phone numbers with and without country codes, names that appear in short and long form - none of these are safe with `EXACT`.

`EXACT` on a phone number field will miss "+1-415-555-0123" and "4155550123" as a match even though they are the same number.

Use `FUZZY` or the field-specific match type (EMAIL for email, PINCODE for postal codes).

</details>

<details>

<summary><strong>Name fields</strong></summary>

Never use `EXACT` on name fields. Even internally consistent data has legitimate name variations - legal name vs common name, maiden name vs married name. Use `FUZZY` for names.

</details>

{% hint style="success" icon="right-long" %}
**Related types:**

* `FUZZY` - use when variation is expected
* `PINCODE` - EXACT-like for postal codes with format normalisation built in
* `DONT_USE` - exclude from matching entirely

**Read more**: [Match Types](./)
{% endhint %}
