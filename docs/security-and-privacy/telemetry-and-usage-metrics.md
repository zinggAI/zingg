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

| Metric | Description |
|---|---|
| Data source type | Format of your data source (e.g. CSV, Snowflake, Parquet) |
| Fields count | Number of fields configured for matching |
| Record count | Total number of records passing through Zingg |
| Execution time | Duration of the phase run |
| Running phase | Which Zingg phase was executed |
| Matches and non-matches | Count of matched and non-matched records in the model |
| JDK information | JDK version and flavour |
| OS information | Operating system type |
| Zingg version | Version of Zingg being used |

No field values, record content, or personally identifiable information is included in any telemetry event.

**Disabling telemetry**

Set `collectMetrics` to `false` in your config:

**Python API**

```python
args.setCollectMetrics(False)
```

**JSON config**

```json
{
  "collectMetrics": false
}
```

When `collectMetrics` is `false`, a blank event is logged and no data is sent to Zingg servers.

**Viewing what is captured**

To see exactly what telemetry data is logged during a run, edit `log4j2.properties` and set the logging level of `zingg_analytics` to `warn` .

```bash
logger.zingg_analytics.level=warn
```

An example telemetry event (when `collectMetrics` is `true`).

```json
{
  "client_id": "localhost",
  "events": [
    {
      "name": "match",
      "params": {
        "executionTime": "1.743246748E9",
        "zingg_version": "0.5.0",
        "modelId": "100",
        "domain": "localhost",
        "java_version": "11.0.18",
        "dataCount": "65.0"
      }
    }
  ],
  "user_id": "zingg"
}
```
