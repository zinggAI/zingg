## The Problem

Real world data contains multiple records belonging to the same customer. These records can be in single or multiple systems and they have variations across fields which makes it hard to combine them together, especially with growing data volumes. This hurts [customer analytics](docs/bizLeaderSurvey.md) - establishing lifetime value, loyalty programs or marketing channels is impossible when the base data is not linked. No AI algorithm for segmentation can produce right results when there are multiple copies of the same customer lurking in the data. No warehouse can live up to its promise if the dimension tables have duplicates. 

![# Zingg - Data Silos](/assets/dataSilos.png)

## Why Zingg

Zingg is an ML based tool for entity resolution. The following features set Zingg apart from other tools and libraries 
- Ability to handle any entity like customer, patient, supplier, product etc 
- Ability to connect to [disparate data sources](https://docs.zingg.ai/zingg/connectors). Local and cloud file systems in any format, enterprise applications and relational, NoSQL and cloud databases and warehouses
- Ability to scale to large volumes of data. [See why this is important](https://docs.zingg.ai/zingg/zmodels/) and [Zingg performance numbers](https://docs.zingg.ai/zingg/stepbystep/hardwaresizing)
- [Interactive training data builder](https://docs.zingg.ai/zingg/stepbystep/createtrainingdata/label) using active learning that builds models on frugally small training samples to high accuracy.
![Shows records and asks user to mark yes, no, cant say on the cli.](/assets/label.gif) 
- Ability to define domain specific functions to improve matching  
- Out of the box support for English as well as Chinese, Thai, Japanese, Hindi and other languages

Zingg is useful for
- Building unified and trusted views of customers and suppliers across multiple systems
- Large Scale Entity Resolution for AML, KYC and other fraud and compliance scenarios
- [Deduplication](docs/patient.md) and data quality
- Identity Resolution 
- Integrating data silos during mergers and acquisitions
- Data enrichment from external sources
- Establishing customer [households](docs/households.md)


![# Zingg - Data Mastering At Scale with ML](/assets/dataMastering.png)

## Demo

See Zingg in action [here](https://www.youtube.com/watch?v=zOabyZxN9b0)

## Getting Started

The easiest way to get started with Zingg is through Docker and by running the prebuilt models.
```
docker pull zingg/zingg:0.3.2
docker run -it zingg/zingg:0.3.2 bash
./scripts/zingg.sh --phase match --conf examples/febrl/config.json
``` 

Check the [step by step guide](https://docs.zingg.ai/zingg/stepbystep) for more details.

## The Story

What is the [backstory behind Zingg](https://sonalgoyal.substack.com/p/time-to-zingg)? 

## Documentation

Check detailed Zingg [documentation](https://docs.zingg.ai/zingg/) 

## Community

Be part of the conversation in the [Zingg Community Slack](https://join.slack.com/t/zinggai/shared_invite/zt-w7zlcnol-vEuqU9m~Q56kLLUVxRgpOA)


## Reporting bugs and contributing 

Want to report a bug or request a feature? Let us know on  [Slack](https://join.slack.com/t/zinggai/shared_invite/zt-w7zlcnol-vEuqU9m~Q56kLLUVxRgpOA), or open an [issue](https://github.com/zinggAI/zingg/issues/new/choose)

Want to commit code? Lets talk on  [Slack](https://join.slack.com/t/zinggai/shared_invite/zt-w7zlcnol-vEuqU9m~Q56kLLUVxRgpOA)

## Book Office Hours

If you want to schedule a 30-min call with our team to help you get set up, please book a slot [here](https://calendly.com/sonalgoyal/30min). 

## Asking questions

If you have a question or issue while using Zingg, kindly log a [question](https://github.com/zinggAI/zingg/issues/new/choose) and we will reply very fast :-)

## License

Zingg is licensed under [AGPL v3.0](https://www.gnu.org/licenses/agpl-3.0.en.html) - which means you have the freedom to distribute copies of free software (and charge for them if you wish), that you receive source code or can get it if you want it, that you can change the software or use pieces of it in new free programs, and that you know you can do these things.

Need a different license? Write to us.

## People behind Zingg

Zingg is being developed by [Zingg.Ai](https://www.zingg.ai) team. 

## Acknowledgements

Zingg would have not have been possible without the excellent work below:
- [Apache Spark](https://spark.apache.org)
- [SecondString](http://secondstring.sourceforge.net/)
- [Febrl](http://users.cecs.anu.edu.au/~Peter.Christen/Febrl/febrl-0.3/febrldoc-0.3/)

