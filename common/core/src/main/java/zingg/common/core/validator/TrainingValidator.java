package zingg.common.core.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.core.ZinggBusinessException;
import zingg.common.core.executor.Trainer;
import zingg.common.client.ZFrame;

public class TrainingValidator {

    public static final Log LOG = LogFactory.getLog(Trainer.class);  

    public static <D, R, C> void validateTrainingData(ZFrame<D, R, C> trainingData, ZFrame<D, R, C> positiveData, ZFrame<D, R, C> negativeData) throws ZinggBusinessException {
        LOG.info("Validating training data with both positive and negative data...");
        
        long posCount = positiveData.count();
		LOG.warn("Training on positive pairs - " + posCount);
		long negCount = negativeData.count();
		LOG.warn("Training on negative pairs - " + negCount);

        if (trainingData == null || trainingData.isEmpty()) {
            throw new ZinggBusinessException("Training data is null. Add more training data.");
        }

        if (positiveData.isEmpty() || negativeData.isEmpty()) {
            throw new ZinggBusinessException("Unable to train as insufficient training data found. Both positive and negative data are required. Training data has " + posCount + " matches and " 
				+ negCount + " non matches. Please run findTrainingData and label till you have sufficient labelled data to build the models");
        }
    }

    public static <D, R, C> void validateTrainingData(ZFrame<D, R, C> trainingData) throws ZinggBusinessException {
        if (trainingData == null || trainingData.isEmpty()) {
            throw new ZinggBusinessException("Training data is null. Add more training data.");
        }
    }
}