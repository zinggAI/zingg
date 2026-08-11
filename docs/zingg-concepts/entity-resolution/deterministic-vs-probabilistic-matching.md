---
description: >-
  How Zingg's two matching approaches work, when to use each, and how to
  configure deterministic rules.
tags:
  - ent
---

# Deterministic vs Probabilistic Matching

Zingg supports two matching approaches: probabilistic and deterministic. Both are explained on this page. Zingg Enterprise runs them together in a single flow. Probabilistic handles variations and uncertainty; deterministic handles trusted identifiers that should always produce an exact match.

### Probabilistic matching

Probabilistic matching is Zingg's default and is available in all editions. The model learns field-level similarity weights from your labeled training pairs and assigns a match score to every candidate pair. Pairs above the automatically optimized threshold are grouped into a cluster.

This approach handles the full range of real-world data quality problems: typos, abbreviations, missing fields, name variations, and format differences across systems."

_You do not write rules. You label examples. Zingg learns the rest._

### Deterministic matching

Deterministic matching lets you define hard rules for cases where certain field combinations should always produce a match, regardless of the probabilistic score. If two records share the same combination of identifiers you specify, Zingg treats that pair as a match with a score of 1, bypassing the ML model entirely for that pair.

Zingg Enterprise applies deterministic rules first. Pairs resolved deterministically are not re-evaluated probabilistically. Pairs that do not satisfy any deterministic condition fall through to the probabilistic model. Both paths produce output in the same format.

### Configuring deterministic matching

{% hint style="info" icon="right-long" %}
Deterministic matching is Enterprise only. Available in Enterprise Lite and above
{% endhint %}

To configure deterministic matching, define each rule as a `DeterministicMatching` condition in Python or as a `matchCondition` block in JSON, then pass all conditions to `setDeterministicMatchingCondition()`. Full Python and JSON examples → [Configure Zingg](../../running-zingg/configure-zingg.md)

### How the conditions work

Each `matchCondition` is evaluated independently. Any pair that satisfies at least one condition is resolved as a match with a score of 1. You can define as many conditions as your data requires.

Using the example above:

<table><thead><tr><th width="252.984375" valign="top">Condition</th><th valign="top">What it means</th></tr></thead><tbody><tr><td valign="top"><code>fname</code> + <code>stNo</code> + <code>add1</code> match <code>exactly</code></td><td valign="top">Same first name at the same street number and address line → treated as same entity, score 1.</td></tr><tr><td valign="top"><code>fname</code> + <code>dob</code> + <code>ssn</code> match <code>exactly</code></td><td valign="top">Same first name, date of birth, and SSN → treated as same entity, score 1.</td></tr><tr><td valign="top"><code>fname</code> + <code>email</code> match <code>exactly</code></td><td valign="top">Same first name and email address → treated as same entity, score 1.</td></tr></tbody></table>

<details>

<summary><strong>When should I use deterministic matching?</strong></summary>

Use deterministic matching when your data contains reliable unique identifier fields where an exact match between two records should always mean they are the same entity.

Good candidates for deterministic rules:

* First name + email address
* First name + date of birth + national ID
* First name + street number + address line 1

Do not use deterministic matching on fields that are frequently incomplete or inconsistent. Null values or formatting variations will cause valid matches to be missed. Apply deterministic rules only to fields you trust.

If you are unsure, start with probabilistic matching only. Add deterministic rules once you have reviewed match output and identified high-confidence identifier combinations in your data.

{% hint style="success" icon="right-long" %}
**Read more:** [Configure Zingg](../../running-zingg/configure-zingg.md) | [Concepts glossary](../concept-glossary.md)
{% endhint %}

</details>
