---
description: >-
  See Zingg resolve duplicate records in under 5 minutes no setup, no
  configuration, no data preparation needed.
---

# Experience Zingg

{% hint style="success" icon="right-long" %}
**Prerequisites:** Docker installed on your machine. Nothing else required.
{% endhint %}

_**CHECK WITH SONAL - IF WE STILL NEED THIS PAGE AS PER THE NEW GROUPING - AS WE HAVE A DEDICATED PAGE FOR DOCKER - QUICK START. OUR INITIAL IDEA ABOUT KEEPING THIS PAGE IS NOW CHANGED.**_

The fastest way to understand what Zingg does is to see it work. This page uses Docker and\
a pre-trained model bundled with Zingg to run entity resolution on a sample dataset in three\
commands. There is no installation, no configuration, and no labeling required.

You will see Zingg take records that look different variations in name, address, and\
date of birth, and resolve them into clusters representing the same real-world person. That\
is entity resolution.

### Run Zingg in 3 commands

#### Step 1: Pull the Zingg Docker image

```bash
docker pull zingg/zingg:0.5.0
```

#### Step 2: Start the container

```bash
docker run -it zingg/zingg:0.5.0 bash
```

If you see a permission error, use:

```bash
docker run -v /tmp:/tmp -it zingg/zingg:0.5.0 bash
```

#### Step 3: Run match using the bundled FEBRL sample data and pre-trained model

```bash
./scripts/zingg.sh --phase match --conf examples/febrl/config.json
```

{% hint style="success" icon="right-long" %}
### **What just happened**

Zingg read the `FEBRL` sample dataset bundled in the Docker image, applied a pre-trained entity resolution model to it, and wrote the resolved output to the output path configured in `examples/febrl/config.json`.

No training was needed because the model was pre-built for this dataset.
{% endhint %}

### Reading the output

The output contains all your input fields plus three columns added by Zingg:

* `Z_CLUSTER` - a unique identifier shared by all records Zingg resolved as the same\
  entity. Records with the same `Z_CLUSTER` value represent the same real-world person\
  in the `FEBRL` dataset.
* `Z_MINSCORE` - the lowest similarity score between any two records in that cluster.\
  Indicates how confidently the weakest link in the cluster was matched.
* `Z_MAXSCORE` - the highest similarity score between any two records in that cluster.\
  Indicates the strongest match within the cluster.

{% hint style="success" icon="right-long" %}
**Read more**: To know more about output scores, check [Interpret Output Score](../interpreting-results/interpret-output-scores.md).
{% endhint %}

### VIDEO TO BE ADDED

**Paste the Zingg video URL here once available from the team.**

<details>

<summary><strong>What is the FEBRL dataset?</strong></summary>

FEBRL (Freely Extensible Biomedical Record Linkage) is a synthetic dataset generator\
that produces realistic person records with deliberate variations. The records contain\
fields for first name, last name, street number, address lines, city, state, date of birth, and a national identifier.

The variations are intentional - the same person appears multiple times with different\
spellings, missing fields, and formatting differences across records. This makes FEBRL\
ideal for demonstrating entity resolution because the correct answer is known: Zingg's\
output can be verified against the ground truth built into the dataset.

Zingg ships with a pre-trained model for FEBRL under the models folder in the release. This is why Experience Zingg requires no training step - the model is already built.

</details>

<details>

<summary><strong>What happens after this?</strong></summary>

Experience Zingg uses a pre-trained model on a sample dataset. In a real implementation, you would:

1. Connect your own data using Zingg pipes.
2. Define the fields you want to match on and choose a match type for each.
3. Run `findTrainingData` to generate candidate pairs from your data.
4. Label those pairs as "Match" or "No Match" to teach Zingg what a match looks like in your domain.
5. Train the model on your labeled pairs.
6. Run `match` or `link` on your full dataset.

The Quick Start page walks you through this full workflow on your own platform using sample data provided by the Zingg team.

As long as your input columns and field types are not changing, the same model works - you do not need to retrain. If you change a match type, you can add more labeled pairs on top of existing training data.

</details>

{% hint style="warning" icon="right-long" %}
Enterprise adds persistent identity with Zingg ID, incremental matching, and production-grade notebooks. [Talk to us about Enterprise](https://www.zingg.ai/company/contact/contact)
{% endhint %}
