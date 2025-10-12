package zingg.common.core.match.output;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.client.util.DSUtil;
import zingg.common.core.util.GraphUtil;

public class GraphMatchOutputBuilder<S,D,R,C> extends AOutputBuilder<S,D,R,C>{

    public static final Log LOG = LogFactory.getLog(GraphMatchOutputBuilder.class); 
    private GraphUtil<D,R,C> graphUtil;
    
    
    public GraphMatchOutputBuilder(GraphUtil<D,R,C> g, DSUtil<S,D,R,C> dsUtil, IArguments args){
        super(dsUtil, args);
		this.graphUtil = g;
    }

    public GraphUtil<D, R, C> getGraphUtil() {
        return graphUtil;
    }

    public void setGraphUtil(GraphUtil<D, R, C> graphUtil) {
        this.graphUtil = graphUtil;
    }

	public ZFrame<D,R,C> getGraph(ZFrame<D, R, C> blocked, ZFrame<D, R, C> dupesActual) throws ZinggClientException{
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

		return graph;
	}

	public ZFrame<D,R,C> getScores(ZFrame<D, R, C> graph, ZFrame<D, R, C> dupesActual) throws Exception{
		//write score
		ZFrame<D,R,C> score = getMinMaxScores(dupesActual, graph).cache();
		//score.toJavaRDD().coalesce(1).saveAsTextFile("/tmp/zallscoresAvg");
		graph = graph.repartition(args.getNumPartitions(), graph.col(ColName.ID_COL)).cache();
		if (LOG.isDebugEnabled()) {
			score.show();
		}
		ZFrame<D, R, C> graphWithScores = getGraphWithScores(graph, score);
			//graphWithScores.toJavaRDD().saveAsTextFile("/tmp/zgraphWScores");
		return graphWithScores;	
	}

	public ZFrame<D, R, C> dropColumns(ZFrame<D, R, C> graphWithScores){
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

    public ZFrame<D, R, C> getOutput(ZFrame<D, R, C> blocked, ZFrame<D, R, C> dupesActual) throws ZinggClientException, Exception {
		ZFrame<D,R,C> graph = getGraph(blocked, dupesActual);
		ZFrame<D,R,C> score = getScores(graph, dupesActual);
		ZFrame<D, R, C> graphWithScores = dropColumns(score);
		return graphWithScores;
	}

    protected ZFrame<D, R, C> getGraphWithScoresOrig(ZFrame<D, R, C> graph, ZFrame<D, R, C> score) {
		graph = graph.withColumnRenamed(ColName.ID_COL, ColName.COL_PREFIX + ColName.ID_COL);
		ZFrame<D, R, C> pairs = score.join(graph, ColName.ID_COL, true, "right");
		//in training, we only need that record matches only with lines bigger than itself
		//in the case of normal as well as in the case of linking
		if (LOG.isDebugEnabled()) {
			LOG.debug("pairs length " + pairs.count());
		}
		//if (filter) pairs = pairs.filter(pairs.gt(ColName.ID_COL));		
		ZFrame<D,R,C> graphWithScores = pairs.drop(ColName.COL_PREFIX + ColName.ID_COL).cache();
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

}
