---
description: >-
  How Zingg builds a matching model from your data and why it handles scale,
  variation, and uncertainty in ways that rules-based approaches cannot.
---

# How Zingg Learns

{% hint style="success" icon="right-long" %}
**Read more**:

* For the step-by-step workflow you run to produce match results - [Step by step Guide](../../running-zingg/step-by-step-guide.md).
* For a deep dive on the two models Zingg builds internally - [Zingg Models](zingg-models/).
{% endhint %}

Most entity resolution ML tools give you two options: write rules by hand or load a pre-trained model and hope it generalizes to your data.

Zingg does neither. It learns a model from your data, calibrated to your specific field distributions, your specific variations, and your specific understanding of what a match means.

Zingg starts by scanning your dataset and selecting the most informative candidate pairs, edge cases, near misses, and ambiguous records where human judgment matters most. It does not ask you to label random samples. It is highly selective about which pairs it surfaces, choosing the ones that will teach it the most with the least labeling effort.

You label those pairs typically 30 to 50, as Match, No Match, or Uncertain. From those labels, Zingg builds two models: a blocking model that cuts down the comparison space to a tiny fraction of all possible pairs, and a similarity model that scores each remaining candidate pair with graded confidence.

The result is a model that understands your data. Not a generic algorithm applied to it.

#### The problem Zingg is built to solve and why it is harder than it looks

Entity resolution at scale is not a data quality problem. It is an algorithmic complexity problem.\
And it is one that most tools paper over rather than solve. Here is what you are actually dealing with.

<table><thead><tr><th width="202.53515625" valign="top">Problem</th><th>Description</th></tr></thead><tbody><tr><td valign="top"><strong>The N² comparison problem</strong></td><td><p>Naive entity resolution compares every record against every other record. With N records, that produces N × (N-1) / 2 comparisons.</p><ul><li>At 10,000 records: ~50 million comparisons.</li><li>At 1 million records: ~500 billion comparisons.</li><li>At 10 million records: ~50 trillion comparisons.</li></ul><p>No rules engine can evaluate 50 trillion pairs. No team can review them. And no cloud compute budget can sustain running that comparison at production cadence - weekly, daily, or on arrival of new records.</p><p>This is not a hardware problem. It is a structural one. The comparison space grows quadratically with dataset size, and linear scaling of compute cannot outrun quadratic growth.</p><p>Zingg's blocking model is the solution. Before any similarity comparison runs, the blocking model groups records into candidate buckets using learned field heuristics. Only records within the same bucket are ever compared. Typical Zingg comparison coverage is 0.05% to 1% of the full problem space - without losing the matching pairs that matter.</p></td></tr><tr><td valign="top"><strong>The variation problem - why rules always break</strong></td><td><p>Entities do not appear consistently across systems. "IBM", "I.B.M.", and "International Business Machines" are the same company. "Jon Smith" and "Jonathan Smith" may be the same person. "42 Main St" and "42 Main Street, Apt 3B" are likely the same address.</p><p>A rule-based approach requires you to enumerate every possible variation for each field. For names alone, the number of possible abbreviations, abbreviation types, and transliteration patterns is unbounded. For addresses, it is worse. For company names across geographies and time periods, it is not practically solvable with rules.</p><p>Zingg's similarity model handles variation by computing multiple field-level features, character differences, string lengths, common transpositions, prefixes, suffix overlaps, and combining them through a classifier trained on your labeled pairs.</p><p>The classifier learns the variation patterns in your specific data. It does not need you to enumerate them in advance.</p></td></tr><tr><td valign="top"><strong>The confidence problem - why binary match/no-match is not enough</strong></td><td><p>A rule either fires or it does not. It gives you a binary answer: match or no match.</p><p>Production entity resolution requires more than that. Some clusters are high confidence - strong matches across multiple fields that can go straight to automated golden record creation. Others are lower confidence plausible matches that a domain expert should review before merging. Others are borderline pairs that look similar but are probably different entities.</p><p>Without a graded confidence signal, every match decision has to be treated the same way. You either automate everything and accept errors or review everything manually and abandon the scale benefit entirely.</p><p>Zingg's similarity model produces a score between 0 and 1 for every candidate pair. That score drives <code>Z_MINSCORE</code> and <code>Z_MAXSCORE</code> on every cluster in your output - so you can route high-confidence clusters to automated processing and low-confidence clusters to a stewardship queue.</p><div data-gb-custom-block data-tag="hint" data-style="success" data-icon="right-long" class="hint hint-success"><p><strong>Read more</strong>:  <a href="../../interpreting-results/interpret-output-scores.md">Interpret Output Scores</a>.</p></div></td></tr><tr><td valign="top"><strong>The data drift problem - why a model trained once decays</strong></td><td><p>Data patterns change. New source systems are added. Naming conventions shift. A model trained on last year's data may perform poorly on this year's records, not because the model was wrong, but because the variation distribution in your data changed.</p><p>Rule-based systems have no mechanism to detect or adapt to these changes. You rewrite the rules manually when performance degrades, which means that someone has to notice the degradation first.</p><p>Zingg Enterprise's incremental flow detects when new records do not fit existing clusters cleanly and surfaces them for review. The compare model results feature (diff phase) lets you benchmark a retrained model against your current production model before deploying. So, you can see exactly what changed before the change goes live.</p><div data-gb-custom-block data-tag="hint" data-style="success" data-icon="right-long" class="hint hint-success"><p><strong>Read more</strong>:</p><ul><li><a href="../../running-zingg/run-incremental-matching.md">Run Incremental Matching</a></li><li><a href="../../running-zingg/compare-model-results.md">Compare Model Results</a></li></ul></div></td></tr><tr><td valign="top"><strong>The incremental new data problem</strong></td><td><p>Production entity resolution rarely sees a static dataset. New records arrive daily; sometimes hourly. The naive approach is to rerun the full match each time new data comes in. This breaks at scale for two reasons.</p><p></p><p>First, re-running the full match is computationally expensive. At one million records, a full match takes hours. Doing it every time a thousand new records arrive is not viable.</p><p>Second, every re-run produces fresh cluster IDs. Downstream systems that store the cluster ID as a customer or entity key break every time the match is re-run.</p><p></p><p>Zingg Enterprise's <code>runIncremental</code> phase handles this. New records are evaluated against the existing identity graph. Records matching an existing cluster inherit its Zingg ID. Records that do not match any existing cluster receive a new Zingg ID. The existing graph stays intact. Downstream systems keep working.   </p><div data-gb-custom-block data-tag="hint" data-style="success" data-icon="right-long" class="hint hint-success"><p><strong>Read more</strong>:</p><ul><li><a href="../../running-zingg/run-incremental-matching.md">Run Incremental Matching</a><br></li></ul></div></td></tr></tbody></table>

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

