#phases to test
FIND_TRAINING_DATA = "findTrainingData"
LABEL = "label"
MATCH = "match"
INCREMENTAL = "runIncrement"

#load file config to test on
config_65 = "examples/febrl120k/config.json"
config_120k = "examples/febrl120k/config120k.json"


#bash script location
ZINGG = "scripts/zingg.sh"


#add all the load to test
load_configs = {"65_samples" : config_65}
#add all the phases on which testing is required
phases = [FIND_TRAINING_DATA, MATCH]