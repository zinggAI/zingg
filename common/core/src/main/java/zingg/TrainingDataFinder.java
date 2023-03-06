package zingg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.pipe.Pipe;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.model.Model;
import zingg.preprocess.StopWordsRemover;

public abstract class TrainingDataFinder<S,D,R,C,T> extends ZinggBase<S,D,R,C,T>{

	protected static String name = "zingg.TrainingDataFinder";
	public static final Log LOG = LogFactory.getLog(TrainingDataFinder.class);    

    public TrainingDataFinder() {
        setZinggOptions(ZinggOptions.FIND_TRAINING_DATA);
    }

	public ZFrame<D,R,C> getTraining() throws ZinggClientException {
		return getDSUtil().getTraining(getPipeUtil(), args);
	}
	
	 public void execute() throws ZinggClientException {
			try{
				ZFrame<D,R,C> data = getPipeUtil().read(true, true, args.getData());
				LOG.warn("Read input data " + data.count());
				//create 20 pos pairs

				ZFrame<D,R,C> posPairs = null;
				ZFrame<D,R,C> negPairs = null;
				ZFrame<D,R,C> trFile = getTraining();					

				if (trFile != null) {
					trFile = getStopWords().preprocessForStopWords(trFile);
					ZFrame<D,R,C> trPairs = getDSUtil().joinWithItself(trFile, ColName.CLUSTER_COLUMN, true);
						
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
					ZFrame<D,R,C> posSamplesOriginal = getPositiveSamples(data);
					ZFrame<D,R,C> posSamples = getStopWords().preprocessForStopWords(posSamplesOriginal);
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
				ZFrame<D,R,C> sampleOrginal = data.sample(false, args.getLabelDataSampleSize()).repartition(args.getNumPartitions()).cache();
				sampleOrginal = getDSUtil().getFieldDefColumnsDS(sampleOrginal, args, true);
				LOG.info("Preprocessing DS for stopWords");

				ZFrame<D,R,C> sample = getStopWords().preprocessForStopWords(sampleOrginal);

				Tree<Canopy<R>> tree = getBlockingTreeUtil().createBlockingTree(sample, posPairs, 1, -1, args, getHashUtil().getHashFunctionList());		
				tree.print(2);	
				ZFrame<D,R,C> blocked = getBlockingTreeUtil().getBlockHashes(sample, tree); 
				
				blocked = blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL)).cache();
				System.out.println("blocked");
				blocked.show(true);
				ZFrame<D,R,C> blocks = getDSUtil().joinWithItself(blocked, ColName.HASH_COL, true);
				blocks = blocks.cache();	
				System.out.println("blocks");
				blocks.show();
				//TODO HASH Partition
				if (negPairs!= null) negPairs = negPairs.cache();
					//train classifier and predict the blocked values from classifier
					//only if we have some user data
					if (posPairs != null && negPairs !=null 
							&& posPairs.count() >= 5 && negPairs.count() >= 5) {	
						
						if (LOG.isDebugEnabled()) {
							LOG.debug("num blocks " + blocks.count());		
						}
						Model<S,T,D,R,C> model = getModelUtil().createModel(posPairs, negPairs, false, args);
						ZFrame<D,R,C> dupes = model.predict(blocks); 
						if (LOG.isDebugEnabled()) {
							LOG.debug("num dupes " + dupes.count());	
						}
						LOG.info("Writing uncertain pairs");
						
						dupes = dupes.cache();
						ZFrame<D,R,C> uncertain = getUncertain(dupes);
										
						writeUncertain(uncertain, sampleOrginal);													
				}
				else {
					LOG.info("Writing uncertain pairs when either positive or negative samples not provided ");
					double blocksCount = 20.0d/(blocks.count() * 1.0d);
					if (blocksCount > 1) blocksCount = 1.0d;
					LOG.info("block count " + blocksCount);
					ZFrame<D,R,C> posFiltered = blocks.sample(false,  blocksCount);
					//ZFrame<D,R,C> posFiltered = blocks.sample(false,  20.0d/blocks.count());
					posFiltered = posFiltered.withColumn(ColName.PREDICTION_COL, ColValues.IS_NOT_KNOWN_PREDICTION);
					posFiltered = posFiltered.withColumn(ColName.SCORE_COL, ColValues.ZERO_SCORE);
					writeUncertain(posFiltered, sampleOrginal);		
				}			
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new ZinggClientException(e.getMessage());
			}	
    }

	public void writeUncertain(ZFrame<D,R,C> dupesActual, ZFrame<D,R,C> sampleOrginal) throws ZinggClientException {
		dupesActual.show(40);
		//input dupes are pairs
		dupesActual = getDSUtil().addClusterRowNumber(dupesActual);
		dupesActual = getDSUtil().addUniqueCol(dupesActual, ColName.CLUSTER_COLUMN );	
		ZFrame<D,R,C> dupes1 = getDSUtil().alignDupes(dupesActual, args);
		dupes1 = getDSUtil().postprocess(dupes1, sampleOrginal);	
		ZFrame<D,R,C> dupes2 = dupes1.orderBy(ColName.CLUSTER_COLUMN);
		//LOG.debug("uncertain output schema is " + dupes2.schema());
		getPipeUtil().write(dupes2 , args, getUnmarkedLocation());
		//PipeUtil.write(jdbc, massageForJdbc(dupes2.cache()) , args, ctx);
	}

	public Pipe getUnmarkedLocation() {
		return getPipeUtil().getTrainingDataUnmarkedPipe(args);
	}

	public ZFrame<D,R,C> getUncertain(ZFrame<D,R,C> dupes) {
		//take lowest positive score and highest negative score in the ones marked matches
		ZFrame<D,R,C> pos = dupes.filter(dupes.equalTo(ColName.PREDICTION_COL, ColValues.IS_MATCH_PREDICTION));
		pos = pos.sortAscending(ColName.SCORE_COL);
		if (LOG.isDebugEnabled()) {
			LOG.debug("num pos " + pos.count());	
		}
		pos = pos.limit(10);
		ZFrame<D,R,C> neg = dupes.filter(dupes.equalTo(ColName.PREDICTION_COL, ColValues.IS_NOT_A_MATCH_PREDICTION));
		neg = neg.sortDescending(ColName.SCORE_COL).cache();
		if (LOG.isDebugEnabled()) {
			LOG.debug("num neg " + neg.count());
		}
		neg = neg.limit(10);
		return pos.union(neg);
	}

	public ZFrame<D,R,C> getPositiveSamples(ZFrame<D,R,C> data) throws Exception {
		if (LOG.isDebugEnabled()) {
			long count = data.count();
			LOG.debug("Total count is " + count);
			LOG.debug("Label data sample size is " + args.getLabelDataSampleSize());
		}
		ZFrame<D,R,C> posSample = data.sample(false, args.getLabelDataSampleSize());
		//select only those columns which are mentioned in the field definitions
		posSample = getDSUtil().getFieldDefColumnsDS(posSample, args, true);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sampled " + posSample.count());
		}
		posSample = posSample.cache();
		ZFrame<D,R,C> posPairs = getDSUtil().joinWithItself(posSample, ColName.ID_COL, false);
		
		LOG.info("Created positive sample pairs ");
		if (LOG.isDebugEnabled()) {
			LOG.debug("Pos Sample pairs count " + posPairs.count());
		}
		return posPairs;
	}

    protected abstract StopWordsRemover<S,D,R,C,T> getStopWords();
		    
}
