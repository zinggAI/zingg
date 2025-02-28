#phases to test
FIND_TRAINING_DATA = "findTrainingData"
LABEL = "label"
TRAIN = "train"
MATCH = "match"

#load file config to test on
febrl = "./examples/febrl120k/config.json"
febrl_120k = "./examples/febrl120k/config120k.json"
ncVoter_5m = "./examples/ncVoters5M/config.json"


#bash script location
ZINGG = "./scripts/zingg.sh"


#add all the load to test
load_configs = {"65_samples" : febrl, "120k_samples" : febrl_120k, "5m_samples" : ncVoter_5m}
#add all the phases on which testing is required
phases = [TRAIN, MATCH]
