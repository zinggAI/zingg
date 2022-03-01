package zingg;

import com.snowflake.snowpark.DataFrame;
import com.snowflake.snowpark.functions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.pipe.Pipe;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.client.util.Util;
import zingg.scala.DFUtil;
import zingg.util.DSUtil;
import zingg.util.PipeUtil;

public class TrainingDataFinder extends ZinggBase{

	protected static String name = "zingg.TrainingDataFinder";
	public static final Log LOG = LogFactory.getLog(TrainingDataFinder.class);    

    public TrainingDataFinder() {
        setZinggOptions(ZinggOptions.FIND_TRAINING_DATA);
    }

	public DataFrame getTraining() {
		return DSUtil.getTraining(snow, args);
	}

    public void execute() throws ZinggClientException {
			try{
				DataFrame data = PipeUtil.read(snow, true, true, args.getData());
				LOG.warn("Read input data " + data.count());
				//create 20 pos pairs

				DataFrame posPairs = null;
				DataFrame negPairs = null;
				DataFrame trFile = getTraining();					
				
				if (trFile != null) {
					DataFrame trPairs = DSUtil.joinWithItself(trFile, ColName.CLUSTER_COLUMN, true);
						posPairs = trPairs.filter(trPairs.col(ColName.MATCH_FLAG_COL).$eq$eq$eq(ColValues.MATCH_TYPE_MATCH));
						posPairs = trPairs.filter(trPairs.col(ColName.MATCH_FLAG_COL).$eq$eq$eq(ColValues.MATCH_TYPE_MATCH));
						posPairs = trPairs.filter(trPairs.col(ColName.MATCH_FLAG_COL).$eq$eq$eq(ColValues.MATCH_TYPE_MATCH));		
						negPairs = trPairs.filter(trPairs.col(ColName.MATCH_FLAG_COL).$eq$eq$eq(ColValues.MATCH_TYPE_NOT_A_MATCH));
						posPairs = posPairs.drop(new String[]{ColName.MATCH_FLAG_COL, 
								ColName.COL_PREFIX + ColName.MATCH_FLAG_COL,
								ColName.CLUSTER_COLUMN,
								ColName.COL_PREFIX + ColName.CLUSTER_COLUMN});
						negPairs = negPairs.drop(new String[]{ColName.MATCH_FLAG_COL, 
								ColName.COL_PREFIX + ColName.MATCH_FLAG_COL,
								ColName.CLUSTER_COLUMN,
								ColName.COL_PREFIX + ColName.CLUSTER_COLUMN});
						
						LOG.warn("Read training samples " + posPairs.count() + " neg " + negPairs.count());
				}
					
					
				if (posPairs == null || posPairs.count() <= 5) {
					DataFrame posSamples = getPositiveSamples(data);
					//posSamples.printSchema();
					if (posPairs != null) {
						//posPairs.printSchema();
						posPairs = posPairs.union(posSamples);
					}
					else {
						posPairs = posSamples;
					}
				}
				// posPairs = posPairs.cache();
				// if (negPairs!= null) negPairs = negPairs.cache();
				//create random samples for blocking
				DataFrame sample = data.sample(args.getLabelDataSampleSize()); //.repartition(args.getNumPartitions()).persist(StorageLevel.MEMORY_ONLY());
				Tree<Canopy> tree = BlockingTreeUtil.createBlockingTree(sample, posPairs, 1, -1, args, hashFunctions);			
				DataFrame blocked = sample.map(new Block.BlockFunction(tree), RowEncoder.apply(Block.appendHashCol(sample.schema())));
				//blocked = blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL)).cache();
				DataFrame blocks = DSUtil.joinWithItself(blocked, ColName.HASH_COL, true);
				blocks = blocks.cacheResult();	
				//TODO HASH Partition
				//if (negPairs!= null) negPairs = negPairs.persist(StorageLevel.MEMORY_ONLY());
					//train classifier and predict the blocked values from classifier
					//only if we have some user data
					if (posPairs != null && negPairs !=null 
							&& posPairs.count() >= 5 && negPairs.count() >= 5) {	
						
						if (LOG.isDebugEnabled()) {
							LOG.debug("num blocks " + blocks.count());		
						}
						Model model = ModelUtil.createModel(posPairs, negPairs, new LabelModel(this.featurers), spark);
						DataFrame dupes = model.predict(blocks); 
						if (LOG.isDebugEnabled()) {
							LOG.debug("num dupes " + dupes.count());	
						}
						LOG.info("Writing uncertain pairs");
						
						//dupes = dupes.persist(StorageLevel.MEMORY_ONLY());
						DataFrame uncertain = getUncertain(dupes);
										
						writeUncertain(uncertain);													
				}
				else {
					LOG.info("Writing uncertain pairs when either positive or negative samples not provided ");
					DataFrame posFiltered = blocks.sample( 20.0d/blocks.count());
					posFiltered = posFiltered.withColumn(ColName.PREDICTION_COL, functions.lit(ColValues.IS_NOT_KNOWN_PREDICTION));
					posFiltered = posFiltered.withColumn(ColName.SCORE_COL, functions.lit(ColValues.ZERO_SCORE));
					writeUncertain(posFiltered);		
				}			
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new ZinggClientException(e.getMessage());
			}	
    }

