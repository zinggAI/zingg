---
description: >-
  Use a Zingg pre-trained model to start matching without going through the
  findTrainingData and label phases.
---

# Pre-trained Models

Zingg ships with a pre-trained model built on the `FEBRL` dataset; for example, a synthetic Australian population dataset with name, address, date of birth, and SSN fields. The `FEBRL` pre-trained model is bundled in the Zingg Docker image and available from the Zingg GitHub\
repository.

Use a pre-trained model when you want to run a quick proof of concept on data that matches the `FEBRL` schema, or as a starting point that you fine-tune with your own labels.

### Using the FEBRL pre-trained model

#### Step 1: Download the pre-trained model

The FEBRL pre-trained model is bundled in the Zingg Docker image. It is available in the models/ directory inside the container.

```bash
docker pull zingg/zingg:0.5.0
docker run -it zingg/zingg:0.5.0 bash
```

Inside the container, models are at:

```
/zingg/models/
```

{% hint style="success" icon="right-long" %}
Pre-trained models are also available from the Zingg GitHub releases page: `github.com/zinggAI/zingg/releases`
{% endhint %}

#### Step 2: Use the pre-trained model in your config

```json
{
  "fieldDefinition": [
    {
      "fieldName": "fname",
      "matchType": "fuzzy",
      "fields": "fname",
      "dataType": "string"
    },
    {
      "fieldName": "lname",
      "matchType": "fuzzy",
      "fields": "lname",
      "dataType": "string"
    },
    {
      "fieldName": "dob",
      "matchType": "exact",
      "fields": "dob",
      "dataType": "string"
    },
    {
      "fieldName": "ssn",
      "matchType": "exact",
      "fields": "ssn",
      "dataType": "string"
    }
  ],
  "data": [
    {
      "name": "yourInput",
      "format": "csv",
      "props": {
        "location": "path/to/your/data.csv",
        "delimiter": ",",
        "header": "true"
      }
    }
  ],
  "output": [
    {
      "name": "yourOutput",
      "format": "csv",
      "props": {
        "location": "/tmp/zinggOutput"
      }
    }
  ],
  "modelId": "100",
  "zinggDir": "models",
  "labelDataSampleSize": 0.5,
  "numPartitions": 4
}
```

#### Step 3: Run match directly (no training needed)

**Python**

```python
options = ClientOptions([ClientOptions.PHASE, "match"])
zingg = Zingg(args, options)
zingg.initAndExecute()
```

**CLI**

Skip `findTrainingData` and `label`. Run `match` directly using the pre-trained model.

```bash
./zingg.sh --phase match --conf config.json
```

{% hint style="success" icon="right-long" %}
The pre-trained `FEBRL` model works best on data with the same field schema: `fname`, `lname`, `stNo`,\
`add1`, `add2`, `city`, `areacode`, `state`, `dob`, `ssn`.

For significantly different schemas or data distributions, fine-tuning with your own labels will improve accuracy.
{% endhint %}

### Fine-tuning a pre-trained model

You can use a pre-trained model as a starting point and improve it with your own labeled data. Run `findTrainingData` and `label` on your own dataset, then run train. Zingg will continue training using the pre-trained model weights rather than starting from scratch.

This is faster than building a model from scratch when your data is structurally similar to the pre-trained model's training data.

{% hint style="success" icon="right-long" %}
**Read more**:

* Create training data - [Create training data](../running-zingg/create-training-data.md)
* Label training pairs - [Label training pairs](../running-zingg/label-training-pairs.md)
* Build and save the model - [Build and save the model](../running-zingg/build-and-save-the-model.md)
{% endhint %}

### Other available pre-trained models

Zingg ships with pre-trained models for several datasets. All models are bundled under the `models/` directory in the Zingg installation. Check `models/modelIds.txt` for the complete list of available models and their IDs.

<table><thead><tr><th valign="top">Model</th><th valign="top">Dataset</th><th valign="top">Fields</th></tr></thead><tbody><tr><td valign="top">FEBRL</td><td valign="top">Synthetic Australian population</td><td valign="top"><code>fname</code>, <code>lname</code>, <code>stNo</code>, <code>add1</code>, <code>add2</code>, <code>city</code>, <code>areacode</code>, <code>state</code>, <code>dob</code>, <code>ssn</code></td></tr><tr><td valign="top">North Carolina Voters</td><td valign="top">US voter registration records</td><td valign="top">Name, address, date of birth</td></tr><tr><td valign="top">Amazon-Google Products</td><td valign="top">Product catalog matching</td><td valign="top">Product title, description</td></tr></tbody></table>

For schemas and model IDs, see `models/modelIds.txt` inside your Zingg installation.
