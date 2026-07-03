---
description: >-
  Normalize and standardize output fields after entity resolution to produce
  consistent golden records.
tags:
  - ent
---

# Standardize Fields and Results

{% hint style="info" icon="right-long" %}
Enterprise only. Field standardization is not available in Community.
{% endhint %}

Field standardization normalizes the values in your match output to a canonical form using a mapping file you provide. For example, 'CEO', 'Chief Executive Officer', and 'Executive Director' all become 'Chief Executive Officer' in the output. Standardization happens after matching and before writing the output. It does not change which records match, only how field values appear in the final result.

Use it when matched records contain the same entity but use different representations of the same value like job titles, gender codes, country names, and company name variants that need to be normalized before downstream use.

### **What it does and does not do**

<table><thead><tr><th width="248.80859375" valign="top">Behavior</th><th valign="top">Details</th></tr></thead><tbody><tr><td valign="top">When it runs</td><td valign="top">After matching, before writing the output.<br>It does not affect matching behaviour it transforms output values only</td></tr><tr><td valign="top">What it applies to</td><td valign="top">Any field you configure it on. One field can have one mapping file.</td></tr><tr><td valign="top">What it does not apply to</td><td valign="top">Primary key fields. These are never postprocessed.</td></tr><tr><td valign="top">Case sensitivity</td><td valign="top">Lookup is case-insensitive. CEO, ceo, and Ceo all match<br>the same mapping entry.</td></tr><tr><td valign="top">What it does not handle</td><td valign="top">Extra whitespace, punctuation differences, or partial matches. Those need to be cleaned upstream.</td></tr></tbody></table>

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
**Read more**:&#x20;

* To create a mapping file and configure the postprocessor on a field → [Configure Field Standardization](../../../tuning/configure-field-standardization.md)
* For the full field definition configuration including all `EFieldDefinition` methods - [Configure Zingg](../../../running-zingg/configure-zingg.md)
* For dictionary-based matching using the MAPPING match type (nicknames, aliases, company names) - [Match Types](../match-types/)
{% endhint %}
