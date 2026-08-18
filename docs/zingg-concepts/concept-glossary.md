---
description: >-
  Plain-language definitions of every term used across Zingg docs.  Click any
  term to expand.
---

# Frequently Asked Questions

{% hint style="success" icon="right-long" %}
New to Zingg? Start here. Understanding these terms will make every other page easier to follow.
{% endhint %}

### How Zingg Matches

Explore the core concepts behind Zingg's entity resolution process.\
Learn how Zingg reduces candidate comparisons, learns matching behavior from labeled examples, and groups related records into accurate clusters without handwritten rules.

<details>

<summary><strong>Blocking model</strong></summary>

Blocking group records into candidate buckets before any matching begins. Without it, Zingg would need to compare every record against every other; at 1 million records, that is 500 billion pairs before any field-level comparison runs. The blocking model reduces Zingg's actual comparison space to 0.05–1% of the full problem, making\
Enterprise-scale entity resolution is computationally feasible.

{% hint style="success" icon="right-long" %}
**Read more:** [How Zingg learns](../running-zingg/step-by-step-guide.md) | [Verify blocking](../running-zingg/verify-blocking.md)
{% endhint %}

</details>

<details>

<summary><strong>Active learning</strong></summary>

The process by which Zingg builds its matching model from your feedback rather than a pre-labeled dataset. Zingg presents record pairs; you label each as Match, No Match or Can't Say. Because Zingg selects the pairs where it is most uncertain, 30–50 labelled pairs are\
typically enough to train a high-accuracy model on datasets of 100,000+ records.

{% hint style="success" icon="right-long" %}
**Read more:** [How Zingg learns](how-zingg-learns/) | [Label training pairs](../running-zingg/label-training-pairs.md)
{% endhint %}

</details>

<details>

<summary><strong>Probabilistic matching</strong></summary>

Zingg's default matching mode. The model learns similarity weights for each field from your labeled pairs and assigns a match score to every candidate pair. Records above the threshold are grouped into a cluster. The threshold is automatically chosen to balance accuracy and recall. Zingg optimizes it so you do not need to tune a cut-off manually.

Probabilistic matching handles variations in names, addresses, and other messy real-world data that exact rules cannot account for.\
\&#xNAN;_Available in all editions_

{% hint style="success" icon="right-long" %}
**Read more:** [Deterministic vs probabilistic matching](zingg-entity-resolution-platform/deterministic-vs-probabilistic-matching.md)
{% endhint %}

</details>

<details>

<summary><strong>Deterministic matching</strong></summary>

User-defined hard rules that force a match regardless of the probabilistic score. If the fields you specify all match exactly; for example, tax ID, date of birth, and registered address Zingg treats those records as the same entity without consulting the ML model.

Both approaches run in a single flow; deterministic rules are evaluated first, and probabilistic matching handles everything else.\
\&#xNAN;_**Enterprise only**_

{% hint style="success" icon="right-long" %}
**Read more:** [Deterministic vs probabilistic matching](zingg-entity-resolution-platform/deterministic-vs-probabilistic-matching.md)
{% endhint %}

</details>

<details>

<summary><strong>Transitive closure / graph clustering</strong></summary>

The logic that turns pairwise match decisions into complete clusters. If Record A matches B, and B matches C, transitive closure concludes A, B, and C represent the same entity and groups all three together, even if A and C were never directly compared.

Zingg uses a graph clustering algorithm to apply this logic correctly without creating false chains from weak indirect matches. This is why records in a cluster can appear at varying confidence levels they matched transitively, not always directly.

{% hint style="success" icon="right-long" %}
**Read more:** [How Zingg learns](how-zingg-learns/)
{% endhint %}

</details>

### Match Types and Field Definitions

Configure the fields used for matching and the logic used to compare them.\
Learn how field definitions, match types, and related settings influence matching accuracy and model performance.

<details>

<summary><strong>Which Match types should I use?</strong></summary>

Match types are the combination of similarity functions assigned to each field, telling Zingg how to compare values in that field across records. Zingg provides match types for fuzzy comparison, exact match, email, numeric, text, and several specialized types for alphabet-only or null handling.

Each field receives one match type. The full list of all match types; what they do, when to use each, and examples on [Match Types](how-zingg-learns/match-types/).

A quick reference for the most common fields:

* **Name fields (first name, last name, company name)** → `FUZZY`. Handles spelling variations and abbreviations. Use MAPPING for known alias or nickname lists (Enterprise only).
* **Email address** → `EMAIL`. Matches before the `@` ; only avoids mismatches from different email domains for the same person.
* **Date of birth / registration date** → `EXACT`. Dates should not have fuzzy tolerance.
* **Postal / ZIP code** → `PINCODE`. Handles common format variants.
* **Street address (full address line)** → `FUZZY` or `ONLY_ALPHABETS_FUZZY` combined with NUMERIC for the street number as a separate field.
* **Street number / apartment number** → `NUMERIC`. Extracts and compares the number portion only.
* **Internal record ID (not used for matching)** → `DONT_USE`. Appears in output but excluded from comparison.
* **Any field frequently null across source systems** → Add `NULL_OR_BLANK` alongside the main match type.
* **Product descriptions / notes** → TEXT. `Word` overlap comparison for longer free-text fields.

