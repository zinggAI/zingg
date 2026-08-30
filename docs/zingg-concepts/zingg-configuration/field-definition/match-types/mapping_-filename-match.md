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

### What `MAPPING_(FileName)` does

`MAPPING` resolves variants of the same value using a lookup file you provide. Use it when records contain the same entity referred to by different names - nicknames like "Jon" and "Jonathan", company abbreviations like "IBM" and "International Business Machines", or country codes like "US" and "United States" - that you want Zingg to treat as equivalent.

The mapping file lists groups of equivalent values. `MAPPING` is typically combined with another match type using a comma-separated combination in the `matchType` string, e.g. `MAPPING_nicknames,FUZZY`.

### How the algorithm works

`MAPPING_(FILENAME)` runs as a standardization step before Zingg scores similarity, but it isn't a similarity scorer itself.

1. **Dictionary loading** - Zingg reads the referenced lookup file, which groups equivalent values together (e.g., nicknames, abbreviations, codes). Each group represents synonyms for one real-world value.
2. **Representative value selection** - within each group, Zingg picks the longest value as the representative form. Every other member of the group is treated as an alias for it.
3. **Value substitution** - each field value is checked against the dictionary, case-insensitively. A match gets replaced with its representative form; no match means the original value passes through untouched.
4. **Similarity scoring** - the standardized values are then handed to whichever other match type is configured on the field - eg. `FUZZY`. That's where the real comparison happens.

{% hint style="danger" icon="right-long" %}
**Important:** `MAPPING_(FILENAME)` must be paired with another match type (eg. `FUZZY`, `EXACT` etc.). On its own, it only standardizes values - it doesn't score them.
{% endhint %}

### What `MAPPING_(FileName)` matches and what it does not

| Value A | Value B | Result with MAPPING + EXACT | Result with MAPPING + FUZZY |
|---|---|---|---|
| Jon | Jonathan | Match - both normalize to the same representative value | Match |
| Bob | Robert | Match - both normalize to the same representative value | Match |
| Jon | Jhon | No match - "Jhon" isn't in the mapping, so it's compared as-is against "Jonathan" | Match - caught by FUZZY's tolerance for the misspelling |
| Jon | John | No match - neither "John" nor the mapped "Jonathan" are equal strings | Match - FUZZY's similarity scoring treats them as close enough, even though they're not listed as synonyms |
| Robert | William | No match - normalize to different representative values | No match - FUZZY also finds them too dissimilar |
| Jon | [null] | No match - nothing to compare against | No match - combine with `NULL_OR_BLANK` to handle explicitly |

**Example mapping file**

```json
[
  ["Jonathan", "Jon"],
  ["Robert", "Rob", "Bob", "Bobby"],
  ["William", "Will", "Bill"]
]
```

The mapping file is a JSON array where each element is an array of equivalent values. Each inner array is one equivalence group. The longest value in a group becomes its representative form - so "Jon" normalizes to "Jonathan", and "Bob"/"Rob"/"Bobby" all normalize to "Robert". This is the exact dictionary used to produce the table above. "Jhon" and "John" aren't listed anywhere, so `MAPPING` leaves them untouched, and only `FUZZY` can catch them.

### When to use `MAPPING`

<details>

<summary><strong>Names with nicknames</strong></summary>

For datasets where the same person appears under different familiar names: Jonathan / Jon / Johnny / John; Robert / Bob / Rob / Bobby; William / Will / Bill. `FUZZY` alone catches small variations like "Jon" vs "John" but fails on completely different forms like "Robert" vs "Bob". Use `MAPPING` for the known nickname patterns and combine with `FUZZY` for everything else.

</details>

<details>

<summary><strong>Company names with abbreviations and legal suffixes</strong></summary>

"IBM" / "I.B.M." / "International Business Machines" will never be matched by `FUZZY` alone because the strings are too different. Build a mapping file of representative company name variants and apply `MAPPING` to the company name field.

</details>

<details>

<summary><strong>Country codes vs full names</strong></summary>

"US" / "USA" / "U.S.A." / "United States" / "United States of America" all represent the same country. A mapping file lists each variant in one group.

</details>

<details>

<summary><strong>Product code variants across systems</strong></summary>

When merging records from systems that use different SKU formats - "SKU-1234" vs "1234" vs "PROD\_1234" - list the representative variants in a mapping file.

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



{% hint style="info" icon="right-long" %}
`MAPPING_(FILENAME)` is Enterprise only. The Community edition does not support mapping-based match types. Available in Enterprise Lite and above.
{% endhint %}
