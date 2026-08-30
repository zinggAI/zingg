---
description: >-
  Configure where Zingg saves trained models. zinggDir sets the base location (default /tmp/zingg), modelId identifies each model under that directory.
---

# Model Location

#### zinggDir

The **location** where trained models will be saved. Defaults to `/tmp/zingg`

#### modelId

An **identifier** for the model. You can train multiple models - say, one for **customers** matching _names_, _age_, and other personal details and one for **households** matching _addresses_. Each model gets saved under `zinggDir/modelId`
