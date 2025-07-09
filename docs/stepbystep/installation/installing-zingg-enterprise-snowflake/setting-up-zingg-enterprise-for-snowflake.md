
# Installing Zingg Enterprise For Snowflake

Copy the release and license to a folder of your choice. Say directly under /home/ubuntu. Then execute the following:

> `gzip -d zingg-enterprise-snowflake-0.5.0-SNAPSHOT.tar.gz `

> `tar xvf zingg-enterprise-snowflake-0.5.0-SNAPSHOT.tar `

> `cd zingg-enterprise-snowflake-0.5.0-SNAPSHOT `

> `export ZINGG_SNOW_JAR=~/zingg-enterprise-snowflake-0.5.0-SNAPSHOT `

> `export ZINGG_SNOW_HOME=~/zingg-enterprise-snowflake-0.5.0-SNAPSHOT `

**It is better to keep ZINGG_SNOW_JAR and ZINGG_SNOW_HOME as part of the .bashrc for always having this value as part of the shell**

> `mv ~/zingg.license .  `
