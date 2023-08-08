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

package zingg.spark.core.model;

import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

import zingg.spark.client.ZSparkSession;
import zingg.spark.core.similarity.SparkBaseTransformer;

public class VectorValueExtractor extends SparkBaseTransformer implements UDF1<Vector, Double>{

	private static final long serialVersionUID = 1L;

	public VectorValueExtractor(String inputCol, String outputCol) {
		super(inputCol, outputCol, "VectorValueExtractor");
		
	}
	
	@Override
	public Double call(Vector v) {
		return v.toArray()[1];
	}
	
	@Override
	public void register(ZSparkSession spark) {
    	spark.getSession().udf().register(uid, (UDF1) this, DataTypes.DoubleType);
    }
	
	/*@Override
	public String getUid() {
   	if (uid == null) {
   		uid = Identifiable$.MODULE$.randomUID("VectorValueExtractor");
   	}
   	return uid;
   }*/
	
	@Override	
	public Dataset<Row> transform(Dataset<?> ds){
		LOG.debug("transforming dataset for " + uid);
		transformSchema(ds.schema());
		return ds.withColumn(getOutputCol(), 
				functions.callUDF(this.uid, ds.col(getInputCol())));
	}

}