{% hint style="success" icon="right-long" %}
**Read more:** [Match types](concept-glossary.md#match-types) reference | [Configure Zingg](../running-zingg/configure-zingg.md)
{% endhint %}

</details>

<details>

<summary><strong>Field definition</strong></summary>

The configuration object that tells Zingg which fields to use for matching and how to compare them.

Each field definition has four attributes: `fieldName` (the column name), `fields` (same as `fieldName` for now), `dataType` (string, integer, double, etc.), and `matchType` (the similarity function to apply).

{% hint style="success" icon="right-long" %}
**Read more:** [Configure Zingg](../running-zingg/configure-zingg.md)
{% endhint %}

</details>

<details>

<summary><strong>Stop words</strong></summary>

Values in a field that appear so frequently across records that they carry no useful signal for matching.

Common examples in address fields: "Street", "Avenue", "Building", "Floor". In company fields: "LLC", "Ltd", "Corp". Including stop words in matching inflates similarity scores between unrelated records. Zingg's `stopwords` removal phase analyses your data and recommends which values to treat as stop words before training begins.

{% hint style="success" icon="right-long" %}
**Read more:** [Remove stopwords](../tuning/improve-accuracy/remove-stopwords-optional.md)
{% endhint %}

</details>

### Identity and Output

Understand how Zingg represents and manages resolved entities.\
Learn how identities are assigned, tracked across runs, and made available to downstream systems.

<details>

<summary><strong>Z Cluster</strong></summary>

The cluster identifier used in Zingg Community (Open Source). Records Zingg groups together as the same entity and shares a `Z Cluster` in the output.

`Z Cluster` is non-persistent; re-running the job may produce different IDs for the same data. It cannot be safely referenced in downstream systems across runs.

{% hint style="success" icon="right-long" %}
**Read more:** [How Zingg learns](how-zingg-learns/)
{% endhint %}

</details>

<details>

<summary><strong>Zingg ID</strong></summary>

A globally unique, persistent identifier assigned to each resolved entity in Zingg Enterprise.

Unlike Z Cluster, the Zingg ID does not change between runs. The downstream systems can store and reference it with confidence it will remain stable across incremental updates. _**Enterprise only**_

{% hint style="success" icon="right-long" %}
**Read more:** [Z Cluster and Zingg ID](z-cluster-and-zingg-id.md)
{% endhint %}

</details>

<details>

<summary><strong>Identity graph</strong></summary>

Zingg's underlying data structure representing resolved entities and the relationships between their source records.

* In Community, the graph is non-persistent and rebuilt from scratch each run.
* In Enterprise, it is persistent and updated incrementally: new and changed records are incorporated without a full re-run, and Zingg IDs serve as stable node identifiers within it.

{% hint style="success" icon="right-long" %}
**Read more:** [Identity graph](identity-graph.md) | [Run incremental matching](../running-zingg/run-incremental-matching.md)
{% endhint %}

</details>

<details>

<summary><strong>Golden record</strong></summary>

The single, authoritative version of an entity is built by combining the best available data from all matching source records.

For example, a retail customer appears in an e-commerce platform, a loyalty program, and a support system under slightly different names and addresses. The golden record merges the most complete and reliable field values from each into a single trusted profile. Zingg identifies which records belong together and the golden record is typically constructed downstream in a data platform or MDM layer that consumes them.

{% hint style="success" icon="right-long" %}
**Read more:** [What is Zingg](../) | [Community vs Enterprise](community-vs-enterprise/).
{% endhint %}

</details>



### Data and Configuration

Learn how data is prepared and configured for matching.\
Explore the settings, inputs, and preprocessing concepts that determine how records enter and move through the matching pipeline.

<details>

<summary><strong>Pass Through</strong></summary>

An Enterprise feature for records that are partially populated, outdated, or not suitable for matching, but which still need to appear in the output for governance or compliance purposes. `Pass Through` records are excluded from matching, linking, and incremental processes. Each pass through the record receives its own unique `Zingg ID` in the output.

_Common uses_: Records flagged as deceased, bot-generated accounts identifiable by a synthetic email pattern, or corrupted records that must be traceable but should not influence cluster formation.

{% hint style="success" icon="right-long" %}
**Read more:** [Pass Through](../running-zingg/pass-through.md)
{% endhint %}

</details>

### Platform and Runtime

Understand the platform components and runtime environment that power Zingg.\
Learn how execution settings, deployment options, and infrastructure components affect system behavior and performance.

<details>

<summary><strong>Spark cluster</strong></summary>

The distributed compute environment that runs Zingg's `matching` pipeline is described here. Spark can be self-managed (Apache Spark installed on your own machine or server) or provided as a managed service (Azure Databricks, Microsoft Fabric, AWS EMR, AWS Glue, GCP Dataproc, and Azure Synapse Analytics).

All of these are different ways to get Spark; Zingg runs on all of them. Zingg Enterprise\
also supports Snowflake native compute, which requires no Spark cluster at all.

{% hint style="success" icon="right-long" %}
**Read more:** [Install Zingg](../running-zingg/install-zingg.md) | [Run on cloud](../platform-guides/platform-guide-for-azure-databricks.md)
{% endhint %}

</details>

<details>

<summary><strong>Python API vs CLI</strong></summary>

The two ways to invoke Zingg. The Python API is the most widely adopted method; you call the Zingg phases as Python functions from a notebook or script. The CLI invokes Zingg phases from the command line using a JSON config file.

Both produce identical results; the choice is a developer preference based on your workflow. The Python API is the default method documented throughout these docs.

{% hint style="success" icon="right-long" %}
**Read more:** [Python API](../zingg-python-api/working-with-python.md) | [Zingg command line](../reference/zingg-command-line.md)
{% endhint %}

</details>
