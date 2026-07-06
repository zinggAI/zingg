---
description: >-
  Plain-language definitions of every term used across Zingg docs.  Click any
  term to expand.
---

# Concept Glossary

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

Probabilistic matching handles variations in names, addresses, and other messy real-world data that exact rules cannot account for. \
&#xNAN;_&#x41;vailable in all editions_

{% hint style="success" icon="right-long" %}
**Read more:** [Deterministic vs probabilistic matching](entity-resolution/deterministic-vs-probabilistic-matching.md)
{% endhint %}

</details>

<details>

<summary><strong>Deterministic matching</strong></summary>

User-defined hard rules that force a match regardless of the probabilistic score. If the fields you specify all match exactly; for example, tax ID, date of birth, and registered address Zingg treats those records as the same entity without consulting the ML model.

Both approaches run in a single flow; deterministic rules are evaluated first, and probabilistic matching handles everything else. \
&#xNAN;_**Enterprise only**_

{% hint style="success" icon="right-long" %}
**Read more:** [Deterministic vs probabilistic matching](entity-resolution/deterministic-vs-probabilistic-matching.md)
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

<summary><strong>Match types</strong></summary>

The similarity function assigned to each field, telling Zingg how to compare values in that field across records. Zingg provides match types for fuzzy comparison, exact match, email, numeric, text, and several specialized types for alphabet-only or null handling.

Each field receives one match type. The full list of all match types; what they do, when to use each, and examples on [Match Types](how-zingg-learns/match-types/).

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

<summary><strong>Incremental flow</strong></summary>

The process of updating the identity graph with new, changed, or deleted records without re-running the full match across the entire dataset. Zingg incorporates incoming records into existing clusters, handles cluster merges and unmerges automatically, generates new Zingg IDs for records that do not match any existing cluster, and preserves human-approved decisions so they are not overridden.

Many tools claim incremental capability but cannot maintain match quality and stable IDs simultaneously. This feature is a key Zingg Enterprise differentiator. _Enterprise only_.

{% hint style="success" icon="right-long" %}
**Read more:** [Run incremental matching](../running-zingg/run-incremental-matching.md)
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

### Zingg Phases

Understand the phases that make up the Zingg workflow.\
Learn how operations such as training, matching, and model management work together to resolve entities from raw data.

<details>

<summary><strong><code>findTrainingData</code></strong></summary>

Scans your dataset and selects the most informative candidate record pairs for labeling - edge cases where the model has the most to learn. Candidate pairs are written to `UNMARKED_DIR`.

</details>

<details>

<summary><strong><code>label</code></strong></summary>

Loads the candidate pairs from `findTrainingData` and presents them in a labeling widget. You mark each pair as Match, No Match, or Uncertain. Labels are saved to `MARKED_DIR`.

</details>

<details>

<summary><strong><code>train</code></strong></summary>

Builds the blocking and similarity models from your labeled pairs. Both models are persisted to `zinggDir/modelId`. Once trained, the model can be reused on new data without retraining.

</details>

<details>

<summary><strong><code>match</code></strong></summary>

Applies the trained model to your full dataset and writes resolved clusters to the output location. This is the phase that produces deduplicated, entity-resolved output for a single dataset.

</details>

<details>

<summary><strong><code>link</code></strong></summary>

Like `match`, but for linking records across two or more separate datasets. Each output record carries a `Z_SOURCE` column indicating which source dataset it came from. Use when you want to find the same entity across datasets without merging them into one.

</details>

<details>

<summary><strong><code>runIncremental</code> (Enterprise)</strong></summary>

Updates the identity graph with new or changed records without retraining the model. Records that match an existing cluster inherit its Zingg ID. Records that do not match any existing cluster receive a new Zingg ID.

</details>

<details>

<summary><strong><code>explain</code> (Enterprise)</strong></summary>

Shows how a specific cluster was formed, which record pairs were compared, their similarity scores, and how transitive matching connected records through intermediate pairs. Used for governance, audit, and stakeholder review.

</details>

<details>

<summary><strong><code>generateDocs</code></strong></summary>

Produces HTML documentation of your labeled training pairs and model statistics. Useful for sharing with subject matter experts before training, or for audit and governance after.

</details>

<details>

<summary><strong><code>verifyBlocking</code></strong></summary>

Reports what percentage of your known matching pairs are being blocked together correctly. Run after labeling and before training to catch blocking issues early.

</details>

<details>

<summary><strong><code>recommend</code> (stopwords)</strong></summary>

Analyses a specified column and returns a list of high-frequency words that should be treated as stopwords. Optional phase used before training when address or company name fields contain repeated noise words like 'St', 'Ave', 'Inc', 'Ltd'.

</details>

### Data and Configuration

Learn how data is prepared and configured for matching.\
Explore the settings, inputs, and preprocessing concepts that determine how records enter and move through the matching pipeline.

<details>

<summary><strong>Pipes</strong></summary>

Zingg's abstraction for connecting to data. A pipe encapsulates the source or destination of records; a CSV file path, a Snowflake table, a BigQuery dataset, a Cassandra keyspace. `CsvPipe`, `SnowflakePipe`, `BigQueryPipe`, and the generic `Pipe` class are the main pipe types in Community. Enterprise adds `ECsvPipe` and `UCPipe`.

{% hint style="success" icon="right-long" %}
**Read more**: [Pipes and Data Connections](../connect-your-data/pipes-and-data-connections.md)
{% endhint %}

</details>

<details>

<summary><strong>Pass Through</strong></summary>

An Enterprise feature for records that are partially populated, outdated, or not suitable for matching, but which still need to appear in the output for governance or compliance purposes. `Pass Through` records are excluded from matching, linking, and incremental processes. Each pass through the record receives its own unique `Zingg ID` in the output.

_Common uses_: Records flagged as deceased, bot-generated accounts identifiable by a synthetic email pattern, or corrupted records that must be traceable but should not influence cluster formation.

{% hint style="success" icon="right-long" %}
**Read more:** [Pass Through](pass-through.md)
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
**Read more:** [Install Zingg](../running-zingg/install-zingg.md) | [Run on cloud](https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/platform-guides)
{% endhint %}

</details>

<details>

<summary><strong>Cloud infrastructure vs data platform</strong></summary>

Two things that are easy to conflate but mean different things in Zingg's context. Cloud infrastructure is where computation happens, such as the Spark service or Snowflake compute that processes your data. A data platform is where your data lives -Snowflake.\
Databricks, BigQuery, Redshift, or a file store like S3.

These are independent: you can have your data in Snowflake but run Zingg's computation on Spark. Zingg connects to the data platform and runs computations on whichever engine you configure.

{% hint style="success" icon="right-long" %}
**Read more:** [Connect data](https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/connect-your-data) | [Install Zingg](../running-zingg/install-zingg.md)
{% endhint %}

</details>

<details>

<summary><strong>Python API vs CLI</strong></summary>

The two ways to invoke Zingg. The Python API is the most widely adopted method; you call the Zingg phases as Python functions from a notebook or script. The CLI invokes Zingg phases from the command line using a JSON config file.

Both produce identical results; the choice is a developer preference based on your workflow. The Python API is the default method documented throughout these docs.

{% hint style="success" icon="right-long" %}
**Read more:** [Python API](https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/zingg-python-api) | [Zingg command line](../reference/zingg-command-line.md)
{% endhint %}

</details>
