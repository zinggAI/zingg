---
description: >-
  Compares the overlap of words between two strings. Built for longer
  descriptive or free-text fields where word presence matters more than
  character-level similarity.
---

# TEXT Match

### What `TEXT` does

`TEXT` compares two string values by measuring the overlap of words between them, how many words appear in both. It is designed for longer descriptive fields like product descriptions, notes, professional bios, or service descriptions where the overall word content matters more than exact character matches.

`TEXT` works best when your text fields do not have many typos - the comparison is at word level, not character level, so it will not catch spelling errors within words. For shorter name and address fields, use `FUZZY` instead.

### How the algorithm works

_**COMMENT FOR TEAM — Algorithm detail for TEXT to be added here.**_

### What `TEXT` matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Match?</th></tr></thead><tbody><tr><td valign="top">Enterprise data management platform</td><td valign="top">Data management platform for enterprise</td><td valign="top">Yes - high word overlap<br>("enterprise", "data", "management",<br>"platform" all shared)</td></tr><tr><td valign="top">Enterprise software solutions</td><td valign="top">Consumer hardware products</td><td valign="top">No - low word overlap</td></tr><tr><td valign="top">Machine learning model training</td><td valign="top">Training machine learning models</td><td valign="top">Yes - same words, different order</td></tr><tr><td valign="top">ML model</td><td valign="top">Machine learning model</td><td valign="top">Partial - "model" shared,<br>"ML" vs "Machine learning" differs. Score reflects partial overlap.</td></tr><tr><td valign="top">[empty]</td><td valign="top">Enterprise data platform</td><td valign="top"><em><strong>Confirm with team —</strong></em><br><em><strong>empty string behaviour</strong></em></td></tr><tr><td valign="top"></td><td valign="top"></td><td valign="top"></td></tr></tbody></table>

### When to use `TEXT`

<details>

<summary><strong>Product descriptions and catalog fields</strong></summary>

E-commerce product titles and descriptions often contain the same information in different word orders or phrasing. "16GB RAM Laptop with SSD" and "Laptop SSD 16GB RAM" are the same product. `TEXT` captures word-level similarity across these variations.

Combine with `NUMERIC_WITH_UNITS` for fields containing both descriptive text and numeric specifications.

</details>

<details>

<summary><strong>Professional bios, service descriptions, notes</strong></summary>

Long-form text fields where the same entity appears with different but semantically similar descriptions across systems. `TEXT` handles word-level overlap\
without requiring exact string similarity.

</details>

### When not to use `TEXT`

<details>

<summary><strong>Short fields - names, addresses, codes</strong></summary>

`TEXT` is designed for longer strings with multiple words. On a first name field with 1-2 words, `FUZZY` handles character-level variation far better than word overlap.

Use `FUZZY` for name and address fields. Use `TEXT` for description and notes fields.

</details>

<details>

<summary><strong>Fields with many typos</strong></summary>

`TEXT` compares at word level. "Enterprize" (misspelled) does not overlap with "Enterprise" using `TEXT`.

If your free-text fields have typos and spelling errors, `FUZZY` handles those better at the character level.

</details>

{% tabs %}
{% tab title="Python" %}
### **Community**

```python
from zingg.client import *

    description = FieldDefinition("description", "string", MatchType.TEXT)
```

### **Enterprise**

```python
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition

    description = EFieldDefinition("description", "string", MatchType.TEXT)
```
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block is identical for Community and Enterprise. Only the Python class differs between editions — `FieldDefinition` (Community) vs `EFieldDefinition` (Enterprise). The CLI command is identical for both editions.
{% endhint %}

```json
{
  "fieldDefinition" : [ {
    "fieldName" : "description",
    "matchType" : "text",
    "fields" : "description",
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

{% hint style="info" icon="right-long" %}
**Related types:**

* `FUZZY` - better for short fields and when typos are present
* `NUMERIC_WITH_UNITS` - combine for product description fields

**Read more**: [Match Types](./)&#x20;
{% endhint %}
