---
description: >-
  What entity resolution is, why the problem is hard to solve at scale, and how
  Zingg handles each part of it systematically.
---

# Entity Resolution

Entity resolution is the process of identifying that multiple records across different systems represent the same real-world entity.

It answers one question: Given fragmented, inconsistent data spread across sales,\
marketing, support, and operations systems, whose records actually belong to the same\
customer, supplier, patient, or product?

This question is harder than it looks. The data does not tell you. The records do not share a common key. The same person appears as "Jon Smith" in your CRM, "Jonathan Smith" in your billing system, and "J. Smith" with a different address in your support platform. No field is wrong. No field is complete. And your downstream systems like analytics, compliance, and AI agents are making decisions on all three records as if they were three different people.

### Why this problem exists

Fragmented entity data is a structural consequence of how enterprise software is built. Each application owns its own records and its own identity model. There is no cross-system agreement on how to represent a person, a company, or a location.

Four forces drive the fragmentation:

* **Systems are built independently:** CRMs, billing platforms, ERP systems, and support tools each have their own customer or entity schema. A customer acquired through a marketing campaign enters the CRM. The same customer's invoice enters the billing system. Their support ticket enters a helpdesk. No system knows about the others. Each creates its own record.
* **Data entry is inconsistent:** Name abbreviations, spelling variations, transposed date formats, address shorthand—these are not data quality failures. They are the natural output of humans entering data under time pressure across different interfaces. "IBM" and "International Business Machines" are the same company. "Dr. A. Sharma" and "Anita Sharma" may be the same person. No rule-based system can enumerate all the ways real data varies.
* **Records change over time:** People move. Companies merge. Names change after marriage or acquisition. A customer record that was accurate two years ago now has a different address, a different email, and a different phone number—and it still needs to resolve to the same entity in your identity graph.
* **Scale makes manual resolution impossible:** At a few thousand records, you can resolve entities manually or with simple rules. With one million records, naive comparison necessitates evaluating 500 billion record pairs before making a single match decision. At ten million records, the number is 50 trillion.\
  No rules engine can scale to this. No team can review it manually. The problem requires a system that learns and one that reduces the comparison space before it starts.

### Why custom matching logic breaks down

Rule-based entity resolution fails for three reasons:

1. **Rules cannot enumerate variation**. The number of ways a name, address, or company identifier can vary is unbounded. For every rule you write, new variations appear in production data that the rule does not cover. Maintaining a ruleset for a live dataset is a permanent, open-ended engineering commitment.
2. **Rules cannot score confidence**. A rule fires, or it does not. It cannot tell you that two records are probably the same entity or that a cluster has a weak link worth human review. Entity resolution at production scale requires a graded confidence signal; not a binary match/no-match.
3. **Rules do not scale to the comparison space**. At one million records, the naive approach requires evaluating 500 billion record pairs. At ten million, it is 50 trillion. A rule engine applied to every pair is computationally impossible.

Zingg's ML model solves all three:

* It learns variation from your data - 30 to 50 labeled examples are enough to build a model that generalizes to patterns it has not seen before.
* It produces a graded confidence score (`Z_MINSCORE` and `Z_MAXSCORE`) per cluster, so you can route high-confidence matches to automated processing and low-confidence matches to human review.
* It uses a blocking model to cut down the comparison space from billions of pairs to a tiny fraction without losing recall, so the similarity model only checks candidate pairs that could plausibly match.

### How Zingg solves it

Zingg is an ML-powered entity resolution engine built to run where your data already lives directly on your warehouse or lakehouse, with no data movement and no rules to write or maintain.

Three capabilities work together to handle the full problem:

