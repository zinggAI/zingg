---
description: >-
  Manually review and approve or reject matched clusters before committing them
  to the identity graph.
tags:
  - ent
---

# Cluster Approval

{% hint style="info" icon="right-long" %}
Enterprise only. Manual review and sign-off of matched clusters before committing to the identity graph. Approved decisions are preserved across incremental runs and never overridden.
{% endhint %}

After Zingg runs the match or incremental phase, some clusters may need human review before they are committed to the identity graph. Cluster approval gives data stewards and domain experts the ability to review Zingg's match decisions, approve the ones that are correct, and reject the ones that are wrong.

Approved and rejected decisions are stored by Zingg and preserved across subsequent incremental runs. Zingg will not override a human-approved or human-rejected cluster decision\
in future runs, the human decision always takes precedence.

_**CHECK WITH SONAL—The cluster approval page currently shows "Coming Soon" on the live docs with no technical content. All content above is written from KT notes and the incremental page context. Please review and confirm**_

### When to use cluster approval

Records are high-stakes, and errors have downstream consequences. For example, a financial compliance system that links counterparties to sanctions lists or a KYC system where a false match could affect a customer's account.

Subject matter experts have domain knowledge that Zingg's model does not. For example, two companies with similar names that are actually separate legal entities, or two individuals who share a name and address but are different people.

You need an audit trail of human decisions for governance or regulatory purposes.

### How cluster approval works

1. Run the match or incremental phase to generate clusters.
2. Review the clusters flagged for approval; either all clusters or a subset based on score thresholds or cluster size.
3. For each cluster, decide: Approve (the records are correctly grouped) or Reject (the records should not be in the same cluster).
4. Approved clusters are committed to the identity graph with their Zingg IDs. Rejected clusters are split, and their records are treated as separate entities.
5. In subsequent incremental runs, Zingg respects your approval decisions. A record that was manually approved as part of a cluster will not be removed from that cluster in a future run.

### Effect on the identity graph

* **Approved clusters**: The Zingg ID is locked to those records. Future incremental runs will keep a manually approved Zingg ID assigned unless a human explicitly rejects it.
* **Rejected clusters**: The records that were incorrectly grouped are separated. They may receive new Zingg IDs in the next incremental run if they match other clusters or remain as independent entities with their own IDs.

This makes cluster approval the governance layer on top of Zingg's automated entity resolution - human decisions take precedence over model decisions.
