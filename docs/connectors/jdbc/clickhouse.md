# ClickHouse

ClickHouse Pipe Definitions
JSON settings for reading and writing data using the ClickHouse JDBC driver.

## ClickHouse Input (Reading)

```json
"data": [
    {
      "name": "clickhouse_input",
      "format": "jdbc",
      "props": {
        "url": "jdbc:clickhouse:https://<HOST>:<PORT>/<DATABASE>?ssl=true",
        "driver": "com.clickhouse.jdbc.ClickHouseDriver",
        "user": "<USERNAME>",
        "password": "<PASSWORD>",
        "dbtable": "<INPUT_TABLE_NAME>"
      }
    }
]
```

## ClickHouse Output (Writing)

```json
"output": [
    {
      "name": "clickhouse_output",
      "format": "jdbc",
      "props": {
        "url": "jdbc:clickhouse:https://<HOST>:<PORT>/<DATABASE>?ssl=true",
        "driver": "com.clickhouse.jdbc.ClickHouseDriver",
        "user": "<USERNAME>",
        "password": "<PASSWORD>",
        "dbtable": "<OUTPUT_TABLE_NAME>",
        "saveMode": "append"
      }
    }
]
```

## Implementation Steps

### Add the Driver Jar
Download the `clickhouse-jdbc-0.9.8-all.jar` and add its path to `config/zingg.conf` to ensure Spark can load the driver:

```properties
spark.jars=/path/to/clickhouse-jdbc-0.9.8-all.jar
```

### Align Schema Names
Ensure that the `fieldName` in your Zingg `fieldDefinition` matches the Column Names in your ClickHouse table exactly (e.g., if the column is `fname`, the Zingg field must be `fname`).

### Port
Use port `8443` for ClickHouse Cloud (HTTPS) or `8123` for local HTTP instances.
