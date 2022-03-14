package zingg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Functions;
import com.snowflake.snowpark_java.Row;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.model.Model;

import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.util.Analytics;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;

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
			
			DataFrame positives = null;
			DataFrame negatives = null;
			DataFrame tra = DSUtil.getTraining(snow, args);
			tra = DSUtil.joinWithItself(tra, ColName.CLUSTER_COLUMN, true);
			tra = tra.cacheResult();
			positives = tra.filter(tra.col(ColName.MATCH_FLAG_COL).equal_to(Functions.lit(ColValues.MATCH_TYPE_MATCH)));
			negatives = tra.filter(tra.col(ColName.MATCH_FLAG_COL).equal_to(Functions.lit(ColValues.MATCH_TYPE_NOT_A_MATCH)));
			LOG.warn("Training on positive pairs - " + positives.count());
			LOG.warn("Training on negative pairs - " + negatives.count());
				
			DataFrame testData = PipeUtil.read(snow, true, args.getNumPartitions(), false, args.getData());
			Tree<Canopy> blockingTree = BlockingTreeUtil.createBlockingTreeFromSample(testData,  positives, 0.5,
					-1, args, hashFunctions);
			if (blockingTree == null || blockingTree.getSubTrees() == null) {
				LOG.warn("Seems like no indexing rules have been learnt");
			}
			BlockingTreeUtil.writeBlockingTree(snow, blockingTree, args);
			LOG.info("Learnt indexing rules and saved output at " + args.getZinggDir());
			// model
			Model model = ModelUtil.createModel(positives, negatives, new Model(this.featurers), snow);
			model.save(args.getModel());
			LOG.info("Learnt similarity rules and saved output at " + args.getZinggDir());
			//Analytics.track(Metric.TRAINING_MATCHES, Metric.approxCount(positives), args.getCollectMetrics());
			//Analytics.track(Metric.TRAINING_NONMATCHES, Metric.approxCount(negatives), args.getCollectMetrics());
			LOG.info("Finished Learning phase");			
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
    }

		    
}
