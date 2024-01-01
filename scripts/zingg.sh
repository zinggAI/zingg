#!/bin/bash
#ZINGG_HOME=./assembly/target
ZINGG_JARS=$ZINGG_HOME/zingg-0.4.1.jar
EMAIL=zingg@zingg.ai
LICENSE=zinggLicense.txt
log4j_setting="-Dlog4j2.configurationFile=file:log4j2.properties"

POSITIONAL_ARGS=() # Need to save all the arguments in the list
while [[ $# -gt 0 ]]; do
	case $1 in
		--properties-file)
  			PROPERTIES_FILE="$2"
        PROPERTIES="--properties-file $PROPERTIES_FILE"
        shift
		shift
			;;
		--run)
			# this option is to run a user script (python)
			  RUN_PYTHON_PHASE=1
			  PYTHON_SCRIPT="$2"
			  shift # past argument "run"
			  shift
			;;
		--run-databricks)
			# this option is to run a user script (python)
			  RUN_PYTHON_DB_CONNECT_PHASE=1
			  PYTHON_SCRIPT_DB_CONNECT="$2"
			  shift # past argument "run-databricks"
			  shift
			;;			
                --log)
			LOG_FILE=$2
			LOGGING="--files $LOG_FILE"
        		shift
				shift
					;;

		*)
			POSITIONAL_ARGS+=("$1") # save positional arg
			shift # past argument
			;;
	esac
done
set -- "${POSITIONAL_ARGS[@]}" # restore positional parameters
#echo $PROPERTIES
#echo $PROPERTIES_FILE
#echo $@
#echo $RUN_PYTHON_PHASE

# if it is a python phase
if [[ $RUN_PYTHON_PHASE -eq 1 ]]; then
  EXECUTABLE="$PYTHON_SCRIPT"
elif [[ $RUN_PYTHON_DB_CONNECT_PHASE -eq 1 ]]; then
  EXECUTABLE="$PYTHON_SCRIPT_DB_CONNECT"
else  
  EXECUTABLE="--class zingg.spark.client.SparkClient $ZINGG_JARS"
fi

if [[ $RUN_PYTHON_DB_CONNECT_PHASE -eq 1 ]]; then
	unset SPARK_MASTER
	unset SPARK_HOME
	export DATABRICKS_CONNECT=Y
	python $EXECUTABLE
else
	# All the additional options must be added here
	$SPARK_HOME/bin/spark-submit --master $SPARK_MASTER $PROPERTIES --files "./log4j2.properties" --conf spark.executor.extraJavaOptions="$log4j_setting -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -Xloggc:/tmp/memLog.txt -XX:+UseCompressedOops" --conf spark.driver.extraJavaOptions="$log4j_setting" $LOGGING --driver-class-path $ZINGG_JARS $EXECUTABLE $@ --email $EMAIL --license $LICENSE
fi
