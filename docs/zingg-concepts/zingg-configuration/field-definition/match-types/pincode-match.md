---
description: >-
  Matches postal and ZIP codes across the common format variants they appear
  in—5-digit, 9-digit with hyphen, and others. The right choice for postal code
  fields instead of EXACT or FUZZY.
---

# PINCODE Match

### What `PINCODE` does

`PINCODE` splits a value on the first hyphen, keeps only the part before it, and compares that prefix for exact equality. It is not a fuzziness dial sitting between `EXACT` and `FUZZY` - it's exact-match on a preprocessed string, where the preprocessing is "truncate at the first hyphen." Concretely, this models the **US ZIP+4 format specifically**: "12345-6789" is compared as just "12345", since the "-6789" is a discardable delivery-route extension in that format.

A null or blank value on either side is an automatic match.

**This is not a general international postal-code normalizer.** The hyphen-truncation logic assumes the part after the hyphen is always a discardable suffix. That's true for US ZIP+4, but many countries use a hyphen as a meaningful separator where _both_ halves matter - for example, Polish postal codes ("00-950" vs "00-123") or Portuguese ones. On those formats, `PINCODE` truncates both sides down to "00" and reports a match, silently collapsing two genuinely different postal codes. See the table and "When not to use" below.

### What `PINCODE` matches and what it does not

| Value A | Value B | Match? |
|---|---|---|
| 94102 | 94102-1234 | Yes - both truncate to "94102" |
| 94102 | 94103 | No - "94102" vs "94103" |
| EC1A 1BB | EC1A1BB | No - neither string has a hyphen, so both pass through unsplit |
| 110001 | 110001 | Yes - Indian PIN code, identical |
| 94102 | 941-02 | No - "94102" vs "941" ("941-02" truncates to "941", discarding "02") |
| 00-950 | 00-123 | Yes - a false match. Both truncate to "00"; this is the Polish-postal-code failure case |
| [null] | 94102 | Yes - null/blank on either side auto-matches |

### When to use `PINCODE`

<details>

<summary><strong>Postal code, ZIP code, PIN code fields</strong></summary>

Any field containing a postal code or PIN code. `PINCODE` is always preferable to `EXACT` (which misses format variants) or `FUZZY` (which introduces tolerance that causes false matches between nearby but different codes).

This is the only match type specifically designed for postal codes

</details>

### When not to use `PINCODE`

<details>

<summary><strong>Phone numbers or other numeric codes</strong></summary>

`PINCODE` is designed for postal codes specifically. For phone numbers, use `FUZZY` or `NUMERIC`. For product codes with numeric components, use `NUMERIC_WITH_UNITS` or `NUMERIC`.

</details>

<details>

<summary><strong>Hyphenated postal codes where both halves are significant</strong></summary>

`PINCODE`'s hyphen handling is US-ZIP+4-specific: it assumes anything after the first hyphen is a discardable suffix. Many countries' formats don't work that way - Polish postal codes ("00-950" vs "00-123") use the hyphen as a meaningful internal separator, and `PINCODE` would truncate both down to "00" and report a false match.

If your postal code format uses a hyphen where both sides carry meaning, use `EXACT` instead, or normalise the format upstream before matching.

</details>

{% hint style="success" icon="book-open" %}
**Related types:**

* `EXACT` - use when format is guaranteed consistent
* `NUMERIC` - for other numeric identifier fields
* `NULL_OR_BLANK` - combine when postal codes are often missing

**Read more:** [Match Types](./)
{% endhint %}
