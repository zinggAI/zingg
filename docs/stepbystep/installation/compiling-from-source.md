---
description: For a different Spark version or compiling latest code
---

# Compiling From Source

If you need to compile the latest code or build for a different Spark version, you can clone this repo and

* Install maven
* Install JDK 1.8
* Set JAVA\_HOME to JDK base directory
* Run the following: `mvn clean compile package -Dspark=sparkVer`&#x20;

&#x20;     where sparkVer is one of 2.4, 3.0 or 3.1
