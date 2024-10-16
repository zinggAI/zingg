# Documenting The Model

Zingg generates readable documentation about the training data, including those marked as matches, as well as non-matches. The documentation is written to the **zinggDir/modelId** folder and can be built using the following

```
./scripts/zingg.sh --phase generateDocs --conf <location to conf.json> <optional --showConcise=true|false>
```

The generated documentation file can be viewed in a browser and looks like as below:

![Training Data](../assets/documentation1.png)
