package zingg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.storage.StorageLevel;

import static org.apache.spark.sql.functions.asc;
import static org.apache.spark.sql.functions.desc;

import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.pipe.Pipe;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.client.util.Util;
import zingg.model.LabelModel;
import zingg.model.Model;
import zingg.preprocess.StopWords;
import zingg.util.BlockingTreeUtil;
import zingg.util.DSUtil;
import zingg.util.ModelUtil;
import zingg.util.PipeUtil;


import zingg.scala.TypeTags;
import zingg.scala.DFUtil;

public class TrainingDataFinder extends ZinggBase{

	protected static String name = "zingg.TrainingDataFinder";
	public static final Log LOG = LogFactory.getLog(TrainingDataFinder.class);    

    public TrainingDataFinder() {
        setZinggOptions(ZinggOptions.FIND_TRAINING_DATA);
    }

	public Dataset<Row> getTraining() {
		return DSUtil.getTraining(spark, args);
	}

    public void execute() throws ZinggClientException {
			try{
				Dataset<Row> data = PipeUtil.read(spark, true, true, args.getData());
				LOG.warn("Read input data " + data.count());
				//create 20 pos pairs

				Dataset<Row> posPairs = null;
				Dataset<Row> negPairs = null;
				Dataset<Row> trFile = getTraining();					

				if (trFile != null) {
					trFile = StopWords.preprocessForStopWords(spark, args, trFile);
					Dataset<Row> trPairs = DSUtil.joinWithItself(trFile, ColName.CLUSTER_COLUMN, true);
						
						posPairs = trPairs.filter(trPairs.col(ColName.MATCH_FLAG_COL).equalTo(ColValues.MATCH_TYPE_MATCH));
						negPairs = trPairs.filter(trPairs.col(ColName.MATCH_FLAG_COL).equalTo(ColValues.MATCH_TYPE_NOT_A_MATCH));
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
					Dataset<Row> posSamplesOriginal = getPositiveSamples(data);
					Dataset<Row> posSamples = StopWords.preprocessForStopWords(spark, args, posSamplesOriginal);
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
				Dataset<Row> sampleOrginal = data.sample(false, args.getLabelDataSampleSize()).repartition(args.getNumPartitions()).persist(StorageLevel.MEMORY_ONLY());
				LOG.info("Preprocessing DS for stopWords");

				Dataset<Row> sample = StopWords.preprocessForStopWords(spark, args, sampleOrginal);

				Tree<Canopy> tree = BlockingTreeUtil.createBlockingTree(sample, posPairs, 1, -1, args, hashFunctions);			
				Dataset<Row> blocked = sample.map(new Block.BlockFunction(tree), RowEncoder.apply(Block.appendHashCol(sample.schema())));
				blocked = blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL)).cache();
				Dataset<Row> blocks = DSUtil.joinWithItself(blocked, ColName.HASH_COL, true);
				blocks = blocks.cache();	
				//TODO HASH Partition
				if (negPairs!= null) negPairs = negPairs.persist(StorageLevel.MEMORY_ONLY());
					//train classifier and predict the blocked values from classifier
					//only if we have some user data
					if (posPairs != null && negPairs !=null 
							&& posPairs.count() >= 5 && negPairs.count() >= 5) {	
						
						if (LOG.isDebugEnabled()) {
							LOG.debug("num blocks " + blocks.count());		
						}
						Model model = ModelUtil.createModel(posPairs, negPairs, new LabelModel(this.featurers), spark);
						Dataset<Row> dupes = model.predict(blocks); 
						if (LOG.isDebugEnabled()) {
							LOG.debug("num dupes " + dupes.count());	
						}
						LOG.info("Writing uncertain pairs");
						
						dupes = dupes.persist(StorageLevel.MEMORY_ONLY());
						Dataset<Row> uncertain = getUncertain(dupes);
										
						writeUncertain(uncertain, sampleOrginal);													
				}
				else {
					LOG.info("Writing uncertain pairs when either positive or negative samples not provided ");
					Dataset<Row> posFiltered = blocks.sample(false,  20.0d/blocks.count());
					posFiltered = posFiltered.withColumn(ColName.PREDICTION_COL, functions.lit(ColValues.IS_NOT_KNOWN_PREDICTION));
					posFiltered = posFiltered.withColumn(ColName.SCORE_COL, functions.lit(ColValues.ZERO_SCORE));
					writeUncertain(posFiltered, sampleOrginal);		
				}			
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new ZinggClientException(e.getMessage());
			}	
    }

	public void writeUncertain(Dataset<Row> dupesActual, Dataset<Row> sampleOrginal) {
		//dupesActual.show(4);
		//input dupes are pairs
		dupesActual = DFUtil.addClusterRowNumber(dupesActual, spark);
		dupesActual = Util.addUniqueCol(dupesActual, ColName.CLUSTER_COLUMN );	
		Dataset<Row> dupes1 = DSUtil.alignDupes(dupesActual, args);
		dupes1 = StopWords.postprocess(dupes1, sampleOrginal);	
		Dataset<Row> dupes2 = dupes1.orderBy(ColName.CLUSTER_COLUMN);
		LOG.debug("uncertain output schema is " + dupes2.schema());
		PipeUtil.write(dupes2 , args, ctx, getUnmarkedLocation());
		//PipeUtil.write(jdbc, massageForJdbc(dupes2.cache()) , args, ctx);
	}

	public Pipe getUnmarkedLocation() {
		return PipeUtil.getTrainingDataUnmarkedPipe(args);
	}

	public Dataset<Row> getUncertain(Dataset<Row> dupes) {
		//take lowest positive score and highest negative score in the ones marked matches
		Dataset<Row> pos = dupes.filter(dupes.col(ColName.PREDICTION_COL).equalTo(ColValues.IS_MATCH_PREDICTION));
		pos = pos.sort(asc(ColName.SCORE_COL)).cache();
		if (LOG.isDebugEnabled()) {
			LOG.debug("num pos " + pos.count());	
		}
		pos = pos.limit(10);
		Dataset<Row> neg = dupes.filter(dupes.col(ColName.PREDICTION_COL).equalTo(ColValues.IS_NOT_A_MATCH_PREDICTION));
		neg = neg.sort(desc(ColName.SCORE_COL)).cache();
		if (LOG.isDebugEnabled()) {
			LOG.debug("num neg " + neg.count());
		}
		neg = neg.limit(10);
		return pos.union(neg);
	}

	public Dataset<Row> getPositiveSamples(Dataset<Row> data) throws Exception {
		if (LOG.isDebugEnabled()) {
			long count = data.count();
			LOG.debug("Total count is " + count);
			LOG.debug("Label data sample size is " + args.getLabelDataSampleSize());
		}
		Dataset<Row> posSample = data.sample(false, args.getLabelDataSampleSize());
		//select only those columns which are mentioned in the field definitions
		posSample = DSUtil.getFieldDefColumnsDS(posSample, args, true);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sampled " + posSample.count());
		}
		posSample = posSample.cache();
		Dataset<Row> posPairs = DSUtil.joinWithItself(posSample, ColName.ID_COL, false);
		
		LOG.info("Created positive sample pairs ");
		if (LOG.isDebugEnabled()) {
			LOG.debug("Pos Sample pairs count " + posPairs.count());
		}
		return posPairs;
	}

		    
}
