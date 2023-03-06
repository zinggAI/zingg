package zingg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.model.Model;
import zingg.util.Analytics;
import zingg.util.Metric;

public abstract class Matcher<S,D,R,C,T> extends ZinggBase<S,D,R,C,T>{


	protected static String name = "zingg.Matcher";
	public static final Log LOG = LogFactory.getLog(Matcher.class);    

    public Matcher() {
        setZinggOptions(ZinggOptions.MATCH);
    }

	protected  ZFrame<D,R,C>  getTestData() throws ZinggClientException{
		 ZFrame<D,R,C>  data = getPipeUtil().read(true, args.getNumPartitions(), true, args.getData());
		return data;
	}

	protected  ZFrame<D,R,C>  getBlocked( ZFrame<D,R,C>  testData) throws Exception, ZinggClientException{
		LOG.debug("Blocking model file location is " + args.getBlockFile());
		Tree<Canopy<R>> tree = getBlockingTreeUtil().readBlockingTree(args);
		ZFrame<D,R,C> blocked = getBlockingTreeUtil().getBlockHashes(testData, tree);		
		ZFrame<D,R,C> blocked1 = blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL)); //.cache();
		return blocked1;
	}

	
	
	protected ZFrame<D,R,C> getBlocks(ZFrame<D,R,C>blocked) throws Exception{
		return getDSUtil().joinWithItself(blocked, ColName.HASH_COL, true).cache();
	}

	protected ZFrame<D,R,C> getBlocks(ZFrame<D,R,C>blocked, ZFrame<D,R,C>bAll) throws Exception{
		ZFrame<D,R,C>joinH =  getDSUtil().joinWithItself(blocked, ColName.HASH_COL, true).cache();
		/*ZFrame<D,R,C>joinH = blocked.as("first").joinOnCol(blocked.as("second"), ColName.HASH_COL)
			.selectExpr("first.z_zid as z_zid", "second.z_zid as z_z_zid");
		*/
		//joinH.show();
		joinH = joinH.filter(joinH.gt(ColName.ID_COL));	
		LOG.warn("Num comparisons " + joinH.count());
		joinH = joinH.repartition(args.getNumPartitions(), joinH.col(ColName.ID_COL));
		bAll = bAll.repartition(args.getNumPartitions(), bAll.col(ColName.ID_COL));
		joinH = joinH.joinOnCol(bAll, ColName.ID_COL);
		LOG.warn("Joining with actual values");
		//joinH.show();
		bAll = getDSUtil().getPrefixedColumnsDS(bAll);
		//bAll.show();
		joinH = joinH.repartition(args.getNumPartitions(), joinH.col(ColName.COL_PREFIX + ColName.ID_COL));
		joinH = joinH.joinOnCol(bAll, ColName.COL_PREFIX + ColName.ID_COL);
		LOG.warn("Joining again with actual values");
		//joinH.show();
		return joinH;
	}

	protected ZFrame<D,R,C>massageAllEquals(ZFrame<D,R,C>allEqual) {
		allEqual = allEqual.withColumn(ColName.PREDICTION_COL, ColValues.IS_MATCH_PREDICTION);
		allEqual = allEqual.withColumn(ColName.SCORE_COL, ColValues.FULL_MATCH_SCORE);
		return allEqual;
	}

	protected abstract Model getModel() throws ZinggClientException;

	protected ZFrame<D,R,C>selectColsFromBlocked(ZFrame<D,R,C>blocked) {
		return blocked.select(ColName.ID_COL, ColName.HASH_COL);
	}

    public void execute() throws ZinggClientException {
        try {
			// read input, filter, remove self joins
			 ZFrame<D,R,C>  testDataOriginal = getTestData();
			testDataOriginal =  getDSUtil().getFieldDefColumnsDS(testDataOriginal, args, true);
			 ZFrame<D,R,C>  testData = testDataOriginal; //StopWords.preprocessForStopWords(args, testDataOriginal);
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
			ZFrame<D,R,C>blocks = getBlocks(selectColsFromBlocked(blocked), testData);
			//blocks.explain();
			//LOG.info("Blocks " + blocks.count());
			if (LOG.isDebugEnabled()) {
				LOG.debug("block size" + blocks.count());
			}
			//blocks.toJavaRDD().saveAsTextFile("/tmp/zblocks");
			//check if all fields equal			
			//ZFrame<D,R,C>allEqual =  getDSUtil().allFieldsEqual(blocks, args);
			//allEqual = allEqual.cache();
			//send remaining to model 
			Model model = getModel();
			//blocks.cache().withColumn("partition_id", functions.spark_partition_id())
			//	.groupBy("partition_id").agg(functions.count("z_id")).ias("zid").orderBy("partition_id").;
			/*
			ZFrame<D,R,C>blocksRe = blocks.repartition(args.getNumPartitions());
			blocksRe = blocksRe.cache();
			blocksRe.withColumn("partition_id", functions.spark_partition_id())
				.groupBy("partition_id").agg(functions.count("z_zid")).as("zid").orderBy("partition_id").toJavaRDD().saveAsTextFile("/tmp/zblocksPart");
			*/
			ZFrame<D,R,C>dupes = model.predict(blocks); //.exceptAll(allEqual));	
			//allEqual = massageAllEquals(allEqual);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Found dupes " + dupes.count());	
			}
			//dupes = dupes.cache();			
			
			//allEqual = allEqual.cache();
			//writeOutput(blocked, dupes.union(allEqual).cache());		
			ZFrame<D,R,C>dupesActual = getDupesActualForGraph(dupes);
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
			//-1 is initial suggestion, 1 is add, 0 is deletion, 2 is unsure
			/*blocked = blocked.drop(ColName.HASH_COL);
			blocked = blocked.drop(ColName.SOURCE_COL);
			blocked = blocked.cache();
			*/
			
			dupesActual = dupesActual.cache();
			System.out.println("dupes ------------");
			dupesActual.show();
			ZFrame<D,R,C>graph = getGraphUtil().buildGraph(blocked, dupesActual).cache();
			//graph.toJavaRDD().saveAsTextFile("/tmp/zgraph");
			System.out.println("graph ------------");
			graph.show();
			//write score
			ZFrame<D,R,C>score = getMinMaxScores(dupesActual, graph).cache();
			//score.toJavaRDD().coalesce(1).saveAsTextFile("/tmp/zallscoresAvg");
			graph = graph.repartition(args.getNumPartitions(), graph.col(ColName.ID_COL)).cache();
			
			score.show();
			ZFrame<D,R,C>graphWithScores = getDSUtil().joinZColFirst(
				score, graph, ColName.ID_COL, false).cache();
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
			getPipeUtil().write(graphWithScores, args, args.getOutput());
		}
		}
		catch(Exception e) {
			e.printStackTrace(); 
		}
		
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
		
		return allScores.groupByMinMax(allScores.col(ColName.ID_COL));			
	}

	protected ZFrame<D,R,C>getDupesActualForGraph(ZFrame<D,R,C>dupes) {
		ZFrame<D,R,C> dupesActual = selectColsFromDupes(dupes);
		LOG.debug("dupes al");
		if (LOG.isDebugEnabled()) dupes.show();
		return dupes.filter(dupes.equalTo(ColName.PREDICTION_COL,ColValues.IS_MATCH_PREDICTION));
	}

	protected ZFrame<D,R,C>selectColsFromDupes(ZFrame<D,R,C>dupesActual) {
		List<C> cols = new ArrayList<C>();
		cols.add(dupesActual.col(ColName.ID_COL));
		cols.add(dupesActual.col(ColName.COL_PREFIX + ColName.ID_COL));
		cols.add(dupesActual.col(ColName.PREDICTION_COL));
		cols.add(dupesActual.col(ColName.SCORE_COL));
		ZFrame<D,R,C> dupesActual1 = dupesActual.select(cols); //.cache();
		return dupesActual1;
	}
	

	    
}
