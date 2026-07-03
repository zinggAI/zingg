---
description: >-
  Zingg captures a small set of runtime metrics to help improve the product. No
  input data, output data, or user data is ever captured or transmitted.
---

# Telemetry and Usage Metrics

Zingg captures a small set of anonymous runtime metrics like execution time, Zingg version, Java version, model ID, and record count, to help the team understand how Zingg is used in real deployments and prioritise improvements.

The telemetry is intentionally minimal. No field values, no record content, no personally identifiable information, and no user data leaves your environment. If you prefer not to send any telemetry at all, it can be disabled with a single config setting.

{% hint style="success" icon="right-long" %}
Telemetry events are sent to Zingg servers when `collectMetrics` is `true` (the default). Setting `collectMetrics` to `false` logs a blank event and prevents any data from being sent.
{% endhint %}

### **What is captured**

<table><thead><tr><th width="251.61328125" valign="top">Metric</th><th valign="top">Description</th></tr></thead><tbody><tr><td valign="top">Data source type</td><td valign="top">Format of your data source (e.g. CSV, Snowflake, Parquet)</td></tr><tr><td valign="top">Fields count</td><td valign="top">Number of fields configured for matching</td></tr><tr><td valign="top">Record count</td><td valign="top">Total number of records passing through Zingg</td></tr><tr><td valign="top">Execution time</td><td valign="top">Duration of the phase run</td></tr><tr><td valign="top">Running phase</td><td valign="top">Which Zingg phase was executed</td></tr><tr><td valign="top">Matches and non-matches</td><td valign="top">Count of matched and non-matched records in the model</td></tr><tr><td valign="top">JDK information</td><td valign="top">JDK version and flavour</td></tr><tr><td valign="top">OS information</td><td valign="top">Operating system type</td></tr><tr><td valign="top">Zingg version</td><td valign="top">Version of Zingg being used</td></tr></tbody></table>

No field values, record content, or personally identifiable information is included in any telemetry event.

**Disabling telemetry**

Set `collectMetrics` to `false` in your config:

**Python API**

```python
args.setCollectMetrics(False)
```

**JSON config**

```json
{ "collectMetrics" : false }
```

When `collectMetrics` is `false`, a blank event is logged and no data is sent to Zingg servers.

**Viewing what is captured**

To see exactly what telemetry data is logged during a run, edit `log4j2.properties` and set the logging level of `zingg_analytics` to `warn` .

```bash
logger.zingg_analytics.level = warn
```

An example telemetry event (when `collectMetrics` is `true`).

```json
{
  "client_id" : "localhost",
                "events" : [ {
                  "name" : "match",
                  "params" : {
                    "executionTime" : "1.743246748E9",
                    "zingg_version" : "0.5.0",
                    "modelId" : "100",
                    "domain" : "localhost",
                    "java_version" : "11.0.18",
                    "dataCount" : "65.0"
                  }
                } ],
                           "user_id" : "zingg"
}
```
