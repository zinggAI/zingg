## Zingg Pipes

Zingg Pipes are an abstraction for a data source from which Zingg fetches data for matching or to which Zingg writes its output. Zingg pipes can be configured through the config [JSON](configuration.md) passed to the program. Each pipe has 

### name

unique name to identify the data store

### format

One of the Spark supported connector formats. 

### options