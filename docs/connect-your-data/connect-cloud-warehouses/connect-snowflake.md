---
description: >-
  Connect Zingg to Snowflake as a data source using the Snowflake Spark
  connector. Available in all editions.
---

# Connect Snowflake

{% hint style="success" icon="right-long" %}
This `config` connects Zingg to Snowflake as a DATA SOURCE using Spark. Available in all editions.
{% endhint %}

{% hint style="warning" icon="right-long" %}
Snowflake as a COMPUTE ENGINE (native Snowflake run without Spark) is Enterprise only.\
[Talk to us about Enterprise](https://www.zingg.ai/company/contact/contact).
{% endhint %}

### Prerequisites

Snowflake as a data source requires two dependency JARs on the Spark classpath. Download from `Maven` and add to `zingg.conf.`

```bash
spark.jars = snowflake - jdbc - 3.13.19.jar, \
spark - snowflake_2 .12 - 2.10.0 - spark_3 .1.jar
```

For the full Zingg installation, cluster setup, and any additional dependency configuration required when connecting Spark to Snowflake, follow the [Snowflake Platform Guide](../../platform-guides/platform-guide-for-snowflake.md).

### Python API

```python
from zingg.client import*
from zingg.pipes import*

snowflakePipe = SnowflakePipe("snowflakeInput")
snowflakePipe.addProperty("sfUrl", "rfa59271.snowflakecomputing.com")
snowflakePipe.addProperty("sfUser", "sonalgoyal")
snowflakePipe.addProperty("sfPassword", "ZZ")
snowflakePipe.addProperty("sfDatabase", "TEST")
snowflakePipe.addProperty("sfSchema", "PUBLIC")
snowflakePipe.addProperty("sfWarehouse", "COMPUTE_WH")
snowflakePipe.addProperty("dbtable", "FEBRL")
snowflakePipe.addProperty("application", "zingg_zingg")
args.setData(snowflakePipe)
```

### JSON Config

```json
{
  "data" : [ {
    "name" : "test",
    "format" : "net.snowflake.spark.snowflake",
    "props" : {
      "sfUrl" : "rfa59271.snowflakecomputing.com",
      "sfUser" : "sonalgoyal",
      "sfPassword" : "ZZ",
      "sfDatabase" : "TEST",
      "sfSchema" : "PUBLIC",
      "sfWarehouse" : "COMPUTE_WH",
      "dbtable" : "FEBRL",
      "application" : "zingg_zingg"
    }
  } ]
}
```

{% hint style="success" icon="right-long" %}
### Supported file formats

Snowflake as a data source connects to tables and views via the Snowflake Spark connector. File formats (CSV, Parquet) are handled by Snowflake internally before Zingg reads the data. No additional format config needed on this connector.
{% endhint %}
