---
description: >-
  Same matching quality as FUZZY with significantly lower CPU and memory usage.
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

`FUZZY_OPTIMISED` produces the same matching results as `FUZZY` - it handles typos, abbreviations, transpositions, and real-world variation on name, addresses, and free-text fields.\
The difference is internal: it uses an optimized implementation that runs with significantly lower CPU and memory usage on large datasets.

In evaluation and development, use `FUZZY`. In production, especially on datasets over 1 million records where performance and infrastructure cost matter, switch to `FUZZY_OPTIMISED`.

### How the algorithm works

_**COMMENT FOR TEAM — Algorithm detail for FUZZY\_OPTIMISED to be added here.**_

### What `FUZZY_OPTIMISED` matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Match?</th></tr></thead><tbody><tr><td valign="top">Jonathan Smith</td><td valign="top">Jon Smith</td><td valign="top">Yes - same as <code>FUZZY</code></td></tr><tr><td valign="top">J. Smith</td><td valign="top">John Smith</td><td valign="top">Yes - same as <code>FUZZY</code></td></tr><tr><td valign="top">Jonathon</td><td valign="top">Jonathan</td><td valign="top">Yes - single transposition</td></tr><tr><td valign="top">Johnson</td><td valign="top">Smith</td><td valign="top">No - too different</td></tr><tr><td valign="top">IBM Corp</td><td valign="top">IBM Corporation</td><td valign="top">Yes - abbreviation variant</td></tr><tr><td valign="top">[null]</td><td valign="top">John Smith</td><td valign="top">Depends - add <code>NULL_OR_BLANK</code> to control null behaviour</td></tr></tbody></table>

_**COMMENT FOR TEAM — Please extend this table with real Zingg test runs comparing `FUZZY` and `FUZZY_OPTIMISED` outputs on the same data to confirm they are identical or document**_\
&#xNAN;_**any known differences.**_

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

<summary><strong>During development and evaluation</strong></summary>

`FUZZY` is simpler to reason about during development. Use `FUZZY` while you are building and tuning your configuration. Switch to `FUZZY_OPTIMISED` before your first production run.

</details>

{% hint style="info" icon="right-long" %}
`FUZZY_OPTIMISED` is **Enterprise** only. Community users use `FUZZY`.
{% endhint %}

### Configuring `FUZZY_OPTIMISED`

{% tabs %}
{% tab title="Python" %}
```python
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition

fname = EFieldDefinition("fname", "string", MatchType.FUZZY_OPTIMISED)
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

### **CLI**

```bash
./ scripts / zingg.sh-- phase findTrainingData-- conf config.json
```
{% endtab %}
{% endtabs %}

{% hint style="success" icon="right-long" %}
**Related Match types:**

* `FUZZY` - same quality, use in development and evaluation
* `EMAIL_OPTIMISED` - optimised version of EMAIL (ENT)
* `ONLY_ALPHABETS_FUZZY_OPTIMISED` - optimised version for address fields (ENT)

**Read more:** [Match Types](./)
{% endhint %}
