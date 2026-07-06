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

### What it `EMAIL_OPTIMISED` does

`EMAIL_OPTIMISED` matches email addresses using the same local-part comparison as `EMAIL` it ignores the domain and matches on the portion before `@`.

The difference is performance: `EMAIL_OPTIMISED` is substantially faster when comparing large numbers of email addresses.

Use `EMAIL` in development. Switch to `EMAIL_OPTIMISED` for production runs.

### How the algorithm works

_**COMMENT FOR TEAM — Same algorithm question as EMAIL applies here.**_

### What `EMAIL_OPTIMISED` matches and what it does not

{% hint style="info" icon="right-long" %}
`EMAIL_OPTIMISED` produces the same matching results as `EMAIL`.

The behaviour table is identical—only performance differs.
{% endhint %}

### When to use `EMAIL_OPTIMISED`

<details>

<summary><strong>Email fields on datasets over 1M records</strong></summary>

Any use case where you would use `EMAIL` but your dataset is large enough that\
performance matters. Customer datasets, patient registries, voter files—any domain where millions of email addresses are being compared.

</details>

{% hint style="info" icon="right-long" %}
`EMAIL_OPTIMISED` is **Enterprise** only. Use `EMAIL` in Community.
{% endhint %}

{% tabs %}
{% tab title="Python" %}
```python
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition

    email = EFieldDefinition("email", "string", MatchType.EMAIL_OPTIMISED)
```
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block below uses Enterprise-only match type `EMAIL_OPTIMISED`. The CLI command is identical to other phases.
{% endhint %}

```json
{
  "fieldDefinition" : [ {
    "fieldName" : "email",
    "matchType" : "email_optimised",
    "fields" : "email",
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

* `EMAIL` - use in Community or in development (all editions)
* `FUZZY_OPTIMISED` - same optimisation pattern for name/text fields

**Read more**: [Match types](./)
{% endhint %}
