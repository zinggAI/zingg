---
description: >-
  The broadest match type handles typos, abbreviations, transpositions, and
  real-world data variation. The default starting point for most name and
  address fields.
---

# FUZZY Match

### What `FUZZY` does

`FUZZY` applies a string similarity comparison to two field values and produces a score reflecting how similar they are. It tolerates the full range of real-world variation: missing characters, transposed letters, common abbreviations, spelling differences, and format inconsistencies.

It is Zingg's most permissive match type and the right starting point for any field where values can legitimately vary between records representing the same entity.

### What `FUZZY` matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Match?</th></tr></thead><tbody><tr><td valign="top">Jonathan Smith</td><td valign="top">Jon Smith</td><td valign="top">Yes - abbreviation and short-form variation</td></tr><tr><td valign="top">J. Smith</td><td valign="top">John Smith</td><td valign="top">Yes - initial vs full name</td></tr><tr><td valign="top">Jonathon</td><td valign="top">Jonathan</td><td valign="top">Yes - common transposition</td></tr><tr><td valign="top">Johnson</td><td valign="top">Smith</td><td valign="top">No - too different, no common characters</td></tr><tr><td valign="top">IBM Corp</td><td valign="top">IBM Corporation</td><td valign="top">Yes - long shared prefix scores high</td></tr><tr><td valign="top">[null]</td><td valign="top">John Smith</td><td valign="top">Depends - add <code>NULL_OR_BLANK</code> to control null behaviour</td></tr></tbody></table>

### When to use `FUZZY`

<details>

<summary><strong>First name, last name, full name fields</strong></summary>

`FUZZY` is the right match type for name fields in most datasets. Real-world name data contains spelling variations, abbreviations, culturally different name orders, and transliterations that `FUZZY` handles automatically.

For datasets with known nickname patterns ("Jon" / "Jonathan", "Bill" / "William"),\
consider combining `FUZZY` with `MAPPING_(nicknames)` to catch cases that are too different for `FUZZY` alone.

</details>

<details>

<summary><strong>Company names and organisation names</strong></summary>

"IBM", "I.B.M.", and "IBM Corporation" have high `FUZZY` similarity. Most common abbreviations and punctuation variants will score above the match threshold.

For very different representations ("IBM" vs "International Business Machines"), combine `FUZZY` with `MAPPING_(company_names)` to map the canonical form explicitly.

</details>

<details>

<summary><strong>Free-text address lines</strong></summary>

Full address lines benefit from `FUZZY` for the street name component. For structured addresses where the street number and street name are in the same field, consider splitting your approach:

Use `ONLY_ALPHABETS_FUZZY` for the street name component (ignores numbers, applies fuzzy to letters) and `NUMERIC` for the number component. Or apply `FUZZY` to the full field and accept that number differences will influence the score.

</details>

### When not to use `FUZZY`

<details>

<summary><strong>Trusted identifiers—SSN, passport, tax ID, national ID</strong></summary>

Never use `FUZZY` on fields that are reliable unique identifiers. `FUZZY` tolerance on an SSN field means "123-45-6789" and "123-45-6780" could score above the match threshold, that is a false positive you cannot afford in a compliance context.

Use `EXACT` for trusted identifiers.

</details>

<details>

<summary><strong>Date of birth, registration date, event date</strong></summary>

Dates should always use `EXACT`. A fuzzy comparison between "1985-06-15" and "1985-06-16" might score high enough to produce a match - but those are different people or different events. Use `EXACT` for all date fields.

</details>

<details>

<summary><strong>Postal codes and ZIP codes</strong></summary>

Use `PINCODE` not `FUZZY` for postal codes. `PINCODE` is built to handle the specific format variants postal codes appear in (xxxxx vs xxxxx-xxxx) without introducing the tolerance that `FUZZY` adds, which could match "94102" and "94103" as similar.

</details>

<details>

<summary><strong>When performance at scale is critical</strong></summary>

`FUZZY` is computationally heavier than `FUZZY_OPTIMISED`. For production runs on large datasets where you want the same matching quality with faster results, use `FUZZY_OPTIMISED` instead.

</details>



{% hint style="success" icon="right-long" %}
**Related match types:**

* `FUZZY_OPTIMISED` - same quality, better performance at scale
* `ONLY_ALPHABETS_FUZZY` - strip numbers first, then apply fuzzy to letters only
* `MAPPING_(FILENAME)` - handle completely different strings (nicknames, abbreviations) that `FUZZY` cannot bridge

**Read more**: [Match Types](./) | [Configure Zingg](../../../running-zingg/configure-zingg.md) | [How Zingg Learns](../)
{% endhint %}
