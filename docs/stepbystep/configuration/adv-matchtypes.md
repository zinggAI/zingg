---
description: Defining match types to match better and faster
---

# User Defined Mapping Match Types

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

**Leveraging domain expertise to push matching accuracy. Also save time massaging data before matching.**

While the Zingg matching algorithms learn the variations from the user labels, completely different attributes pose a challenge to matching accuracy. In the case of nicknames, or company abberviations, domain input can greatly enhance matching accuracy. Zingg's `MAPPING` match type is built for such cases. &#x20;

To leverage user expertise for nicknames for example, the user can supply Zingg with a mapping json file `nicknames.json` which denotes nicknames. Zingg has prebuilt mappings for company names, nicknames etc. Or you can define your own mappings too.&#x20;

Here is the structure of the json:



<pre class="language-json"><code class="lang-json"><strong>[  
</strong>  ["Will", "Bill", "William"],
  ["John", "Johnny", "Jack"],
  ["Robert", "Rob", "Bob", "Bobby"],
  ["Charles", "Charlie", "Chuck"],
  ["James", "Jim", "Jimmy"],
  ["Thomas", "Tom", "Tommy"]
...
]   
</code></pre>

Each line here represents common nicknames which represent the same name.&#x20;

To use this mapping within Zingg, define the field's match type as `MAPPING_<filename>` which in our case would be `mapping_nicknames`

```json
"fieldDefinition":[
   	{
   		"fieldName" : "name",
   		"matchType" : "mapping_nicknames, fuzzy",
   		"fields" : "name",
   		"dataType": "string"
   	}
```

### Transform and Match

The `MAPPING` match type can also be used to transform and normalise categorical data. Let us say different data sources have different representations of gender. In one, gender is represented as M and F, in another it is noted as Male, Female and in the third as 1 and 2. Instead of transforming the gender column beforehand, one could create a mapping json called `gender.json` .&#x20;

<pre><code><strong>[  
</strong>  ["M", "Male", "1"],
  ["F", "Female", "2"]
]  
</code></pre>

When we use it for the gender field like below, Zingg would automatically handle the transfomration so that you dont have to.&#x20;

```
"fieldDefinition":[
   	{
   		"fieldName" : "gender",
   		"matchType" : "mapping_gender, exact",
   		"fields" : "gender",
   		"dataType": "string"
   	},
```

[^1]: Zingg Enterprise is an advance version of Zingg Community with production grade features
