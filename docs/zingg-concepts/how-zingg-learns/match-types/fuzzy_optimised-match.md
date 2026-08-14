---
description: >-
  Same matching quality as FUZZY with significantly lower time.
  The production-scale choice when FUZZY accuracy is needed on large datasets.
tags:
  - ent
  - tag: enterprise-only
    primary: true
---

# FUZZY\_OPTIMISED Match

{% hint style="info" icon="right-long" %}
`FUZZY_OPTIMISED` is Enterprise only. The Community (open source) edition does not support this match type. Available in Enterprise Lite and above.
{% endhint %}

### What `FUZZY_OPTIMISED` does

`FUZZY_OPTIMISED` produces the same matching results as `FUZZY` - it handles typos, abbreviations, transpositions, and real-world variation on name, addresses, and free-text fields.

The scoring pattern is exactly the same as `FUZZY` - their results are not affected. The only difference is computation time: `FUZZY_OPTIMISED` runs in roughly half the time `FUZZY` takes on the same data.

### What `FUZZY_OPTIMISED` matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Match?</th></tr></thead><tbody><tr><td valign="top">Jonathan Smith</td><td valign="top">Jon Smith</td><td valign="top">Yes - same as <code>FUZZY</code></td></tr><tr><td valign="top">J. Smith</td><td valign="top">John Smith</td><td valign="top">Yes - same as <code>FUZZY</code></td></tr><tr><td valign="top">Jonathon</td><td valign="top">Jonathan</td><td valign="top">Yes</td></tr><tr><td valign="top">Johnson</td><td valign="top">Smith</td><td valign="top">No - too different</td></tr><tr><td valign="top">IBM Corp</td><td valign="top">IBM Corporation</td><td valign="top">Yes - long shared prefix scores high, same as with <code>FUZZY</code></td></tr><tr><td valign="top">[null]</td><td valign="top">John Smith</td><td valign="top">Depends - add <code>NULL_OR_BLANK</code> to control null behaviour</td></tr></tbody></table>

For harder abbreviation cases like "IBM" vs "International Business Machines", `FUZZY_OPTIMISED` needs the same help `FUZZY` does - the strings are too dissimilar on their own, so combine with `MAPPING_(company_names)` to map the representative form explicitly.

### When to use `FUZZY_OPTIMISED`

<details>

<summary><strong>Production runs on large datasets</strong></summary>

Any field where you would use `FUZZY` is a candidate for `FUZZY_OPTIMISED` in production. The matching output is the same. The resource cost is lower.

Switch from `FUZZY` to `FUZZY_OPTIMISED` when you move from evaluation to production, or when a `FUZZY` run is taking longer or consuming more memory than your infrastructure allows.

</details>

<details>

<summary><strong>Name and address fields at scale</strong></summary>

Customer names, company names, street addresses, and city fields on datasets of 1M records or more. The performance improvement is most visible on string fields with high variation, exactly the fields where `FUZZY` is typically applied.

</details>

### **When not to use `FUZZY_OPTIMISED`**

<details>

<summary><strong>When you need exact matching</strong></summary>

`FUZZY_OPTIMISED` carries the same tolerance for variation as `FUZZY` - it is not a substitute for exact comparison. On trusted identifiers, dates, and postal codes, that tolerance is a false positive risk you cannot afford. Use `EXACT` for identifiers and dates, `PINCODE` for postal codes.

</details>

{% hint style="info" icon="right-long" %}
`FUZZY_OPTIMISED` is **Enterprise** only. Community users use `FUZZY`.
{% endhint %}

### Configuring `FUZZY_OPTIMISED`

{% tabs %}
{% tab title="Python" %}
```python
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition
from zinggEC.enterprise.common.EMatchTypes import *

fname = EFieldDefinition("fname", "string", EMatchType.FUZZY_OPTIMISED)
```
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block below uses Enterprise-only match type `FUZZY_OPTIMISED`. The CLI command is identical to other phases.
{% endhint %}

```json
{
  "fieldDefinition" : [ {
    "fieldName" : "fname",
    "matchType" : "fuzzy_optimised",
    "fields" : "fname",
    "dataType" : "string"
  } ]
}
```

{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
**Related Match types:**

* `FUZZY` - same quality, use in development and evaluation
* `EMAIL_OPTIMISED` - optimised version of EMAIL (Enterprise only)
* `ONLY_ALPHABETS_FUZZY_OPTIMISED` - optimised version for address fields (Enterprise only)

**Read more:** [Match Types](./)
{% endhint %}
