---
description: >-
  Defining which field(s) if a match classifies two records as an exact match
---

# Obvious Duplicates

* Certain fields or combination of fields may mean an exact match for two records

* By configuring the obvious dupes condition the user ensures that such records always result in a match and together in a cluster

* This also gives better performance and score

# Configuration

* In the config.json file put the json element like this:

"obviousDupeCondition"	: "field1 & field2 | field3 | field4 & field5 & field6"

| => OR condition
& => AND condition

* The two records in above example will be considered an exact match if:

value of both field1 & field2 is exactly same in both records and both are not null
OR
value of field3 is not null and is exactly same in both records (e.g. something like SSN can't be same for two people)
OR
value of all 3 fields field4 & field5 & field6 is exactly same in both records and none of them is null
