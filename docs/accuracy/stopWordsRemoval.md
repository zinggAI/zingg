# Ignoring Commonly Occuring Words While Matching

Common words like Mr, Pvt, Av, St, Street etc. do not add differential signals and confuse matching. These words are called **stopwords** and matching is more accurate when stopwords are ignored.

The stopwords can be recommended by Zingg by invoking:

`./scripts/zingg.sh --phase recommend --conf <conf.json> --column <name of column to generate stopword recommendations>`

The stopwords generated are stored at the location - models/100/stopWords/columnName. This will give you the list of stopwords along with their frequency.

By default, Zingg extracts 10% of the high-frequency unique words from a dataset. If the user wants a different selection, they should set up the following property in the config file under the respective field for which they want stopwords:

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

For recommending stopwords in **Zingg Enterprise Snowflake**, 

`./scripts/zingg.sh --phase recommend --conf <conf.json> --properties-file <path to Snowflake properties file> --column <name of column to generate stopword recommendations>`

The stopwords generated are stored in the table - zingg_stopWords_columnName_modelId where we can see the list of stopwords and their frequency associated with the given column name.