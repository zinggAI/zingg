# Ignoring Commonly Occuring Words While Matching

Common words like Mr, Pvt, Av, St, Street etc do not add differential signal and confuse matching. These words are called stopwords and matching is more accurate when stopwrods are ignored.

In order to remove stopwords from a field, configure&#x20;

The stopwords can be recommended by Zingg by invoking

`./scripts/zingg.sh --phase recommend --conf <conf.json> --column <column>`&#x20;

By default, Zingg extracts 10% of the high-frequency unique words from a dataset. If the user wants a different selection, they should set up the following property in the config file:

```
stopWordsCutoff: <a value between 0 and 1>
```

Once you have verified the above stop words, you can configure them in the JSON variable **stopWords** with the path to the CSV file containing them. Please ensure while editing the CSV or building it manually that it should contain one word per row.

```
"fieldDefinition":[
   	{
   		"fieldName" : "fname",
   		"matchType" : "fuzzy",
   		"fields" : "fname",
   		"dataType": "string",
   		"stopWords": "models/100/stopWords/fname.csv"
   	},
```

```
Please also configure in the JSON variable **preprocessors** that you want to preprocess your data by removing stop words:
"preprocessors": ["stopWords"],
```

