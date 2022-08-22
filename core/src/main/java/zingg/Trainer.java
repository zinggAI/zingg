package zingg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.model.Model;
import zingg.preprocess.StopWords;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.util.Analytics;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.util.Metric;
import zingg.util.DSUtil;
import zingg.util.ModelUtil;
import zingg.util.PipeUtilBase;

public abstract class Trainer<S,D,R,C,T1,T2> extends ZinggBase<S,D,R,C,T1,T2>{

	protected static String name = "zingg.Trainer";
	public static final Log LOG = LogFactory.getLog(Trainer.class);    

    
	public void execute() throws ZinggClientException {
        try {
			LOG.info("Reading inputs for training phase ...");
			LOG.info("Initializing learning similarity rules");
			
			ZFrame<D,R,C> positives = null;
			ZFrame<D,R,C> negatives = null;
			ZFrame<D,R,C> traOriginal = getDSUtil().getTraining(args);
			ZFrame<D,R,C> tra = StopWords.preprocessForStopWords(args, traOriginal);
			tra = getDSUtil().joinWithItself(tra, ColName.CLUSTER_COLUMN, true);
			tra = tra.cache();
			positives = tra.filter(tra.col(ColName.MATCH_FLAG_COL).equalTo(ColValues.MATCH_TYPE_MATCH));
			negatives = tra.filter(tra.col(ColName.MATCH_FLAG_COL).equalTo(ColValues.MATCH_TYPE_NOT_A_MATCH));
			
			verifyTraining(positives, negatives);

				
			ZFrame<D,R,C> testDataOriginal = getPipeUtil().read(true, args.getNumPartitions(), false, args.getData());
			ZFrame<D,R,C> testData = StopWords.preprocessForStopWords(args, testDataOriginal);

			Tree<Canopy<R>> blockingTree = getBlockingTreeUtil().createBlockingTreeFromSample(testData,  positives, 0.5,
					-1, args, hashFunctions);
			if (blockingTree == null || blockingTree.getSubTrees() == null) {
				LOG.warn("Seems like no indexing rules have been learnt");
			}
			getBlockingTreeUtil().writeBlockingTree(blockingTree, args);
			LOG.info("Learnt indexing rules and saved output at " + args.getZinggDir());
			// model
			Model<S,D,R,C> model = getModelUtil().createModel(positives, negatives, this.featurers, getContext(), false);
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

	public void verifyTraining(ZFrame<D,R,C> positives, ZFrame<D,R,C> negatives) throws ZinggClientException {
		if (positives == null) {
			throw new ZinggClientException("Unable to train as insufficient positive training data found. ");
		}
		if (negatives == null) {
			throw new ZinggClientException("Unable to train as insufficient negative training data found. ");
	
		}
		long posCount = positives.count();
		LOG.warn("Training on positive pairs - " + posCount);
		long negCount = negatives.count();
		LOG.warn("Training on negative pairs - " + negCount);

		if (posCount < 5 || negCount < 5)  
			throw new ZinggClientException("Unable to train as insufficient training data found. Training data has " + posCount + " matches and " 
				+ negCount + " non matches. Please run findTrainingData and label till you have sufficient labelled data to build the models");

	}

		    
}
