---
description: >-
  Same matching quality as FUZZY with significantly lower time. The
  production-scale choice when FUZZY accuracy is needed on large datasets.
tags:
  - ent
  - tag: enterprise-only
    primary: true
---

# FUZZY\_OPTIMISED Match

### What `FUZZY_OPTIMISED` does

`FUZZY_OPTIMISED` produces the same matching results as `FUZZY` - it handles typos, abbreviations, transpositions, and real-world variation on name, addresses, and free-text fields.

The scoring pattern is exactly the same as `FUZZY` - their results are not affected. The only difference is computation time: `FUZZY_OPTIMISED` runs in roughly half the time `FUZZY` takes on the same data.

For harder abbreviation cases like "IBM" vs "International Business Machines", `FUZZY_OPTIMISED` needs the same help `FUZZY` does - the strings are too dissimilar on their own, so combine with `MAPPING_(company_names)` to map the representative form explicitly.

{% hint style="info" icon="book-open" %}
`FUZZY_OPTIMISED` is Enterprise only. The Community (open source) edition does not support this match type. Available in Enterprise Lite and above.
{% endhint %}

{% hint style="success" icon="book-open" %}
**Related Match types:**

* `FUZZY` - same quality, use in development and evaluation
* `EMAIL_OPTIMISED` - optimised version of EMAIL (Enterprise only)
* `ONLY_ALPHABETS_FUZZY_OPTIMISED` - optimised version for address fields (Enterprise only)

**Read more:** [Match Types](./)
{% endhint %}
