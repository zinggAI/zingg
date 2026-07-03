---
description: >-
  Compares only the local part of an email address before the @ symbol and
  ignores the domain. Built for datasets where the same person appears with
  different email domains.
---

# EMAIL Match

### What EMAIL does

`EMAIL` splits an email address at the `@` symbol and compares only the local part, the portion before `@`. The domain is ignored entirely.

This handles the common scenario where the same person has a work email and a personal email, or where an organization's email domain changes over time. As long as the local part\
is the same, EMAIL scores them as a match.

### How the algorithm works

_**COMMENT FOR TEAM — Algorithm detail for EMAIL to be added here.**_

### What `EMAIL` matches and what it does not

{% include "../../../.gitbook/includes/exact-match-behavior-table.md" %}

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

<summary><strong>When local parts vary across your data (initials, format differences)</strong></summary>

"j.smith@company.com" and "john.smith@company.com" have different local parts and will not match with `EMAIL`.

If your data has local part variations like this, consider `FUZZY` on the full email field -accepting that different domains with similar local parts may produce false positives.

</details>

### Configuring `EMAIL`

{% tabs %}
{% tab title="Python" %}
### **Community**

```python
from zingg.client import*

 email = FieldDefinition("email", "string", MatchType.EMAIL)
```

### **Enterprise**

```python
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition

 email = EFieldDefinition("email", "string", MatchType.EMAIL)
```
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block is identical for Community and Enterprise. Only the Python class differs between editions — `FieldDefinition` (Community) vs `EFieldDefinition` (Enterprise). The CLI command is identical for both editions.
{% endhint %}

```json
{
  "fieldDefinition" : [ {
    "fieldName" : "email",
    "matchType" : "email",
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

* `EMAIL_OPTIMISED` - same as EMAIL, faster at production scale (ENT)
* `EXACT` - use when the full email including domain must match
* `FUZZY` - use when local parts also have variation

**Read more**: [Match types](./)
{% endhint %}

