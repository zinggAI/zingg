# Zingg Configuration

To resolve entities, Zingg needs some user inputs or arguments. These arguments help Zingg understand where the input data is, which fields to use in the matching and how, where the output is to be written, where the models are to be persisted or used from. The user can also provide some performance specific settings. 

The configuration can be done either through JSON, or through Zingg's Python API. 

Here is some important terminology to be aware of:

1. Arguments - representative of the user input comprising of input and output data, fields and their matching criteria, models location as well as performance criteria. Zingg Enterprise also has specialised arguments for functionality like incremental run, reassign and diff etc. 
2. Pipes - Zingg's abstraction for the data store. A pipe encapsulates the source or destination of records; a delta file path, a Snowflake table, a UC table, an RDBMS dataset. Different pipes are configured by passing the format string in the configuration. 
3. Field Definition - Field Definition is the configuration object that tells Zingg which fields to use for matching and how to compare them. Each field definition has four attributes: `fieldName` (the column name), `fields` (same as `fieldName` for now), `dataType` (string, integer, double, etc.), and `matchType` (the similarity functions to apply).
4. Match type - 

{% hint style="success" icon="right-long" %}
**Read more**: [Pipes and Data Connections](../../connect-your-data/pipes-and-data-connections.md)
{% endhint %}
