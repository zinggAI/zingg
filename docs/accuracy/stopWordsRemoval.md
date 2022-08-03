# Preparing data with Stop Words Removal

In order to remove stopwords from the data, configure the JSON variable **"stopWords"** with a CSV file containing stop words inside the configuration file's Field Definition block. The CSV file should contain one stop word in each row.

```
"fieldDefinition":[
   	{
   		"fieldName" : "fname",
   		"matchType" : "fuzzy",
   		"fields" : "fname",
   		"dataType": "\"string\"",
   		"stopWords": "models/100/stopWords/fname.csv"
   	},
```

Zingg also recommends likely stop words in the data. The same can be generated using the [generateDocs](../generatingDocumentation.md) command.
