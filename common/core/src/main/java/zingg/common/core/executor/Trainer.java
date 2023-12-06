package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.core.ZinggBusinessException;
import zingg.common.core.block.Canopy;
import zingg.common.core.block.Tree;
import zingg.common.core.model.Model;
import zingg.common.core.util.Analytics;
import zingg.common.core.util.Metric;
import zingg.common.core.validator.TrainingValidator;
import zingg.common.core.preprocess.StopWordsRemover;

public abstract class Trainer<S,D,R,C,T> extends ZinggBase<S,D,R,C,T>{

	protected static String name = "zingg.Trainer";
	public static final Log LOG = LogFactory.getLog(Trainer.class);    

    
	public void execute() throws ZinggClientException {
        try {
			LOG.info("Reading inputs for training phase ...");
			LOG.info("Initializing learning similarity rules");
			
			ZFrame<D,R,C> positives = null;
			ZFrame<D,R,C> negatives = null;
			ZFrame<D,R,C> traOriginal = getDSUtil().getTraining(getPipeUtil(), args);
			ZFrame<D,R,C> tra = getStopWords().preprocessForStopWords(traOriginal);
			tra = getDSUtil().joinWithItself(tra, ColName.CLUSTER_COLUMN, true);
			tra = tra.cache();
			positives = tra.filter(tra.equalTo(ColName.MATCH_FLAG_COL,ColValues.MATCH_TYPE_MATCH));
			negatives = tra.filter(tra.equalTo(ColName.MATCH_FLAG_COL,ColValues.MATCH_TYPE_NOT_A_MATCH));
			
			verifyTraining(tra, positives, negatives);

				
			ZFrame<D,R,C> testDataOriginal = getPipeUtil().read(true, args.getNumPartitions(), false, args.getData());
			LOG.debug("testDataOriginal schema is " +testDataOriginal.showSchema());
			ZFrame<D,R,C> testData = getStopWords().preprocessForStopWords(testDataOriginal);

			Tree<Canopy<R>> blockingTree = getBlockingTreeUtil().createBlockingTreeFromSample(testData,  positives, 0.5,
					-1, args, getHashUtil().getHashFunctionList());
			if (blockingTree == null || blockingTree.getSubTrees() == null) {
				LOG.warn("Seems like no indexing rules have been learnt");
			}
			getBlockingTreeUtil().writeBlockingTree(blockingTree, args);
			LOG.info("Learnt indexing rules and saved output at " + args.getZinggDir());
			// model
			Model<S,T,D,R,C> model = getModelUtil().createModel(positives, negatives, false, args);
			model.save(args.getModel());
			LOG.info("Learnt similarity rules and saved output at " + args.getZinggDir());
			Analytics.track(Metric.TRAINING_MATCHES, positives.count(), args.getCollectMetrics());
			Analytics.track(Metric.TRAINING_NONMATCHES, negatives.count(), args.getCollectMetrics());
			LOG.info("Finished Learning phase");			
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
    }

	public void verifyTraining(ZFrame<D, R, C> tra, ZFrame<D,R,C> positives, ZFrame<D,R,C> negatives) throws ZinggBusinessException {
		if (positives.isEmpty()) {
			LOG.warn("Unable to train as insufficient positive training data found. ");
			TrainingValidator.validateTrainingData(tra, positives, negatives);
		}
		if (negatives.isEmpty()) {
			LOG.warn("Unable to train as insufficient negative training data found. ");
			TrainingValidator.validateTrainingData(tra, positives, negatives);
		}
		long posCount = positives.count();
		LOG.warn("Training on positive pairs - " + posCount);
		long negCount = negatives.count();
		LOG.warn("Training on negative pairs - " + negCount);

		if ((posCount < 5 || negCount < 5) ||  (posCount == 5 && negCount == 5))
			throw new ZinggBusinessException("Unable to train as insufficient training data found. Training data has " + posCount + " matches and " 
				+ negCount + " non matches. Please run findTrainingData and label till you have sufficient labelled data to build the models");

	}

    protected abstract StopWordsRemover<S,D,R,C,T> getStopWords();
		    
}
