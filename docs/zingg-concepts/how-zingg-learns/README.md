---
description: >-
  How Zingg builds ML models from your data and why it handles scale, variation,
  and uncertainty in ways that other approaches cannot.
---

# How Zingg Learns

{% hint style="success" icon="right-long" %}
**Read more**:

* For the step-by-step workflow you run to produce match results - [Step by step Guide](../../running-zingg/step-by-step-guide.md)
* For a deep dive on the two models Zingg builds internally - [Zingg Models](zingg-models/)
{% endhint %}

Zingg learns specific ML models from your data, calibrated to your specific field distributions, your specific variations, and your specific understanding of what a match means.

Zingg starts by scanning your dataset and selecting the most informative candidate pairs, edge cases, near misses, and ambiguous records where human judgment matters most. It does not ask you to label random samples. It is highly selective about which pairs it surfaces, choosing the ones that will teach it the most with the least labeling effort.

You label those pairs typically 30 to 50, as Match, No Match, or Uncertain. From those labels, Zingg builds two models: a blocking model that cuts down the comparison space to a tiny fraction of all possible pairs, and a similarity model that scores each remaining candidate pair with graded confidence.

Zingg models understand your data, and are not a generic algorithm applied to it.

### Quick reference on which match type for which field - TODO Move

When you define your field configuration, you choose a match type for each field. Here is a starting point for the most common field types.

<table><thead><tr><th width="265.6796875">Field type</th><th>Recommended match type + note</th></tr></thead><tbody><tr><td>First name, last name,<br>company name</td><td><code>FUZZY</code> - handles spelling variations<br>and abbreviations. Use <code>MAPPING</code> (Enterprise) for known<br>alias or nickname lists.</td></tr><tr><td>Email address</td><td><code>EMAIL</code> - matches the portion before the <code>@</code> only, avoiding mismatches from different domains for the same person.</td></tr><tr><td>Date of birth, registration date</td><td><code>EXACT</code> - dates should not have fuzzy tolerance.</td></tr><tr><td>Postal code, ZIP code</td><td><code>PINCODE</code> - handles common format variants.</td></tr><tr><td>Street address (full line)</td><td><code>FUZZY</code> - or <code>ONLY_ALPHABETS_FUZZY</code> combined with <code>NUMERIC</code> for the street number as a separate field.</td></tr><tr><td>Street number, apartment number</td><td><code>NUMERIC</code> - extracts and compares the number portion only.</td></tr><tr><td>National ID, SSN, tax ID</td><td><code>EXACT</code> - trusted identifiers should never have fuzzy tolerance. Also consider deterministic matching<br>(Enterprise) for these fields.</td></tr><tr><td>Internal record ID (not used for matching)</td><td><code>DONT_USE</code> - appears in output but excluded from comparison.</td></tr><tr><td>Fields frequently null across source systems</td><td>Add <code>NULL_OR_BLANK</code> alongside the main match type.</td></tr><tr><td>Product descriptions, free-text notes</td><td><code>TEXT</code> - word overlap comparison for longer free-text fields.</td></tr></tbody></table>

{% hint style="success" icon="right-long" %}
**Read more:**

* For full field definition setup including the `fields`, `dataType`, and `stopWords` attributes -\
  [Configure Zingg](../../running-zingg/configure-zingg.md)
* For the complete match types reference with all 12 types - [Match Types](../zingg-configuration/field-definition/match-types/)
{% endhint %}
