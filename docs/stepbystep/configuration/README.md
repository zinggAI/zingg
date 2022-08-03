# Configuration

Zingg comes with a command-line script that invokes spark-submit. This script needs a JSON configuration file to define the input data and match types, location of training data, models, and output.

Sample configuration files are defined at [examples/febrl](https://github.com/zinggAI/zingg/tree/main/examples/febrl) and [/examples/febrl120k](https://github.com/zinggAI/zingg/tree/main/examples/febrl120k)

Here are the JSON variables which you will need to define to work with your data.

An array of input data. Each array entry here refers to a [Zingg Pipe](../../dataSourcesAndSinks/pipes.md). If the data is self-describing, for e.g. Avro or parquet, there is no need to define the schema. Else field definitions with names and types need to be provided.

