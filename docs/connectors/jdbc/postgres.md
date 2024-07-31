# Postgres

## JSON Settings For Reading Data From Postgres Database:

```json
    "data" : [{
        "name":"test", 
        "format":"jdbc", 
        "props": {
            "url": "jdbc:postgresql://localhost:5432/<db_name>",
            "dbtable": "<testData>",
            "driver": "org.postgresql.Driver",
            "user": "<postgres>",
            "password": "<postgres>"				
        }
    }],
```

Replace `<db_name>` with the _name_ of the database in addition to other props. For more details, refer to the [Spark documentation](https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html).
