---
layout: default
nav_order: 6
---
## Generating Documentation
Zingg generates readable documentation about the training data, including those marked as matches as non matches. The documentation is written to the zinggDir/modelId folder and can be built using the following

    ./scripts/zingg.sh --phase generateDocs --conf <location to conf.json>

The generated documentation file can be viewed in a browser and looks like as below.

![Training Data](/assets/documentation1.png)

To view Zingg recommendations for stop words, click on the column header of that field in above documentation. Stop word recommendation files are written in folder zinggDir/modelId/stopWords
