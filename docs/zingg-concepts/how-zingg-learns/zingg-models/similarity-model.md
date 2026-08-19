---
description: solving the accuracy problem
---

# Similarity Model



The similarity model evaluates each candidate pair that the blocking model passes through and produces a similarity score: a number between 0 and 1 reflecting how likely it is that the two records represent the same real-world entity.

It is a classifier, not a rules engine. For each pair, Zingg computes multiple features per field; character-level differences, string lengths, common transpositions, and prefix and suffix overlaps, and combines them into a single prediction. The threshold between match and no-match is automatically optimized. You do not set it manually.

The similarity model learns from your labeled pairs. Match labels show it what a true match looks like in your specific data. Non-match labels show it, what different entities look like\
even when their field values are similar.

This is why label quality matters more than label quantity. A well-chosen set of 30 to 50 match pairs, covering the variation patterns in your schema produces a more accurate model than\
a large set of casually labeled pairs.

{% hint style="success" icon="right-long" %}
For diagnosing similarity model behaviour and concept details → [Similarity Model](similarity-model.md)
{% endhint %}