### The two models Zingg builds

When you run `train`, Zingg builds two separate models from your `labeled` pairs. Each solves a different part of the entity resolution problem.

{% hint style="success" icon="right-long" %}
The blocking model and the similarity model are covered in full on their own pages\
with task-level detail for inspection, tuning, and debugging.

* [Blocking model](zingg-models/blocking-model.md)
* [Similarity model](zingg-models/similarity-model.md)
* [Zingg Models](zingg-models/) (parent page)

The graph algorithm that turns pairwise match decisions into complete clusters is also covered in the Zingg Models section.
{% endhint %}

### Quick reference on which match type for which field

When you define your field configuration, you choose a match type for each field. Here is a starting point for the most common field types.

<table><thead><tr><th width="265.6796875">Field type</th><th>Recommended match type + note</th></tr></thead><tbody><tr><td>First name, last name,<br>company name</td><td><code>FUZZY</code> - handles spelling variations<br>and abbreviations. Use <code>MAPPING</code> (Enterprise) for known<br>alias or nickname lists.</td></tr><tr><td>Email address</td><td><code>EMAIL</code> - matches the portion before the <code>@</code> only, avoiding mismatches from different domains for the same person.</td></tr><tr><td>Date of birth, registration date</td><td><code>EXACT</code> - dates should not have fuzzy tolerance.</td></tr><tr><td>Postal code, ZIP code</td><td><code>PINCODE</code> - handles common format variants.</td></tr><tr><td>Street address (full line)</td><td><code>FUZZY</code> - or <code>ONLY_ALPHABETS_FUZZY</code> combined with <code>NUMERIC</code> for the street number as a separate field.</td></tr><tr><td>Street number, apartment number</td><td><code>NUMERIC</code> - extracts and compares the number portion only.</td></tr><tr><td>National ID, SSN, tax ID</td><td><code>EXACT</code> - trusted identifiers should never have fuzzy tolerance. Also consider deterministic matching<br>(Enterprise) for these fields.</td></tr><tr><td>Internal record ID (not used for matching)</td><td><code>DONT_USE</code> - appears in output but excluded from comparison.</td></tr><tr><td>Fields frequently null across source systems</td><td>Add <code>NULLS_MATCH_NOTHING</code> alongside the main match type.</td></tr><tr><td>Product descriptions, free-text notes</td><td><code>TEXT</code> - word overlap comparison for longer free-text fields.</td></tr></tbody></table>

{% hint style="success" icon="right-long" %}
**Read more:**

* For full field definition setup including the `fields`, `dataType`, and `stopWords` attributes - \
  [Configure Zingg](../../running-zingg/configure-zingg.md)
* For the complete match types reference with all 12 types - [Match Types](match-types/)
{% endhint %}
