package zingg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Functions;
import static com.snowflake.snowpark_java.Functions.col;

import scala.collection.JavaConverters;
import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.model.Model;

import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.util.Analytics;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.client.util.Util;
import zingg.util.BlockingTreeUtil;
import zingg.util.DSUtil;
import zingg.util.GraphUtil;
import zingg.util.ModelUtil;
import zingg.util.PipeUtil;

public class Matcher extends ZinggBase{

	protected static String name = "zingg.Matcher";
	public static final Log LOG = LogFactory.getLog(Matcher.class);    

    public Matcher() {
        setZinggOptions(ZinggOptions.MATCH);
    }

	protected DataFrame getTestData() {
		return PipeUtil.read(snow, true, args.getNumPartitions(), true, args.getData());
	}

	protected DataFrame getBlocked(DataFrame testData) throws Exception{
		LOG.debug("Blocking model file location is " + args.getBlockFile());
		Tree<Canopy> tree = BlockingTreeUtil.readBlockingTree(snow, args);
		DataFrame blocked = testData.map(new Block.BlockFunction(tree), RowEncoder.apply(Block.appendHashCol(testData.schema())));
		//DataFrame blocked1 = blocked.repartition(args.getNumPartitions(), blocked.col(ColName.HASH_COL)); //.cacheResult();
		DataFrame blocked1 = blocked;
		return blocked1;
	}

	protected DataFrame getBlocks(DataFrame blocked) throws Exception{
		return DSUtil.joinWithItself(blocked, ColName.HASH_COL, true).cacheResult();
	}

	protected DataFrame getBlocks(DataFrame blocked, DataFrame bAll) throws Exception{
		//DataFrame joinH = DSUtil.joinWithItself(blocked, ColName.HASH_COL, true).cacheResult();
		DataFrame joinH = blocked.as("first").join(blocked.as("second"), ColName.HASH_COL)
			.selectExpr("first.z_zid as z_zid", "second.z_zid as z_z_zid");
		joinH.show();
		joinH = joinH.filter(joinH.col(ColName.ID_COL).gt(joinH.col(ColName.COL_PREFIX + ColName.ID_COL)));	
		LOG.warn("Num comparisons " + joinH.count());
		//joinH = joinH.repartition(args.getNumPartitions(), joinH.col(ColName.ID_COL));
		//bAll = bAll.repartition(args.getNumPartitions(), bAll.col(ColName.ID_COL));
		joinH = joinH.join(bAll, ColName.ID_COL);
		LOG.warn("Joining with actual values");
		//joinH.show();
		bAll = DSUtil.getPrefixedColumnsDS(bAll);
		//bAll.show();
		//joinH = joinH.repartition(args.getNumPartitions(), joinH.col(ColName.COL_PREFIX + ColName.ID_COL));
		joinH = joinH.join(bAll, ColName.COL_PREFIX + ColName.ID_COL);
		LOG.warn("Joining again with actual values");
		//joinH.show();
		return joinH;
	}

	protected DataFrame massageAllEquals(DataFrame allEqual) {
		allEqual = allEqual.withColumn(ColName.PREDICTION_COL, 
			Functions.lit(ColValues.IS_MATCH_PREDICTION));
		allEqual = allEqual.withColumn(ColName.SCORE_COL, Functions.lit(ColValues.FULL_MATCH_SCORE));
		return allEqual;
	}

	protected Model getModel() {
		Model model = new Model(this.featurers);
		model.register(snow);
		model.load(args.getModel());
		return model;
	}

	protected DataFrame selectColsFromBlocked(DataFrame blocked) {
		return blocked.select(ColName.ID_COL, ColName.HASH_COL);
	}

