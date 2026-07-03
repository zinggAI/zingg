---
description: >-
  No tolerance for variation. Two values must match perfectly for EXACT to score
  them as a match. The right choice for trusted identifiers and categorical
  fields.
---

# EXACT Match

### What `EXACT` does

`EXACT` compares two field values and scores them as matching only when they are identical. There is no tolerance for typos, abbreviations, case differences, or format variation.

Use `EXACT` for any field where a difference in value means a definite difference in entity trusted identifiers like SSN, national ID, passport number, tax code and for categorical fields like country codes and boolean flags where variation is not expected.

### How the algorithm works

_**COMMENT FOR TEAM — Algorithm detail for EXACT to be added here.**_

### What `EXACT` matches and what it does not

<table><thead><tr><th valign="top">Value A</th><th valign="top">Value B</th><th valign="top">Match?</th></tr></thead><tbody><tr><td valign="top">123-45-6789</td><td valign="top">123-45-6789</td><td valign="top">Yes - identical</td></tr><tr><td valign="top">123-45-6789</td><td valign="top">123-45-6780</td><td valign="top">No - single-digit difference</td></tr><tr><td valign="top">US</td><td valign="top">US</td><td valign="top">Yes - identical</td></tr><tr><td valign="top">US</td><td valign="top">us</td><td valign="top"><em><strong>Confirm with team —</strong></em><br><em><strong>see algorithm note above</strong></em></td></tr><tr><td valign="top">true</td><td valign="top">true</td><td valign="top">Yes</td></tr><tr><td valign="top">true</td><td valign="top">1</td><td valign="top"><em><strong>Confirm with team —</strong></em><br><em><strong>see algorithm note above</strong></em></td></tr><tr><td valign="top">2024-01-15</td><td valign="top">2024-01-15</td><td valign="top">Yes</td></tr><tr><td valign="top">2024-01-15</td><td valign="top">01/15/2024</td><td valign="top"><em><strong>Confirm with team —</strong></em><br><em><strong>different date format</strong></em></td></tr><tr><td valign="top">[null]</td><td valign="top">123-45-6789</td><td valign="top"><em><strong>Confirm — null behaviour with EXACT. Add NULL_OR_BLANK to be explicit.</strong></em></td></tr></tbody></table>

### When to use `EXACT`

<details>

<summary><strong>National identifiers - SSN, passport, tax ID, national ID</strong></summary>

These fields are reliable unique identifiers. An exact match on SSN means the two records are definitively the same entity. Any tolerance on these fields creates false positives that are very difficult to correct after the fact.

Always use `EXACT` on trusted identifiers. In Zingg Enterprise, also add them as deterministic matching conditions, an exact match on a trusted identifier produces a guaranteed match with score 1 before the probabilistic model runs.

→ [Deterministic vs Probabilistic Matching](../../entity-resolution/deterministic-vs-probabilistic-matching.md)

</details>

<details>

<summary><strong>Date of birth, event dates</strong></summary>

Dates should not have fuzzy tolerance. "1985-06-15" and "1985-06-16" are different dates of birth - those are different people.

Use `EXACT` for all date fields. If your date formats vary across source systems, normalise them upstream before matching - do not use `FUZZY` to bridge\
format differences on dates.

</details>

<details>

<summary><strong>Country codes, boolean flags, categorical variables</strong></summary>

Short categorical codes where variation is not expected: "US", "GB", "IN", true/false,\
status codes belong with `EXACT`.

These fields should have no tolerance. If a record says "US" and another says "UK", they are different countries. `FUZZY` would score them as similar (two-character strings\
sharing one character). `EXACT` does not.

</details>

### When not to use `EXACT`

<details>

<summary><strong>Any field with known format variation</strong></summary>

Email addresses entered with and without dots, phone numbers with and without country codes, names that appear in short and long form - none of these are safe with `EXACT`.

`EXACT` on a phone number field will miss "+1-415-555-0123" and "4155550123" as a match even though they are the same number.

Use `FUZZY` or the field-specific match type (EMAIL for email, PINCODE for postal codes).

</details>

<details>

<summary><strong>Name fields</strong></summary>

Never use `EXACT` on name fields. Even internally consistent data has legitimate name variations - legal name vs common name, maiden name vs married name. Use `FUZZY` for names.

</details>

### Configuring `EXACT`

{% tabs %}
{% tab title="Python" %}
### **Community**

```python
from zingg.client import *

ssn = FieldDefinition("ssn", "string", MatchType.EXACT) 
dob = FieldDefinition("dob", "string", MatchType.EXACT)
```

### **Enterprise**

```python
from zinggEC.enterprise.common.EFieldDefinition import EFieldDefinition

ssn = EFieldDefinition("ssn", "string", MatchType.EXACT)
dob = EFieldDefinition("dob", "string", MatchType.EXACT)
```
{% endtab %}

{% tab title="JSON" %}
{% hint style="info" icon="right-long" %}
The JSON `fieldDefinition` block is identical for Community and Enterprise. Only the Python class differs between editions — `FieldDefinition` (Community) vs `EFieldDefinition` (Enterprise). The CLI command is identical for both editions.
{% endhint %}

```json
{
  "fieldDefinition" : [
    {
      "fieldName" : "ssn",
      "matchType" : "exact",
      "fields" : "ssn",
      "dataType" : "string"
    },
    {
      "fieldName" : "dob",
      "matchType" : "exact",
      "fields" : "dob",
      "dataType" : "string"
    }
  ]
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

* `FUZZY` - use when variation is expected
* `PINCODE` - EXACT-like for postal codes with format normalisation built in
* `DONT_USE`  - exclude from matching entirely (vs EXACT which contributes\
  a match signal)
* `Deterministic vs Probabilistic Matching`  - combine EXACT fields with deterministic rules (Enterprise)

**Read more**: [Match Types](./)&#x20;
{% endhint %}
