---
description: >-
  Two paths, one engine. Choose based on where you are in your entity resolution
  journey.
---

# Community vs Enterprise

Every Zingg edition is built on the same foundation: probabilistic matching, active learning, and warehouse-native execution. Enterprise takes that further delivering faster, more accurate entity resolution with deterministic rules, explainability, and full model lifecycle control

What changes is the identity infrastructure, operational control, and advanced matching capability you obtain on top of it.

Community is the right place to start. Enterprise is where you go when identity resolution moves into production and needs to stay accurate, auditable, and stable over time.

<table><thead><tr><th width="148.5625" valign="top"></th><th valign="top">Community</th><th valign="top">Enterprise Lite</th><th valign="top">Enterprise</th></tr></thead><tbody><tr><td valign="top">Who it is for</td><td valign="top">Teams that are evaluating Zingg, running proofs of concept, or operating on self-managed Spark infrastructure.</td><td valign="top">Teams that are moving entity resolution into production and need persistent entity IDs and incremental updates.</td><td valign="top">Teams who need full lifecycle control: seamless model upgrades, governance, explainability, and dictionary-based matching for complex data.</td></tr><tr><td valign="top">Identity graph</td><td valign="top"><code>Z Cluster</code> is a unique cluster identifier assigned per run.</td><td valign="top">Persistent <code>Zingg ID</code> (GUID). Stable across runs and safe to reference in CRMs, warehouses, and pipelines.</td><td valign="top">Persistent <code>Zingg ID</code> plus seamless ID reassignment when you upgrade your model. Downstream systems stay intact across changes.</td></tr><tr><td valign="top">Matching capability</td><td valign="top">Probabilistic matching trained on your labeled pairs. Handles typos, abbreviations, and variations across any field type.</td><td valign="top">Probabilistic + deterministic matching in a single flow. Hard rules for trusted identifiers like SSN, tax ID, email. <code>Pass Through</code> for records that should appear in output but not influence matching.</td><td valign="top">Everything in Lite, plus dictionary-based matching (nicknames, aliases, company name variants). Standardize fields before and after matching. Match statistics and explainability for audit and governance.</td></tr><tr><td valign="top">Model operations</td><td valign="top">Train once and match as data arrives.</td><td valign="top">Incremental runs update the identity graph as new records arrive without retraining. Verify blocking before committing to training.</td><td valign="top">Everything in Lite, plus compare two trained models side by side before deploying. Upgrade models without disrupting <code>Zingg ID</code>s. Re-assign IDs from an old model to a new one.</td></tr><tr><td valign="top">Platform</td><td valign="top">Any Spark environment - Databricks, Fabric, EMR, GCP Dataproc, and local Spark.</td><td valign="top">All Community platforms, plus native Snowflake run (no Spark cluster required), Unity Catalog, and OneLake integration.</td><td valign="top">All platforms in Lite.</td></tr></tbody></table>

### When to choose each edition

_Start with Community._

You get the full probabilistic matching engine, active learning, and support for every major Spark platform. Community runs on Databricks, Fabric, EMR, GCP Dataproc, or a local Docker container.

You can match any entity type, like customers, patients, suppliers, products, and citizens, on your own data with no time limit and no cost.

_The limitation is operational:_ Community's `Z Cluster` is non-persistent. If you re-run the match job, cluster IDs may change. You cannot safely store them in downstream systems. That is the boundary where Community ends and Enterprise begins.

<details>

<summary><strong>We need to put entity resolution into production and keep it running reliably</strong></summary>

_Choose Enterprise Lite_.

The step from Community to Enterprise Lite is a step from evaluation to production. The core change is the `Zingg ID`: a globally unique, persistent `GUID` assigned to each resolved entity. It does not change between runs. Your CRM, data warehouse, and downstream pipelines can store it with confidence.

Enterprise Lite also gives you:

* [**Incremental matching**](../../running-zingg/run-incremental-matching.md) **-** update the identity graph as new records arrive, without re-running on your entire dataset
* [**Deterministic matching**](../entity-resolution/deterministic-vs-probabilistic-matching.md) **-** combine hard rules for known trusted identifiers (SSN, tax ID, email) with probabilistic ML in a single flow
* [**Pass Through**](../pass-through.md) **-** records that are incomplete or unsuitable for matching still appear in your output and receive a Zingg ID for governance and compliance
* [**Production-grade notebooks**](https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/platform-guides) **-** a 7-notebook sequence, one per workflow phase, built for repeatable production runs

Clients who move to Enterprise Lite are typically building Customer 360 views, patient master indexes, or supplier deduplication pipelines where the entity identifier needs to be stable and trusted downstream.

</details>

<details>

<summary><strong>We need to tune our model over time and handle complex data patterns</strong></summary>

_Choose Enterprise._

Enterprise is built for teams where entity resolution is a core data product, not a one-time project.

The additional capabilities in Enterprise address two things: complex data patterns and model lifecycle control.

**Complex data patterns**:

* Dictionary-based matching - match records where the same person appears under a nickname, short form, or alias (Jon / Jonathan, IBM / International Business Machines) using a user-supplied lookup file
* Standardise fields before matching - normalize address formats, name casing, and code fields before the match runs, for higher accuracy
* Standardize output - normalize field values in resolved output to a canonical form for clean golden records

**Model lifecycle control**:

* Compare model outputs - benchmark two trained models against each other before deciding which to deploy. See exactly which clusters changed, merged, or split.
* Re-assign Zingg IDs - when you upgrade your model, Zingg carries over existing IDs to the new model. Downstream systems stay intact. No ID disruption.
* Explainability - see which fields and scores drove each match decision for audit, governance, and stakeholder sign-off
* Match statistics - summary, cluster-level, and record-level breakdowns across every run

Enterprise clients are typically teams who run Zingg as a core data infrastructure component like MDM platforms, composable CDP pipelines, knowledge graphs, or compliance data products where model quality needs to be demonstrable and IDs need to survive model changes.

</details>

{% hint style="info" icon="right-long" %}
_Community → Enterprise_ is the move you make when you need persistent entity IDs, incremental updates, and matching that stays accurate as your data and models evolve.

Enterprise Lite is for production with stability. Enterprise is for production with full lifecycle control.

Ready to move to Enterprise?

* See [Migrating to Enterprise](migrating-from-community-to-enterprise-content-to-be-added.md) for the full upgrade path
* [Contact Us](https://www.zingg.ai/company/contact/contact)
{% endhint %}

{% hint style="success" icon="right-long" %}
**Read more**: Every record in Enterprise output receives a `Zingg_ID`, a GUID that persists across runs, incremental updates, and model upgrades.

This is the foundational difference from Community, where `Z_Cluster` is non-persistent and cannot be safely stored downstream.

* [Zingg ID](../concept-glossary.md#zingg-id)
* [Z Cluster and Zingg ID](../z-cluster-and-zingg-id.md)
{% endhint %}
