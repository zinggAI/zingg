---
description: >-
  How to inspect the similarity model, diagnose false positives and false
  negatives, and improve accuracy through targeted retraining.
---

# Similarity Model

The similarity model scores every candidate pair that the blocking model passes through. If your results contain records incorrectly merged into the same cluster (false positives) or matching records that were missed (false negatives), the similarity model is where to investigate after first confirming that blocking is not the cause.

{% hint style="success" icon="right-long" %}
Before debugging the similarity model, confirm the missed pairs are actually reaching it. If two records are in different blocks, the similarity model never evaluates them regardless of its accuracy.
{% endhint %}

### Diagnosing false positives records that should not be together

A false positive is a cluster that contains records representing different real-world entities. They look similar enough that the model merged them, but they should not be in the same cluster.

<details>

<summary><strong>How to identify the cause</strong></summary>

Look at the false positive cluster in your output. Ask:

Do the records share high values on some fields but clearly differ on fields that should be discriminating? For example, can you have the same first name and city but different dates of birth?

This scenario is almost always a training data issue. The model has not seen enough non-match-labeled pairs that look similar in some fields but differ in the discriminating ones. It has learned that similarity in those shared fields is enough for a match because you have not shown it the counter-examples.

**The fix**: Run `findTrainingData` again and find pairs that look like the false positive, similar on the misleading fields, different on the discriminating ones and label them as No Match. The similarity model will learn to use the discriminating fields correctly.

</details>

<details>

<summary><strong>If false positives are widespread, not isolated</strong></summary>

If false positives affect many clusters rather than a specific pattern, check your field match types before adding more training data.

Fields that should use `EXACT` but are set to `FUZZY` are the most common cause\
of widespread false positives. Date of birth, SSN, national ID, and tax IDs should always use `EXACT`. Fuzzy tolerance on these fields allows records with different values to score above the match threshold.

**Also check**: Are there any fields that should be `DONT_USE` contributing to the match?\
scores? Internal IDs and sequence numbers that happen to appear similar across records can inflate match scores incorrectly.

</details>

### Diagnosing false negatives matches that were missed

A false negative is a pair of records that represent the same entity but were not placed in the same cluster. The similarity model scored them below the match threshold or the blocking model never allowed them to be compared.

#### Confirm it as a similarity problem, not a blocking problem

Run `verifyBlocking`. If the missed pair is not being blocked together, the similarity model is not the cause. Address the blocking issue first by referring to the [Blocking Model](blocking-model.md) page.

If `verifyBlocking` confirms the pair is reaching the similarity model but still not matching, proceed to the training data fix below.

#### Add match labels for the missed pattern

Run `findTrainingData` and look for pairs similar to the ones being missed. Label them as `match`. The similarity model needs to see this pattern in the training data to learn it. If the variation that\
causes the mismatch like a specific abbreviation pattern, a missing field, or a transliteration not represented in your labeled pairs; the model has no basis for scoring it above the threshold.

A small number of targeted labels for the specific pattern being missed is more effective than a large general labeling run.

{% hint style="success" icon="right-long" %}
Ordering clusters by `avg_min` ascending puts the weakest clusters at the top, those are the ones worth reviewing for false positives first.

**Read more**:

* [Interpret Output Scores](../../../interpreting-results/interpret-output-scores.md) - `Z_MINSCORE` and `Z_MAXSCORE` explained
* [Label Training Pairs](../../../running-zingg/label-training-pairs.md) - adding training data for missed patterns
* [Verify blocking](../../../running-zingg/verify-blocking.md)
{% endhint %}

