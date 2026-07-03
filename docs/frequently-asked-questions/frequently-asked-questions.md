---
description: >-
  Frequently asked questions about Zingg installation, configuration,
  performance, and troubleshooting.
---

# Frequently Asked Questions

### Training and labeling

<details>

<summary><strong>How many labeled pairs do I need before training?</strong></summary>

30 to 40 matching pairs is a strong starting point before your first `train` run. Label until you feel that your labeled examples represent all field types and data patterns in your schema. If accuracy needs improvement after your first match run, return to labeling; focus on the patterns or field combinations that appear to be missing or underrepresented. Ensure your training set is balanced with roughly equal numbers of match and non-match labels.

</details>

<details>

<summary><strong><code>findTrainingData</code> is running very slowly. What should I do?</strong></summary>

Reduce `labelDataSampleSize`. The default is `0.5` which scans `50%` of your dataset. For large datasets, try `0.05` or `0.01`. You will get fewer candidate pairs per run, but the phase will complete faster. Run multiple rounds to accumulate enough labels.

</details>

<details>

<summary><strong>What happens to my trained model if I change a field definition?</strong></summary>

If you add, remove, or change the match type of a field, your existing trained model is no longer valid for that configuration. You must retrain. If you are using Zingg\
Enterprise, use `reassignZinggId` after retraining to preserve existing Zingg IDs in downstream systems.

</details>

### Match results and accuracy

<details>

<summary><strong>My match results have too many false positives. What is wrong?</strong></summary>

The similarity model has not seen enough non-match examples. Run `findTrainingData` and `label` again, focusing on pairs that look similar but are different entities. Label them as No Match. Retrain. Also check your field match types; fields that should use `EXACT`\
but are set to `FUZZY` are a common source of false positives.

</details>

<details>

<summary><strong>My match results are missing matches I know should be there. What is wrong?</strong></summary>

Check blocking first. Run `verifyBlocking` to confirm your known matching pairs are being blocked. If they are in different blocks, the similarity model will never compare them regardless of how well it is trained. If blocking is fine, add more match labels for this type of pair and retrain.

</details>

<details>

<summary><strong>What is the difference between match and link?</strong></summary>

`match` finds duplicates within a single dataset. `link` matches records across two separate datasets that are each individually duplicate-free. Both use the same trained model and produce the same output columns. Neither is a subset of the other; they are two equal operations for different use cases.

</details>

### Platform and installation

<details>

<summary><strong>Can I use Zingg without writing any code?</strong></summary>

Yes, using the JSON config file and CLI. Define your field definitions, data source, and output in config.json, then run each phase with `./zingg.sh --phase <phase_name> --conf config.json`. No Python code required.

</details>

<details>

<summary><strong>Does Zingg work on Windows?</strong></summary>

Zingg is tested primarily on Linux and macOS. On Windows, use the Zingg Docker image, which includes a pre-configured Linux environment. For production workloads use a managed Spark platform such as Databricks, Fabric, or GCP Dataproc.

</details>

<details>

<summary><strong>How do I upgrade Zingg?</strong></summary>

* For the Python package: `pip install --upgrade zingg`.
* For the Docker image: `docker pull zingg/zingg:<version>` - replace `<version>` with the release tag, for example `zingg/zingg:0.5.0`. Check `github.com/zinggAI/zingg/releases` for the latest version.

After upgrading, your existing trained models remain compatible. You do not need to retrain unless you want to use new match types or features that are introduced in the new version.

</details>

### How Zingg compares with other tools

<details>

<summary><strong>How is Zingg different from a record linkage library like Splink or Dedupe?</strong></summary>

Zingg runs natively on Apache Spark which gives it horizontal scalability for very large datasets. It is also designed for warehouse-native operation on platforms like Databricks, Fabric, and Snowflake without needing a separate Python environment. Zingg Enterprise adds persistent entity IDs, incremental matching, and deterministic matching features that are typically not available in open-source libraries.

</details>

<details>

<summary><strong>Is Zingg an MDM?</strong></summary>

Zingg Community is a DIY Master Data Management product that solves the entity matching and linking process. You can build an MDM on a data store of your choice using Zingg Community; bring your own warehouse, data lake, or database.

Zingg Enterprise is a lakehouse- and warehouse-native MDM with advanced features: globally unique and persistent entity identifiers (Zingg ID), golden record survivorship, deterministic matching, incremental identity graph updates, and agentic stewardship.

If you already have an MDM platform, Zingg can act as the entity resolution engine that feeds it - providing the matching and linking layer that most MDM tools require but do not include.

</details>

<details>

<summary><strong>Can I do entity resolution in TigerGraph or Neo4j instead of Zingg?</strong></summary>

Graph databases are a good fit for entity resolution if you have trusted, high-quality identifiers, such as passport IDs, SSNs, or other stable reference numbers through which edges can be defined between records. If your data has clean unique keys, a graph database can do entity resolution natively.

Where graph databases struggle is fuzzy matching: handling typos, abbreviations, name variations, and data quality issues across millions of records. For that you need a purpose-built similarity model.

Zingg and graph databases work well together. The recommended pattern is to use Zingg for the fuzzy entity resolution step, resolving ambiguous records into clusters, and then persisting Zingg's output (with Zingg IDs as node identifiers) in TigerGraph, Neo4j, or another graph database for relationship analysis, AML screening, KYC, and graph traversal queries.

This combination gives you the best of both: Zingg handles the matching at scale; the graph database handles the relationship analysis and traversal.

</details>
