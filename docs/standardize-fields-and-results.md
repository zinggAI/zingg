---
description: >-
  Normalize and standardize output fields after entity resolution to produce
  consistent golden records.
tags:
  - ent
  - tag: enterprise-only
    primary: true
---

# Standardize Fields and Results

{% hint style="info" icon="right-long" %}
Enterprise only. Field standardization is not available in Community.
{% endhint %}

Field standardization normalizes the values in your match output to a canonical form using a mapping file you provide. For example, 'CEO', 'Chief Executive Officer', and 'Executive Director' all become 'Chief Executive Officer' in the output. Standardization happens after matching and before writing the output. It does not change which records match, only how field values appear in the final result.

Use it when matched records contain the same entity but use different representations of the same value like job titles, gender codes, country names, and company name variants that need to be normalized before downstream use.

### **What it does and does not do**

| Behavior | Details |
|---|---|
| When it runs | After matching, before writing the output. It does not affect matching behaviour it transforms output values only |
| What it applies to | Any field you configure it on. One field can have one mapping file. |
| What it does not apply to | Primary key fields. These are never postprocessed. |
| Case sensitivity | Lookup is case-insensitive. CEO, ceo, and Ceo all match the same mapping entry. |
| What it does not handle | Extra whitespace, punctuation differences, or partial matches. Those need to be cleaned upstream. |

### Where this is useful

<details>

<summary><strong>Human resources and org data</strong></summary>

Standardise job titles and roles across HR, payroll, and directory systems to produce consistent headcount reporting and org charts. "VP Engineering", "Vice President of Engineering", and "VP Eng" all map to the same canonical title.

</details>

<details>

<summary><strong>Sales and CRM</strong></summary>

Normalise company names for consistent account consolidation and revenue analytics.\
"IBM", "I.B.M.", and "International Business Machines" resolve to a single canonical account name in your output.

</details>

<details>

<summary><strong>Product and catalog management</strong></summary>

Unify legacy product codes and new SKU formats across inventory, billing, and e-commerce systems for clean catalog reconciliation.

</details>

<details>

<summary><strong>Compliance and regulatory reporting</strong></summary>

Enforce canonical values on classification fields like industry codes, status labels, category names to simplify downstream regulatory reporting and reduce manual normalization before submissions.

</details>

{% hint style="success" icon="right-long" %}
**Read more**:

* To create a mapping file and configure the postprocessor on a field → [Configure Field Standardization](tuning/configure-field-standardization.md)
* For the full field definition configuration including all `EFieldDefinition` methods - [Configure Zingg](running-zingg/configure-zingg.md)
* For dictionary-based matching using the MAPPING match type (nicknames, aliases, company names) - [Match Types](zingg-concepts/zingg-configuration/field-definition/match-types/)
{% endhint %}
