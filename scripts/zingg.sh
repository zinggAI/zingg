#!/bin/bash
ZINGG_JARS=$ZINGG_HOME/zingg-0.6.0.jar
EMAIL=zingg@zingg.ai
LICENSE=zinggLicense.txt
log4j_setting="-Dlog4j2.configurationFile=file:log4j2.properties"

export SESSION_TYPE="PY4J"
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
			  export SESSION_TYPE="CLUSTER"
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
	GC_LOG_DIR="${GC_LOG_DIR:-/tmp}"
	GC_LOG_OPTS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${GC_LOG_DIR}/heapdump-%p.hprof -Xlog:gc*:file=${GC_LOG_DIR}/memLog.txt:time,uptime,level,tags"
	export SPARK_SUBMIT_OPTS="${SPARK_SUBMIT_OPTS:-} $log4j_setting $GC_LOG_OPTS"
  export SPARK_DRIVER_MEMORY="${SPARK_DRIVER_MEMORY:-4g}"
	$SPARK_HOME/bin/spark-submit \
	  --master "$SPARK_MASTER" \
	  $PROPERTIES \
	  --files "./log4j2.properties" \
	  --driver-java-options "$SPARK_JAVA_OPTS" \
	  --conf "spark.executor.extraJavaOptions=$SPARK_JAVA_OPTS" \
    $LOGGING \
    --driver-class-path "$ZINGG_JARS" \
    "$EXECUTABLE" \
    "$@" \
    --email "$EMAIL" \
    --license "$LICENSE"
fi
