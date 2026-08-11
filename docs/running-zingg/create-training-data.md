---
description: >-
  Run findTrainingData to generate candidate pairs from your data for labeling.
  The first active step in building your Zingg model.
---

# Create Training Data

The `findTrainingData` phase prompts Zingg to search for edge cases in your data-record pairs that are informative for learning. Zingg selects these judiciously so that your labeling effort is minimized and models can be built quickly.

Run `findTrainingData` first, then run the `label` phase. Repeat this cycle until you have enough labeled pairs.

{% hint style="success" icon="right-long" %}
Zingg selects the most informative pairs from your data not random samples. Label until all field types and data variation patterns in your schema are represented. If accuracy needs improvement after the first match run, return to labeling and focus on patterns that are missing or underrepresented.
{% endhint %}

{% tabs %}
{% tab title="Community" %}
### Set label data sample size

```python
args.setLabelDataSampleSize(0.5)
```

### Run findTrainingData

```bash
./zingg.sh --phase findTrainingData --conf config.json
```

#### Using Python API

```python
options = ClientOptions([
    ClientOptions.PHASE,
    "findTrainingData"
])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

#### Using pre-existing training data

If you already have labeled pairs from a previous run or external source, you can supply them to Zingg using the `trainingSamples` attribute in your config.

Your training data must include:

* `z_cluster` - groups records together; uniquely identifies the group
* `z_isMatch` - `1` if the records match, `0` if they do not. Must be the same value for all records in the same `z_cluster` group.

#### Using JSON config

```json
{
  "trainingSamples": [
    {
      "name": "existingTraining",
      "format": "csv",
      "props": {
        "path": "/path/to/training.csv",
        "delimiter": ",",
        "header": "true"
      }
    }
  ]
}
```

{% hint style="success" icon="right-long" %}
Even when supplying pre-existing training data, it is advisable to run `findTrainingData` and `label` for a few rounds to tune Zingg with the patterns it needs to learn independently.
{% endhint %}
{% endtab %}

{% tab title="Enterprise" %}
### Set label data sample size

```python
args.setLabelDataSampleSize(0.5)
```

#### Python

```python
options = ClientOptions([
    ClientOptions.PHASE,
    "findTrainingData"
])
zingg = EZingg(args, options)
zingg.initAndExecute()
```

#### CLI

```bash
./zingg.sh --phase findTrainingData --conf config.json
```

Enterprise also provides the `findAndLabel` combined phase, which runs `findTrainingData` and `label` together in a single call. Use this for smaller datasets where `findTrainingData` completes quickly:

{% hint style="info" icon="right-long" %}
Candidate pairs are written to `zinggDir/modelId`. Run the label phase next to review and label these pairs.
{% endhint %}
{% endtab %}

{% tab title="Enterprise Snowflake" %}
**CHECK WITH SONAL ABOUT THIS TOPIC - NEEDS ENTIRELY DIFFERENT SET OF CONTENT TO BE DISCUSSED LATER.**
{% endtab %}
{% endtabs %}
