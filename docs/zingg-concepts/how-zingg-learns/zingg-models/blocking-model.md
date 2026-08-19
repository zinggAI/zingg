---
description: Solving the scale problem by learning from the data directly
---

# Blocking Model

The fundamental challenge of entity resolution at scale is not accuracy. It is the size of the comparison space.

Comparing every record against every other record produces `N × (N-1) / 2` comparisons, a number that grows quadratically with dataset size. At one million records, that is 500 billion comparisons. At ten million records, it is 50 trillion. No system can evaluate 50 trillion pairs at production cadence.

The blocking model is Zingg's approach to making entity resolution scalable. Before any similarity comparison runs, the blocking model groups records into candidate buckets using field heuristics it learns from your training data. This approach finds the best possible reduction in comparisons completely tailored to your data, while ensuring results are highly accurate. Typical Zingg blocking reduces the comparison space to 0.05% to 1% of all possible pairs, without losing the pairs that matter.\
\
Only records within the same bucket are ever compared against each other. Records in different buckets are never compared, which means the blocking model is the first and most\
consequential filter in the pipeline.

### Blocking functions

The blocking model is a purpose built entity resolution micro clustering model, comprising of a tree with hash functions. Blocking functions control which records are compared. The blocking tree learns on the matched records you provided during labeling; at every node, Zingg selects the function and the field that produces the least elimination of your known matching pairs.

**What makes a good blocking function:**

A good blocking function never eliminates a matching pair entirely. It groups records that could be the same entity, even imperfectly; so the similarity model can evaluate them. A poor blocking function eliminates matching pairs from comparison entirely; no similarity scoring happens after that point.

**Example: evaluating `first1char` on `firstname`**

Take two labeled matching pairs:

| 1 | A | john  | j |
| - | - | ----- | - |
| 1 | B | johnh | j |
| 2 | A | mary  | m |
| 2 | B | marry | m |

Both pairs produce the same output from `first1char` - no elimination. This is a good function for `firstname`.

**Contrast: `last1char` on `firstname`**

| **1** | A | john  | n |
| ----- | - | ----- | - |
| 1     | B | johnh | h |
| 1     | A | mary  | y |
| 2     | B | marry | y |

Pair 1 is eliminated (`n` ≠ `h`). `last1char` is not a good function for `firstname`. Zingg will therefore not choose it.

So `first1char(firstname)` will be selected. It brings near-similar records together - clustering them to break the cartesian join.

The good part is that the user does not have to think about these constructs at all. During active learning, the model is automatically learnt based on what the user labels.

{% hint style="success" icon="right-long" %}
The blocking model learns from your labeled training pairs, the same training data that teaches the\
similarity model. Better training data improves both models.

For diagnosing blocking coverage and concept details → [Blocking Model](blocking-model.md)
{% endhint %}
