package zingg.snowpark;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.RelationalGroupedDataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Session;
import com.snowflake.snowpark_java.Functions;
import com.snowflake.snowpark_java.Window;
import com.snowflake.snowpark_java.WindowSpec;
import com.snowflake.snowpark_java.types.DataType;

import scala.collection.JavaConverters;
import zingg.TrainingDataFinder;
import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.model.Model;
import zingg.snowpark.model.SnowModel;
import zingg.client.SnowFrame;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.client.util.Util;
import zingg.preprocess.StopWords;
import zingg.util.DSUtil;
import zingg.util.GraphUtil;
import zingg.util.ModelUtil;
import zingg.util.PipeUtilBase;



public class SnowTrainingDataFinder extends TrainingDataFinder<Session, DataFrame, Row, Column,DataType> {

	public static String name = "zingg.SnowTrainingDataFinder";
	public static final Log LOG = LogFactory.getLog(SnowTrainingDataFinder.class);

	public SnowTrainingDataFinder() {
		setZinggOptions(ZinggOptions.FIND_TRAINING_DATA);
	}
	

	@Override
	public void execute() throws ZinggClientException {
		try{
			ZFrame<DataFrame,Row,Column> data = getPipeUtil().read(true, true, args.getData());
			LOG.warn("Read input data " + data.count());
			//create 20 pos pairs

			ZFrame<DataFrame,Row,Column> posPairs = null;
			ZFrame<DataFrame,Row,Column> negPairs = null;
			ZFrame<DataFrame,Row,Column> trFile = getTraining();					

			if (trFile != null) {
				trFile = StopWords.preprocessForStopWords(args, trFile);
				ZFrame<DataFrame,Row,Column> trPairs = getDSUtil().joinWithItself(trFile, ColName.CLUSTER_COLUMN, true);
					
					posPairs = trPairs.filter(trPairs.equalTo(ColName.MATCH_FLAG_COL, ColValues.MATCH_TYPE_MATCH));
					negPairs = trPairs.filter(trPairs.equalTo(ColName.MATCH_FLAG_COL, ColValues.MATCH_TYPE_NOT_A_MATCH));
					posPairs = posPairs.drop(ColName.MATCH_FLAG_COL, 
							ColName.COL_PREFIX + ColName.MATCH_FLAG_COL,
							ColName.CLUSTER_COLUMN,
							ColName.COL_PREFIX + ColName.CLUSTER_COLUMN);
					negPairs = negPairs.drop(ColName.MATCH_FLAG_COL, 
							ColName.COL_PREFIX + ColName.MATCH_FLAG_COL,
							ColName.CLUSTER_COLUMN,
							ColName.COL_PREFIX + ColName.CLUSTER_COLUMN);
					
					LOG.warn("Read training samples " + posPairs.count() + " neg " + negPairs.count());
			}
				
				
			if (posPairs == null || posPairs.count() <= 5) {
				ZFrame<DataFrame,Row,Column> posSamplesOriginal = getPositiveSamples(data);
				ZFrame<DataFrame,Row,Column> posSamples = StopWords.preprocessForStopWords(args, posSamplesOriginal);
				//posSamples.printSchema();
				if (posPairs != null) {
					//posPairs.printSchema();
					posPairs = posPairs.union(posSamples);
				}
				else {
					posPairs = posSamples;
				}
			}
			posPairs = posPairs.cache();
			if (negPairs!= null) negPairs = negPairs.cache();
			//create random samples for blocking
			ZFrame<DataFrame,Row,Column> sampleOrginal = data.sample(false, args.getLabelDataSampleSize()).repartition(args.getNumPartitions()).persist(StorageLevel.MEMORY_ONLY());
			sampleOrginal = getDSUtil().getFieldDefColumnsDS(sampleOrginal, args, true);
			LOG.info("Preprocessing DS for stopWords");

			ZFrame<DataFrame,Row,Column> sample = StopWords.preprocessForStopWords(args, sampleOrginal);

			Tree<Canopy<Row>> tree = getBlockingTreeUtil().createBlockingTree(sample, posPairs, 1, -1, args, hashFunctions);			
			ZFrame<DataFrame,Row,Column> blocked = sample.map(new Block.BlockFunction(tree), RowEncoder.apply(Block.appendHashCol(sample.schema())));
			blocked = blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL)).cache();
			ZFrame<DataFrame,Row,Column> blocks = getDSUtil().joinWithItself(blocked, ColName.HASH_COL, true);
			blocks = blocks.cache();	
			//TODO HASH Partition
			if (negPairs!= null) negPairs = negPairs.cache();
				//train classifier and predict the blocked values from classifier
				//only if we have some user data
				if (posPairs != null && negPairs !=null 
						&& posPairs.count() >= 5 && negPairs.count() >= 5) {	
					
					if (LOG.isDebugEnabled()) {
						LOG.debug("num blocks " + blocks.count());		
					}
					Model<Session,DataFrame,Row,Column> model = getModelUtil().createModel(posPairs, negPairs, this.featurers, getContext(), false);
					ZFrame<DataFrame,Row,Column> dupes = model.predict(blocks); 
					if (LOG.isDebugEnabled()) {
						LOG.debug("num dupes " + dupes.count());	
					}
					LOG.info("Writing uncertain pairs");
					
					dupes = dupes.cache();
					ZFrame<DataFrame,Row,Column> uncertain = getUncertain(dupes);
									
					writeUncertain(uncertain, sampleOrginal);													
			}
			else {
				LOG.info("Writing uncertain pairs when either positive or negative samples not provided ");
				ZFrame<DataFrame,Row,Column> posFiltered = blocks.sample(false,  20.0d/blocks.count());
				posFiltered = posFiltered.withColumn(ColName.PREDICTION_COL, Functions.lit(ColValues.IS_NOT_KNOWN_PREDICTION));
				posFiltered = posFiltered.withColumn(ColName.SCORE_COL, Functions.lit(ColValues.ZERO_SCORE));
				writeUncertain(posFiltered, sampleOrginal);		
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}	
	}

	


	

	@Override
	public void cleanup() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setSession(Session session) {
		// TODO Auto-generated method stub
		
	}
	
}
