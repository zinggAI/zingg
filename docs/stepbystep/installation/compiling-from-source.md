---
description: For a different Spark version or compiling latest code
---

# Compiling From Source

If you need to compile the latest code or build for a different Spark version, you can clone this [repo](https://github.com/zinggAI/zingg/tree/main) and

* Install **maven 3.8.7**
* Install **JDK 1.8**
* Set `JAVA_HOME` to JDK base directory
* Install **Apache Spark (version spark-3.5.0-bin-hadoop3)**
* Set `SPARK_HOME` as path to location of Apache Spark
* Set `SPARK_MASTER` as **local[*]**

* Run the following: `mvn initialize` and then `mvn clean compile package`
