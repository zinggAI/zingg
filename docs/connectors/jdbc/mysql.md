# MySQL

## Reading From MySQL Database:

```json
    "data" : [{
        "name":"test", 
        "format":"jdbc", 
        "props": {
            "url": "jdbc:mysql://localhost:3306/<db_name>",
            "dbtable": "testData",
            "driver": "com.mysql.cj.jdbc.Driver",
            "user": "root",
            "password": "password"				
        }
    }],
```

Please replace `<db_name>` with the _name_ of the database in addition to other props. For more details, refer to the [Spark documentation](https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html).
