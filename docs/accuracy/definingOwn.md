---
layout: default
nav_order: 6
---
## Improving Accuracy By Defining Own Functions 

You can add your own [blocking functions](https://github.com/zinggAI/zingg/tree/main/core/src/main/java/zingg/hash) which will be evaluated by Zingg to build the [blocking tree.](zModels.md)

These business specific blocking functions go into [Hash Functions](https://github.com/zinggAI/zingg/tree/main/core/src/main/java/zingg/hash) and need to be added to [HashFunctionRegistry](https://github.com/zinggAI/zingg/blob/main/core/src/main/java/zingg/hash/HashFunctionRegistry.java) and [hash functions config](https://github.com/zinggAI/zingg/blob/main/core/src/main/resources/hashFunctions.json)

Also, for similarity, you can define your own measures. Each dataType has predefined features, for example [String](https://github.com/zinggAI/zingg/blob/main/core/src/main/java/zingg/feature/StringFeature.java) fuzzy type is configured for affine and jaro

You can define your own [comparisons](https://github.com/zinggAI/zingg/tree/main/core/src/main/java/zingg/similarity/function) and use them
