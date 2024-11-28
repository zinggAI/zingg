---
description: To verify that Zingg ENterprise installation works perfectly
---

# Verifying The Installation

Let us now run a sample program to ensure that our installation is correct.

> `./scripts/zingg.sh --properties-file snowEnv.txt --phase findTrainingData --conf  examples/febrl/configSnow.json `

The above will build Zingg models and use that to find duplicates in the **examples/febrl/test.csv** file. You will see Zingg logs on the console and once the job finishes, you will see tables being formed with the name **UNIFIED_CUSTOMERS_MODELID** with matching records sharing the same _cluster id_.

Congratulations, Zingg has been installed!