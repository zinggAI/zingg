# Contents
- [Why?](#why?)
- [Key Zingg Concepts](#key-zingg-concepts)
- [Installation](docs/installation.md)
- [Configuration](docs/configuration.md)
- [Zingg phases](docs/running.md)
- [Hardware Sizing](docs/hardware-sizing.md)
- [Pretrained models](#pretrained-models)
- [Running on Amazon EMR](docs/aws.md)
- [Running on Databricks](docs/databricks.md)
- [Connectors](docs/pipes.md)
- [Acknowledgements](#acknowledgements)
- [License](#license)

## Why?

Real world data contains multiple records belonging to the same customer. These records can be in single or multiple systems and they have variations across fields which makes it hard to combine them together, especially with growing data volumes. This hurts customer analytics - establishing lifetime value, loyalty programs or marketing channels is impossible when the base data is unlean. No AI algorithm for segmentation can produce right results when there are multiple copies of the same customer lurking in the data. No warehouse can live upto its promise if the dimension tables have duplicates. 

![data silos](assets/dataSilos.png)


With a modern data stack and DataOps, we have established patterns for E and L in ELT for  building data warehouses, datalakes and deltalakes. However, the T - getting data ready for analytics still needs a lot of effort. Modern tools like [DBT](https://www.getdbt.com) are actively and successfuly addressing this. What is missing is a quick and scalable way to build the single source of truth of core business entities post Extraction and pre or post Loading. 

With Zingg, the analytics engineer and the data scientist can quickly intergate data silos and build unified views at scale! For customers, suppliers, organizations , addresses and other entities.

![# Zingg - Data Mastering At Scale with ML](/assets/dataMastering.png)


Zingg integrates different records of an entity like customer, supplier, product etc in same or disparate data sources. Zingg can be used for

- Master Data Management - building unified and trusted views of customers and suppliers across multiple systems
- Large Scale Entity Resolution for fraud and compliance
- Deduplication and data quality
- Identity Resolution 
- Integrating data silos during mergers and acquisitions
- Reference Data Management
- Data enrichment from external sources

## Key Zingg Concepts

For data mastering, Zingg learns 2 models from the training data. 

1. Blocking Model

One fundamental problem will scaling data mastering is that the number of comparisons increase quadratically as the size of input record increases. A blocking model helps Zingg to index close records together, so that it does not compare every record with every other record. 

![Data Mastering At Scale](/assets/fuzzymatchingcomparisons.jpg)


Zingg learns a clustering/blocking model to index near similar records together to avoid this problem. Typical Zingg comparisons are 0.05-1% of the possible problem space.

2. Similarity Model 

The similarity model helps Zingg to predict which record pairs match. Similarity is run only on records within the same block to scale the problem to larger datasets. The similarity model is a classifier which predicts similarity of records which are not exactly same, but could belong together.

![Fuzzy matching comparisons](/assets/dataMatching.jpg) 

To build these models, training data is needed. Zingg comes with an interactive learner to rapidly build training sets. 

![Shows records and asks user to mark yes, no, cant say on the cli.](assets/label2.gif) 

## Connectors

Zingg connects, reads and writes to most on-premise and cloud data sources. Zingg also runs on any private or cloud based Spark service. 

![zinggConnectors](assets/zinggOSS.png)


Zingg can read and write to Snowflake, Cassandra, S3, Azure, Elastic, major RDBMSes and any other Spark supported data sources. Zingg also works with all major file formats like Parquet, Avro, JSON, XLSX, CSV, TSV etc. Read more about the Zingg [pipe](docs/pipes.md) interface.  


## Pretrained models

Zingg comes with pretrained models for the Febrl dataset under the models folder.

## Acknowledgements

Zingg would have not have been possible without the excellent work below:
- [Apache Spark](https://spark.apache.org)
- [SecondString](http://secondstring.sourceforge.net/)
- [Febrl](http://users.cecs.anu.edu.au/~Peter.Christen/Febrl/febrl-0.3/febrldoc-0.3/)

## License

Zingg is licensed under [AGPL v3.0](https://www.gnu.org/licenses/agpl-3.0.en.html) - which means you have the freedom to distribute copies of free software (and charge for them if you wish), that you receive source code or can get it if you want it, that you can change the software or use pieces of it in new free programs, and that you know you can do these things.

