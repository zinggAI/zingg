package zingg.spark.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.graphx.Edge;
import org.apache.spark.graphx.Graph;

import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Functions;

import org.apache.spark.storage.StorageLevel;
import org.graphframes.GraphFrame;
import org.graphframes.lib.AggregateMessages;

import zingg.client.SparkFrame;
import zingg.client.ZFrame;
import zingg.client.util.ColName;
import scala.collection.JavaConverters;
import scala.reflect.ClassTag;
import zingg.scala.DFUtil;
import zingg.util.GraphUtil;

public class SnowGraphUtil implements GraphUtil<DataFrame, Row, Column> {

	public ZFrame<DataFrame, Row, Column> buildGraph(ZFrame<DataFrame, Row, Column> vOrig, ZFrame<DataFrame, Row, Column> ed) {
		// we need to transform the input here by using stop words
		//rename id field which is a common field in data to another field as it 
		//clashes with graphframes :-(
		DataFrame vertices = vOrig.df();	
		DataFrame edges = ed.df();
		vertices = vertices.withColumnRenamed(ColName.ID_EXTERNAL_ORIG_COL, ColName.ID_EXTERNAL_COL);
		
		DataFrame v1 = vertices.withColumnRenamed(ColName.ID_COL, "id");
		DataFrame v = v1.select("id").cache();
		List<Column> cols = new ArrayList<Column>();
		cols.add(edges.col(ColName.ID_COL));
		cols.add(edges.col(ColName.COL_PREFIX + ColName.ID_COL));
		
		DataFrame e = edges.select(JavaConverters.asScalaIteratorConverter(
				cols.iterator()).asScala().toSeq());
		e = e.toDF("src","dst").cache();
		GraphFrame gf = new GraphFrame(v, e);
		//gf = gf.dropIsolatedVertices();
		//DataFrame returnGraph = gf.connectedComponents().setAlgorithm("graphx").run().cache();
		DataFrame returnGraph = gf.connectedComponents().run().cache();
		//reverse back o avoid graphframes id :-()
		returnGraph = returnGraph.join(vertices, returnGraph.col("id").equalTo(vertices.col(ColName.ID_COL)));
		returnGraph = returnGraph.drop(ColName.ID_COL).withColumnRenamed("id", ColName.ID_COL);		
		returnGraph = returnGraph.withColumnRenamed("component", ColName.CLUSTER_COLUMN);
		returnGraph = returnGraph.withColumnRenamed(ColName.ID_EXTERNAL_COL, ColName.ID_EXTERNAL_ORIG_COL);
		return new SnwoFrame(returnGraph);
	}

	

	/*
	public static DataFrame buildGraph(DataFrame vertices, DataFrame edges, boolean withScore) {
		// we need to transform the input here by using stop words
		//rename id field which is a common field in data to another field as it 
		//clashes with graphframes :-(
		vertices = vertices.withColumnRenamed(ColName.ID_EXTERNAL_ORIG_COL, ColName.ID_EXTERNAL_COL);
		
		DataFrame v = vertices.withColumnRenamed(ColName.ID_COL, "id").cache();
		List<Column> cols = new ArrayList<Column>();
		cols.add(edges.col(ColName.ID_COL));
		cols.add(edges.col(ColName.COL_PREFIX + ColName.ID_COL));
		cols.add(edges.col(ColName.SCORE_COL));
		DataFrame e = edges.select(JavaConverters.asScalaIteratorConverter(
				cols.iterator()).asScala().toSeq());
		e = e.toDF("src","dst", "MSG").cache();
		e.show(true);
		GraphFrame gf = new GraphFrame(v, e);
		//gf = gf.dropIsolatedVertices();
		DataFrame returnGraph = gf.connectedComponents().run().cache();
		
		DataFrame dsWithScore = DFUtil.scoring(returnGraph, e);
		//reverse back o avoid graphframes id :-()
		dsWithScore = dsWithScore.withColumnRenamed("id", ColName.ID_COL);
		dsWithScore = dsWithScore.withColumnRenamed("component", ColName.CLUSTER_COLUMN);
		dsWithScore = dsWithScore.withColumnRenamed(ColName.ID_EXTERNAL_COL, ColName.ID_EXTERNAL_ORIG_COL);
		dsWithScore.show(false);
		return dsWithScore;
	}
*/	
			
}
