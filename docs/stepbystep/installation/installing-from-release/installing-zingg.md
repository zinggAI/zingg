---
description: Downloading and setting things up
---

# Installing Zingg

Download the tar zingg-version.tar.gz from the [Zingg releases page](https://github.com/zinggAI/zingg/releases) to a folder of your choice and run the following:

> gzip -d zingg-0.4.1.tar.gz ; tar xvf zingg-0.4.1.tar

This will create a folder zingg-0.4.1 under the chosen folder.

Move the above folder to zingg.

> mv zingg-0.4.1 \~/zingg

> export ZINGG\_HOME=path to zingg

> export PATH=$PATH:$JAVA\_HOME/bin:$SPARK\_HOME/bin:$SPARK\_HOME/sbin:ZINGG\_HOME/scripts
