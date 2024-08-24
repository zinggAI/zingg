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

If records across multiple sources have to be matched, the [link phase](./link.md) should be used.
