---
title: ExplainOutput
parent: Step By Step Guide
description: Get pair-level traceability for clusters with the Explain phase.
---

# Explanation of Zingg Output (Explain Phase)

The Explain phase shows how a chosen cluster (Zingg ID) came together, surfacing the record pairs that contributed, whether matched directly or transitively.

Why it matters
- Traceability & Governance: See exactly how clusters were formed.
- Model Validation: Quickly check if results match expectations.
- Human-in-the-loop Reviews: Share pair-level evidence with domain experts.
- Operational Confidence: Build trust in production pipelines.

What you’ll see
- A clear, pair-level view `(pk1, pk2)` for the selected Zingg ID
- Concise, shareable insights that explain cluster composition

Use this phase when you want to audit clusters, investigate anomalies, present rationale to stakeholders, or refine your matching strategy with confidence.

---

## Prerequisites

Before running the Explain phase:
- You’ve completed matching/clustering and have outputs with Zingg IDs.
- You know the specific Zingg ID you want to explain.

---

## Configuration (explain.json)

Your Explain configuration references the original matching config and defines where to write the results.

```json
{
  "config": "path_to_original_matching_config/config.json",
  "explainOutput": [
    {
      "name": "outputexplain",
      "format": "csv",
      "props": {
        "location": "/tmp/zinggOutput_explain",
        "delimiter": ",",
        "header": true
      }
    }
  ]
}
```
---

## Quick Start

Run the Explain phase from your Zingg Enterprise checkout:

```bash
./scripts/zingg.sh --phase explain --zinggid <zingg-id> --conf <path-to-explain-config.json>
```

Example:

```bash
./scripts/zingg.sh --phase explain \
  --zinggid ea67d79a-56a7-4431-ab55-d08bb3c10e2e \
  --conf ./examples/febrl/explainConfig.json
```

Ready to audit a cluster? Plug in the Zingg ID and go.

---

## Expected Output

The Explain output contains the following columns:
- `pk1`: Primary key (record ID) of the first record in the pair
- `pk2`: Primary key (record ID) of the second record in the pair

---

[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to [Zingg Enterprise feature comparison](https://www.zingg.ai/product/zingg-entity-resolution-compare-versions) for individual tier features.