/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
