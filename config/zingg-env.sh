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


#### SPARK_EXECUTOR_MEMORY ########################################
# The SPARK_EXECUTOR_MEMORY variable updates spark.executor.memory. It may be modified based on memory available in the system. Default is 8GB
SPARK_EXECUTOR_MEMORY=8g

#### SPARK_DRIVER_MEMORY ##########################################
# The SPARK_DRIVER_MEMORY variable updates spark.driver.memory. It may be modified based on memory available in the system. Default is 8GB.
SPARK_DRIVER_MEMORY=8g
