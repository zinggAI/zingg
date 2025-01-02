---
description: To ensure Zingg works as a background process
---

# Running Asynchronously For Long Duration Jobs:

Using no-hup mode, we can run Zingg as a background process and even if SSH is broken it will continue to work. 

> `nohup ./scripts/zingg.sh --properties-file ~/zingg/snowEnv.txt --phase findTrainingData --conf ~/zingg/snowConfigFile.json & `

We can see the logs of scripts by running the following command: 

> `tail -f nohup.out `