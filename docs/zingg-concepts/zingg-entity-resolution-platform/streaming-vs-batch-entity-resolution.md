---
description: Two architectures, one problem
---

# Streaming vs Batch Entity Resolution

Entity resolution is a problem every organization with more than one data source eventually runs into. What differs is _when_ you need the answer. That single question, more than data volume or record count, is what should decide whether you reach for batch entity resolution, streaming entity resolution, or both.

This page explains why Zingg supports both architectures natively, what each is actually for, and how to decide which one (or which combination) fits your system.

### The core distinction: when is identity needed?

Entity resolution always answers the same question — "is this the same entity I've seen before?" — but systems ask that question at different points in time:

* **Before a decision is made**, as part of a scheduled process: nightly customer master builds, weekly deduplication runs, periodic golden record refreshes.
* **At the moment a decision is made**, inline with a live event: a fraud check during checkout, a personalization decision on page load, a real-time risk score before a transaction clears.

Batch entity resolution is built for the first case. Streaming entity resolution is built for the second. Neither is a more evolved version of the other — they're solving for different constraints.

### Batch entity resolution: the system of record

Batch ER processes a full dataset (or a large incremental slice of one) at once, producing a consolidated, deduplicated, high-confidence view of entities. It is the right architecture when:

* **The output is a system of record**, like a master customer, patient, or product dataset that downstream systems depend on being stable and correct.
* **You need full-dataset context.** Batch runs can consider every record against every other record, using the entire history to resolve ambiguous matches that a narrow, real-time window couldn't confidently resolve.
* **Latency is measured in hours or days, not milliseconds**, and correctness matters more than immediacy — MDM, CDP identity spines, compliance and reporting datasets, analytics-ready customer 360 views.

Batch is the foundation most entity resolution and MDM programs are built on, and it remains the right architecture for the majority of identity workloads today.

### Streaming entity resolution: identity at the moment of decision (Enterprise Only)

Streaming ER resolves identity against individual events as they arrive, typically within milliseconds, so that a decision being made _right now_ can use a trusted identity instead of a raw, unresolved record.

This matters because a growing set of use cases can't wait for the next batch cycle:

* **Real-time fraud and risk scoring**, where the decision to approve or block happens in the same request that the event arrives in.
* **In-session personalization**, where knowing "this is the same customer who abandoned a cart an hour ago" only has value if it's known before the session ends.
* **Event-driven architectures** (built on Kafka, Confluent, or similar streaming platforms) where identity resolution needs to be a stage in the pipeline itself, not a downstream batch job the pipeline waits on.
* **Operational systems that act on events**, not just report on them — where an unresolved or duplicate identity at decision time means a wrong action taken, not just a dirty report generated later.

The requirement underneath all of these is the same: **real-time decisioning requires real-time trusted identity.** A batch-resolved golden record that's accurate as of last night's run doesn't help a system that has to act in the next 50 milliseconds.

### Why not just make batch faster?

It's tempting to think streaming ER is just batch ER running on a shorter schedule. It isn't, for a structural reason: architectures are built differently in data platforms. 

Streaming entity resolution is different engineering problem (stateful stream processing, incremental identity graphs) rather than a smaller version of the same one.

### Why not real time?

Warehouses and datalakes are still not ready for real time design patterns. While there is promising movement towards LTAP/HTAP with Lakebase and Postgres suppoer in Snowflake, we are watching this space keenly and will build something when the technology is right. Going out of the warehouse and supporting real time is one option, but that becomes a separate source of truth with its own ETL and governance. Hence it gets limited to single departments and fails to become the universal source of truth.  

### Choosing an architecture in Zingg

Zingg supports both batch and streaming entity resolution natively, so the choice is driven by your use case rather than by platform limitations:

* If your pipelines run batch, **use batch**.
* If a decision is being made at the moment an event arrives and needs a trusted identity to act on — **use streaming**.

####
