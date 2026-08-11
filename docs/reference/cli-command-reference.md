---
description: All Zingg CLI phase commands and flags in one place.
---

# CLI Command Reference

### Basic invocation pattern

```bash
./scripts/zingg.sh --phase <phase_name> --conf <path_to_config.json>
```

```bash
./scripts/zingg.sh <optional --properties-file path to zingg.conf> --run <path to python program>
```

Use `--run` to execute a Python program through `zingg.sh`. Zingg Python programs are PySpark programs. The CLI executes both JSON config jobs and Python program jobs. It is not a replacement for the Python API; it is the runtime that executes both.

### All phases

<table><thead><tr><th width="199.75860595703125" valign="top">Phase name</th><th width="119.59686279296875" valign="top">Edition</th><th valign="top">Description</th></tr></thead><tbody><tr><td valign="top"><code>findTrainingData</code></td><td valign="top">All</td><td valign="top">Scans the dataset and selects candidate pairs for labeling.</td></tr><tr><td valign="top"><code>label</code></td><td valign="top">All</td><td valign="top">Opens interactive labeling session. Run after<br><code>findTrainingData</code>.</td></tr><tr><td valign="top"><code>generateDocs</code></td><td valign="top">All</td><td valign="top">Generates HTML documentation of labeled<br>training data for review.</td></tr><tr><td valign="top"><code>train</code></td><td valign="top">All</td><td valign="top">Builds blocking and similarity models using labeled training data.</td></tr><tr><td valign="top"><code>match</code></td><td valign="top">All</td><td valign="top">Applies trained models to the full dataset. Writes clusters to output.</td></tr><tr><td valign="top"><code>link</code></td><td valign="top">All</td><td valign="top">Matches records across two separate datasets. Same model as <code>match</code>.</td></tr><tr><td valign="top"><code>recommend</code></td><td valign="top">All</td><td valign="top">Generates stopword candidates for a specified field.</td></tr><tr><td valign="top"><code>verifyBlocking</code></td><td valign="top">Enterprise</td><td valign="top">Checks blocking model coverage against known<br>matching pairs.</td></tr><tr><td valign="top"><code>trainMatch</code></td><td valign="top">Enterprise</td><td valign="top">Combined <code>train</code> and <code>match</code> in one phase call.</td></tr><tr><td valign="top"><code>findAndLabel</code></td><td valign="top">Enterprise</td><td valign="top">Combined <code>findTrainingData</code> and label in one phase call.</td></tr><tr><td valign="top"><code>runIncremental</code></td><td valign="top">Enterprise</td><td valign="top">Updates identity graph with new or changed<br>records without a full re-match.</td></tr><tr><td valign="top"><code>explainOutput</code></td><td valign="top">Enterprise</td><td valign="top">Shows pair-level evidence for how a specific cluster was formed.</td></tr><tr><td valign="top"><code>generateDocs</code></td><td valign="top">All</td><td valign="top">Generates HTML documentation of model and data statistics.</td></tr><tr><td valign="top"><code>reassignZinggId</code></td><td valign="top">Enterprise Plus</td><td valign="top">Reassigns Zingg IDs when switching<br>to a new trained model.</td></tr><tr><td valign="top"><code>updateLabel</code></td><td valign="top">All</td><td valign="top">Revisit and update previously marked training pairs. Run before <code>train</code> to correct labeling errors. Run <code>generateDocs</code> first to identify pairs to update.</td></tr><tr><td valign="top"><code>diff</code></td><td valign="top">Enterprise</td><td valign="top">Compare two model outputs to understand exactly what changed between them before deploying. Identifies which clusters merged, split, or moved.</td></tr><tr><td valign="top"><code>runLookup</code></td><td valign="top">Enterprise</td><td valign="top">Look up specific records in existing match output to find which entity cluster they belong to and get their Zingg ID</td></tr></tbody></table>

### All flags

