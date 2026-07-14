---
description: Configuring Zingg Enterprise For Snowflake 
---

# Match Configuration:  

Create the snowflake config file. This config file will contain the model locations, match types defined on fields, input and output tables in Snowflake. Please refer to **examples/febrl/configSnow.json** for a sample. Documentation for fields and match types is defined at [Zingg Field Definitions](https://docs.zingg.ai/zingg0.5.0/stepbystep/configuration/field-definitions).

Along with the changes to the field definitions, please do the following: 
- Give ‘modelId’ a name. An example could be 28NovDev.
- Change ‘INPUT_TABLE_NAME’ in data with the name of source table 
- Change ‘OUTPUT_TABLE_NAME’ in output with the name of output table 
