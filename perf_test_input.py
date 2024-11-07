#phases to test
FIND_TRAINING_DATA = "findTrainingData"
LABEL = "label"
MATCH = "match"
INCREMENTAL = "runIncrement"

#load file config to test on
LOAD_65 = "examples/febrl120k/config.json"
LOAD_120k = "examples/febrl120k/config120k.json"


#bash script location
ZINGG = "scripts/zingg.sh"


#add all the load to test
load_configs = {LOAD_65 : "LOAD_65", LOAD_120k: "LOAD_120k"}
#add all the phases on which testing is required
phases = [FIND_TRAINING_DATA, MATCH]
