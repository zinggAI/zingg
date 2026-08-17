---
description: Yes, no, cant say is all it takes
---

# Zingg Active Learning

### The active learning loop

Zingg does not require thousands of pre-labeled training examples. It uses an active learning approach: it finds the record pairs where it is most uncertain, presents them to you, and learns from your labels.

The loop is:

{% stepper %}
{% step %}
### Step 1: `findTrainingData`

Zingg scans your dataset and selects a set of candidate record pairs specifically chosen because they are the most uncertain and therefore the most informative for the model to learn from. Not random samples. The pairs are most likely to improve the model.
{% endstep %}

{% step %}
### Step 2: `label`

You review each pair and mark it as Match, No Match, or Uncertain. This is the only step in the Zingg workflow that requires human input. No ML knowledge needed; only\
domain understanding of whether two records represent the same real-world entity.
{% endstep %}

{% step %}
### Step 3: Repeat steps 1 and 2

Run `findTrainingData` and `label` again. Zingg surfaces a new set of candidate pairs; it is still uncertain about after learning from your previous labels. You repeat this loop until the pairs being surfaced align with your expectations.

For most datasets, 30 to 50 labeled `match` pairs is a good starting point. The goal is not a specific count; it is coverage. Label until every field type in your schema is represented in your training data.
{% endstep %}

{% step %}
### Step 4: `train`

Once you are satisfied with your labeled pairs, run train. Zingg builds the blocking model and the similarity model from your labels. This step runs once per training cycle.
{% endstep %}
{% endstepper %}

{% hint style="success" icon="right-long" %}
`findTrainingData` and `label` run multiple times in a loop before train ever runs.

`train` runs once, after you have enough labeled pairs.
{% endhint %}

### Why 30 to 50 labels are enough, and when to do more

Standard supervised ML requires thousands or tens of thousands of labeled examples to reach\
production accuracy. Active learning requires far less because the labels are chosen, not random.

Every pair of Zingg surfaces for labeling is specifically selected because it is uncertain given what the model already knows. Each label you add is maximally informative. There is no noise from irrelevant or redundant examples.

The practical implication: a well-chosen set of 30 to 50 match labels produces a similarity model that generalizes to your full dataset.

More labels improve accuracy, but the return diminishes quickly. The more important variable is coverage. `Label` until your training set includes examples of all the variation patterns in your data - different name formats, address abbreviations, missing fields; not until you reach a specific number.

If match performance needs improvement after your first run, return to the label loop. Focus on the patterns your results show are missing or underperforming.
