# Table of contents

## Zingg Concepts

* [What is Zingg](README.md)
* [Entity Resolution](zingg-concepts/entity-resolution/README.md)
  * [Deterministic vs Probabilistic Matching](zingg-concepts/entity-resolution/deterministic-vs-probabilistic-matching.md)
* [Community vs Enterprise](zingg-concepts/community-vs-enterprise/README.md)
  * [Migrating from Community to Enterprise (Content to be Added)](zingg-concepts/community-vs-enterprise/migrating-from-community-to-enterprise-content-to-be-added.md)
* [How Zingg Learns](zingg-concepts/how-zingg-learns/README.md)
  * [Zingg Models](zingg-concepts/how-zingg-learns/zingg-models/README.md)
    * [Blocking Model](zingg-concepts/how-zingg-learns/zingg-models/blocking-model.md)
    * [Similarity Model](zingg-concepts/how-zingg-learns/zingg-models/similarity-model.md)
    * [Standardize Fields and Results](zingg-concepts/how-zingg-learns/zingg-models/standardize-fields-and-results.md)
  * [Match Types](zingg-concepts/how-zingg-learns/match-types/README.md)
    * [FUZZY Match](zingg-concepts/how-zingg-learns/match-types/fuzzy-match.md)
    * [FUZZY\_OPTIMISED Match](zingg-concepts/how-zingg-learns/match-types/fuzzy_optimised-match.md)
    * [EXACT Match](zingg-concepts/how-zingg-learns/match-types/exact-match.md)
    * [EMAIL Match](zingg-concepts/how-zingg-learns/match-types/email-match.md)
    * [EMAIL\_OPTIMISED Match](zingg-concepts/how-zingg-learns/match-types/email_optimised-match.md)
    * [PINCODE Match](zingg-concepts/how-zingg-learns/match-types/pincode-match.md)
    * [NUMERIC Match](zingg-concepts/how-zingg-learns/match-types/numeric-match.md)
    * [NUMERIC\_WITH\_UNITS Match](zingg-concepts/how-zingg-learns/match-types/numeric_with_units-match.md)
    * [TEXT Match](zingg-concepts/how-zingg-learns/match-types/text-match.md)
    * [ONLY\_ALPHABETS\_EXACT Match](zingg-concepts/how-zingg-learns/match-types/only_alphabets_exact-match.md)
    * [ONLY\_ALPHABETS\_FUZZY Match](zingg-concepts/how-zingg-learns/match-types/only_alphabets_fuzzy-match.md)
    * [ONLY\_ALPHABETS\_FUZZY\_OPTIMISED Match](zingg-concepts/how-zingg-learns/match-types/only_alphabets_fuzzy_optimised-match.md)
    * [NULL\_OR\_BLANK Match](zingg-concepts/how-zingg-learns/match-types/null_or_blank-match.md)
    * [DONT\_USE Match](zingg-concepts/how-zingg-learns/match-types/dont_use-match.md)
    * [MAPPING\_(FILENAME) Match](zingg-concepts/how-zingg-learns/match-types/mapping_-filename-match.md)
* [Identity Graph](zingg-concepts/identity-graph.md)
* [Z Cluster and Zingg ID](zingg-concepts/z-cluster-and-zingg-id.md)
* [Pass Through](zingg-concepts/pass-through.md)
* [Concept Glossary](zingg-concepts/concept-glossary.md)

## Running Zingg

* [Step-by-Step Guide](running-zingg/step-by-step-guide.md)
* [Experience Zingg](running-zingg/experience-zingg.md)
* [Quick Start (Docker)](running-zingg/quick-start-docker.md)
* [Install Zingg](running-zingg/install-zingg.md)
* [Configure Zingg](running-zingg/configure-zingg.md)
* [Create Training Data](running-zingg/create-training-data.md)
* [Label Training Pairs](running-zingg/label-training-pairs.md)
* [Verify Blocking](running-zingg/verify-blocking.md)
* [Generate Model Documentation](running-zingg/generate-model-documentation.md)
* [Build and Save the Model](running-zingg/build-and-save-the-model.md)
* [Run the match phase](running-zingg/run-the-match-phase.md)
* [Link across Datasets](running-zingg/link-across-datasets.md)
* [Run Incremental Matching](running-zingg/run-incremental-matching.md)
* [Reassign Zingg ID](running-zingg/reassign-zingg-id.md)
* [Lookup Data](running-zingg/lookup-data.md)
* [Cluster Approval](running-zingg/cluster-approval.md)
* [Compare Model Results](running-zingg/compare-model-results.md)
* [Knowledge Graph](running-zingg/knowledge-graph.md)

