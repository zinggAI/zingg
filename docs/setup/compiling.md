---
layout: default
title: Compiling
parent: Setup
nav_order: 4
---
## Compile
If you need to compile the latest code or build for a different Spark version, you can clone this repo and 

a. Install maven (We are on version 3.3.9)
b. Install JDK 1.8
c. Set JAVA_HOME to JDK base directory
d. Run the following
`mvn clean compile package -Dspark=sparkVer` where sparkVer is one of 2.4, 3.0 or 3.1