	public void writeUncertain(DataFrame dupesActual) {
		//input dupes are pairs
		dupesActual = DFUtil.addClusterRowNumber(dupesActual, spark);
		dupesActual = Util.addUniqueCol(dupesActual, ColName.CLUSTER_COLUMN );		
		DataFrame dupes1 = DSUtil.alignDupes(dupesActual, args);
		DataFrame dupes2 = dupes1.sort(dupes1.col(ColName.CLUSTER_COLUMN), null);
		LOG.debug("uncertain output schema is " + dupes2.schema());
		PipeUtil.write(dupes2 , args, "", getUnmarkedLocation());
		//PipeUtil.write(jdbc, massageForJdbc(dupes2.cache()) , args, ctx);
	}

	public Pipe getUnmarkedLocation() {
		return PipeUtil.getTrainingDataUnmarkedPipe(args);
	}

	public DataFrame getUncertain(DataFrame dupes) {
		//take lowest positive score and highest negative score in the ones marked matches
		DataFrame pos = dupes.filter(dupes.col(ColName.PREDICTION_COL).$eq$eq$eq(ColValues.IS_MATCH_PREDICTION));
		pos = pos.sort(pos.col(ColName.SCORE_COL), null);
		if (LOG.isDebugEnabled()) {
			LOG.debug("num pos " + pos.count());	
		}
		pos = pos.limit(10);
		DataFrame neg = dupes.filter(dupes.col(ColName.PREDICTION_COL).$eq$eq$eq(ColValues.IS_NOT_A_MATCH_PREDICTION));
		neg = neg.sort(com.snowflake.snowpark.functions.col(ColName.SCORE_COL), null);
		if (LOG.isDebugEnabled()) {
			LOG.debug("num neg " + neg.count());
		}
		neg = neg.limit(10);
		return pos.union(neg);
	}

	public DataFrame getPositiveSamples(DataFrame data) throws Exception {
		if (LOG.isDebugEnabled()) {
			long count = data.count();
			LOG.debug("Total count is " + count);
			LOG.debug("Label data sample size is " + args.getLabelDataSampleSize());
		}
		DataFrame posSample = data.sample(args.getLabelDataSampleSize());
		//select only those columns which are mentioned in the field definitions
		posSample = DSUtil.getFieldDefColumnsDS(posSample, args, true);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sampled " + posSample.count());
		}
		//posSample = posSample.cache();
		DataFrame posPairs = DSUtil.joinWithItself(posSample, ColName.ID_COL, false);
		
		LOG.info("Created positive sample pairs ");
		if (LOG.isDebugEnabled()) {
			LOG.debug("Pos Sample pairs count " + posPairs.count());
		}
		return posPairs;
	}

		    
}
