package zingg.common.core.util;

import zingg.common.client.ZFrame;

public interface GraphUtil<D,R,C> {

	public ZFrame<D,R,C> buildGraph(ZFrame<D,R,C> vertices, ZFrame<D,R,C>edges) ;
	

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
