package zingg.common.core.executor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.PredictionColsSelector;
import zingg.common.client.cols.ZidAndFieldDefSelector;
import zingg.common.client.options.ZinggOptions;
import zingg.common.client.util.ColName;
import zingg.common.core.block.Canopy;
import zingg.common.core.block.Tree;
import zingg.common.core.filter.IFilter;
import zingg.common.core.filter.PredictionFilter;
import zingg.common.core.model.Model;
import zingg.common.core.pairs.IPairBuilder;
import zingg.common.core.pairs.SelfPairBuilder;
import zingg.common.core.preprocess.StopWordsRemover;
import zingg.common.core.util.Analytics;
import zingg.common.core.util.Metric;

public abstract class Matcher<S,D,R,C,T> extends ZinggBase<S,D,R,C,T>{

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.Matcher";
	public static final Log LOG = LogFactory.getLog(Matcher.class);    
	
    public Matcher() {
        setZinggOption(ZinggOptions.MATCH);
    }

	public ZFrame<D,R,C>  getTestData() throws ZinggClientException{
		 ZFrame<D,R,C>  data = getPipeUtil().read(true, true, args.getNumPartitions(), true, args.getData());
		return data;
	}

	public ZFrame<D, R, C> getFieldDefColumnsDS(ZFrame<D, R, C> testDataOriginal) {
		ZidAndFieldDefSelector zidAndFieldDefSelector = new ZidAndFieldDefSelector(args.getFieldDefinition());
		return testDataOriginal.select(zidAndFieldDefSelector.getCols());
//		return getDSUtil().getFieldDefColumnsDS(testDataOriginal, args, true);
	}


