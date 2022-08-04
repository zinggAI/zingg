---
nav_order: 6
description: To add blocking functions and how they work
---

# Defining Own Functions

You can add your own [blocking functions](https://github.com/zinggAI/zingg/tree/main/core/src/main/java/zingg/hash) which will be evaluated by Zingg to build the [blocking tree.](../zModels.md)

The blocking tree works on the matched records provided by the user as part of the training. At every node, it selects the hash function and the field on which it should be applied so that there is the least elimination of the matching pairs. Say we have data like this:

|  Pair 1  | firstname | lastname |
| :------: | :-------: | :------: |
| Record A |    john   |    doe   |
| Record B |   johnh   |   d oe   |

****

|   Pair 2  | firstname | lastname |
| :-------: | :-------: | :------: |
| Rrecord A |    mary   |    ann   |
|  Record B |   marry   |          |



Let us assume we have hash function first1char and we want to check if it is a good function to apply to firstname:

| Pair |  Record  | Output |
| :--: | :------: | ------ |
|   1  | Record A | j      |
|   1  | Record B | j      |
|   2  | Record A | m      |
|   2  | Record B | m      |

There is no elimination in the pairs above, hence it is a good function.



Now let us try last1char on firstname

| Pair |  Record  | Output |
| :--: | :------: | ------ |
|   1  | Record A | n      |
|   1  | Record B | h      |
|   2  | Record A | y      |
|   2  | Record B | y      |

Pair 1 is getting eliminated above, hence last1char is not a good function.&#x20;

So, first1char(firstname) will be chosen. This brings near similar records together - in a way, clusters them to break the cartesian join.

These business-specific blocking functions go into [Hash Functions](https://github.com/zinggAI/zingg/tree/main/core/src/main/java/zingg/hash) and must be added to [HashFunctionRegistry](../../core/src/main/java/zingg/hash/HashFunctionRegistry.java) and [hash functions config](../../core/src/main/resources/hashFunctions.json).

Also, for similarity, you can define your own measures. Each dataType has predefined features, for example, [String](../../core/src/main/java/zingg/feature/StringFeature.java) fuzzy type is configured for Affine and Jaro.

You can define your own [comparisons](https://github.com/zinggAI/zingg/tree/main/core/src/main/java/zingg/similarity/function) and use them.
