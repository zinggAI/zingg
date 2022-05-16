#### ZINGG_EXTRA_JARS #############################################
# The ZINGG_EXTRA_JARS variable is set to pass additional dependencies to ZINGG. This env variable must be set while working with BigQuery, Snowflake, Mysql etc.
# multiple jars should be separated by comma
# e.g. ZINGG_EXTRA_JARS=one.jar,two.jar,three.jar
#ZINGG_EXTRA_JARS=


#### ZINGG_EXTRA_SPARK_CONF #######################################
# The ZINGG_EXTRA_SPARK_CONF variable must be set to pass additional parameters to pass to spark. For example, when running Zingg with BigQuery.
# the variable must be in below format. Multiple 'conf' entries can be combined and passed in this variable.
# e.g. ZINGG_EXTRA_SPARK_CONF="--conf spark.hadoop.fs.gs.impl=com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem"
#ZINGG_EXTRA_SPARK_CONF=

#General
EMAIL=xxx@yyy.com
LICENSE="test"
SPARK_MEM=10g