	public ZFrame<D,R,C>  getBlocked( ZFrame<D,R,C>  testData) throws Exception, ZinggClientException{
		LOG.debug("Blocking model file location is " + args.getBlockFile());
		Tree<Canopy<R>> tree = getBlockingTreeUtil().readBlockingTree(args);
		ZFrame<D,R,C> blocked = getBlockingTreeUtil().getBlockHashes(testData, tree);		
		ZFrame<D,R,C> blocked1 = blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL)).cache();
		return blocked1;
	}
	
	public ZFrame<D,R,C> getPairs(ZFrame<D,R,C>blocked, ZFrame<D,R,C>bAll, IPairBuilder<S, D, R, C> iPairBuilder) throws Exception{
		return iPairBuilder.getPairs(blocked, bAll);
	}

	protected abstract Model getModel() throws ZinggClientException;

	protected ZFrame<D,R,C> selectColsFromBlocked(ZFrame<D,R,C>blocked) {
		return blocked.select(ColName.ID_COL, ColName.HASH_COL);
	}

	protected ZFrame<D,R,C> predictOnBlocks(ZFrame<D,R,C>blocks) throws Exception, ZinggClientException{
		if (LOG.isDebugEnabled()) {
				LOG.debug("block size" + blocks.count());
		}
		Model model = getModel();
		ZFrame<D,R,C> dupes = model.predict(blocks); 
		if (LOG.isDebugEnabled()) {
				LOG.debug("Found dupes " + dupes.count());	
		}
		return dupes;
	}

	protected ZFrame<D,R,C> getActualDupes(ZFrame<D,R,C> blocked, ZFrame<D,R,C> testData) throws Exception, ZinggClientException{
		PredictionFilter<D, R, C> predictionFilter = new PredictionFilter<D, R, C>();
		SelfPairBuilder<S, D, R, C> iPairBuilder = new SelfPairBuilder<S, D, R, C> (getDSUtil(),args);
		return getActualDupes(blocked, testData,predictionFilter, iPairBuilder,new PredictionColsSelector());
	}

	protected ZFrame<D,R,C> getActualDupes(ZFrame<D,R,C> blocked, ZFrame<D,R,C> testData, 
			IFilter<D, R, C> predictionFilter, IPairBuilder<S, D, R, C> iPairBuilder, PredictionColsSelector colsSelector) throws Exception, ZinggClientException{
		ZFrame<D,R,C> blocks = getPairs(selectColsFromBlocked(blocked), testData, iPairBuilder);
		ZFrame<D,R,C>dupesActual = predictOnBlocks(blocks); 
		ZFrame<D, R, C> filteredData = predictionFilter.filter(dupesActual);
		if(colsSelector!=null) {
			filteredData = filteredData.select(colsSelector.getCols());
		}
		return filteredData;
	}
	
	@Override
    public void execute() throws ZinggClientException {
        try {
			// read input, filter, remove self joins
			ZFrame<D,R,C>  testDataOriginal = getTestData();
			testDataOriginal =  getFieldDefColumnsDS(testDataOriginal);
			ZFrame<D,R,C>  testData = getStopWords().preprocessForStopWords(testDataOriginal);
			testData = testData.repartition(args.getNumPartitions(), testData.col(ColName.ID_COL));
			//testData = dropDuplicates(testData);
			long count = testData.count();
			LOG.info("Read " + count);
			Analytics.track(Metric.DATA_COUNT, count, args.getCollectMetrics());

			ZFrame<D,R,C>blocked = getBlocked(testData);
			LOG.info("Blocked ");
			/*blocked = blocked.cache();
			blocked.withColumn("partition_id", functions.spark_partition_id())
				.groupBy("partition_id").agg(functions.count("z_zid")).as("zid").orderBy("partition_id").toJavaRDD().saveAsTextFile("/tmp/zblockedParts");
				*/
			if (LOG.isDebugEnabled()) {
				LOG.debug("Num distinct hashes " + blocked.select(ColName.HASH_COL).distinct().count());
				blocked.show();
			}
			//LOG.warn("Num distinct hashes " + blocked.agg(functions.approx_count_distinct(ColName.HASH_COL)).count());
			ZFrame<D,R,C> dupesActual = getActualDupes(blocked, testData);
			
			//dupesActual.explain();
			//dupesActual.toJavaRDD().saveAsTextFile("/tmp/zdupes");
			
			writeOutput(testDataOriginal, dupesActual);		
			
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) e.printStackTrace();
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
    }

	

	
	public void writeOutput( ZFrame<D,R,C>  blocked,  ZFrame<D,R,C>  dupesActual) throws ZinggClientException {
		try{
		//input dupes are pairs
		///pick ones according to the threshold by user
		
			
		//all clusters consolidated in one place
		if (args.getOutput() != null) {
			ZFrame<D, R, C> graphWithScores = getOutput(blocked, dupesActual);
			getPipeUtil().write(graphWithScores, args.getOutput());
		}
		}
		catch(Exception e) {
			e.printStackTrace(); 
		}
		
	}

	

	protected ZFrame<D, R, C> getOutput(ZFrame<D, R, C> blocked, ZFrame<D, R, C> dupesActual) throws ZinggClientException, Exception {
		//-1 is initial suggestion, 1 is add, 0 is deletion, 2 is unsure
		/*blocked = blocked.drop(ColName.HASH_COL);
		blocked = blocked.drop(ColName.SOURCE_COL);
		blocked = blocked.cache();
		*/
		
		dupesActual = dupesActual.cache();
		if (LOG.isDebugEnabled()) {
			LOG.debug("dupes ------------");
			dupesActual.show();
		}
		ZFrame<D,R,C>graph = getGraphUtil().buildGraph(blocked, dupesActual).cache();
		//graph.toJavaRDD().saveAsTextFile("/tmp/zgraph");
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("graph ------------");
			graph.show();

		}
		//write score
		ZFrame<D,R,C>score = getMinMaxScores(dupesActual, graph).cache();
		//score.toJavaRDD().coalesce(1).saveAsTextFile("/tmp/zallscoresAvg");
		graph = graph.repartition(args.getNumPartitions(), graph.col(ColName.ID_COL)).cache();
		if (LOG.isDebugEnabled()) {
			score.show();
		}
		ZFrame<D, R, C> graphWithScores = getGraphWithScores(graph, score);
			//graphWithScores.toJavaRDD().saveAsTextFile("/tmp/zgraphWScores");
		graphWithScores = graphWithScores.drop(ColName.HASH_COL);
		graphWithScores = graphWithScores.drop(ColName.COL_PREFIX + ColName.ID_COL);
		graphWithScores = graphWithScores.drop(ColName.ID_COL);
		graphWithScores = graphWithScores.drop(ColName.SOURCE_COL);
		/*String[] cols = graphWithScores.columns();
		List<Column> columns = new ArrayList<Column>();
		//columns.add(graphWithScores.col(ColName.CLUSTER_COLUMN));
		//go only upto the last col, which is cluster col
		for (int i=0; i < cols.length - 1; ++i) {
			columns.add(graphWithScores.col(cols[i]));
		}
		graphWithScores =  getDSUtil().select(graphWithScores, columns);
		*/
		return graphWithScores;
	}

	protected ZFrame<D, R, C> getGraphWithScoresOrig(ZFrame<D, R, C> graph, ZFrame<D, R, C> score) {
		ZFrame<D,R,C>graphWithScores = getDSUtil().joinZColFirst(
			score, graph, ColName.ID_COL, false).cache();
		return graphWithScores;
	}

	protected ZFrame<D, R, C> getGraphWithScores(ZFrame<D, R, C> graph, ZFrame<D, R, C> score) {
		return this.getGraphWithScoresOrig(graph, score);
	}

	protected ZFrame<D,R,C>getMinMaxScores(ZFrame<D,R,C>dupes, ZFrame<D,R,C>graph) throws Exception {
		if (LOG.isDebugEnabled()) dupes.show(500);
		
		ZFrame<D,R,C> graph1 = graph.select(ColName.ID_COL, ColName.CLUSTER_COLUMN);
		graph1 = graph1.repartition(args.getNumPartitions(),
			graph1.col(ColName.CLUSTER_COLUMN));
		ZFrame<D,R,C> dupesWithIds = dupes.select(ColName.ID_COL, ColName.COL_PREFIX + ColName.ID_COL);
		LOG.warn("Dupes with ids ");
		if (LOG.isDebugEnabled()) dupesWithIds.show(500);
		/*ZFrame<D,R,C>graphPairsFound = graph1.as("first").joinOnCol(graph1.as("second"), ColName.CLUSTER_COLUMN)
			.selectExpr("first.z_zid as z_zid", "second.z_zid as z_z_zid");
			*/
		ZFrame<D,R,C> graphPairsFound =  getDSUtil().joinWithItself(graph1, ColName.CLUSTER_COLUMN, true).
			select(ColName.ID_COL, ColName.COL_PREFIX + ColName.ID_COL).cache();
		//graphPairsFound = graphPairsFound.filter(graphPairsFound.gt(ColName.ID_COL));*/

		
		LOG.warn("graph pairs ");
		if (LOG.isDebugEnabled()) graphPairsFound.show(500);
		
		ZFrame<D,R,C> graphPairsExtra = graphPairsFound.except(dupesWithIds);
		ZFrame<D,R,C> graphPairsExtrawithDummyScore = graphPairsExtra.withColumn(ColName.SCORE_COL, 0.0);
		LOG.warn("graph pairs extra");
		if (LOG.isDebugEnabled()) graphPairsExtra.show(500);
		
		//original
		ZFrame<D,R,C> s1 = dupes.select(ColName.SCORE_COL, ColName.ID_COL);
		ZFrame<D,R,C> s2 = dupes.select(ColName.SCORE_COL, ColName.COL_PREFIX + ColName.ID_COL);

		//add the graph discovered extra pairs
		s1 = s1.union(graphPairsExtrawithDummyScore.select(ColName.SCORE_COL, ColName.ID_COL));
		s2 = s2.union(graphPairsExtrawithDummyScore.select(ColName.SCORE_COL, ColName.COL_PREFIX + ColName.ID_COL));
		List<C> cols = new ArrayList<C>();
		
		ZFrame<D,R,C>s2RightCols = s2.toDF(ColName.SCORE_COL, ColName.ID_COL).cache();
		ZFrame<D,R,C>allScores = s2RightCols.union(s1);
		//allScores.toJavaRDD().coalesce(1).saveAsTextFile("/tmp/zallscores");
		/*WindowSpec window = Window.partitionBy(ColName.ID_COL).orderBy(ColName.SCORE_COL);
		//WindowSpec window = Window.orderBy(ColName.CLUSTER_COLUMN);
		ZFrame<D,R,C>ranked = allScores.withColumn("rank", functions.rank().over(window)).
			withColumn("minScore", functions.min(ColName.SCORE_COL).over(window)).
			withColumn("maxScore", functions.max(ColName.SCORE_COL).over(window)).
			where("rank == 1");
			ranked.toJavaRDD().saveAsTextFile("/tmp/allscoresRanked");

		//graph = graph.withColumn("rank", functions.rank().over(window));
		//graph = graph.withColumn(ColName.DENSE_COL, functions.dense_rank().over(window));
		//graph = graph.withColumn("row_num", functions.row_number().over(window));
		*/
		allScores = allScores.repartition(args.getNumPartitions(), allScores.col(ColName.ID_COL));
		
		return allScores.groupByMinMaxScore(allScores.col(ColName.ID_COL));			
	}

    protected abstract StopWordsRemover<S,D,R,C,T> getStopWords();

	    
}
