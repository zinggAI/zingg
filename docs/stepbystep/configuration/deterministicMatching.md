---
description: >-
  Defining which field(s) if a match classifies two records as an exact match
---

# Deterministic Matchings

* Certain fields or combination of fields may mean an exact match for two records

* By configuring the deterministic matching condition the user ensures that such records always result in a match and together in a cluster

* This also gives better performance and score

# Configuration

* In the config.json file put the json element like this:

"deterministicMatchingCondition"	: "[
    {
        "matchCondition": [
            {"fieldName": "field1"},
            {"fieldName": "field2"}
        ]
    },
    {
        "matchCondition": [
            {"fieldName": "field3"}
        ]
    },
    {
        "matchCondition": [
            {"fieldName": "field4"},
            {"fieldName": "field5"},
            {"fieldName": "field6"}
        ]
    }
]"

OR condition between 2 matchConditions
AND condition between fieldNames of same matchCondition

* The two records in above example will be considered an exact match if:

value of both field1 & field2 is exactly same in both records and both are not null
OR
value of field3 is not null and is exactly same in both records (e.g. something like SSN can't be same for two people)
OR
value of all 3 fields field4 & field5 & field6 is exactly same in both records and none of them is null
