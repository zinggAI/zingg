---
description: Keeping secrets private in the JSON config
---

# Configuring through environment variables



If you do not want to pass sensitive values such as passwords through the config file, configure them through system environment variables. Wrap the variable name in dollar signs in your config:

* Strings: `"$var$"` (with quotes)
* Booleans and numerics: `$var$` (without quotes)

```json
{
  "output" : [ {
    "name" : "unifiedCustomers",
    "format" : "net.snowflake.spark.snowflake",
    "props" : {"path" : "$location$", "password" : "$passwd$"}
  } ],
  "labelDataSampleSize" : 0.5,
  "numPartitions" : 4,
  "modelId" : "$modelId$",
  "zinggDir" : "models",
  "collectMetrics" : "$collectMetrics$"
}
```
