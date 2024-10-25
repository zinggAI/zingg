#phases to test
FIND_TRAINING_DATA = "findTrainingData"
LABEL = "label"
MATCH = "match"
INCREMENTAL = "runIncrement"

#load file config to test on
LOAD_65 = "/home/administrator/zingg/zinggEnterprise/spark/examples/febrl/config.json"
LOAD_120k = "/home/administrator/zingg/zinggEnterprise/spark/examples/febrl120k/config120k.json"


#bash script location
ZINGG = "/home/administrator/zingg/zinggEnterprise/spark/scripts/zingg.sh"


#add all the load to test
load_configs = {LOAD_65 : "LOAD_65"}
#add all the phases on which testing is required
phases = [FIND_TRAINING_DATA]
