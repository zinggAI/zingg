---
description: >-
  Same matching behavior as EMAIL with faster evaluation on large datasets. The
  production-scale choice for email address matching in Enterprise.
tags:
  - ent
  - tag: enterprise-only
    primary: true
---

# EMAIL\_OPTIMISED Match

{% hint style="info" icon="right-long" %}
`EMAIL_OPTIMISED` is Enterprise only. The Community (open source) edition does not support this match type. Available in Enterprise Lite and above.
{% endhint %}

### What `EMAIL_OPTIMISED` does

`EMAIL_OPTIMISED` produces the same matching results as `EMAIL` - it splits on `@`, discards the domain, and scores the local part using the similarity function. It is not an exact/binary comparison, it is `FUZZY`.

The scoring pattern is exactly the same as `EMAIL` - only the computation time differs. `EMAIL_OPTIMISED` is substantially faster when comparing large numbers of email addresses.

### What `EMAIL_OPTIMISED` matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Match?</th></tr></thead><tbody><tr><td valign="top">j.smith@company.com</td><td valign="top">j.smith@other.com</td><td valign="top">Yes - same as <code>EMAIL</code>, identical local part, domain ignored</td></tr><tr><td valign="top">j.smith@company.com</td><td valign="top">john.smith@company.com</td><td valign="top">Partial - same as <code>EMAIL</code>, local parts differ but share "smith"; scores a graded similarity, not a hard match or non-match</td></tr><tr><td valign="top">support@ibm.com</td><td valign="top">support@microsoft.com</td><td valign="top">Yes - identical local part, even though the organisations are unrelated</td></tr><tr><td valign="top">[null]</td><td valign="top">j.smith@company.com</td><td valign="top">Yes - same as <code>EMAIL</code>, a null/blank value on either side is an automatic match; add <code>NULL_OR_BLANK</code> if you want nulls excluded</td></tr></tbody></table>

### When to use `EMAIL_OPTIMISED`

<details>

<summary><strong>Email fields on large datasets</strong></summary>

Any use case where you would use `EMAIL` but your dataset is large enough that\
performance matters. Customer datasets, patient registries, voter files—any domain where millions of email addresses are being compared.

</details>

### When not to use `EMAIL_OPTIMISED`

<details>

<summary><strong>When the domain is part of the identity signal, or should factor into the score</strong></summary>

`EMAIL_OPTIMISED` carries the same domain-blind scoring as `EMAIL` - "support@ibm.com" and "support@microsoft.com" still score as an identical-local-part match even though the organisations are unrelated. If you are matching organisations rather than individuals, or the domain should count towards the score rather than be ignored, use `EXACT` (full email must match) or `FUZZY_OPTIMISED` (fuzzy-matches the whole string, domain included) instead.

</details>

{% hint style="success" icon="right-long" %}
**Related types**:

* `EMAIL` - use in Community or in development (all editions)
* `FUZZY_OPTIMISED` - same optimisation pattern for name/text fields

**Read more**: [Match types](./)
{% endhint %}