    public void execute() throws ZinggClientException {
        try {
			// read input, filter, remove self joins
			DataFrame testData = getTestData();
			//testData = testData.repartition(args.getNumPartitions(), testData.col(ColName.ID_COL));
			//testData = dropDuplicates(testData);
			long count = testData.count();
			LOG.info("Read " + count);
			//Analytics.track(Metric.DATA_COUNT, count, args.getCollectMetrics());

			DataFrame blocked = getBlocked(testData);
			LOG.info("Blocked ");
			/*blocked = blocked.cacheResult();
			blocked.withColumn("partition_id", functions.spark_partition_id())
				.groupBy("partition_id").agg(functions.count("z_zid")).as("zid").orderBy("partition_id").toJavaRDD().saveAsTextFile("/tmp/zblockedParts");
				*/
			if (LOG.isDebugEnabled()) {
				LOG.debug("Num distinct hashes " + blocked.select(ColName.HASH_COL).distinct().count());
			}
				//LOG.warn("Num distinct hashes " + blocked.agg(functions.approx_count_distinct(ColName.HASH_COL)).count());
			DataFrame blocks = getBlocks(selectColsFromBlocked(blocked), testData);
			//blocks.explain();
			//LOG.info("Blocks " + blocks.count());
			if (LOG.isDebugEnabled()) {
				LOG.debug("block size" + blocks.count());
			}
			//blocks.toJavaRDD().saveAsTextFile("/tmp/zblocks");
			//check if all fields equal			
			//DataFrame allEqual = DSUtil.allFieldsEqual(blocks, args);
			//allEqual = allEqual.cache();
			//send remaining to model 
			Model model = getModel();
			//blocks.cacheResult().withColumn("partition_id", functions.spark_partition_id())
			//	.groupBy("partition_id").agg(functions.count("z_id")).ias("zid").orderBy("partition_id").;
			/*
			DataFrame blocksRe = blocks.repartition(args.getNumPartitions());
			blocksRe = blocksRe.cache();
			blocksRe.withColumn("partition_id", functions.spark_partition_id())
				.groupBy("partition_id").agg(functions.count("z_zid")).as("zid").orderBy("partition_id").toJavaRDD().saveAsTextFile("/tmp/zblocksPart");
			*/
			DataFrame dupes = model.predict(blocks); //.exceptAll(allEqual));	
			//allEqual = massageAllEquals(allEqual);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Found dupes " + dupes.count());	
			}
			//dupes = dupes.cache();			
			
			//allEqual = allEqual.cache();
			//writeOutput(blocked, dupes.union(allEqual).cache());		
			DataFrame dupesActual = getDupesActualForGraph(dupes);
			//dupesActual.explain();
			//dupesActual.toJavaRDD().saveAsTextFile("/tmp/zdupes");
			
			writeOutput(testData, dupesActual);		
			
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
    }

	public void writeOutput(DataFrame blocked, DataFrame dupesActual) {
		try{
		//input dupes are pairs
		///pick ones according to the threshold by user
		
			
		//all clusters consolidated in one place
		if (args.getOutput() != null) {
			//-1 is initial suggestion, 1 is add, 0 is deletion, 2 is unsure
			/*blocked = blocked.drop(ColName.HASH_COL);
			blocked = blocked.drop(ColName.SOURCE_COL);
			blocked = blocked.cacheResult();
			*/
			
			dupesActual = dupesActual.cacheResult();
			DataFrame graph = GraphUtil.buildGraph(blocked, dupesActual).cacheResult();
			//graph.toJavaRDD().saveAsTextFile("/tmp/zgraph");
			
			//write score
			DataFrame score = getMinMaxScores(dupesActual, graph).cacheResult();
			//score.toJavaRDD().coalesce(1).saveAsTextFile("/tmp/zallscoresAvg");
			//graph = graph.repartition(args.getNumPartitions(), graph.col(ColName.ID_COL)).cacheResult();
			DataFrame graphWithScores = DSUtil.joinZColFirst(
				score, graph, ColName.ID_COL, false).cacheResult();
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
			graphWithScores = DSUtil.select(graphWithScores, columns);
			*/
			PipeUtil.write(graphWithScores, args, args.getOutput());
		}
		}
		catch(Exception e) {
			e.printStackTrace(); 
		}
		
	}

