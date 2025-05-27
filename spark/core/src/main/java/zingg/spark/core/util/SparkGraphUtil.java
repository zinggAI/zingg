package zingg.spark.core.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.graphframes.GraphFrame;

import org.graphframes.lib.ConnectedComponents;
import scala.collection.JavaConverters;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;
import zingg.common.core.util.GraphUtil;
import zingg.spark.client.SparkFrame;

public class SparkGraphUtil implements GraphUtil<Dataset<Row>, Row, Column> {

	private static final String CHECKPOINT_INTERVAL_ENV_VARIABLE = "ZINGG_CONNECTED_COMPONENT_CHECKPOINT_INTERVAL";

	public ZFrame<Dataset<Row>, Row, Column> buildGraph(ZFrame<Dataset<Row>, Row, Column> vOrig, ZFrame<Dataset<Row>, Row, Column> ed) {
		// we need to transform the input here by using stop words
		//rename id field which is a common field in data to another field as it 
		//clashes with graphframes :-(
		vOrig = vOrig.cache();
		Dataset<Row> vertices = vOrig.df();	
		Dataset<Row> edges = ed.df();
		vertices = vertices.withColumnRenamed(ColName.ID_EXTERNAL_ORIG_COL, ColName.ID_EXTERNAL_COL);
		
		Dataset<Row> v1 = vertices.withColumnRenamed(ColName.ID_COL, "id");
		Dataset<Row> v = v1.select("id").cache();
		List<Column> cols = new ArrayList<Column>();
		cols.add(edges.col(ColName.ID_COL));
		cols.add(edges.col(ColName.COL_PREFIX + ColName.ID_COL));
		
		Dataset<Row> e = edges.select(JavaConverters.asScalaIteratorConverter(
				cols.iterator()).asScala().toSeq());
		e = e.toDF("src","dst").cache();
		GraphFrame gf = new GraphFrame(v, e);
		//gf = gf.dropIsolatedVertices();
		//Dataset<Row> returnGraph = gf.connectedComponents().setAlgorithm("graphx").run().cache();

		Dataset<Row> returnGraph = setCheckpointInterval(gf.connectedComponents()).run().cache();
		//reverse back o avoid graphframes id :-()
		returnGraph = returnGraph.join(vertices, returnGraph.col("id").equalTo(vertices.col(ColName.ID_COL)));
		returnGraph = returnGraph.drop(ColName.ID_COL).withColumnRenamed("id", ColName.ID_COL);		
		returnGraph = returnGraph.withColumnRenamed("component", ColName.CLUSTER_COLUMN);
		returnGraph = returnGraph.withColumnRenamed(ColName.ID_EXTERNAL_COL, ColName.ID_EXTERNAL_ORIG_COL);
		return new SparkFrame(returnGraph);
	}

	private ConnectedComponents setCheckpointInterval(ConnectedComponents connectedComponents) {
		String connectedComponentCheckpointInterval = System.getenv(CHECKPOINT_INTERVAL_ENV_VARIABLE);
		if (connectedComponentCheckpointInterval == null) {
			return connectedComponents;
		}
		return connectedComponents.setCheckpointInterval(Integer.parseInt(connectedComponentCheckpointInterval));
	}

	

	/*
	public static Dataset<Row> buildGraph(Dataset<Row> vertices, Dataset<Row> edges, boolean withScore) {
		// we need to transform the input here by using stop words
		//rename id field which is a common field in data to another field as it 
		//clashes with graphframes :-(
		vertices = vertices.withColumnRenamed(ColName.ID_EXTERNAL_ORIG_COL, ColName.ID_EXTERNAL_COL);
		
		Dataset<Row> v = vertices.withColumnRenamed(ColName.ID_COL, "id").cache();
		List<Column> cols = new ArrayList<Column>();
		cols.add(edges.col(ColName.ID_COL));
		cols.add(edges.col(ColName.COL_PREFIX + ColName.ID_COL));
		cols.add(edges.col(ColName.SCORE_COL));
		Dataset<Row> e = edges.select(JavaConverters.asScalaIteratorConverter(
				cols.iterator()).asScala().toSeq());
		e = e.toDF("src","dst", "MSG").cache();
		e.show(true);
		GraphFrame gf = new GraphFrame(v, e);
		//gf = gf.dropIsolatedVertices();
		Dataset<Row> returnGraph = gf.connectedComponents().run().cache();
		
		Dataset<Row> dsWithScore = DFUtil.scoring(returnGraph, e);
		//reverse back o avoid graphframes id :-()
		dsWithScore = dsWithScore.withColumnRenamed("id", ColName.ID_COL);
		dsWithScore = dsWithScore.withColumnRenamed("component", ColName.CLUSTER_COLUMN);
		dsWithScore = dsWithScore.withColumnRenamed(ColName.ID_EXTERNAL_COL, ColName.ID_EXTERNAL_ORIG_COL);
		dsWithScore.show(false);
		return dsWithScore;
	}
*/	
			
}
