---
description: >-
  Complete reference for all Zingg JSON configuration parameters and Python API
  arguments.
---

# Configuration Schema

This page is the reference. Every Zingg configuration parameter, JSON key and Python API method is documented here with type, valid values, and edition availability.

{% hint style="success" icon="right-long" %}
**Read more**: For the step-by-step task of setting up configuration - [Configure Zingg](../running-zingg/configure-zingg.md).
{% endhint %}

### Top-level parameters

<table><thead><tr><th width="150.62890625" valign="top">Parameter</th><th width="108.40234375" valign="top">Type</th><th width="180.0703125" valign="top">Edition</th><th valign="top">Description</th></tr></thead><tbody><tr><td valign="top"><code>modelId</code></td><td valign="top">string</td><td valign="top">All editions</td><td valign="top">Unique identifier for this model. Used as folder name under <code>zinggDir</code>. Use the same <code>modelId</code> across all phases for a given run.</td></tr><tr><td valign="top"><code>zinggDir</code></td><td valign="top">string</td><td valign="top">All editions</td><td valign="top">Root directory where Zingg writes model files and training data. Can be a local path, DBFS path (<code>dbfs:/</code>), GCS path (<code>gs://</code>), S3 path (<code>s3a://</code>), or OneLake path (<code>abfss://</code>).</td></tr><tr><td valign="top"><code>numPartitions</code></td><td valign="top">integer</td><td valign="top">All editions</td><td valign="top">Number of Spark partitions. Controls how data is distributed across cluster nodes. Rule of thumb: set to approximately 20–30× the number of worker vCPUs. Start with 4–8 for a standard development cluster. Increase proportionally for larger datasets.</td></tr><tr><td valign="top"><code>labelDataSampleSize</code></td><td valign="top">float (0.0001–0.1)</td><td valign="top">All editions</td><td valign="top">Fraction of the dataset scanned when running <code>findTrainingData</code>. Valid range: 0.0001 to 0.1. For 100k records use 0.1–0.5. For 1M+ records use 0.01–0.05. Reduce to 0.05 or lower if <code>findTrainingData</code> is slow on large datasets.</td></tr><tr><td valign="top"><code>stopWordsCutoff</code></td><td valign="top">float (0–1)</td><td valign="top">All editions, optional</td><td valign="top">Used with the <code>recommend</code> phase. Fraction of high-frequency words to extract as stopword candidates. Default is <code>0.1</code> (10%).</td></tr><tr><td valign="top"><code>collectMetrics</code></td><td valign="top">boolean</td><td valign="top">All editions</td><td valign="top">Controls telemetry collection. Default <code>true</code>. Set to <code>false</code> to disable. When enabled, Zingg captures runtime metrics (record count, field count, running phase, execution time). No input data or user data is ever captured. See <a href="../security-and-privacy/security-and-privacy.md">Security and Privacy</a> for full details.</td></tr><tr><td valign="top"><code>passthroughExpr</code></td><td valign="top">string</td><td valign="top"><strong>Enterprise only</strong>, optional</td><td valign="top">SQL expression defining records to exclude from matching. Records matching this expression are carried through to output with their own Zingg ID but do not influence cluster formation. Example: <code>"fname = 'matilda'"</code>.</td></tr><tr><td valign="top"><code>deterministicMatching</code></td><td valign="top">array</td><td valign="top"><strong>Enterprise only</strong>, optional</td><td valign="top">Deterministic match conditions, where exact field matches always result in a match regardless of the probabilistic score. See <a href="../zingg-concepts/entity-resolution/deterministic-vs-probabilistic-matching.md">Deterministic vs Probabilistic Matching</a> for full configuration.</td></tr><tr><td valign="top"><code>setBlockingModel</code></td><td valign="top">string</td><td valign="top"><strong>Enterprise only</strong>, optional</td><td valign="top">Blocking strategy. Valid values: <code>"DEFAULT"</code>, <code>"WIDER"</code>. If not set, the model uses <code>DEFAULT</code>.</td></tr></tbody></table>

### `fieldDefinition`

Each entry in the `fieldDefinition` array defines one field in your input schema.

<table><thead><tr><th width="152.9921875" valign="top">Parameter</th><th width="116.16015625" valign="top">Type</th><th width="179.5390625" valign="top">Edition</th><th valign="top">Description</th></tr></thead><tbody><tr><td valign="top"><code>fieldName</code></td><td valign="top">string</td><td valign="top">All editions</td><td valign="top">Name of the field in your input dataset. Must match exactly.</td></tr><tr><td valign="top"><code>matchType</code></td><td valign="top">enum</td><td valign="top">All editions</td><td valign="top">How to compare this field. See <a href="../zingg-concepts/how-zingg-learns/match-types/">Match Types</a> for all values.</td></tr><tr><td valign="top"><code>fields</code></td><td valign="top">string</td><td valign="top">All editions</td><td valign="top">The field to use for comparison. Keep the same as <code>fieldName</code> for standard use. Can reference a different column name if the field you want to compare appears under a different name in the input schema. For now, keep this the same as <code>fieldName</code> unless specifically advised otherwise.</td></tr><tr><td valign="top"><code>dataType</code></td><td valign="top">string</td><td valign="top">All editions</td><td valign="top">Spark SQL data type - <code>string</code>, <code>integer</code>, <code>double</code>, <code>date</code>, <code>timestamp</code>.</td></tr><tr><td valign="top"><code>stopWords</code></td><td valign="top">string (path)</td><td valign="top">All editions, optional</td><td valign="top">Path to a CSV file of stopwords for this field. One word per row. The file must have a header row. Generated by the <code>recommend</code> phase.</td></tr><tr><td valign="top"><code>primaryKey</code></td><td valign="top">boolean</td><td valign="top"><strong>Enterprise only</strong>, optional</td><td valign="top">Marks this field as the primary key. Used by <code>runIncremental</code> to identify records uniquely across runs and by <code>reassignZinggId</code> for ID preservation. Set <code>true</code> on exactly one field per config.</td></tr><tr><td valign="top"><code>postProcessors</code></td><td valign="top">string</td><td valign="top"><strong>Enterprise only</strong>, optional</td><td valign="top">Standardise postprocessor reference. Format: <code>STANDARDISE_&#x3C;basename></code> where <code>&#x3C;basename>.json</code> is the mapping file. Normalises field values to canonical form after matching.</td></tr><tr><td valign="top"><code>MappingMatchType</code></td><td valign="top">nested object</td><td valign="top"><strong>Enterprise only</strong>, optional</td><td valign="top">User-supplied lookup file for nickname/abbreviation matching. See <a href="../zingg-concepts/how-zingg-learns/match-types/mapping_-filename-match.md">MAPPING match type</a> for the mapping file format and rules.</td></tr></tbody></table>

### `data` (input pipe)

<table><thead><tr><th width="159.1328125" valign="top">Parameter</th><th width="110.30078125" valign="top">Type</th><th width="181.984375" valign="top">Edition</th><th valign="top">Description</th></tr></thead><tbody><tr><td valign="top"><code>name</code></td><td valign="top">string</td><td valign="top">All editions</td><td valign="top">Logical name for this pipe. Used in logging.</td></tr><tr><td valign="top"><code>format</code></td><td valign="top">string</td><td valign="top">All editions</td><td valign="top">Connector format. See <a href="https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/connect-your-data">Connect Your Data</a> for all supported formats.</td></tr><tr><td valign="top"><code>props</code></td><td valign="top">object</td><td valign="top">All editions</td><td valign="top">Connector-specific properties. Keys depend on the format. See <a href="https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/connect-your-data">Connect Your Data</a> for <code>props</code> per connector.</td></tr><tr><td valign="top"><code>schema</code></td><td valign="top">string</td><td valign="top">All editions</td><td valign="top">Spark SQL schema string. Required for CSV. Format: <code>"field1 type1, field2 type2, ..."</code>.</td></tr></tbody></table>

### `output` (output pipe)

<table><thead><tr><th width="167.05078125" valign="top">Parameter</th><th width="106.66796875" valign="top">Type</th><th width="181.15625" valign="top">Edition</th><th valign="top">Description</th></tr></thead><tbody><tr><td valign="top"><code>name</code></td><td valign="top">string</td><td valign="top">All editions</td><td valign="top">Logical name for this output pipe.</td></tr><tr><td valign="top"><code>format</code></td><td valign="top">string</td><td valign="top">All editions</td><td valign="top">Connector format. See <a href="https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/connect-your-data">Connect Your Data</a> for supported output formats.</td></tr><tr><td valign="top"><code>props</code></td><td valign="top">object</td><td valign="top">All editions</td><td valign="top">Connector-specific properties. Keys depend on the format.</td></tr></tbody></table>

### `outputStats` (statistics pipe)

Uses the same `format`, `name`, and `props` pattern as `data` and `output`. The path must contain the `$ZINGG_DYNAMIC_STAT_NAME` placeholder, which is replaced at runtime with `SUMMARY`, `CLUSTER`, or `RECORD` depending on the statistics type written. If not configured, statistics are not written and the run proceeds normally.

<table><thead><tr><th width="164.5" valign="top">Parameter</th><th width="102.40234375" valign="top">Type</th><th width="172.96484375" valign="top">Edition</th><th valign="top">Description</th></tr></thead><tbody><tr><td valign="top"><code>name</code></td><td valign="top">string</td><td valign="top">Enterprise only</td><td valign="top">Logical name for this stats pipe.</td></tr><tr><td valign="top"><code>format</code></td><td valign="top">string</td><td valign="top">Enterprise only</td><td valign="top">Connector format. Usually <code>csv</code>.</td></tr><tr><td valign="top"><code>props.location</code></td><td valign="top">string</td><td valign="top">Enterprise only</td><td valign="top">Path must contain <code>$ZINGG_DYNAMIC_STAT_NAME</code>. Example: <code>/tmp/zinggStats_$ZINGG_DYNAMIC_STAT_NAME</code></td></tr></tbody></table>

### JSON Config

A complete JSON config combines top-level parameters, `fieldDefinition`, `data`, and `output`, plus `outputStats` for Enterprise.

#### **Using environment variables in JSON config**

If you do not want to pass sensitive values (passwords, API keys, connection strings) through the config file directly, you can configure them through system environment variables. Wrap the variable name in dollar signs in your config:

* **Strings:** `"$var$"` (with quotes)
* **Booleans and numerics:** `$var$` (without quotes)

At runtime, Zingg replaces the placeholder with the actual environment variable value.

**Example JSON config with environment variables**

```json
{
  "output": [
    {
      "name": "unifiedCustomers",
      "format": "net.snowflake.spark.snowflake",
      "props": {
        "path": "$location$",
        "password": "$passwd$"
      }
    }
  ],
  "labelDataSampleSize": 0.5,
  "numPartitions": 4,
  "modelId": "$modelId$",
  "zinggDir": "models",
  "collectMetrics": "$collectMetrics$"
}
```

{% hint style="success" icon="right-long" %}
Environment variable substitution applies anywhere in the JSON config - top-level parameters, pipe `props`, field paths, output locations, and credentials. The substitution is purely textual at runtime, so any environment variable Zingg can read can be referenced this way.
{% endhint %}

### Python API equivalents

For every JSON parameter, there is an equivalent Python API method on the `Arguments` (Community) or `EArguments` (Enterprise) object.

<table><thead><tr><th valign="top">JSON parameter</th><th valign="top">Python API method</th><th width="145.0078125" valign="top">Edition</th><th valign="top">Example</th></tr></thead><tbody><tr><td valign="top"><code>modelId</code></td><td valign="top"><code>args.setModelId()</code></td><td valign="top">All editions</td><td valign="top"><code>args.setModelId("100")</code></td></tr><tr><td valign="top"><code>zinggDir</code></td><td valign="top"><code>args.setZinggDir()</code></td><td valign="top">All editions</td><td valign="top"><code>args.setZinggDir("/tmp/models")</code></td></tr><tr><td valign="top"><code>numPartitions</code></td><td valign="top"><code>args.setNumPartitions()</code></td><td valign="top">All editions</td><td valign="top"><code>args.setNumPartitions(4)</code></td></tr><tr><td valign="top"><code>labelDataSampleSize</code></td><td valign="top"><code>args.setLabelDataSampleSize()</code></td><td valign="top">All editions</td><td valign="top"><code>args.setLabelDataSampleSize(0.5)</code></td></tr><tr><td valign="top"><code>fieldDefinition</code></td><td valign="top"><code>args.setFieldDefinition([])</code></td><td valign="top">All editions</td><td valign="top"><code>args.setFieldDefinition(fieldDefs)</code></td></tr><tr><td valign="top"><code>data</code></td><td valign="top"><code>args.setData()</code></td><td valign="top">All editions</td><td valign="top"><code>args.setData(inputPipe)</code></td></tr><tr><td valign="top"><code>output</code></td><td valign="top"><code>args.setOutput()</code></td><td valign="top">All editions</td><td valign="top"><code>args.setOutput(outputPipe)</code></td></tr><tr><td valign="top"><code>collectMetrics</code></td><td valign="top"><code>args.setCollectMetrics()</code></td><td valign="top">All editions</td><td valign="top"><code>args.setCollectMetrics(False)</code></td></tr><tr><td valign="top"><code>outputStats</code></td><td valign="top"><code>args.setOutputStats()</code></td><td valign="top">Enterprise only</td><td valign="top"><code>args.setOutputStats(statsPipe)</code></td></tr><tr><td valign="top"><code>passthroughExpr</code></td><td valign="top"><code>args.setPassthroughExpr()</code></td><td valign="top">Enterprise only</td><td valign="top"><code>args.setPassthroughExpr("fname = 'matilda'")</code></td></tr><tr><td valign="top"><code>deterministicMatching</code></td><td valign="top"><code>args.setDeterministicMatchingCondition()</code></td><td valign="top">Enterprise only</td><td valign="top"><code>args.setDeterministicMatchingCondition(dm1, dm2)</code></td></tr><tr><td valign="top"><code>setBlockingModel</code></td><td valign="top"><code>args.setBlockingModel()</code></td><td valign="top">Enterprise only</td><td valign="top"><code>args.setBlockingModel("DEFAULT")</code></td></tr><tr><td valign="top"><code>primaryKey</code> (field-level)</td><td valign="top"><code>fieldDef.setPrimaryKey()</code></td><td valign="top">Enterprise only</td><td valign="top"><code>recId.setPrimaryKey(True)</code></td></tr><tr><td valign="top"><code>postProcessors</code> (field-level)</td><td valign="top"><code>fieldDef.setPostProcessors()</code></td><td valign="top">Enterprise only</td><td valign="top"><code>job_title.setPostProcessors([StandardisePostprocessorType("STANDARDISE", "jobtitles")])</code></td></tr></tbody></table>

{% hint style="success" icon="right-long" %}
Sample config files:

* `github.com/zinggAI/zingg/tree/main/ examples/febrl/config.json`
* `github.com/zinggAI/zingg/tree/main/examples/febrl120k/config.json`
{% endhint %}

{% hint style="success" icon="right-long" %}
**Related pages:**

* [Configure Zingg](../running-zingg/configure-zingg.md) - step-by-step task of building your config
* [Match Types](../zingg-concepts/how-zingg-learns/match-types/) - full reference for all `matchType` values
* [Connect Your Data](https://app.gitbook.com/s/4FvYw4VaCJcugJzWCiLX/connect-your-data) - all `data` and `output` pipe formats and props
* [Working With Python](../zingg-python-api/working-with-python.md) - Python API alternative to JSON config
{% endhint %}
