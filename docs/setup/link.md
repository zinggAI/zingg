
### link

In many cases like reference data mastering, enrichment etc, 2 individual datasets are duplicate free but they need to be matched against each other. The link phase is used for such scenarios. 

`./zingg.sh --phase link --conf config.json`

Sample configuration file [configLink.json](https://github.com/zinggAI/zingg/blob/main/examples/febrl/configLink.json) is defined at [examples/febrl](https://github.com/zinggAI/zingg/tree/main/examples/febrl). In this option, each record from the first source is matched with all the records from remaining sources.

A sample output is given in the image below.  The linked records are given same z_cluster id. Last column (z_source) in the output tells the source dataset of that record.

![Link results](/assets/link.png) 