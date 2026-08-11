---
description: >-
  Approaches for tuning your Zingg model when match results are not accurate
  enough.
---

# Improve Accuracy

When your Zingg match results contain false positives (records incorrectly grouped together) or false negatives (matching records not found), there are several ways to improve accuracy\
without starting over.

Start with the approaches in this order; each is less effort than the next.

### Step 1: Add more training data

The most reliable way to improve accuracy is to label more pairs. Return to the `label` phase, run `findTrainingData` again, and focus on labeling the types of pairs that are causing problems.

* If you have too many false positives (records incorrectly merged), add more non-match labels for pairs that look similar but are different entities.
* If you have too many false negatives (matches being missed), add more match labels for pairs that look different but are the same entity.

{% hint style="success" icon="right-long" %}
**Read more**:

* Label training pairs - [Label training pairs](../../running-zingg/label-training-pairs.md)&#x20;
* Create training data - [Create training data ](../../running-zingg/create-training-data.md)
{% endhint %}

### Step 2: Check your field match types

Review your `FieldDefinition` match types. Common mismatches:

* Using `FUZZY` for a field that should be `EXACT` - for example, SSN or national ID numbers should always use EXACT. Using FUZZY allows near matches that produce false positives.
* Using `EXACT` for a field that has variations - for example, address fields almost always have variations and should use FUZZY.
* Using `DONT_USE` for a field that actually carries strong identity signal - adding it back with the right match type can improve recall

{% hint style="success" icon="right-long" %}
**Read more**: Match types reference - [Match types](../../zingg-concepts/how-zingg-learns/match-types/)
{% endhint %}

### Step 3: Remove stopwords

Common words like Mr, St, Street, Pvt add noise to fuzzy matching on address and name fields. Running the stopwords removal step reduces this noise and can improve both precision and recall.

{% hint style="success" icon="right-long" %}
**Read more**: Remove stopwords - [Remove stopwords](remove-stopwords-optional.md)
{% endhint %}

### Step 4: Verify blocking

If Zingg is missing matches that you know exist (false negatives), the blocking model may be filtering them out before they even reach the similarity model. Run Verify Blocking to inspect which pairs are being blocked and identify coverage gaps.

{% hint style="success" icon="right-long" %}
**Read more**: Verify blocking - [Verify blocking](../../running-zingg/verify-blocking.md)
{% endhint %}

### Step 5: Custom blocking and similarity

For advanced use cases, you can define your own blocking and similarity functions to replace\
or extend Zingg's built-in approaches. See [Custom Blocking and Similarity](../custom-blocking-and-similarity.md) for advanced tuning approaches.