<table><thead><tr><th valign="top">Warehouse-native execution</th><th valign="top">Probabilistic + deterministic matching</th><th valign="top">Persistent identity graph</th></tr></thead><tbody><tr><td valign="top">Zingg runs inside Databricks, Microsoft<br>Fabric, Snowflake, GCP Dataproc, AWS Glue, and AWS EMR. Your data never<br>leaves your environment. No ETL pipelines. No external APIs. There is no separate infrastructure to operate. The same model that runs on 100,000 records scales to hundreds of millions using your existing Spark or Snowflake compute, without any architectural changes.</td><td valign="top"><p>Probabilistic matching is Zingg's default. The ML model learns from your labeled pairs; 30 to 50 examples are enough to build a model calibrated<br>to your specific data and scores every candidate pair on multiple field-level features. It handles<br>typos, abbreviations, missing values, and format variations automatically.</p><p>Deterministic matching (Enterprise) adds hard rules for trusted identifiers.<br>When two records share the same SSN, tax ID, or email, Zingg treats them<br>as the same entity without consulting the ML model. Both approaches run in a single flow.</p><p>→ <a href="deterministic-vs-probabilistic-matching.md">Deterministic vs Probabilistic<br>Matching</a></p></td><td valign="top"><p>Every resolved entity receives a <code>Zingg ID</code>, a globally unique, persistent GUID assigned in Enterprise that remains stable<br>across runs, incremental updates, and model changes. Community produces a <code>Z Cluster</code><br>that is reassigned each run.<br>Enterprise produces a <code>Zingg ID</code> you can store in downstream systems<br>with confidence.</p><p>The identity graph grows incrementally. New records are matched to existing clusters without rerunning on your full dataset.</p><p>→ <a href="../identity-graph.md">Identity Graph</a><br>→ <a href="../z-cluster-and-zingg-id.md">Z Cluster and Zingg ID</a></p></td></tr></tbody></table>

This is the combination that makes enterprise-scale entity resolution computationally feasible and practically maintainable.

When your data contains reliable unique identifiers, a national ID, an email, or a combination like first name plus date of birth Zingg Enterprise can apply deterministic rules on top of the probabilistic model. Pairs that satisfy a deterministic condition are resolved with a score of 1, without consulting the ML model. Pairs that do not satisfy any condition fall through to probabilistic matching as normal. Enterprise runs both in a single flow, so you do not have to choose between them.

{% hint style="success" icon="right-long" %}
**Read more**:

* [Deterministic vs Probabilistic Matching](deterministic-vs-probabilistic-matching.md)
* [How Zingg Learns](../how-zingg-learns/)
* [Zingg Models](../how-zingg-learns/zingg-models/) (blocking + similarity)
{% endhint %}

### Deduplication, linking, and resolving

These three operations are often confused. They are distinct in scope and complexity.

**DEDUPLICATION - within one dataset**

Deduplication identifies that two records within the same system represent the same entity. This is the most constrained version of the problem same schema, same\
source, single dataset.

Zingg's `match` phase does just that. You run it against one dataset. Every record that resolves to the same entity gets the same `Z Cluster` or `Zingg ID`.

**LINKING - across two datasets**

Linking identifies that a record in dataset A matches a record in dataset B, where each dataset is individually duplicate-free. Linking is required for reference data mastering, enrichment, and dataset joins when exact key matches do not exist.

Zingg's link phase does this. Same trained model, two input datasets. The output shows which records from each dataset represent the same entity.

**RESOLVING - across many systems at scale**

Full entity resolution identifies the same entity across multiple systems, with no shared key, inconsistent formats, partial data, and records that change over time. This is the general problem that Zingg is built to solve.

{% hint style="success" icon="right-long" %}
**Read more**:

* [Run the match phase](../../running-zingg/run-the-match-phase.md)
* [Link across datasets](../../running-zingg/link-across-datasets.md)
{% endhint %}

### Where teams use entity resolution

Entity resolution is not a customer data problem. It is an entity data problem. Any time a real-world object, a person, organisation, location, product, or asset appears under different\
representations across systems, entity resolution is the mechanism that unifies it.

<details>

<summary><strong>Customer 360</strong></summary>

Modern enterprises run on specialised applications, CRM, e-commerce, support,\
loyalty, and finance. Each system holds a partial view of the customer.

The goal of Customer 360 is not to delete duplicate records. It is to bring all interactions and data points for a customer together so you can understand the complete journey, enable accurate segmentation, and power downstream AI agents with a unified view.

Entity resolution is the step that makes such a scenario possible. Without it, every downstream model, campaign, and agent is working from a fragmented picture.

</details>

<details>

<summary><strong>Master Data Management (MDM)</strong></summary>

MDM is the discipline of creating and maintaining a single authoritative record, a golden record for each core business entity: customer, supplier, product, location, and employee.

Entity resolution is the part of every MDM system that finds out which records from different source systems are for the same entity before the golden record is created.

Zingg Community is a DIY MDM. You build the golden record layer on top of Zingg's match output in your data store of choice.

Zingg Enterprise is a warehouse-native MDM that uses `Zingg ID`s, has incremental updates, and supports survivorship, providing the complete identity infrastructure stack without needing a separate MDM platform.

</details>

<details>

<summary><strong>Composable CDPs</strong></summary>

A composable CDP is a customer data platform built from modular components on your existing data stack, rather than a monolithic vendor platform.

