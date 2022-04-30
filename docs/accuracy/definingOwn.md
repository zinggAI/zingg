---
layout: default
nav_order: 6
---
## Improving Accuracy By Defining Own Functions 

You can add your own [blocking functions](https://github.com/zinggAI/zingg/tree/main/core/src/main/java/zingg/hash) which will be evaluated by Zingg to build the [blocking tree.](zModels.md)

The blocking tree works on the matched records provided by the user as part of the training. At every node, it selects the hash function and the field on which it should be applied so that there is least elimination of the matching pairs. Say we have data like this

| Labeled Pair Number | record number | firstname | last name |
| 1 | a | john | doe |
| 1 | b | johnh | d oe |
| 2 | a | mary | ann |
| 2 | b | marry |  |


**pair 1**
-----------
**record a:**
firstname: john
last name: doe
-------------
**record b:**
firstname: johnh
last name: d oe
-------------
**pair 2**
-------------
**record a:**
firstname: mary
last name: ann
-------------
**record b:**
firstname: marry
last name: 

let us assume we have hash functions first1char, last1char
first1char(firstname, pair1, record a) = j
first1char(firstname, pair1, record b) = j
first1char(firstname, pair2, record a) = m
first1char(firstname, pair2, record b) = m

No elimination, good function. 

last1char(firstname, pair1, record a) = n
last1char(firstname, pair1, record b) = h
last1char(firstname, pair2, record a) = y
last1char(firstname, pair2, record b) = y

pair2 is getting eliminated above, not good.

first1char(lastname, pair1, record a) = d
first1char(lastname, pair1, record b) = d
first1char(lastname, pair2, record a) = a
first1char(lastname, pair2, record b) = 

pair2 is getting eliminated above, not good.

last1char(lastname, pair1, record a) = e
last1char(lastname, pair1, record b) = e
last1char(lastname, pair2, record a) = n
last1char(lastname, pair2, record b) = 

pair2 is getting eliminated above, not good.

So first1char(firstname) or first1char(lastname) will be chosen at the root of the blocking tree. Child nodes get added in a similar function to build the heriarchical blocking function tree. This brings near similar records together - in a way clusters them to  break the cartesian join. The hash functions

These business specific blocking functions go into [Hash Functions](https://github.com/zinggAI/zingg/tree/main/core/src/main/java/zingg/hash) and need to be added to [HashFunctionRegistry](https://github.com/zinggAI/zingg/blob/main/core/src/main/java/zingg/hash/HashFunctionRegistry.java) and [hash functions config](https://github.com/zinggAI/zingg/blob/main/core/src/main/resources/hashFunctions.json)

Also, for similarity, you can define your own measures. Each dataType has predefined features, for example [String](https://github.com/zinggAI/zingg/blob/main/core/src/main/java/zingg/feature/StringFeature.java) fuzzy type is configured for affine and jaro

You can define your own [comparisons](https://github.com/zinggAI/zingg/tree/main/core/src/main/java/zingg/similarity/function) and use them
