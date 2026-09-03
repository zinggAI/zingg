---
description: Steps in the entity resolution pipeline
---

# 📘 Zingg Phases

Zingg programs run different aspects of the entity resolution pipeline. Each phase covers one aspect of the pipeline. Here are the phases Zingg defines. 

<details>

<summary><strong><code>findTrainingData</code></strong> — <strong>scans your dataset and selects the most informative candidate pairs for labeling</strong></summary>

Scans your dataset and selects the most informative candidate record pairs for labeling - edge cases where the model has the most to learn. Candidate pairs are written within the warehouse/lakehouse based on your configuration. 

</details>

<details>

<summary><strong><code>label</code></strong> — <strong>presents candidate pairs for human review (Match / No Match / Uncertain)</strong></summary>

Loads the candidate pairs from `findTrainingData` and presents them in a labeling widget. You mark each pair as Match, No Match, or Uncertain. 

</details>

<details>

<summary><strong><code>train</code></strong> — <strong>builds blocking and similarity models from labeled pairs; models persisted and reusable</strong></summary>

Builds the blocking and similarity models from your labeled pairs. Both models are persisted to the customer environment. Once trained, the model can be reused on new data without retraining.

</details>

<details>

<summary><strong><code>match</code></strong> — <strong>applies trained model to full dataset, writes resolved clusters (deduplication within one dataset)</strong></summary>

Applies the trained model to your full dataset and writes resolved clusters to the output location. This is the phase that produces deduplicated, entity-resolved output for a single dataset.

</details>

<details>

<summary><strong><code>link</code></strong> — <strong>matches records across two or more separate datasets; outputs `Z_SOURCE` column</strong></summary>

Like `match`, but for linking records across two or more separate datasets. Each output record carries a `Z_SOURCE` column indicating which source dataset it came from. Use when you want to find the same entity across datasets without merging them into one.

</details>

<details>

<summary><strong><code>runIncremental</code> (Enterprise)</strong> — <strong>updates identity graph with new/changed records without retraining; inherits stable Zingg IDs</strong></summary>

Updates the identity graph with new or changed records without retraining the model. Records that match an existing cluster inherit its Zingg ID. Records that do not match any existing cluster receive a new Zingg ID.

</details>

<details>

<summary><strong><code>explain</code> (Enterprise)</strong> — <strong>shows how a cluster was formed, pair scores, transitive connections; for governance/audit</strong></summary>

Shows how a specific cluster was formed, which record pairs were compared, their similarity scores, and how transitive matching connected records through intermediate pairs. Used for governance, audit, and stakeholder review.

</details>

<details>

<summary><strong><code>generateDocs</code></strong> — <strong>produces HTML docs of training pairs and model stats; for SME review and audit</strong></summary>

Produces HTML documentation of your labeled training pairs and model statistics. Useful for sharing with subject matter experts before training, or for audit and governance after.

</details>

<details>

<summary><strong><code>verifyBlocking</code> (Enterprise)</strong> — <strong>reports % of known matching pairs blocked together correctly; run before training</strong></summary>

Reports what percentage of your known matching pairs are being blocked together correctly. Run after labeling and before training to catch blocking issues early.

</details>

<details>

<summary><strong><code>stopwords</code> recommend</strong> — <strong>analyzes column for high-frequency noise words (e.g., St, Ave, Inc, Ltd) to exclude from blocking</strong></summary>

Analyses a specified column and returns a list of high-frequency words that should be treated as stopwords. Optional phase used before training when address or company name fields contain repeated noise words like 'St', 'Ave', 'Inc', 'Ltd'.

</details>

<details>

<summary><strong><code>transform</code> (Enterprise)</strong> — <strong>post-processes Zingg output for downstream apps; supports dictionary-based value substitution</strong></summary>

Post processes Zingg output to consume in downstream applications. Supports disctionary based value substitution

</details>
