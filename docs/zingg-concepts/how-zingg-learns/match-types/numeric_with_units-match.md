---
description: >-
  Extracts product codes or numbers with units (16gb, 500ml, 2.4GHz) and
  compares how many are the same across both values. Built for product
  specification fields.
---

# NUMERIC\_WITH\_UNITS Match

### What `NUMERIC_WITH_UNITS` does

`NUMERIC_WITH_UNITS` extracts number-and-unit pairs from a string field - "16gb," "500ml", "2.4GHz", and compares how many are the same across two records. It is an extension of NUMERIC that recognises units alongside numbers, making it suitable for product catalogs and specification data where a value without its unit is ambiguous.

### How the algorithm works

_**COMMENT FOR TEAM — Algorithm detail for NUMERIC\_WITH\_UNITS to be added here.**_

### What `NUMERIC_WITH_UNITS` matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Match?</th></tr></thead><tbody><tr><td valign="top">16gb RAM</td><td valign="top">16 GB Memory</td><td valign="top">Yes - 16 GB extracted from both</td></tr><tr><td valign="top">16gb RAM</td><td valign="top">32gb RAM</td><td valign="top">No - different numbers</td></tr><tr><td valign="top">500ml</td><td valign="top">0.5L</td><td valign="top"><em><strong>Confirm with team —</strong></em><br><em><strong>unit normalisation (ml vs L</strong></em>)</td></tr><tr><td valign="top">2.4GHz Dual Core</td><td valign="top">2.4GHz Processor</td><td valign="top">Yes - 2.4GHz matches</td></tr><tr><td valign="top">16gb</td><td valign="top">16</td><td valign="top">Confirm - with vs without unit</td></tr></tbody></table>

### When to use `NUMERIC_WITH_UNIT`

<details>

<summary><strong>Product specification fields</strong></summary>

Any field containing product specifications with units - storage capacity, screen size, processor speed, volume, weight. E-commerce product catalogs, manufacturing BOMs, medical device specifications.

"16GB RAM, 512GB SSD" and "16 GB RAM, 512 GB SSD" represent the same configuration. `NUMERIC_WITH_UNIT` handles this.

</details>

### When not to use `NUMERIC_WITH_UNIT`

<details>

<summary><strong>Address fields with numbers</strong></summary>

Use `NUMERIC` for address fields. "42 Main Street" does not have units. `NUMERIC_WITH_UNITS` is built for product specification patterns.

</details>

{% tabs %}
{% tab title="Python" %}
### **Community**

```python
from zingg.client import *

    weight = FieldDefinition("weight", "string", MatchType.NUMERIC_WITH_UNITS)
```

### **Enterprise**

```python
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition

    weight = EFieldDefinition("weight", "string", MatchType.NUMERIC_WITH_UNITS)
```
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block is identical for Community and Enterprise. Only the Python class differs between editions — `FieldDefinition` (Community) vs `EFieldDefinition` (Enterprise). The CLI command is identical for both editions.
{% endhint %}

```json
{
  "fieldDefinition" : [ {
    "fieldName" : "weight",
    "matchType" : "numeric_with_units",
    "fields" : "weight",
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

* `NUMERIC` - for numbers without unit
* `TEXT` - combine for the descriptive parts of product fields

**Read more**: [Match types](./)
{% endhint %}
