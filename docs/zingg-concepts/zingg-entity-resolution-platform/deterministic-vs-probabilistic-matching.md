---
description: >-
  How Zingg's two matching approaches work individually and collectively, when
  to use each
tags:
  - ent
---

# Deterministic vs Probabilistic Matching

Zingg supports two matching approaches: probabilistic and deterministic. Both are explained on this page. Zingg Enterprise runs them together in a single flow. Probabilistic handles variations and uncertainty; deterministic handles trusted identifiers that should always produce an exact match.

### Probabilistic matching

Probabilistic matching is Zingg's default and is available in all editions. The model learns field-level similarity weights from your labeled training pairs and assigns a match score to every candidate pair. Pairs above the automatically optimized threshold are grouped into a cluster.

This approach handles the full range of real-world data quality problems: typos, abbreviations, missing fields, name variations, and format differences across systems.

_You do not write rules. You label examples. Zingg learns the rest._

### Deterministic matching

Deterministic matching is only available in the Enterprise Edition. It lets you define hard rules for cases where certain field combinations should always produce a match, regardless of the probabilistic score. When your data contains reliable unique identifiers, a national ID, an email, or a combination like first name, email plus date of birth, determinstic matching can bring them together. Zingg Enterprise can apply any combination of deterministic rules like (ssn) or (email and dob) or (passport number and dob). 

### How Zingg matches probabilistically AND deterministically

Zingg applies deterministic rules first. If two records share the same combination of identifiers you specify, Zingg treats that pair as a match with a score of 1, bypassing the ML model entirely for that pair. Pairs resolved deterministically are not re-evaluated probabilistically. Pairs that do not satisfy any deterministic condition fall through to the probabilistic model. All matching pairs get rolled into clusters.

Probabilistic and deterministic matching in Zingg Enterprise runs both in a single flow, so you do not have to choose between them.



