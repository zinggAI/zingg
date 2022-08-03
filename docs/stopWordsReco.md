# Stop Words

Zingg may help users by recommending stop words by extracting high-frequency words from the user data. The same can be generated using the [generateDocs](generatingDocumentation.md) command. the stop word recommendation files are written in folder zinggDir/modelId/stopWords. These files may be used after modification in [configuration](accuracy/stopWordsRemoval.md) to build a model.

By default, Zingg extracts 10% of the high-frequency unique words from a dataset. If the user wants a different selection, they should set up the following property in the config file:

```
stopWordsCutoff: <a value between 0 and 1>
```
