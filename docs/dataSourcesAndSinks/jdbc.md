## Using JDBC Driver to Connect to Databases As Data Source or Data Sink

Zingg can connect to various databases such as Mysql, DB2, MariaDB, MS Sql, Oracle, PostgreSQL etc. using JDBC. 
One just needs to download appropriate driver and made it accessible to the application.

To include the JDBC driver for your particular database on the spark classpath, set the following environment variable before running Zingg.
```
export ZINGG_EXTRA=<path of the driver jar>
```

Connection details are given below for a few jdbc sources. Please replace <db_name> with name of the database in addition to other props. For more detail, refer to the [spark documentation](https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html)

### Properties for postgreSQL database:

```json
    "data" : [{
        "name":"test", 
        "format":"jdbc", 
        "props": {
            "url": "jdbc:postgresql://localhost:5432/<db_name>",
            "dbtable": "testData",
            "driver": "org.postgresql.Driver",
            "user": "postgres",
            "password": "postgres"				
        }
    }],
``` 
```
$ export ZINGG_EXTRA=path to postgresql-xx.jar
```

### Properties for mysql database:

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
```
$ export ZINGG_EXTRA=path to mysql-connector-java-xx.jar
```
