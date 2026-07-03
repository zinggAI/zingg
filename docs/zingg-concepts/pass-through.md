---
description: >-
  Exclude specific records from matching while still including them in the Zingg
  output with their own Zingg ID.
tags:
  - ent
---

# Pass Through

{% hint style="info" icon="right-long" %}
Enterprise only. Pass Through excludes specific records from the matching process while still including them in the output. Each Pass Through record receives its own unique Zingg ID.
{% endhint %}

Not all records in your dataset should participate in matching. Some records are partially populated, corrupted, bot-generated, or have attributes that should not influence cluster formation, but they still need to appear in the output for governance, compliance, or audit purposes.

`Pass Through` lets you define a filter expression for these records. Records that meet the pass through condition are excluded from matching, linking, and incremental processes. They are\
carried through to the output unchanged, and each receives its own unique `Zingg ID`.

### When to use Pass Through

<details>

<summary><strong>Use case 1: Bot-generated records</strong></summary>

Some datasets contain records created by bots or automated processes rather than real entities. These records are identifiable by a synthetic email pattern, a placeholder name, or a specific marker field.

Including these items in the matching process inflates the sizes of the clusters and degrades the accuracy of the results. `Pass Through` lets you exclude them by defining a filter expression on the identifying field.

Example: Exclude all records where the email field contains "`@bot`." or where a marker field equals "`AUTOMATED`".

These records still appear in the output with their own `Zingg ID`s so they can be traced and audited.

</details>

<details>

<summary><strong>Use case 2: Corrupted or spam records</strong></summary>

Some records are corrupted, incomplete, or contain spam data that would produce false matches if included in the matching process. Common examples: records with all fields blank except an ID, records with placeholder values like "`TEST`" or "`NULL`", or records flagged by an upstream data quality check.

`Pass Through` lets you filter these records out of matching while keeping them in the output for auditing purposes. The audit trail shows which records were treated as `Pass Through` and why.

</details>

<details>

<summary><strong>Use case 3: Attribute exclusion</strong></summary>

Occasionally a specific attribute value should prevent a record from influencing cluster formation, even if the record itself is valid. For example, a gender field that should not factor into matching, or a record status flag that indicates the record is archived or inactive.

`Pass Through` lets you filter by any field expression. Records matching the condition are excluded from matching entirely, even if their other fields would produce a valid match.

</details>

### How to configure Pass Through

`Pass Through` is not a separate phase; it is a filter expression you set on your `args` object in [Configure Zingg](../running-zingg/configure-zingg.md), before running any phase. Records matching the expression are excluded from matching, linking, and incremental runs automatically.

{% tabs %}
{% tab title="Enterprise" %}
#### Python

Set the pass through expression on your args.

```python
args.setPassthroughExpr("fname = 'matilda'")
```

#### JSON

```json
{ "passthroughExpr" : "fname = 'matilda'" }
```

For nullable fields, ensure the negative of your expression correctly identifies non-passthrough records:

```json
{ "passthroughExpr" : "is_deceased = true AND is_deceased IS NOT NULL" }
```

Zingg internally applies the negation of `passthroughExpr` to filter which records participate in matching. If you apply the condition to a nullable field without the null guard, records with null values in that field may behave unexpectedly.
{% endtab %}

{% tab title="Enterprise Snowflake" %}
_**CHECK WITH SONAL - Enterprise Snowflake content for this topic to be provivded by Sonal**_
{% endtab %}
{% endtabs %}

### Pass Through records in the output

`Pass Through` records appear in the match output alongside normally matched records. The key difference:

* Normally matched records share a `Zingg ID` with other records in their cluster.
* `Pass Through` records each receiving their own unique `Zingg ID`. They are not grouped with any other record. They are traceable and auditable but do not contribute to or inherit any entity cluster.

{% hint style="success" icon="right-long" %}
**Read more**:

* Pass Through concept in the glossary - [Concepts glossary](concept-glossary.md)&#x20;
* Configure Zingg where Pass Through is set up - [Configure Zingg](../running-zingg/configure-zingg.md)&#x20;
{% endhint %}
