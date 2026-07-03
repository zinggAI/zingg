---
description: >-
  Same matching behavior as ONLY_ALPHABETS_FUZZY with optimised processing for
  production-scale address matching. Enterprise only.
tags:
  - ent
---

# ONLY\_ALPHABETS\_FUZZY\_OPTIMISED Match

### What it does

`ONLY_ALPHABETS_FUZZY_OPTIMISED` produces the same matching results as `ONLY_ALPHABETS_FUZZY` - strips numbers, applies fuzzy to the alphabetic portion with optimised processing that reduces CPU and memory usage on large datasets.

Use `ONLY_ALPHABETS_FUZZY` in development. Switch to `ONLY_ALPHABETS_FUZZY_OPTIMISED` for production runs on large address datasets.

### How the algorithm works

_**COMMENT FOR TEAM - Same questions as ONLY\_ALPHABETS\_FUZZY apply. Additionally: what is the performance improvement in practice vs ONLY\_ALPHABETS\_FUZZY on a large address dataset?**_

### What **`ONLY_ALPHABETS_FUZZY_OPTIMISED`** matches and what it does not

{% hint style="info" icon="right-long" %}
`ONLY_ALPHABETS_FUZZY_OPTIMISED` is Enterprise only. The Community (open source) edition does not support this match type. Available in Enterprise Lite and above.
{% endhint %}

{% include "../../../.gitbook/includes/only_alphabet_fuzzy_match_table.md" %}

{% tabs %}
{% tab title="Python" %}
{% code expandable="true" %}
```python
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition

    street = EFieldDefinition("street", "string", MatchType.ONLY_ALPHABETS_FUZZY_OPTIMISED)
```
{% endcode %}
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block below uses Enterprise-only match type `ONLY_ALPHABETS_FUZZY_OPTIMISED`. The CLI command is identical to other phases.
{% endhint %}

```json
{
  "fieldDefinition" : [ {
    "fieldName" : "street",
    "matchType" : "only_alphabets_fuzzy_optimised",
    "fields" : "street",
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
**Related types**:

* `ONLY_ALPHABETS_FUZZY` - use in development (all editions)
* `FUZZY_OPTIMISED` - same optimisation pattern for name/text fields
* `NUMERIC` - always combine with this for address fields

**Read more**: [Match Types](./)&#x20;
{% endhint %}
