---
description: >-
  Connect Zingg to MySQL, PostgreSQL,  SQL Server, DB2, MariaDB, and Oracle  via
  JDBC.
---

# Connect Relational Databases

{% hint style="success" icon="right-long" %}
New to Zingg pipes? Understand how pipes work before configuring them - [Pipes and data connections](pipes-and-data-connections.md).
{% endhint %}

Zingg connects to relational databases using the JDBC connector. This covers any JDBC-compatible database including MySQL, PostgreSQL, SQL Server, DB2, MariaDB, and Oracle. Available in all editions.

Download the JDBC driver for your database and add to `zingg.conf`.

```
spark.jars=<location of jdbc driver jar>
```

{% tabs %}
{% tab title="PostgreSQL" %}
### Python API

```python
from zingg.client import* from zingg.pipes import*

    jdbcPipe =
    Pipe("pgInput", "jdbc") jdbcPipe
        .addProperty("url", "jdbc:postgresql://localhost:5432/dbname")
            jdbcPipe.addProperty("dbtable", "your_table")
                jdbcPipe.addProperty("driver", "org.postgresql.Driver")
                    jdbcPipe.addProperty("user", "postgres")
                        jdbcPipe.addProperty("password", "your_password")
                            args.setData(jdbcPipe)
```

### JSON Config

```json
{
  "data" : [ {
    "name" : "test",
    "format" : "jdbc",
    "props" : {
      "url" : "jdbc:postgresql://localhost:5432/<db_name>",
      "dbtable" : "<testData>",
      "driver" : "org.postgresql.Driver",
      "user" : "<postgres>",
      "password" : "<postgres>"
    }
  } ]
}
```
{% endtab %}

{% tab title="MySQL" %}
### Python API

```python
from zingg.client import* from zingg.pipes import*

    jdbcPipe = Pipe("mysqlInput", "jdbc") jdbcPipe
                   .addProperty("url", "jdbc:mysql://localhost:3306/<db_name>")
                       jdbcPipe.addProperty("dbtable", "testData") jdbcPipe
                   .addProperty("driver", "com.mysql.cj.jdbc.Driver")
                       jdbcPipe.addProperty("user", "root")
                           jdbcPipe.addProperty("password", "password")
                               args.setData(jdbcPipe)
```

### JSON Config

```json
{
  "data" : [ {
    "name" : "test",
    "format" : "jdbc",
    "props" : {
      "url" : "jdbc:mysql://localhost:3306/<db_name>",
      "dbtable" : "testData",
      "driver" : "com.mysql.cj.jdbc.Driver",
      "user" : "root",
      "password" : "password"
    }
  } ]
}
```
{% endtab %}
{% endtabs %}

<details>

<summary><strong>SQL Server, DB2, MariaDB, Oracle and other JDBC databases</strong></summary>

For any JDBC-compatible database, use the same pattern as PostgreSQL. Change two things only:

1. The url format for that database
2. The driver class name

**Common driver class names and URL formats:**

* **SQL Server:**
  * **url**: `jdbc:sqlserver://host:1433;databaseName=dbname`
  * **driver**: `com.microsoft.sqlserver.jdbc.SQLServerDriver`
* **MySQL:**
  * **url**: `jdbc:mysql://host:3306/dbname`
  * **driver**: `com.mysql.cj.jdbc.Driver`
* **DB2:**
  * **url**: `jdbc:db2://host:50000/dbname`
  * **driver**: `com.ibm.db2.jcc.DB2Driver`
* **MariaDB:**
  * **url**: `jdbc:mariadb://host:3306/dbname`
  * **driver**: `org.mariadb.jdbc.Driver`
* **Oracle:**
  * **url**: `jdbc:oracle:thin:@host:1521:dbname`
  * **driver**: `oracle.jdbc.OracleDriver`

Download the JDBC driver JAR from the database vendor and add it to `spark.jars`\
in `zingg.conf` before running.



</details>

{% hint style="success" icon="right-long" %}
**Read more:**

* For NoSQL databases - [Connect NoSQL databases](connect-nosql-databases.md)
* For cloud warehouses - [Connect Cloud Warehouses](connect-cloud-warehouses/)
{% endhint %}