Entity resolution is the identity layer in a composable CDP architecture. Zingg resolves customer identities across all source systems and produces a persistent Zingg ID that becomes the customer identifier for the rest of the stack segmentation, activation,\
personalization, and AI agents.

Because Zingg runs inside your warehouse, it fits natively into Databricks, Fabric, or Snowflake-based composable architectures without a separate data movement layer.

</details>

<details>

<summary><strong>Knowledge Bases and Graph RAG</strong></summary>

Large language models and retrieval-augmented generation (RAG) pipelines retrieve knowledge from structured data sources. When those sources contain fragmented, inconsistent entity representations, the retrieval layer returns incomplete or conflicting\
context, and the LLM gives wrong or incomplete answers.

Entity resolution is the pre-processing step that unifies entity representations before they reach the knowledge base or vector store. Zingg clusters all records representing the same entity and assigns a single `Zingg ID`. The retrieval layer then finds all of them\
together, and the LLM reasons over a complete, entity-aware view.

This process is the Identity RAG pattern entity resolution as infrastructure for AI accuracy.

{% hint style="success" icon="right-long" %}
**Read more**: [Enhancing LLM applications with Zingg and LangChain](https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/recipes-and-integration)
{% endhint %}

</details>

<details>

<summary><strong>Fraud Detection</strong></summary>

Fraudulent actors create multiple accounts or profiles with small variations in identity fields to evade detection different email formats, transposed date of birth, and slight name variations.

Entity resolution identifies when records that look different actually belong to the same entity, surfacing hidden connections that threshold-based or rule-based systems miss.

Zingg's probabilistic matching is calibrated to detect these deliberate variations without generating false positives on legitimate name differences.

</details>

<details>

<summary><strong>KYC and AML</strong></summary>

Know Your Customer and Anti-Money Laundering workflows require matching incoming data against existing records, sanctions lists, and watchlists often across inconsistent name formats, transliterations, and missing fields.

Entity resolution handles these variations systematically, reducing false positives and ensuring compliance teams review the right cases rather than being overwhelmed by noise.

Zingg Enterprise's deterministic matching adds a hard-rule layer for trusted identifiers alongside probabilistic matching, which is exactly the combination KYC and AML workflows require: fuzzy matching for variations, exact matching for confirmed identifiers.

</details>

### Frequently Asked Questions

<details>

<summary><strong>Is entity resolution the same as deduplication?</strong></summary>

No, and this distinction matters. Deduplication removes exact or near-exact duplicate records within a single system. It is a special case of entity resolution where the\
Variations are minimal, and the scope is limited to a single dataset.

Entity resolution goes further. It identifies records that represent the same real-world entity even when they look significantly different, with variations in spelling, missing fields, different formats and across completely separate systems.

"John Smith" in the CRM, "J. Smith" in the billing tool, and "Jonathan Smith" in the support platform may be the same person. Deduplication would not catch this. Entity\
resolution does.

Zingg's commercial use cases, Customer 360, fraud, KYC, AML, MDM all require full entity resolution. This is why Zingg is positioned around entity resolution and identity\
resolution, not data deduplication.

</details>

<details>

<summary><strong>Can I do entity resolution with a graph database alone?</strong></summary>

Not if your data has fuzzy variation. Graph databases like Neo4j are excellent at representing and querying relationships between resolved entities. But they rely on trusted, high-quality identifiers like passport IDs, tax numbers, and exact match keys to define edges between records. When your data has typos, abbreviations, missing fields, or format variations, you cannot define those edges reliably without fuzzy matching first.

Zingg and graph databases work best together. Zingg does entity resolution, which means it finds out which records are for the same entity using ML-based probabilistic and deterministic matching. The graph database is used for downstream relationship analysis and inference in AML, KYC, Knowledge Graph, and Customer 360 scenarios.

{% hint style="success" icon="right-long" %}
**Read more**: [Connect graph databases](../../connect-your-data/connect-graph-databases-neo4j.md)
{% endhint %}

</details>

{% hint style="success" icon="right-long" %}
Ready to see entity resolution in action?

* [Run Zingg on your local machine](../../running-zingg/quick-start-docker.md) - full workflow in 30 minutes
{% endhint %}

{% hint style="warning" icon="align-justify" %}
Need entity resolution at production scale with persistent identity and incremental\
processing?

* [Talk to us about Enterprise](https://www.zingg.ai/company/contact/contact)
* [Community vs Enterprise](../community-vs-enterprise/)<br>
{% endhint %}
