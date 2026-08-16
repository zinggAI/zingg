---
description: Why custom matching logic breaks down
---

# Rule based entity resolution

Rule-based entity resolution fails for three reasons:

1. **Rules cannot enumerate variation**. The number of ways a name, address, or company identifier can vary is unbounded. For every rule you write, new variations appear in production data that the rule does not cover. Maintaining a ruleset for a live dataset is a permanent, open-ended engineering commitment.
2. **Rules cannot score confidence**. A rule fires, or it does not. It cannot tell you that two records are probably the same entity or that a cluster has a weak link worth human review. Entity resolution at production scale requires a graded confidence signal; not a binary match/no-match.
3. **Rules do not scale to the comparison space**. At one million records, the naive approach requires evaluating 500 billion record pairs. At ten million, it is 50 trillion. A rule engine applied to every pair is computationally impossible.

Zingg's ML model solves all three:

* It learns variation from your data - roughly 30 - 50 labeled examples are enough to build a model that generalizes to patterns it has not seen before.
* It produces a graded confidence score (`Z_MINSCORE` and `Z_MAXSCORE`) per cluster, so you can route high-confidence matches to automated processing and low-confidence matches to human review.
* It learns a blocking model to cut down the comparison space from billions of pairs to a tiny fraction without losing recall, so the similarity model only checks candidate pairs that could plausibly match.
