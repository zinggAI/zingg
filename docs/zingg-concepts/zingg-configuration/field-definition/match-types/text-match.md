---
description: >-
  Compares the overlap of words between two strings. Built for longer
  descriptive or free-text fields where word presence matters more than
  character-level similarity.
---

# TEXT Match

### What `TEXT` does

`TEXT` compares two string values by measuring the overlap of words between them, how many words appear in both. It is designed for longer descriptive fields like product descriptions, notes, professional bios, or service descriptions where the overall word content matters more than exact character matches. The score is a continuous similarity value in \[0, 1].

`TEXT` works best when your text fields do not have many typos - the comparison is at word level, not character level, tokens are lowercased but not stemmed, so it will not catch spelling errors or inflections ("model" vs "models") within words. For shorter name and address fields, use `FUZZY` instead.

A null or blank value on either side is an automatic match (1.0).

### What `TEXT` matches and what it does not

| Value A | Value B | Score | Notes |
|---|---|---|---|
| Enterprise data management platform | Data management platform for enterprise | 0.8 (Yes) | 4 of 5 distinct words shared - only "for" is unmatched |
| Enterprise software solutions | Consumer hardware products | 0.0 (No) | No shared words |
| Machine learning model training | Training machine learning model | 1.0 (Yes) | Same four words, different order - word overlap is order-independent |
| ML model | Machine learning model | 0.25 (Partial) | Only "model" is shared out of 4 distinct words across both - "ML" and "machine learning" don't overlap at the word level |
| [null] | Enterprise data platform | 1.0 (Yes) | Null/blank on either side auto-matches |
| [empty string] | Enterprise data platform | 1.0 (Yes) | Empty string is treated exactly like null |

### When to use `TEXT`

<details>

<summary><strong>Product descriptions and catalog fields</strong></summary>

E-commerce product titles and descriptions often contain the same information in different word orders or phrasing. "16GB RAM Laptop with SSD" and "Laptop SSD 16GB RAM" are the same product. `TEXT` captures word-level similarity across these variations.

</details>

<details>

<summary><strong>Professional bios, service descriptions, notes</strong></summary>

Long-form text fields where the same entity appears with different but semantically similar descriptions across systems. `TEXT` handles word-level overlap\
without requiring exact string similarity.

</details>

### When not to use `TEXT`

<details>

<summary><strong>Short fields - names, addresses, codes</strong></summary>

`TEXT` is designed for longer strings with multiple words. On a first name field with 1-2 words, `FUZZY` handles character-level variation far better than word overlap.

Use `FUZZY` for name and address fields. Use `TEXT` for description and notes fields.

</details>

<details>

<summary><strong>Fields with many typos</strong></summary>

`TEXT` compares at word level. "Enterprize" (misspelled) does not overlap with "Enterprise" using `TEXT`.

If your free-text fields have typos and spelling errors, `FUZZY` handles those better at the character level.

</details>

{% hint style="info" icon="right-long" %}
**Related types:**

* `FUZZY` - better for short fields and when typos are present
* `NUMERIC_WITH_UNITS` - Use for product description fields

**Read more**: [Match Types](./)
{% endhint %}
