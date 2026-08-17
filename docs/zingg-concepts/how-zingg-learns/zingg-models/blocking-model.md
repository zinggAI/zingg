---
description: Solving the scale problem
---

# Blocking Model

The fundamental challenge of entity resolution at scale is not accuracy. It is the size of the comparison space.

Comparing every record against every other record produces `N × (N-1) / 2` comparisons, a number that grows quadratically with dataset size. At one million records, that is 500 billion comparisons. At ten million records, it is 50 trillion. No system can evaluate 50 trillion pairs at production cadence.

The blocking model is Zingg's approach to making entity resolution scalable. Before any similarity comparison runs, the blocking model groups records into candidate buckets using field heuristics it learns from your training data. This approach finds the best possible reduction in comparisons completely tailored to your data, while ensuring results are highly accurate. Typical Zingg blocking reduces the comparison space to 0.05% to 1% of all possible pairs, without losing the pairs that matter.\
\
Only records within the same bucket are ever compared against each other. Records in different buckets are never compared, which means the blocking model is the first and most\
consequential filter in the pipeline.





{% hint style="success" icon="right-long" %}
The blocking model learns from your labeled training pairs, the same training data that teaches the\
similarity model. Better training data improves both models.

For diagnosing blocking coverage and concept details → [Blocking Model](blocking-model.md)
{% endhint %}
