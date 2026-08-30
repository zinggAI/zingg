---
description: >-
  Two paths, one engine. Choose based on where you are in your entity resolution
  journey.
---

# Community vs Enterprise

Every Zingg edition is built on the same foundation: probabilistic matching, active learning, and warehouse-native execution. What changes is the identity infrastructure, operational control, and advanced matching capability you obtain on top of it.

|  | Community | Enterprise Lite | Enterprise |
|---|---|---|---|
| Who it is for | Teams that are evaluating Zingg, running proofs of concept, or do not need a persistent identity graph with continously updated data. | Teams that want to run entity resolution into production and need persistent entity IDs, deterministic matching and incremental updates. | Teams who need full lifecycle control: seamless model upgrades, governance, explainability, and dictionary-based matching for complex data. |
| Identity graph | `Z Cluster` is a unique cluster identifier assigned per run. | Persistent `Zingg ID` (GUID). Stable across runs and safe to reference in CRMs, warehouses, and pipelines. | Persistent `Zingg ID` plus seamless ID reassignment when you upgrade your model. Downstream systems stay intact across changes. |
| Matching capability | Probabilistic matching trained on your labeled pairs. Handles typos, abbreviations, and variations across any field type. | Probabilistic + deterministic matching in a single flow. Hard rules for trusted identifiers like SSN, tax ID, email. `Pass Through` for records that should appear in output but not influence matching. | Everything in Lite, plus dictionary-based matching (nicknames, aliases, company name variants). Match statistics and explainability for audit and governance. |
| Model operations | Train once and match as data arrives. | * Incremental runs update the identity graph as new records arrive without retraining. * Verify blocking before committing to matching. | Everything in Lite, plus * Upgrade models without disrupting `Zingg ID`s. * Compare two trained models side by side before deploying. * Re-assign IDs from an old model to a new one. |
| Platform | Any Spark environment - Databricks, Fabric, EMR, GCP Dataproc, and local Spark. | All Community platforms plus Native Snowflake run (no Spark cluster required) | All platforms in Lite. |

### When to choose each edition

Community is the right place to start for many teams. You get the full probabilistic matching engine, active learning, and support for every major Spark platform. Community runs on Databricks, Fabric, AWS Glue, AWS EMR, GCP Dataproc, or a Docker container.

You can match any entity type, like customers, patients, suppliers, products, and citizens, on your own data with no record limit and no cost.

_The limitation is operational:_ Community's `Z Cluster` is non-persistent. If you re-run the match job, cluster IDs may change. You cannot safely store them in downstream systems. That is the boundary where Community ends and Enterprise begins.\
\
_&#x54;he limitation is also in the resolution:_ Compared to any other, Zingg Community is the most powerful entity resolution product on the planet. Zingg Enterprise is even more powerful.

<details>

<summary><strong>We need to put entity resolution into production and keep it running reliably</strong></summary>

_Choose Enterprise Lite_.

The step from Community to Enterprise Lite is a step from evaluation to production. The core change is the `Zingg ID`: a globally unique, persistent `GUID` assigned to each resolved entity. It does not change between runs. Your CRM, data warehouse, and downstream pipelines can store it with confidence.

Enterprise Lite also gives you:

* [**Incremental matching**](../../running-zingg/run-incremental-matching.md) **-** update the identity graph as new records arrive, without re-running on your entire dataset
* [**Deterministic matching**](../../zingg-concepts/zingg-entity-resolution-platform/deterministic-vs-probabilistic-matching.md) **-** combine hard rules for known trusted identifiers (SSN, tax ID, email) with probabilistic ML in a single flow
* [**Pass Through**](../../running-zingg/pass-through.md) **-** records that are incomplete or unsuitable for matching still appear in your output and receive a Zingg ID for governance and compliance
* [**Production-grade notebooks**](../../platform-guides/platform-guide-for-azure-databricks.md) **-** a 7-notebook sequence, one per workflow phase, built for repeatable production runs

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

* [Zingg ID](../../frequently-asked-questions/concept-glossary.md#zingg-id)
* [Z Cluster and Zingg ID](../../zingg-concepts/z-cluster-and-zingg-id.md)
{% endhint %}
