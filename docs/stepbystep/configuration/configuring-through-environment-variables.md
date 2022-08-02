---
description: Passing Configuration value through the system environment variable
---

# Configuring Through Environment Variables

If a user does not want to pass the value of any JSON parameter through the config file for security reasons or otherwise, they can configure that value through the system environment variable. The system variable name needs to be put in the config file in place of its JSON value. At runtime, the config file gets updated with the value of the environment variable.

Below is the config file snippet that references a few environment variables.

```json
"output" : [{
  "name":"unifiedCustomers", 
  "format":"net.snowflake.spark.snowflake",
  "props": {
    "location": "$location$",
    "delimiter": ",",
    "header": false,				
    "password": "$passwd",					
  }
}],

"labelDataSampleSize" : 0.5,
"numPartitions":4,
"modelId": $modelId$,
"zinggDir": "models",
"collectMetrics": $collectMetrics$
```

Environment variables must be enclosed within dollar signs **$var$** to take effect. Also, the config file name must be suffixed with \***.env**. As usual, String variables need to be put within quotes **"$var$"**, Boolean and Numeric values should be put without quotes **$var$**.