## Connect Your Data

* [Pipes and Data Connections](connect-your-data/pipes-and-data-connections.md)
* [Connect Cloud Warehouses](connect-your-data/connect-cloud-warehouses/README.md)
  * [Connect Azure Databricks](connect-your-data/connect-cloud-warehouses/connect-azure-databricks.md)
  * [Connect Snowflake](connect-your-data/connect-cloud-warehouses/connect-snowflake.md)
  * [Connect BigQuery](connect-your-data/connect-cloud-warehouses/connect-bigquery.md)
  * [Connect Redshift](connect-your-data/connect-cloud-warehouses/connect-redshift.md)
  * [Connect Microsoft Fabric](connect-your-data/connect-cloud-warehouses/connect-microsoft-fabric.md)
  * [Connect Exasol](connect-your-data/connect-cloud-warehouses/connect-exasol.md)
* [Connect Cloud Storage](connect-your-data/connect-cloud-storage.md)
* [Connect File Formats](connect-your-data/connect-file-formats.md)
* [Connect Relational Databases](connect-your-data/connect-relational-databases.md)
* [Connect NoSQL Databases](connect-your-data/connect-nosql-databases.md)
* [Connect Graph Databases (Neo4j)](connect-your-data/connect-graph-databases-neo4j.md)

## Zingg Python API

* [Working with Python](zingg-python-api/working-with-python.md)
* [Community Python API](zingg-python-api/community-python-api.md)
* [Enterprise ZinggEC Python API](zingg-python-api/enterprise-zinggec-python-api.md)
* [Enterprise ZinggES Python API](zingg-python-api/enterprise-zingges-python-api.md)

## Tuning

* [Configure Field Standardization](tuning/configure-field-standardization.md)
* [Improve Accuracy](tuning/improve-accuracy/README.md)
  * [Remove Stopwords (Optional)](tuning/improve-accuracy/remove-stopwords-optional.md)
* [Custom Blocking and Similarity](tuning/custom-blocking-and-similarity.md)

## Interpreting Results

* [Interpret Output Scores](interpreting-results/interpret-output-scores.md)
* [Explain Matches](interpreting-results/explain-matches.md)
* [Explain a Specific Cluster](interpreting-results/explain-a-specific-cluster.md)
* [Output Statistics](interpreting-results/output-statistics.md)

## Platform Guides

* [Platform Guide for Azure Databricks](platform-guides/platform-guide-for-azure-databricks.md)
* [Platform Guide for Microsoft Fabric](platform-guides/platform-guide-for-microsoft-fabric.md)
* [Platform Guide for AWS EMR](platform-guides/platform-guide-for-aws-emr.md)
* [Platform Guide for AWS GLUE](platform-guides/platform-guide-for-aws-glue.md)
* [Platform Guide for GCP Dataproc](platform-guides/platform-guide-for-gcp-dataproc.md)
* [Platform Guide for Snowflake](platform-guides/platform-guide-for-snowflake.md)

## Recipes and Integration

* [Identity RAG with Zingg and LangChain](recipes-and-integration/identity-rag-with-zingg-and-langchain.md)
* [Combine Match Models](recipes-and-integration/combine-match-models.md)
* [Pre-trained Models](recipes-and-integration/pre-trained-models.md)

## Reference

* [Configuration Schema](reference/configuration-schema.md)
* [Zingg Command Line](reference/zingg-command-line.md)
* [CLI Command Reference](reference/cli-command-reference.md)
* [Runtime Properties](reference/runtime-properties.md)
* [Hardware Sizing and Benchmarks](reference/hardware-sizing-and-benchmarks.md)
* [Reading Material](reference/reading-material.md)

## Contributing

* [Contributing to Zingg](contributing/contributing-to-zingg.md)
* [Setting Up Zingg Development Environment](contributing/setting-up-zingg-development-environment/README.md)
  * [macOS Setup Guide](contributing/setting-up-zingg-development-environment/macos-setup-guide.md)
  * [Ubuntu/WSL2 Setup Guide](contributing/setting-up-zingg-development-environment/ubuntu-wsl2-setup-guide.md)

## Frequently Asked Questions

* [Frequently Asked Questions](frequently-asked-questions/frequently-asked-questions.md)

## Security and Privacy

* [Security and Privacy](security-and-privacy/security-and-privacy.md)
* [Telemetry and Usage Metrics](security-and-privacy/telemetry-and-usage-metrics.md)
