---
title: Relations
parent: Step By Step Guide
nav_order: 14
description: When a single match model is not sufficient
---

# Combining Different Match Models

[Zingg Enterprise Feature](#user-content-fn-1)[^1]

In many cases, we want to build the identity graph using a combination of different datasets, schemas and matching logic. An example could be having a source system which only contains userids and emails, another one wtih user name and phone numbers and a few others with person information with addresses. Another example could be some systems capturing spousal information,  but others to be matched on the basis of lastname and address.&#x20;

In such cases, Zingg can build the entire graph and relate different models together. In the following case, results of a query with exact match on family Id and a matching model(household) using address and lastname are brought together.&#x20;

````json
```
{ 
    "vertices" : 
    [ 
        { 
            "name" : "spouse",  
            "vertexType" : "zingg_pipe", 
            "data" : [
                {
                "name" : "spouse", 
                "format" : "snowflake", 
                "props": {
                        "query": "select a.id as id, a.FNAME, a.LNAME, a.STNO, a.ADD1, a.CITY, a.STATE, a.ZINGG_ID_PERSON, b.id as z_id, b.fname as Z_FNAME,b.lname as Z_LNAME,b.stno as Z_STNO,b.add1 as Z_ADD1, b.city as Z_CITY,b.state as Z_STATE, b.ZINGG_ID_PERSON as Z_ZINGG_ID_PERSON from CUSTOMER_RELATE_PARTIAL a, CUSTOMER_RELATE_PARTIAL b where a.familyId = b.familyId"
                        }
                }
                ],
            "edges" :  
            {   "edgeType" : "same_edge",
                "edges":[
                    {
                        "dataColumn" : "zingg_personId",
                        "column" : "zingg_personId",
                        "name" : "zingg_personId1"
                    },
                    {
                        "dataColumn" : "zingg_personId",
                        "column" : "z_zingg_personId",
                        "name" : "zingg_personId2"
                    }
                ]
            }
        },
        { 
            "name" : "household",
            "config" : "$ZINGG_ENTERPRISE_HOME$/zinggEnterprise/configHousehold.json", 
            "strategy" : {
                "vDataStrategy" : "unique_edge",
                "props" : {
                        "column" : "zingg_personId",
                        "edge" : "zingg_personId,z_zingg_personId"
                    }
            },
            "vertexType" : "zingg_match", 
             "edges" :  
            {   "edgeType" : "same_edge",
                "edges":[
                    {
                        "dataColumn" : "zingg_personId",
                        "column" : "zingg_personId",
                        "name" : "zingg_personId1"
                    },
                    {
                        "dataColumn" : "zingg_personId",
                        "column" : "z_zingg_personId",
                        "name" : "zingg_personId2"
                    }
                ]
            }
        }
    ],
    "output" : [{
        "name":"relatedCustomers", 
        "format":"snowflake", 
        "props": {
            "table": "RELATED_CUSTOMERS_PARTIAL"
            }
    }],
    "strategy":"pairs_and_vertices"
}


```
````

[^1]: Zingg Enterprise is the suite of proprietary products licensed by Zingg. Please refer to https://www.zingg.ai/product/zingg-entity-resolution-compare-versions for individual tier features.
