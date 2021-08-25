package zingg.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.graphx.Edge;
import org.apache.spark.graphx.Graph;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.storage.StorageLevel;
import org.graphframes.GraphFrame;
import org.graphframes.lib.AggregateMessages;

import zingg.client.util.ColName;
import scala.collection.JavaConverters;
import scala.reflect.ClassTag;
import zingg.scala.DFUtil;

public class GraphUtil {

	public static Dataset<Row> buildGraph(Dataset<Row> vertices, Dataset<Row> edges) {
		// we need to transform the input here by using stop words
		//rename id field which is a common field in data to another field as it 
		//clashes with graphframes :-(
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
		Dataset<Row> returnGraph = gf.connectedComponents().run().cache();
		//reverse back o avoid graphframes id :-()
		returnGraph = returnGraph.join(vertices, returnGraph.col("id").equalTo(vertices.col(ColName.ID_COL)));
		returnGraph = returnGraph.drop(ColName.ID_COL).withColumnRenamed("id", ColName.ID_COL);		
		returnGraph = returnGraph.withColumnRenamed("component", ColName.CLUSTER_COLUMN);
		returnGraph = returnGraph.withColumnRenamed(ColName.ID_EXTERNAL_COL, ColName.ID_EXTERNAL_ORIG_COL);
		return returnGraph;
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
