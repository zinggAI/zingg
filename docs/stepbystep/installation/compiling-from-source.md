---
description: For a different Spark version or compiling latest code
---

# Compiling From Source

If you need to compile the latest code or build for a different Spark version, you can clone this repo and

* Install maven
* Install JDK 1.8
* Set JAVA\_HOME to JDK base directory
* Run the following: `mvn initialize` and then `mvn clean compile package -Dspark=sparkVer`

where sparkVer is one of 3.4 or 3.5
