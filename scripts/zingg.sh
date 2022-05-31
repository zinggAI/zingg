#!/bin/bash
#ZINGG_HOME=./assembly/target
ZINGG_JARS=$ZINGG_HOME/zingg-0.3.4-SNAPSHOT.jar
EMAIL=xxx@yyy.com
LICENSE="test"
# Dictionary of phases written in python
declare -A  PYTHON_PHASES=(["assessModel"]="api/python/FebrlExample.py" \
						)

if [[ -z "${ZINGG_EXTRA_JARS}" ]]; then
	OPTION_JARS=""
else
	OPTION_JARS="--jars ${ZINGG_EXTRA_JARS}"
fi

function read_zingg_conf() {
	CONF_PROPS=""

	ZINGG_CONF_DIR="$(cd "`dirname "$0"`"/../config; pwd)"

	file="${ZINGG_CONF_DIR}/zingg.conf"
	# Leading blanks removed; comment Lines, blank lines removed
	PROPERTIES=$(sed 's/^[[:blank:]]*//;s/#.*//;/^[[:space:]]*$/d' $file)

	while IFS='=' read -r key value; do
		# Trim leading and trailing spaces
		key=$(echo $key | sed 's/^[[:blank:]]*//;s/[[:blank:]]*$//;')
		value=$(echo $value | sed 's/^[[:blank:]]*//;s/[[:blank:]]*$//;')
		if [[ $key == spark* ]]; then
			# Append to conf variable
			CONF_PROPS+=" --conf ${key}=${value}"
		else
			# Add to env var list
			ENVIRONMENT_VARS[${key}]=${value}
		fi;
	done <<< "$(echo -e "$PROPERTIES")"
}

function is_python_phase() {
	for x in ${!PYTHON_PHASES[@]}; do
		if [ $x = $1 ]; then
			# echo "The phase is listed as a python one : ${1}"
			return 0;
		fi
	done
	return 1;
}

# go through all the arguments. Find '--phase' and check if it is python phase
POSITIONAL_ARGS=() # Need to save all the arguments in the list
while [[ $# -gt 0 ]]; do
	case $1 in
		--phase)
			PHASE="$2"
			is_python_phase $PHASE # check if it is a python phase
			RUN_PYTHON_PHASE=$? # store the return status
			if [[ $RUN_PYTHON_PHASE -eq 0 ]]; then
				PYTHON_PHASE_EXE=${PYTHON_PHASES[$PHASE]}
			fi
			POSITIONAL_ARGS+=("$1") # save positional arg
			POSITIONAL_ARGS+=("$2") # save positional arg
			shift # past argument --phase
			shift # past argument <value>
			;;
		*)
			POSITIONAL_ARGS+=("$1") # save positional arg
			shift # past argument
			;;
	esac
done
set -- "${POSITIONAL_ARGS[@]}" # restore positional parameters

declare -A ENVIRONMENT_VARS
read_zingg_conf

# echo "${!ENVIRONMENT_VARS[@]}"
# echo "${ENVIRONMENT_VARS[@]}"
# echo ${CONF_PROPS}

for x in ${!ENVIRONMENT_VARS[@]}; do
	eval ${x}=${ENVIRONMENT_VARS[${x}]}
done

OPTION_SPARK_CONF=${CONF_PROPS}


# if it is a python phase
if [[ $RUN_PYTHON_PHASE -eq 0 ]]; then
	echo "Executing python phase: ${PHASE} (${PYTHON_PHASE_EXE})"
	$SPARK_HOME/bin/spark-submit --master $SPARK_MASTER ${OPTION_SPARK_CONF} --jars=$ZINGG_JARS --conf spark.driver.extraClassPath=$ZINGG_JARS --conf spark.executor.extraClassPath=$ZINGG_JARS $PYTHON_PHASE_EXE $@ --email $EMAIL --license $LICENSE
	exit 0
fi

# All the additional options must be added here
ALL_OPTIONS=" ${OPTION_JARS} ${OPTION_SPARK_CONF} "
$SPARK_HOME/bin/spark-submit --master $SPARK_MASTER ${ALL_OPTIONS}  --conf spark.executor.extraJavaOptions="-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -Xloggc:/tmp/memLog.txt -XX:+UseCompressedOops" --driver-class-path $ZINGG_JARS --class zingg.client.Client $ZINGG_JARS $@ --email $EMAIL --license $LICENSE 
