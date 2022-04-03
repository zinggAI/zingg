
## Removing Stop Words from the data

 In order to remove stopwords from the data, configure json variable **"stopWords"** with a csv file containing stop words inside configuration file's Field Definition block. The csv file should contain one stop word in each row.

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

Zingg also recommends likely stop words in the data. The same can be generated using [generateDocs](../generatingDocumentation.md) command.
