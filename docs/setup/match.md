---
title: Find the matches
parent: Step By Step Guide
nav_order: 9
---

# Finding The Matches

## Finds the records that match with each other.

`./zingg.sh --phase match --conf config.json`

As can be seen in the image below, matching records are given the same **z\_cluster** id. Each record also gets a **z\_minScore** and **z\_maxScore** which shows the _least/greatest_ it matched with other records in the same cluster.

![Match results](../../assets/match.gif)

If records across multiple sources have to be matched, the [link phase](link.md) should be used.
