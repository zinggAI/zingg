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

### Quick reference on which match type for which field

When you define your field configuration, you choose a match type for each field. Here is a starting point for the most common field types.

| Field type | Recommended match type + note |
|---|---|
| First name, last name, company name | `FUZZY` - handles spelling variations and abbreviations. Use `MAPPING` (Enterprise) for known alias or nickname lists. |
| Email address | `EMAIL` - matches the portion before the `@` only, avoiding mismatches from different domains for the same person. |
| Date of birth, registration date | `EXACT` - dates should not have fuzzy tolerance. |
| Postal code, ZIP code | `PINCODE` - handles common format variants. |
| Street address (full line) | `FUZZY` - or `ONLY_ALPHABETS_FUZZY` combined with `NUMERIC` for the street number as a separate field. |
| Street number, apartment number | `NUMERIC` - extracts and compares the number portion only. |
| National ID, SSN, tax ID | `EXACT` - trusted identifiers should never have fuzzy tolerance. Also consider deterministic matching (Enterprise) for these fields. |
| Internal record ID (not used for matching) | `DONT_USE` - appears in output but excluded from comparison. |
| Fields frequently null across source systems | Add `NULL_OR_BLANK` alongside the main match type. |
| Product descriptions, free-text notes | `TEXT` - word overlap comparison for longer free-text fields. |

{% hint style="success" icon="right-long" %}
**Read more:**

* For full field definition setup including the `fields`, `dataType`, and `stopWords` attributes -\
  [Configure Zingg](../../running-zingg/configure-zingg.md)
* For the complete match types reference with all 12 types - [Match Types](../zingg-configuration/field-definition/match-types/)
{% endhint %}
