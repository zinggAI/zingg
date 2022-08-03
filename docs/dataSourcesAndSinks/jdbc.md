# Jdbc

Zingg can connect to various databases such as Mysql, DB2, MariaDB, MS SQL, Oracle, PostgreSQL, etc. using JDBC. One just needs to download the appropriate driver and made it accessible to the application.

To include the JDBC driver for your particular database on the Spark classpath, please add the property **spark.jars** in [Zingg's runtime properties.](../stepbystep/zingg-runtime-properties.md)

```
spark.jars=<location of jdbc driver jar>
```

Connection details are given below for a few JDBC sources. Please replace \<db\_name> with the name of the database in addition to other props. For more details, refer to the [spark documentation](https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html).

## Properties for PostgreSQL database:

## Properties for MySQL database:

