---
layout: default
title: Compiling
parent: Setup
nav_order: 4
---
## Compiling from sources
If you need to compile the latest code or build for a different Spark version, you can clone this repo and 

- Install maven (We are on version 3.3.9)
- Install JDK 1.8
- Set JAVA_HOME to JDK base directory
- Run the following

`mvn clean compile package -Dspark=sparkVer` where sparkVer is one of 2.4, 3.0 or 3.1

