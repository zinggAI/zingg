---
description: >-
  Extracts product codes or numbers with units and compares how many are the
  same across both values. Built for product specification fields.
---

# NUMERIC\_WITH\_UNITS Match

### What `NUMERIC_WITH_UNITS` does

`NUMERIC_WITH_UNITS` extracts contiguous alphanumeric tokens that contain a digit - the number and any unit letters glued directly to it, like "16gb" or "2.4GHz" - and compares how many of those tokens are shared across two records.

**The unit only glues to the number when there's no space between them.** "16gb" extracts as the single token `16gb`, but "16 GB" (with a space) extracts as just `16` - the unit is dropped, not glued. So a number and its unit written with a space in one record and without a space in the other extract to _different_ tokens and won't match: `{16gb}` vs `{16}` share nothing. There is no unit-conversion logic either - "500ml" and "0.5L" extract to `{500ml}` and `{0.5L}`, which share nothing, regardless of them being the same physical quantity.

**Null handling is a third, distinct behavior**, different from both `NUMERIC` and every other match type: if _both_ sides extract zero tokens (both null/blank, or both have no digit anywhere), it scores a match (1.0). But if only _one_ side extracts zero tokens - including one side being null while the other has real tokens - it scores a non-match (0.0), the same as `NUMERIC`.

### What `NUMERIC_WITH_UNITS` matches and what it does not

| Value A | Value B | Score | Notes |
|---|---|---|---|
| 16gb RAM | 16 GB Memory | 0.0 (No) | `{16gb}` vs `{16}` - the space in "16 GB" stops the unit from gluing to the number, so they extract as different tokens |
| 16gb RAM | 32gb RAM | 0.0 (No) | `{16gb}` vs `{32gb}` share nothing |
| 500ml | 0.5L | 0.0 (No) | `{500ml}` vs `{0.5L}` share nothing; there is no unit conversion |
| 2.4GHz Dual Core | 2.4GHz Processor | 1.0 (Yes) | `2.4GHz` extracted from both - same glued format on both sides |
| 16gb | 16 | 0.0 (No) | `{16gb}` vs `{16}` share nothing |
| [null] | [null] | 1.0 (Yes) | Both sides extract zero tokens - the auto-match case unique to this match type |
| [null] | 16gb RAM | 0.0 (No) | Only one side extracts zero tokens - falls back to a non-match, same as `NUMERIC` |

### When to use `NUMERIC_WITH_UNITS`

<details>

<summary><strong>Product specification fields</strong></summary>

Any field containing product specifications with units - storage capacity, screen size, processor speed, volume, weight. E-commerce product catalogs, medical device specifications - as long as the number and unit are written glued together (no space) consistently in your data.

"16GB RAM, 512GB SSD" and "8GB RAM, 512GB SSD" share the token "512GB" and score a partial match on that overlap. `NUMERIC_WITH_UNITS` works here because the unit is glued to the number the same way in both records.

</details>

### When not to use `NUMERIC_WITH_UNITS`

<details>

<summary><strong>Address fields with numbers</strong></summary>

Use `NUMERIC` for address fields. "42 Main Street" does not have units. `NUMERIC_WITH_UNITS` is built for product specification patterns.

</details>

<details>

<summary><strong>When your data mixes glued and spaced number-unit formatting</strong></summary>

"16GB RAM" and "16 GB RAM" do not match - the space in "16 GB" prevents the unit from gluing to the number, so they extract to different tokens (`16GB` vs `16`) with nothing in common. `NUMERIC_WITH_UNITS` does not normalise this, and there is no unit-conversion logic ("500ml" vs "0.5L" also won't match). If your source systems format this inconsistently, normalise the spacing (and units, if needed) upstream before matching.

</details>

{% hint style="success" icon="book-open" %}
**Related types**:

* `NUMERIC` - for numbers without unit
* `TEXT` - for the descriptive parts of product fields

**Read more**: [Match types](./)
{% endhint %}
