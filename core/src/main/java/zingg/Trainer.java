package zingg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.model.Model;
import zingg.preprocess.StopWords;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.util.Analytics;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.util.Metric;

import zingg.util.BlockingTreeUtil;
import zingg.util.DSUtil;
import zingg.util.ModelUtil;
import zingg.util.PipeUtil;

public class Trainer extends ZinggBase{

	protected static String name = "zingg.Trainer";
	public static final Log LOG = LogFactory.getLog(Trainer.class);    

    public Trainer() {
        setZinggOptions(ZinggOptions.TRAIN);
    }

	public void execute() throws ZinggClientException {
        try {
			LOG.info("Reading inputs for training phase ...");
			LOG.info("Initializing learning similarity rules");
			
			Dataset<Row> positives = null;
			Dataset<Row> negatives = null;
			Dataset<Row> traOriginal = DSUtil.getTraining(spark, args);
			Dataset<Row> tra = StopWords.preprocessForStopWords(spark, args, traOriginal);
			tra = DSUtil.joinWithItself(tra, ColName.CLUSTER_COLUMN, true);
			tra = tra.cache();
			positives = tra.filter(tra.col(ColName.MATCH_FLAG_COL).equalTo(ColValues.MATCH_TYPE_MATCH));
			negatives = tra.filter(tra.col(ColName.MATCH_FLAG_COL).equalTo(ColValues.MATCH_TYPE_NOT_A_MATCH));
			
			verifyTraining(positives, negatives);

				
			Dataset<Row> testDataOriginal = PipeUtil.read(spark, true, args.getNumPartitions(), false, args.getData());
			Dataset<Row> testData = StopWords.preprocessForStopWords(spark, args, testDataOriginal);

			Tree<Canopy> blockingTree = BlockingTreeUtil.createBlockingTreeFromSample(testData,  positives, 0.5,
					-1, args, hashFunctions);
			if (blockingTree == null || blockingTree.getSubTrees() == null) {
				LOG.warn("Seems like no indexing rules have been learnt");
			}
			BlockingTreeUtil.writeBlockingTree(spark, ctx, blockingTree, args);
			LOG.info("Learnt indexing rules and saved output at " + args.getZinggDir());
			// model
			Model model = ModelUtil.createModel(positives, negatives, new Model(this.featurers), spark);
			model.save(args.getModel());
			LOG.info("Learnt similarity rules and saved output at " + args.getZinggDir());
			Analytics.track(Metric.TRAINING_MATCHES, Metric.approxCount(positives), args.getCollectMetrics());
			Analytics.track(Metric.TRAINING_NONMATCHES, Metric.approxCount(negatives), args.getCollectMetrics());
			LOG.info("Finished Learning phase");			
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
    }

	public void verifyTraining(Dataset<Row> positives, Dataset<Row> negatives) throws ZinggClientException {
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
