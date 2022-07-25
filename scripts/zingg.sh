#!/bin/bash
#ZINGG_HOME=./assembly/target
ZINGG_JARS=$ZINGG_HOME/zingg-0.3.4-SNAPSHOT.jar
EMAIL=zingg@zingg.ai
LICENSE=zinggLicense.txt

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
else  
  EXECUTABLE="--class zingg.client.Client $ZINGG_JARS"
fi

# All the additional options must be added here
$SPARK_HOME/bin/spark-submit --master $SPARK_MASTER $PROPERTIES --conf spark.executor.extraJavaOptions="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -Xloggc:/tmp/memLog.txt -XX:+UseCompressedOops" --driver-class-path $ZINGG_JARS $EXECUTABLE $@ --email $EMAIL --license $LICENSE