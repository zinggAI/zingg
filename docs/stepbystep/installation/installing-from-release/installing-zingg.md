---
description: Downloading and setting things up
---

# Installing Zingg

Download the tar zingg-version.tar.gz from the [Zingg releases page](https://github.com/zinggAI/zingg/releases) to a folder of your choice and run the following:

> `gzip -d zingg-0.5.0.tar.gz ; tar xvf zingg-0.5.0.tar`

This will create a folder *zingg-0.5.0* under the chosen folder.

Move the above folder to zingg.

> `mv zingg-0.5.0 ~/zingg`

> `export ZINGG_HOME=path to zingg`

> `export PATH=$PATH:$JAVA_HOME/bin:$SPARK_HOME/bin:$SPARK_HOME/sbin:$ZINGG_HOME/scripts`
