# Ignoring Commonly Occuring Words While Matching

Common words like Mr, Pvt, Av, St, Street etc. do not add differential signals and confuse matching. These words are called **stopwords** and matching is more accurate when stopwords are ignored.

The stopwords can be recommended by Zingg by invoking:

`./scripts/zingg.sh --phase recommend --conf <conf.json> --column <name of column to generate stop word recommendations>`

The stopwords generated are stored at the location - models/100/stopWords/<columnname>. By default, Zingg extracts 10% of the high-frequency unique words from a dataset. If the user wants a different selection, they should set up the following property in the config file:

```
stopWordsCutoff: <a value between 0 and 1>
```

Once you have verified the above stop words, you can configure them in the JSON variable **stopWords** with the path to the CSV file containing them. Please ensure while editing the CSV or building it manually that it should contain _one word per row_.

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

For Enterprise Snowflake, we will be having tables with the names - zingg/modelId/stopWords/<columnname> where we can see the list of stopwords associated with the respective column name.