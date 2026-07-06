---
description: >-
  Matches postal and ZIP codes across the common format variants they appear
  in—5-digit, 9-digit with hyphen, and others. The right choice for postal code
  fields instead of EXACT or FUZZY.
---

# PINCODE Match

### What `PINCODE` does

`PINCODE` is built specifically for postal and PIN code fields. It handles the format variants that postal codes commonly appear in across source systems; a 5-digit US ZIP code and its 9-digit ZIP+4 equivalent. For example, without the tolerance that `FUZZY` would add.

It is more permissive than `EXACT` (which would not match "94102" and "94102-1234") and more precise than `FUZZY` (which would score "94102" and "94103" as similar).

### How the algorithm works

_**COMMENT FOR TEAM — Algorithm detail for PINCODE to be added here.**_

### What `PINCODE` matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Match?</th></tr></thead><tbody><tr><td valign="top">94102</td><td valign="top">94102-1234</td><td valign="top">Yes - 5-digit and ZIP+4 same base</td></tr><tr><td valign="top">94102</td><td valign="top">94103</td><td valign="top">No - different postal codes</td></tr><tr><td valign="top">EC1A 1BB</td><td valign="top">EC1A1BB</td><td valign="top"><em><strong>Confirm with team —</strong></em><br><em><strong>UK postcode with/without space</strong></em></td></tr><tr><td valign="top">110001</td><td valign="top">110001</td><td valign="top">Yes - Indian PIN code, identical</td></tr><tr><td valign="top">94102</td><td valign="top">941-02</td><td valign="top"><em><strong>Confirm with team —</strong></em><br><em><strong>hyphen in different position</strong></em></td></tr><tr><td valign="top">[null]</td><td valign="top">94102</td><td valign="top"><em><strong>Confirm — add NULL_OR_BLANK to control null behaviour</strong></em></td></tr></tbody></table>

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

{% tabs %}
{% tab title="Python" %}
### **Community**

```python
from zingg.client import *

    pincode = FieldDefinition("pincode", "string", MatchType.PINCODE)
```

### **Enterprise**

```python
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition

    pincode = EFieldDefinition("pincode", "string", MatchType.PINCODE)
```
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block is identical for Community and Enterprise. Only the Python class differs between editions — `FieldDefinition` (Community) vs `EFieldDefinition` (Enterprise). The CLI command is identical for both editions.
{% endhint %}

```json
{
  "fieldDefinition" : [ {
    "fieldName" : "pincode",
    "matchType" : "pincode",
    "fields" : "pincode",
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
**Related types:**

* `EXACT` - use when format is guaranteed consistent
* `NUMERIC` - for other numeric identifier fields
* `NULL_OR_BLANK` - combine when postal codes are often missing

**Read more:** [Match Types](./)
{% endhint %}
