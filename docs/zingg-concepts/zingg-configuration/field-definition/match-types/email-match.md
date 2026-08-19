---
description: >-
  Fuzzy-matches only the local part of an email address before the @ symbol and
  ignores the domain. Built for datasets where the same person appears with
  different email domains.
---

# EMAIL Match

### What EMAIL does

`EMAIL` splits an email address at the `@` symbol, discards the domain entirely, and scores the local part (the portion before `@`) using the similarity function. It is not an exact/binary comparison, it is `FUZZY`.

This handles the common scenario where the same person has a work email and a personal email, or where an organization's email domain changes over time.

### What `EMAIL` matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Match?</th></tr></thead><tbody><tr><td valign="top">j.smith@company.com</td><td valign="top">j.smith@other.com</td><td valign="top">Yes - identical local part, domain ignored</td></tr><tr><td valign="top">j.smith@company.com</td><td valign="top">john.smith@company.com</td><td valign="top">Partial - local parts differ but share "smith"; scores a graded similarity, not a hard match or non-match</td></tr><tr><td valign="top">support@ibm.com</td><td valign="top">support@microsoft.com</td><td valign="top">Yes - identical local part, even though the organisations are unrelated</td></tr><tr><td valign="top">[null]</td><td valign="top">j.smith@company.com</td><td valign="top">Yes - a null/blank value on either side is an automatic match; add <code>NULL_OR_BLANK</code> if you want nulls excluded</td></tr></tbody></table>

### When to use `EMAIL`

<details>

<summary><strong>Email address fields in multi-system datasets</strong></summary>

Any dataset where the same person or organisation may appear with different email domains across source systems. Work email in CRM, personal email in e-commerce,\
university email in a registration system - all with the same local part.

`EMAIL` ignores the domain and matches on the local part, which is the most stable identifier.

</details>

<details>

<summary><strong>When domain differences are expected and normal</strong></summary>

After an acquisition, employees may appear in one system with their old company domain and in another with their new one. `EMAIL` handles this automatically.

</details>

### When not to use `EMAIL`

<details>

<summary><strong>When the domain is part of the identity signal</strong></summary>

If you are matching organisations rather than individuals, the domain is often the most reliable identifier. "support@ibm.com" and "support@microsoft.com" have the\
same local part but are completely different organisations.

Use `EXACT` on the full email field when the domain matters.

</details>

<details>

<summary><strong>When you need domain to factor into the score too</strong></summary>

`EMAIL` ignores the domain unconditionally - "j.smith@company.com" and "john.smith@company.com" are scored purely on "j.smith" vs "john.smith", with no credit or penalty for the domain being the same. If the domain itself should count towards the score (not just be a tie-breaker), use `FUZZY` on the full email field instead. Note this compares the whole string including the domain, so it doesn't restrict fuzziness to the local part the way `EMAIL` does, and dissimilar domains with similar local parts can drag the score down.

</details>

{% hint style="success" icon="right-long" %}
**Related types**:

* `EMAIL_OPTIMISED` - same as EMAIL, faster at production scale (ENT)
* `EXACT` - use when the full email including domain must match exactly
* `FUZZY` - use when local parts also have variation

**Read more**: [Match types](./)
{% endhint %}
