---
description: >-
  Identifying and resolving customer records for Customer 360 initiatives on the
  Databricks platform
---

# Using Spark Jobs

Our modern enterprises operate through a variety of specialized applications for sales, marketing, and customer support. Thus, customer data gets siloed in different systems which makes customer analytics extremely challenging. To understand customer behavior, we need to group the individual records spread in these different systems into distinct customer identities. Thus, Identity Resolution, building a single source of truth about customers, becomes an important step in the data analysis pipeline to unify customer data from different sources.

In this post, we will learn to resolve identities and build a single source of truth of customers on the Databricks environment and open-source [Zingg](https://github.com/zinggAI/zingg). Zingg is an open source ML based identity and entity resolution framework. It takes away the complexity of scaling and matching definition from us so that we can focus on the business problem. To resolve our customer entities and build a 360 view, we will configure Zingg to run within Databricks, run its [findTrainingData and label phases](https://docs.zingg.ai/zingg/stepbystep/createtrainingdata) to build training data and finally resolve the entities.

Let us take a look at what our customer data looks like. We have taken an example customer dataset with demographic information about a customer and loaded it in Databricks as a csv file. This example set is borrowed from the [Zingg repository](https://github.com/zinggAI/zingg) and can be found [here](https://github.com/zinggAI/zingg/blob/main/examples/febrl120k/test.csv). Reading the first few lines of the customer data shows that many records have variations but belong to the same real-world customer. There are missing attributes and typos across different source systems, and unifying these records by specifying conditional logic is tough.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/623215a1dc687020bcfa8eb3_1*S5ilWc9--RhitEsCW3Z7Ag.png" alt=""><figcaption></figcaption></figure>

Besides first name, last name and address attributes, this dataset also contains SSN and date of birth. But these fields are unreliable, and records of the same customer from different sources have variations in them.

We add the Zingg jar from the [releases](https://github.com/zinggAI/zingg/releases) and build a configuration file [config120.json](https://github.com/zinggAI/zingg/blob/main/examples/databricks/config120.json). This is how the files show up in the Databricks File System.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/623215a18e64478594808e42_1*rauqOaa8E8cJpVd0VqiUSA.png" alt=""><figcaption></figcaption></figure>

The[ JSON configuration](https://github.com/zinggAI/zingg/blob/main/examples/databricks/config120.json) is built to define the fields in our data and the type of matching we want on each attribute. Let us look at a snippet of the configuration — it contains the attribute names and type of matching we want to run on it.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/623215a1378e371ae43ea47f_1*D8yJ2KkK_cXDnhxE1bZTAA.png" alt=""><figcaption></figcaption></figure>

Using this attribute definition, Zingg will build [machine learning models](https://docs.zingg.ai/zingg/zmodels) for identity resolution.

We now configure a Spark Submit Job in the Databricks console to run Zingg. The Type of the Job is set to Spark Submit and the Parameters are the command line parameters used to run Zingg. The phase is set to findTrainingData which makes Zingg selects some pairs for training.

<figure><img src="../../.gitbook/assets/DB Spark Jobs 1.png" alt=""><figcaption></figcaption></figure>

The Spark Submit Job parameters are

\["--class","zingg.client.Client","dbfs:/FileStore/zingg\_0\_3\_2\_SNAPSHOT.jar","--phase=findTrainingData","--conf=/dbfs/FileStore/config120.json","--license=abc"]

We run the Spark job and once it is done, we want to inspect the pairs Zingg found and mark them as matches or non matches for Zingg to learn. Zingg has a Databricks [notebook that can be used to label the records](https://github.com/zinggAI/zingg/blob/main/examples/databricks/zinggLabeler.py). We add this notebook to our Databricks account so that we can use it.

<figure><img src="../../.gitbook/assets/DB Spark Jobs 2.png" alt=""><figcaption></figcaption></figure>

Let us now run the above notebook which will show us pairs that we can mark. Running the notebook shows us record pairs along with Zingg’s predictions. We can record our feedback through the combo box widget at the top. This is how the labelling appears to us — we are shown the pairs and we add 0 for a nonmatch, 1 for a match, or 2 if we are not sure about the pair by adding our feedback through the input widget.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/623215a1f130e91c1c45c18b_1*_maOmYo2THru4pYocsr1aw.gif" alt=""><figcaption></figcaption></figure>

We run the Spark Job of findTrainingData and the above labeler notebook a few times till we have marked 30–40 actual matches. This should be good enough to run Zingg for the 120k records we have in our input. We then run Zingg in the trainMatch phase to build the models and run the matching. To do so, we alter our previous Spark Job specification and change the phase to trainMatch.

<figure><img src="../../.gitbook/assets/DB Spark Jobs 3.png" alt=""><figcaption></figcaption></figure>

Running this job writes the output to the output location as specified in the configuration json. When we view this output, we see that all records have a few additional columns.

* The Z\_CLUSTER column is a unique customer id Zingg provides such that the matching records can be grouped.
* The Z\_MINSCORE column denotes the least that record matched to any other record in the cluster
* The Z\_MAXSCORE denotes the most that record matched to another record in the cluster.

As matching records have identical z\_cluster, we can resolve them and use this value as the customer identifier which represents a unique person.

<figure><img src="https://cdn.prod.website-files.com/61ee7c3b937e8a5919a6a12d/623215a18e644774bb808e43_0*cBrSvKDAqbOdyOup.png" alt=""><figcaption></figcaption></figure>

The high probability matches can be consumed in subsequent pipelines and transactional data can be overlaid in conjunction with the Z\_CLUSTER to build the customer 360 view. The low probability matches can be manually reviewed and utilized within the pipeline again.

