---
nav_order: 9
---

# Frequently Asked Questions

## How much training is enough?

Typically 30-40 positive pairs (matches) should build a good model. While marking records through the interactive learner, you can check Zingg's predictions for the shown pair. If they seem to be correct, you can pause and run Zingg in train and match phases to see what result you are getting. If not satisfied, you can always run the findTrainingData and label jobs again and they will pick from the last training round.

## Do I need to train for every new dataset?

No, absolutely not! Train only if the schema(attributes or their types) has changed.

## Do I need to use a Spark cluster or can I run on a single machine?

Depends on the data size you have. Check [hardware sizing](setup/hardwareSizing.md) for more details.

## I dont have much background in ML or Spark. Can I still use Zingg ?

Very much! Zingg uses Spark and ML under the hood so that you don't have to worry about the rules and the scale.

## Is Zingg an MDM?

No, Zingg is not an MDM. An MDM is the system of record, it has its own store where linked and mastered records are saved. Zingg enables MDM but is not a system of record. You can build an MDM in a data store of your choice using Zingg however.

## Is Zingg a CDP ?

No, Zingg is not a CDP, as it does not stream events or customer data through different channels. Zingg does overlap with the CDPs identity resolution and building customer 360 views.

## I can do Entity Resolution using a graph database like TigerGraph/Neo4J, why do I need Zingg ?

Doing entity resolution in graph databases is easy only if you have trusted and high-quality identifiers like passport id, SSN id, etc. through which edges can be defined between different records. If you need fuzzy matching, you will have to build your own rules and algorithms with thresholds to define matching records. Zingg and Graph Databases go hand in hand for Entity Resolution. It is far easier to use Zingg and persist its graph output to a graph database and do further processing for AML, and KYC scenarios there. Read [the article](https://towardsdatascience.com/entity-resolution-with-tigergraph-add-zingg-to-the-mix-95009471ca02) for details on how Zingg uses TigerGraph for Entity Resolution.
