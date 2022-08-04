# Jdbc

Zingg can connect to various databases such as Mysql, DB2, MariaDB, MS SQL, Oracle, PostgreSQL, etc. using JDBC. One just needs to download the appropriate driver and made it accessible to the application.

To include the JDBC driver for your particular database on the Spark classpath, please add the property **spark.jars** in [Zingg's runtime properties.](../stepbystep/zingg-runtime-properties.md)

```
spark.jars=<location of jdbc driver jar>
```

Connection details are given in the following sections for a few common JDBC sources.&#x20;