<table><thead><tr><th width="222.57421875" valign="top">Flag</th><th width="171.83984375" valign="top">Required/Optional</th><th valign="top">Description</th></tr></thead><tbody><tr><td valign="top"><code>--phase</code></td><td valign="top">Required</td><td valign="top">The Zingg phase is to run. See phases table above.</td></tr><tr><td valign="top"><code>--conf</code></td><td valign="top">Required</td><td valign="top">Path to your JSON config file.</td></tr><tr><td valign="top"><code>--properties-file</code></td><td valign="top">Optional</td><td valign="top">Path to Zingg runtime properties<br>file (<code>zingg.conf</code>). This is required for Snowflake and some cloud connectors.</td></tr><tr><td valign="top"><code>--zinggDir</code></td><td valign="top">Optional</td><td valign="top">Override the <code>zinggDir</code> from config. Useful<br>for S3 and GCS paths.</td></tr><tr><td valign="top"><code>--showConcise</code></td><td valign="top">Optional</td><td valign="top"><code>true</code> or <code>false</code>. When <code>true</code>, hides<br><code>DONT_USE</code> fields in the label phase<br>terminal display.</td></tr><tr><td valign="top"><code>--column</code></td><td valign="top">Required with <code>recommend</code></td><td valign="top">The field name to generate stopword<br>recommendations for.</td></tr><tr><td valign="top"><code>--originalZinggId</code></td><td valign="top">Required with<br><code>reassignZinggId</code></td><td valign="top">Path to the original production config file.</td></tr><tr><td valign="top"><code>--run</code></td><td valign="top">Optional</td><td valign="top">Path to a Python program to execute through <code>zingg.sh</code>. Use instead of <code>--phase</code> and <code>--conf</code> when running Python-based Zingg programs.</td></tr><tr><td valign="top"><code>--zinggid</code></td><td valign="top">Required with <code>explainOutput</code></td><td valign="top">The Zingg ID of the cluster to explain. Find Zingg IDs in your match output <code>ZINGG_ID</code> column.</td></tr></tbody></table>

### Example commands

#### **`findTrainingData`**

```bash
./scripts/zingg.sh --phase findTrainingData --conf config.json
```

#### **`label` (with concise display)**

```bash
./scripts/zingg.sh --phase label --conf config.json --showConcise=true
```

#### **`generateDocs`**

```bash
./scripts/zingg.sh --phase generateDocs --conf config.json --showConcise=true
```

#### **`train`**

```bash
./scripts/zingg.sh --phase train --conf config.json
```

#### **`match`**

```bash
./scripts/zingg.sh --phase match --conf config.json
```

#### **`link`**

```bash
./scripts/zingg.sh --phase link --conf config.json
```

#### **`recommend` (stopwords for `fname`)**

```bash
./scripts/zingg.sh --phase recommend --conf config.json --column fname
```

#### **`runIncremental`**

```bash
./scripts/zingg.sh --phase runIncremental --conf incrementalConf.json
```

#### **`reassignZinggId`**

```bash
./scripts/zingg.sh --phase reassignZinggId --conf configReassign.json --originalZinggId config.json --properties-file config/zingg.conf
```

#### **`updateLabel`**

```bash
./scripts/zingg.sh --phase updateLabel --conf config.json
```

#### `findAndLabel` (combined - smaller datasets)

```bash
./scripts/zingg.sh --phase findAndLabel --conf config.json
```

#### `verifyBlocking` (run after label)

```bash
./scripts/zingg.sh --phase verifyBlocking --conf config.json
```

#### **`diff` (compare two model outputs)**

```bash
./scripts/zingg.sh --phase diff --conf configNew.json --compareTo configBaseline.json --properties-file config/zingg.conf
```

#### **`explainOutput` (explain a specific cluster)**

```bash
./scripts/zingg.sh --phase explainOutput --zinggid ea67d79a-56a7-4431-ab55-d08bb3c10e2e --conf explainConfig.json
```

#### **trainMatch (combined - Enterprise)**

```bash
./scripts/zingg.sh --phase trainMatch --conf config.json
```
