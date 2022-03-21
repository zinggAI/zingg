package zingg.model;

import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.util.Identifiable$;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.types.DataTypes;

import zingg.similarity.function.BaseTransformer;
import zingg.client.util.ColName;

public class VectorValueExtractor extends BaseTransformer implements UDF1<Vector, Double>{
	
	@Override
	public Double call(Vector v) {
		return v.toArray()[1];
	}
	
	@Override
	public void register(SparkSession spark) {
    	spark.udf().register(uid, (UDF1) this, DataTypes.DoubleType);
    }
	
	@Override
	public String getUid() {
   	if (uid == null) {
   		uid = Identifiable$.MODULE$.randomUID("VectorValueExtractor");
   	}
   	return uid;
   }
	
	@Override	
	public Dataset<Row> transform(Dataset<?> ds){
		LOG.debug("transforming dataset for " + uid);
		transformSchema(ds.schema());
		return ds.withColumn(getOutputCol(), 
				functions.callUDF(this.uid, ds.col(getInputCol())));
	}

}