	protected DataFrame getMinMaxScores(DataFrame dupes, DataFrame graph) {
		if (LOG.isDebugEnabled()) dupes.show(500);
		
		DataFrame graph1 = graph.select(ColName.ID_COL, ColName.CLUSTER_COLUMN);
		//graph1 = graph1.repartition(args.getNumPartitions(),
			//graph1.col(ColName.CLUSTER_COLUMN));
		DataFrame dupesWithIds = dupes.select(ColName.ID_COL, ColName.COL_PREFIX + ColName.ID_COL);
		LOG.warn("Dupes with ids ");
		if (LOG.isDebugEnabled()) dupesWithIds.show(500);
		DataFrame graphPairsFound = graph1.join(graph1, ColName.CLUSTER_COLUMN);
			//TODO-SNOW .select(Functions.col("l_...._z_zid").as("z_zid"), "r_z_zid as z_z_zid");
		graphPairsFound = graphPairsFound.filter(graphPairsFound.col(ColName.ID_COL).gt(graphPairsFound.col(ColName.COL_PREFIX + ColName.ID_COL)));
		
		LOG.warn("graph pairs ");
		if (LOG.isDebugEnabled()) graphPairsFound.show(500);
		
		DataFrame graphPairsExtra = graphPairsFound.except(dupesWithIds);
		DataFrame graphPairsExtrawithDummyScore = graphPairsExtra.withColumn(ColName.SCORE_COL, Functions.lit(0.0));
		LOG.warn("graph pairs extra");
		if (LOG.isDebugEnabled()) graphPairsExtra.show(500);
		
		//original
		DataFrame s1 = dupes.select(ColName.SCORE_COL, ColName.ID_COL);
		DataFrame s2 = dupes.select(ColName.SCORE_COL, ColName.COL_PREFIX + ColName.ID_COL);

		//add the graph discovered extra pairs
		s1 = s1.union(graphPairsExtrawithDummyScore.select(ColName.SCORE_COL, ColName.ID_COL));
		s2 = s2.union(graphPairsExtrawithDummyScore.select(ColName.SCORE_COL, ColName.COL_PREFIX + ColName.ID_COL));
		List<Column> cols = new ArrayList<Column>();
		
		DataFrame s1RightCols = s1.toDF(ColName.SCORE_COL, ColName.COL_PREFIX + ColName.ID_COL).cacheResult();
		DataFrame allScores = s1RightCols.union(s2);
		//allScores.toJavaRDD().coalesce(1).saveAsTextFile("/tmp/zallscores");
		/*WindowSpec window = Window.partitionBy(ColName.ID_COL).orderBy(ColName.SCORE_COL);
		//WindowSpec window = Window.orderBy(ColName.CLUSTER_COLUMN);
		DataFrame ranked = allScores.withColumn("rank", functions.rank().over(window)).
			withColumn("minScore", functions.min(ColName.SCORE_COL).over(window)).
			withColumn("maxScore", functions.max(ColName.SCORE_COL).over(window)).
			where("rank == 1");
			ranked.toJavaRDD().saveAsTextFile("/tmp/allscoresRanked");

		//graph = graph.withColumn("rank", functions.rank().over(window));
		//graph = graph.withColumn(ColName.DENSE_COL, functions.dense_rank().over(window));
		//graph = graph.withColumn("row_num", functions.row_number().over(window));
		*/
		//allScores = allScores.repartition(args.getNumPartitions(), allScores.col(ColName.COL_PREFIX + ColName.ID_COL));
		
		return allScores.groupBy(allScores.col(ColName.COL_PREFIX + ColName.ID_COL)).agg(
			Functions.min(col(ColName.SCORE_COL)).as(ColName.SCORE_MIN_COL),
			Functions.max(col(ColName.SCORE_COL)).as(ColName.SCORE_MAX_COL));			
	}

	protected DataFrame getDupesActualForGraph(DataFrame dupes) {
		DataFrame dupesActual = selectColsFromDupes(dupes);
		LOG.debug("dupes al");
		if (LOG.isDebugEnabled()) dupes.show();
		return dupes.filter(dupes.col(ColName.PREDICTION_COL).in(ColValues.IS_MATCH_PREDICTION));
	}

	protected DataFrame selectColsFromDupes(DataFrame dupesActual) {
		List<Column> cols = new ArrayList<Column>();
		cols.add(dupesActual.col(ColName.ID_COL));
		cols.add(dupesActual.col(ColName.COL_PREFIX + ColName.ID_COL));
		cols.add(dupesActual.col(ColName.PREDICTION_COL));
		cols.add(dupesActual.col(ColName.SCORE_COL));
		DataFrame dupesActual1 = dupesActual.select(cols.toArray(Column[]::new)); //.cacheResult();
		return dupesActual1;
	}
	

	    
}
