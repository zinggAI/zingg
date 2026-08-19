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

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Score</th><th valign="top">Notes</th></tr></thead><tbody><tr><td valign="top">Enterprise data management platform</td><td valign="top">Data management platform for enterprise</td><td valign="top">0.8 (Yes)</td><td valign="top">4 of 5 distinct words shared - only "for" is unmatched</td></tr><tr><td valign="top">Enterprise software solutions</td><td valign="top">Consumer hardware products</td><td valign="top">0.0 (No)</td><td valign="top">No shared words</td></tr><tr><td valign="top">Machine learning model training</td><td valign="top">Training machine learning model</td><td valign="top">1.0 (Yes)</td><td valign="top">Same four words, different order - word overlap is order-independent</td></tr><tr><td valign="top">ML model</td><td valign="top">Machine learning model</td><td valign="top">0.25 (Partial)</td><td valign="top">Only "model" is shared out of 4 distinct words across both - "ML" and "machine learning" don't overlap at the word level</td></tr><tr><td valign="top">[null]</td><td valign="top">Enterprise data platform</td><td valign="top">1.0 (Yes)</td><td valign="top">Null/blank on either side auto-matches</td></tr><tr><td valign="top">[empty string]</td><td valign="top">Enterprise data platform</td><td valign="top">1.0 (Yes)</td><td valign="top">Empty string is treated exactly like null</td></tr></tbody></table>

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
