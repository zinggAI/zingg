---
description: >-
  The two CLI invocation patterns and copy-paste commands for every Zingg phase
  for Docker, local Spark, EMR, and CI/CD environments.
---

# Zingg Command Line

The CLI lets you invoke Zingg phases from any environment that has a terminal - Docker containers, local Spark installations, AWS EMR clusters, or CI/CD pipelines. No notebook required.

You can pass a JSON configuration file directly or point the CLI at a Python program to run. This means you can run Zingg Python programs through `zingg.sh` just as easily as JSON-configured jobs - the CLI is not a replacement for the Python API; it is the runtime that executes both.

### The Two Invocation Patterns

**Pass a JSON config file**

```bash
./ scripts /
    zingg.sh<optional-- properties -
             file path to zingg.conf> --phase<phase> --conf<path to json>
```

**Run a Python program**

```bash
./ scripts /
    zingg.sh<optional-- properties - file path - to -
             zingg.conf> --run<path - to - python - program>
```

{% hint style="success" icon="right-long" %}
Full command reference with all flags and options - [CLI command reference](cli-command-reference.md)
{% endhint %}

{% tabs %}
{% tab title="Community (OS)" %}
{% hint style="success" icon="right-long" %}
Replace `config.json` with your actual config file path.
{% endhint %}

### **Find training pairs**

```bash
./ zingg.sh-- phase findTrainingData-- conf config.json
```

### **Label pairs**

The `--showConcise=true` flag shows only fields used for matching and hides `DONT_USE` fields.

```bash
./ zingg.sh-- phase label-- conf config.json-- showConcise = true
```

### Train the model

```bash
./ zingg.sh-- phase train-- conf config.json
```

### **Run match**

`match` finds duplicates within a single dataset.

```bash
./ zingg.sh-- phase match-- conf config.json
```

### **Run link**

`link` matches records across two separate datasets.

```bash
./ zingg.sh-- phase link-- conf config.json
```
{% endtab %}

{% tab title="Enterprise" %}
{% hint style="info" icon="right-long" %}
Enterprise adds two phases not available in Community: `generateDoc`s and\
`runIncremental`. Replace `config.json` with your actual config file path.
{% endhint %}

### Find training pairs

```bash
./ zingg.sh-- phase findTrainingData-- conf config.json
```

### Label pairs

```bash
./ zingg.sh-- phase label-- conf config.json-- showConcise = true
```

### **Generate model documentation**

Optional but recommended before `train` — lets you inspect training data quality before committing to the train phase.

```bash
./ zingg.sh-- phase generateDocs-- conf config.json
```

### Train the model

```bash
./ zingg.sh-- phase train-- conf config.json
```

### Run match

```bash
./ zingg.sh-- phase match-- conf config.json
```

### Run link

```bash
./ zingg.sh-- phase link-- conf config.json
```

### **Run incremental**

Updates the identity graph with new and changed records without re-running the full match.

```bash
./ zingg.sh-- phase runIncremental-- conf config.json
```

{% hint style="info" icon="right-long" %}
`generateDocs`, `runIncremental`, and `diff` are Enterprise only phases.
{% endhint %}
{% endtab %}
{% endtabs %}

<details>

<summary><strong>When should I use link instead of match?</strong></summary>

Use `match` when your goal is to find duplicates within a single dataset. For example, identifying that two customer records in the same system represent the same person.

Use `link` when you have two separate datasets that are individually duplicate-free but need to be matched against each other. For example, this involves matching a supplier list from one system against a vendor master from another. Each record from the first source is matched with all records from the remaining sources. The output includes a `z_sourc`e column that identifies which source dataset each record came from.

Both `match` and `link` use the same trained model. They are equal operations, not a hierarchy.

</details>

<details>

<summary><strong>What does a basic config.json look like?</strong></summary>

The JSON config file tells Zingg which fields to use, where your data lives, and where to write output. Here is the minimum structure to run any phase:

```json
{
  "fieldDefinition": [
    {
      "fieldName": "fname",
      "matchType": "FUZZY",
      "fields": "fname",
      "dataType": "string"
    },
    {
      "fieldName": "lname",
      "matchType": "FUZZY",
      "fields": "lname",
      "dataType": "string"
    },
    {
      "fieldName": "dob",
      "matchType": "EXACT",
      "fields": "dob",
      "dataType": "string"
    }
  ],
  "data": [{
    "name": "inputData",
    "format": "csv",
    "props": {
      "location": "/path/to/input.csv",
      "delimiter": ",",
      "header": "true"
    }
  }],
  "output": [{
    "name": "matchOutput",
    "format": "csv",
    "props": {
      "location": "/path/to/output",
      "delimiter": ",",
      "header": "true"
    }
  }],
  "modelId": "1",
  "zinggDir": "/tmp/zingg",
  "labelDataSampleSize": 0.5,
  "numPartitions": 4
}
```

{% hint style="success" icon="right-long" %}
**Read more**:

* Full configuration schema with all parameters and options - [Configuration schema](configuration-schema.md)
* Full flag and options reference - [CLI command reference](cli-command-reference.md)
* Python API alternative - [Python API](/broken/pages/Frf3DstX6KiO5yly5ZeO)
{% endhint %}

</details>
