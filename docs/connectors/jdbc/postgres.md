# Postgres

## JSON Settings for reading data from Postgres database:

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

Please replace \<db\_name> with the name of the database in addition to other props. For more details, refer to the [spark documentation](https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html).
