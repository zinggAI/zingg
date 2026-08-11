---
description: >-
  Merge match results from separate Zingg jobs into a single unified identity
  graph.
tags:
  - ent
---

# Combine Match Models

In some architectures, you run multiple separate Zingg match jobs on different datasets, different time windows, or with different configurations, and then need to merge the results into a single unified identity graph. Combining match models is a **configuration-based approach**, not a separate phase. You define a graph config JSON with vertices (your data sources and match outputs) and edges (how they connect).

In the resulting graph, Zingg can relate different match models together. For example: a source system that only contains user IDs and email, another with user names and phone numbers, and a third with full person addresses, each with its own Zingg match model. Combining them produces one unified identity graph that spans all three models.

This process is different from comparing model results (`diff`), which evaluates two models against each other for quality assessment before deployment..

### When to use Combine match models

<details>

<summary><strong>Matching across multiple time-partitioned datasets</strong></summary>

You have data partitioned by month or quarter. You run a separate Zingg job on each partition and then combine the results into a single identity graph covering all time\
periods.

</details>

<details>

<summary><strong>Combining results from different source systems</strong></summary>

You run one Zingg job on CRM records and a separate job on transaction data. Each job produces its own `Z_CLUSTER` output. Combine match models merge these into a single unified cluster space.

</details>

<details>

<summary><strong>Ensemble matching</strong></summary>

You run multiple Zingg jobs with different configurations; for example, one optimized for high precision and one for high recall, and combine the results to get the benefit of both approaches.

</details>

### How it works

The combination is defined in a JSON config file with two top-level arrays: `vertices` and `output`. Each vertex represents either a direct data query (`zingg_pipe`) or the output of a Zingg match job (`zingg_match`). Edges define how entities from different vertices connect.

**Enterprise only.** The graph combination is a Zingg Enterprise feature. It requires Zingg Enterprise or Enterprise Plus.

### Graph config structure

```json
{
  "vertices": [
    {
      "name": "spouse",
      "vertexType": "zingg_pipe",
      "data": [
        {
          "name": "spouse",
          "format": "snowflake",
          "props": {
            "query": "select a.id as id, a.FNAME, a.LNAME, a.STNO, a.ADD1, a.CITY, a.STATE, a.ZINGG_ID_PERSON, b.id as z_id, b.fname as Z_FNAME, b.lname as Z_LNAME, b.stno as Z_STNO, b.add1 as Z_ADD1, b.city as Z_CITY, b.state as Z_STATE, b.ZINGG_ID_PERSON as Z_ZINGG_ID_PERSON from CUSTOMER_RELATE_PARTIAL a, CUSTOMER_RELATE_PARTIAL b where a.familyId = b.familyId"
          }
        }
      ],
      "edges": {
        "edgeType": "same_edge",
        "edges": [
          {
            "dataColumn": "zingg_personId",
            "column": "zingg_personId",
            "name": "zingg_personId1"
          },
          {
            "dataColumn": "zingg_personId",
            "column": "z_zingg_personId",
            "name": "zingg_personId2"
          }
        ]
      }
    },
    {
      "name": "household",
      "config": "$ZINGG_ENTERPRISE_HOME$/zinggEnterprise/configHousehold.json",
      "strategy": {
        "vDataStrategy": "unique_edge",
        "props": {
          "column": "zingg_personId",
          "edge": "zingg_personId,z_zingg_personId"
        }
      },
      "vertexType": "zingg_match",
      "edges": {
        "edgeType": "same_edge",
        "edges": [
          {
            "dataColumn": "zingg_personId",
            "column": "zingg_personId",
            "name": "zingg_personId1"
          },
          {
            "dataColumn": "zingg_personId",
            "column": "z_zingg_personId",
            "name": "zingg_personId2"
          }
        ]
      }
    }
  ],
  "output": [
    {
      "name": "relatedCustomers",
      "format": "snowflake",
      "props": {
        "table": "RELATED_CUSTOMERS_PARTIAL"
      }
    }
  ],
  "strategy": "pairs_and_vertices"
}
```

#### **Understanding the config fields**

* `vertexType: 'zingg_pipe'` - a direct data query. The `data` array specifies the source. Use this for exact-match relationships (e.g. shared family ID).
* `vertexType: 'zingg_match'` - the output of a full Zingg match model. The `config` field points to the model's config file.
* `edgeType: 'same_edge'` - connects records that represent the same entity across vertices.
* `strategy: 'pairs_and_vertices'` - the graph combination strategy. Determines how overlapping entity assignments are resolved.

#### PYTHON API

_**CHECK WITH SONAL \_ TEAM - Python API for combining match models is not documented in the reference PDF. Sonal to confirm before adding.**_
