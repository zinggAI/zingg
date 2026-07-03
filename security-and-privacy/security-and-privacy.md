---
description: >-
  How Zingg handles sensitive data, what is stored, and what security
  considerations apply to production deployments.
---

# Security and Privacy

Zingg runs entirely within your infrastructure. Whether you are running Zingg Community on Spark, Zingg Enterprise on a managed Spark platform like Databricks or Fabric, or Zingg Enterprise on Snowflake using Snowpark, all data processing happens inside your environment. Nothing is transmitted to external services by the Zingg engine itself.

This page covers data residency, where Zingg writes its artefacts, how to handle sensitive fields, and how Zingg fits into GDPR and CCPA workflows.

### Data residency

Zingg reads from and writes to the paths or tables you configure via `config.json` or Python API setup for Spark deployments and Snowflake schemas for Enterprise Snowflake deployments. Nothing leaves your infrastructure.

{% hint style="success" icon="right-long" %}
Ensure your configured paths and tables sit within your security perimeter - private S3 buckets, GCS buckets with appropriate IAM, ADLS or OneLake with Entra ID controls, or Snowflake schemas with role-based access controls.
{% endhint %}

#### Zingg Artefacts in Your Environment

<table><thead><tr><th width="168.16796875" valign="top">Artefact</th><th width="145.21875" valign="top">Spark deployment location</th><th width="147.03125" valign="top">Snowflake Enterprise location</th><th valign="top">Contents</th></tr></thead><tbody><tr><td valign="top"><strong>Training data - unmarked</strong></td><td valign="top"><code>zinggDir/modelId/trainingData/unmarked/</code></td><td valign="top"><strong>TO BE ADDED</strong></td><td valign="top">Candidate record pairs selected by Zingg for labelling. Contains field values from your input dataset plus Zingg metadata columns.</td></tr><tr><td valign="top"><strong>Training data - marked</strong></td><td valign="top"><code>zinggDir/modelId/trainingData/marked/</code></td><td valign="top"><strong>TO BE ADDED</strong></td><td valign="top">Pairs after you have labelled them as Match, No Match, or Uncertain. Same field columns as unmarked plus the label column.</td></tr><tr><td valign="top"><strong>Trained model</strong></td><td valign="top"><code>zinggDir/modelId/model/</code></td><td valign="top"><strong>TO BE ADDED</strong></td><td valign="top">Spark ML model artefacts encoding the blocking and similarity learned from your training data. Does not contain raw input records.</td></tr><tr><td valign="top"><strong>Match output</strong></td><td valign="top">The <code>output</code> path configured in your <code>EArguments</code> / <code>Arguments</code></td><td valign="top"><strong>TO BE ADDED</strong></td><td valign="top">All input fields plus Zingg-generated columns (<code>Z_CLUSTER</code>, <code>Z_MINSCORE</code>, <code>Z_MAXSCORE</code> in Community; <code>ZINGG_ID</code> and Enterprise equivalents in Enterprise).</td></tr><tr><td valign="top"><strong>Stopwords</strong> (if configured)</td><td valign="top"><code>zinggDir/modelId/stopWords/&#x3C;columnName></code></td><td valign="top"><strong>TO BE ADDED</strong></td><td valign="top">List of high-frequency words detected for the specified column.</td></tr><tr><td valign="top"><strong>Output statistics</strong> (Enterprise)</td><td valign="top">The <code>outputStats</code> path configured</td><td valign="top"><strong>TO BE ADDED</strong></td><td valign="top">Three statistics files per run: summary, cluster, and record-level metrics.</td></tr><tr><td valign="top"><strong>Credentials</strong></td><td valign="top">Not stored by Zingg</td><td valign="top"><strong>TO BE ADDED</strong></td><td valign="top">Connection credentials in <code>config.json</code> or environment variables remain under your control. Snowflake Enterprise uses Snowflake's native role and credential model.</td></tr></tbody></table>

### Handling sensitive fields

Fields containing PII such as SSN, national ID, date of birth, or financial identifiers should be\
handled carefully:

* Use `MatchType.EXACT` for sensitive identifier fields rather than `FUZZY`. `EXACT` matching does not expose partial field values through similarity scoring.
* Use `MatchType.DONT_USE` to exclude a field from matching entirely while still including it in output. This is useful for fields that are present in your data but should not influence entity resolution decisions.
* Consider pseudonymizing or tokenizing highly sensitive fields before running Zingg if your data governance policy requires it. Zingg can match on tokens as effectively as on raw values when the tokenization is consistent.

{% hint style="success" icon="right-long" %}
**Read more**: For the full match type reference → [Match Types](../zingg-concepts/how-zingg-learns/match-types/)
{% endhint %}

### GDPR and CCPA considerations

Zingg is a processing tool. GDPR and CCPA compliance obligations apply to how you use Zingg output, not to Zingg itself. Specific considerations:

* **Right to erasure**: If a subject requests deletion, identify their Zingg ID in the output and delete all records associated with that cluster from your output store. Zingg Enterprise's persistent Zingg IDs make the process easier to implement consistently.
* **Data minimization**: Configure only the fields you need for matching in your field definitions. Fields not needed for entity resolution can be excluded from Zingg processing.
* **Purpose limitation**: Zingg output should only be used for the entity resolution purpose stated in your data processing agreements.

{% hint style="success" icon="right-long" %}
**Read more**: For GDPR and CCPA identity resolution use cases:

* [GDPR use case on identity resolution](https://zingg.ai/product/entity-resolution-solutions/gdpr)&#x20;
* [CCPA use case on identity resolution](https://zingg.ai/product/entity-resolution-solutions/ccpa)&#x20;
{% endhint %}
