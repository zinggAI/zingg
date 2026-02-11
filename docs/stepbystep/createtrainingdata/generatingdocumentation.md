# Documenting The Training Data

We may need to send our labeled data for a subject matter expert to review. We may also like to understand and analyse our training data. In such scenarios, we can document the records we have marked during the **label** phase by invoking the **generateDocs** phase.&#x20;

During **generateDocs**, Zingg generates readable documentation about the training data, including those marked as matches, as well as non-matches. The documentation is written to the **zinggDir/modelId** folder and can be built using the following

```
./scripts/zingg.sh --phase generateDocs --conf <location to conf.json> <optional --showConcise=true|false>
```

The generated documentation file can be viewed in a browser and looks like as below:

![Training Data](../../.gitbook/assets/documentation.png)
