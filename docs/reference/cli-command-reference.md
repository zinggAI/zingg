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

| Phase name | Edition | Description |
|---|---|---|
| `findTrainingData` | All | Scans the dataset and selects candidate pairs for labeling. |
| `label` | All | Opens interactive labeling session. Run after `findTrainingData`. |
| `generateDocs` | All | Generates HTML documentation of labeled training data for review. |
| `train` | All | Builds blocking and similarity models using labeled training data. |
| `match` | All | Applies trained models to the full dataset. Writes clusters to output. |
| `link` | All | Matches records across two separate datasets. Same model as `match`. |
| `recommend` | All | Generates stopword candidates for a specified field. |
| `verifyBlocking` | Enterprise | Checks blocking model coverage against known matching pairs. |
| `trainMatch` | Enterprise | Combined `train` and `match` in one phase call. |
| `findAndLabel` | Enterprise | Combined `findTrainingData` and label in one phase call. |
| `runIncremental` | Enterprise | Updates identity graph with new or changed records without a full re-match. |
| `explainOutput` | Enterprise | Shows pair-level evidence for how a specific cluster was formed. |
| `generateDocs` | All | Generates HTML documentation of model and data statistics. |
| `reassignZinggId` | Enterprise Plus | Reassigns Zingg IDs when switching to a new trained model. |
| `updateLabel` | All | Revisit and update previously marked training pairs. Run before `train` to correct labeling errors. Run `generateDocs` first to identify pairs to update. |
| `diff` | Enterprise | Compare two model outputs to understand exactly what changed between them before deploying. Identifies which clusters merged, split, or moved. |
| `runLookup` | Enterprise | Look up specific records in existing match output to find which entity cluster they belong to and get their Zingg ID |

### All flags

| Flag | Required/Optional | Description |
|---|---|---|
| `--phase` | Required | The Zingg phase is to run. See phases table above. |
| `--conf` | Required | Path to your JSON config file. |
| `--properties-file` | Optional | Path to Zingg runtime properties file (`zingg.conf`). This is required for Snowflake and some cloud connectors. |
| `--zinggDir` | Optional | Override the `zinggDir` from config. Useful for S3 and GCS paths. |
| `--showConcise` | Optional | `true` or `false`. When `true`, hides `DONT_USE` fields in the label phase terminal display. |
| `--column` | Required with `recommend` | The field name to generate stopword recommendations for. |
| `--originalZinggId` | Required with `reassignZinggId` | Path to the original production config file. |
| `--run` | Optional | Path to a Python program to execute through `zingg.sh`. Use instead of `--phase` and `--conf` when running Python-based Zingg programs. |
| `--zinggid` | Required with `explainOutput` | The Zingg ID of the cluster to explain. Find Zingg IDs in your match output `ZINGG_ID` column. |

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
