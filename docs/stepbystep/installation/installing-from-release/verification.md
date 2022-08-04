---
description: To verify the Zingg installation works fine
---

# Verifying The Installation

Run bash and print the aliases to ensure that they are set correctly.

> `bash`

> `echo $SPARK_HOME`

> `echo $JAVA_HOME`

> `java --version`

> `echo $ZINGG_HOME`

Let us now run a sample program to ensure that our installation is correct.

> `cd zingg`

> `./scripts/zingg.sh --phase match --conf examples/febrl/config.json`

The above will find duplicates in the examples/febl/test.csv file. You will see Zingg logs on the console and once the job finishes, you will see some files under /tmp/zinggOutput with matching records sharing the same cluster id.

Congratulations, Zingg has been installed!
