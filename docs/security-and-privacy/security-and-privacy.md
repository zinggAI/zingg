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

| Artefact | Spark deployment location | Snowflake Enterprise location | Contents |
|---|---|---|---|
| **Training data - unmarked** | `zinggDir/modelId/trainingData/unmarked/` | `@stage/zingg_models/modelId/trainingData/unmarked/` | Candidate record pairs selected by Zingg for labelling. Contains field values from your input dataset plus Zingg metadata columns. |
| **Training data - marked** | `zinggDir/modelId/trainingData/marked/` | `@stage/zingg_models/modelId/trainingData/marked/` | Pairs after you have labelled them as Match, No Match, or Uncertain. Same field columns as unmarked plus the label column. |
| **Trained model** | `zinggDir/modelId/model/` | `@stage/zingg_models/modelId/model/` | Snowpark ML model artefacts encoding the blocking and similarity learned from your training data. Does not contain raw input records. |
| **Match output** | The `output` path configured in your `EArguments` / `Arguments` | Snowflake table configured in `output` section | All input fields plus Zingg-generated columns (`Z_CLUSTER`, `Z_MINSCORE`, `Z_MAXSCORE` in Community; `ZINGG_ID` and Enterprise equivalents in Enterprise). |
| **Stopwords** (if configured) | `zinggDir/modelId/stopWords/<columnName>` | `@stage/zingg_models/modelId/stopWords/<columnName>` | List of high-frequency words detected for the specified column. |
| **Output statistics** (Enterprise) | The `outputStats` path configured | `@stage/zingg_models/modelId/outputStats/` | Three statistics files per run: summary, cluster, and record-level metrics. |
| **Credentials** | Not stored by Zingg | Managed by Snowflake | Connection credentials in `config.json` or environment variables remain under your control. Snowflake Enterprise uses Snowflake's native role and credential model. |

### Handling sensitive fields

Fields containing PII such as SSN, national ID, date of birth, or financial identifiers should be\
handled carefully:

* Use `MatchType.EXACT` for sensitive identifier fields rather than `FUZZY`. `EXACT` matching does not expose partial field values through similarity scoring.
* Use `MatchType.DONT_USE` to exclude a field from matching entirely while still including it in output. This is useful for fields that are present in your data but should not influence entity resolution decisions.
* Consider pseudonymizing or tokenizing highly sensitive fields before running Zingg if your data governance policy requires it. Zingg can match on tokens as effectively as on raw values when the tokenization is consistent.

{% hint style="success" icon="right-long" %}
**Read more**: For the full match type reference → [Match Types](../zingg-concepts/zingg-configuration/field-definition/match-types/)
{% endhint %}

### GDPR and CCPA considerations

Zingg is a processing tool. GDPR and CCPA compliance obligations apply to how you use Zingg output, not to Zingg itself. Specific considerations:

* **Right to erasure**: If a subject requests deletion, identify their Zingg ID in the output and delete all records associated with that cluster from your output store. Zingg Enterprise's persistent Zingg IDs make the process easier to implement consistently.
* **Data minimization**: Configure only the fields you need for matching in your field definitions. Fields not needed for entity resolution can be excluded from Zingg processing.
* **Purpose limitation**: Zingg output should only be used for the entity resolution purpose stated in your data processing agreements.

{% hint style="success" icon="right-long" %}
**Read more**: For GDPR and CCPA identity resolution use cases:

* [GDPR use case on identity resolution](https://zingg.ai/product/entity-resolution-solutions/gdpr)
* [CCPA use case on identity resolution](https://zingg.ai/product/entity-resolution-solutions/ccpa)
{% endhint %}
