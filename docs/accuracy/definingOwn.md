---
nav_order: 6
description: To add blocking functions and how they work
---

# Defining Own Functions

You can add your own [blocking functions](https://github.com/zinggAI/zingg/tree/main/core/src/main/java/zingg/hash) which will be evaluated by Zingg to build the [blocking tree.](../zModels.md)

The blocking tree works on the matched records provided by the user as part of the training. At every node, it selects the hash function and the field on which it should be applied so that there is the least elimination of the matching pairs. Say we have data like this:

|  Pair 1  | firstname | lastname |
| :------: | :-------: | :------: |
| record a |    john   |    doe   |
| record b |   johnh   |   d oe   |

****

|  pair 2  | firstname | lastname |
| :------: | :-------: | :------: |
| record a |    mary   |    ann   |
| record b |   marry   |          |



Let us assume we have hash functions first1char, last1char :

| Hash Function |    Name   |  Record  | Character |
| ------------- | :-------: | :------: | :-------: |
| first1char    | firstname | record a |     j     |
| first1char    | firstname | record b |     j     |
| first1char    | firstname | record a |     m     |
| first1char    | firstname | record b |     m     |

No elimination, good function.



| Hash Function |    Name   |  Record  | Character |
| :-----------: | :-------: | :------: | :-------: |
|   last1char   | firstname | record a |     n     |
|   last1char   | firstname | record b |     h     |
|   last1char   | firstname | record a |     y     |
|   last1char   | firstname | record b |     y     |

pair2 is getting eliminated above, not good.



| Hash Function |   Name   |  Record  | Character |
| :-----------: | :------: | :------: | :-------: |
|   first1char  | lastname | record a |     d     |
|   first1char  | lastname | record b |     d     |
|   first1char  | lastname | record a |     a     |
|   first1char  | lastname | record b |           |

pair2 is getting eliminated above, not good.



| Hash Function |   Name   |  Record  | Character |
| :-----------: | :------: | :------: | :-------: |
|   last1char   | lastname | record a |     e     |
|   last1char   | lastname | record b |     e     |
|   last1char   | lastname | record a |     n     |
|   last1char   | lastname | record b |           |

pair2 is getting eliminated above, not good.



So, first1char(firstname) or first1char(lastname) will be chosen at the root of the blocking tree. Child nodes get added in a similar function to build the hierarchical blocking function tree. This brings near similar records together - in a way, clusters them to break the Cartesian join.

These business-specific blocking functions go into [Hash Functions](https://github.com/zinggAI/zingg/tree/main/core/src/main/java/zingg/hash) and must be added to [HashFunctionRegistry](../../core/src/main/java/zingg/hash/HashFunctionRegistry.java) and [hash functions config](../../core/src/main/resources/hashFunctions.json).

Also, for similarity, you can define your own measures. Each dataType has predefined features, for example, [String](../../core/src/main/java/zingg/feature/StringFeature.java) fuzzy type is configured for Affine and Jaro.

You can define your own [comparisons](https://github.com/zinggAI/zingg/tree/main/core/src/main/java/zingg/similarity/function) and use them.
