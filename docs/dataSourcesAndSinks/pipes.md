---
layout: default
title: Zingg Pipes
parent: Data Sources and Sinks
nav_order: 4
---
## Zingg Pipes

Zingg Pipes are an abstraction for a data source from which Zingg fetches data for matching or to which Zingg writes its output. This lets users connect to literally any datastore that has a Spark connector.

The pipe is an easy way to specify properties and formats for the Spark connector of the relevant datasource. Zingg pipes can be configured through the config [JSON](../../setup/configuration.md) passed to the program by outlining the datastore connection properties. 

Pipes can be configured for the data or the output attributes on the [JSON](../../setup/configuration.md). 

Each pipe has the following

### name

unique name to identify the data store

### format

One of the Spark supported connector formats. jdbc/avro/parquet etc

### options

Properties to be passed to spark.read and spark.write.

Let us look at some common datasources and their configurations. 






