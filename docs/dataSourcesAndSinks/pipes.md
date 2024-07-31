---
title: Zingg Pipes
parent: Data Sources and Sinks
nav_order: 4
---

# Zingg Pipes

Zingg Pipes are an _abstraction_ for a data source from which Zingg fetches data for matching or to which Zingg writes its output. This lets users connect to literally any datastore that has a Spark connector.

The pipe is an easy way to specify _properties_ and _formats_ for the Spark connector of the relevant data source. Zingg pipes can be configured through the config [JSON](../stepbystep/configuration/) passed to the program by outlining the datastore connection properties.

Pipes can be configured for the data or the output attributes on the [JSON](../stepbystep/configuration/).

Each pipe has the following attributes:

## name

A unique name to identify the data store.

## format

One of the Spark-supported connector formats - jdbc/avro/parquet etc.

## options

Properties to be passed to **spark.read** and **spark.write.**

Let us look at some common data sources and their configurations.
