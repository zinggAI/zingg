---
layout: default
title: Find the matches
parent: Step By Step Guide
nav_order: 8
---

### match
Finds the records which match with each other. 

`./zingg.sh --phase match --conf config.json`

As can be seen in the image below, matching records are given the same z_cluster id. Each record also gets a z_minScore and z_maxScore which shows the least/greatest it matched with other records in the same cluster. 

![Match results](/assets/match.gif)

The match results in CSV format will be saved in '/tmp/zinggOutput'.
To view top 100 match results sorted by z_cluster in console, use
```
$ pyspark
_oss = spark.read.option("header", True).csv('/tmp/zinggOutput')
3:50
z_oss = z_oss.sort('z_cluster')
3:50
z_oss.count()
3:51
z_oss.show(100)
```

If records across multiple sources have to be matched, the [link phase](./link.md) should be used.
