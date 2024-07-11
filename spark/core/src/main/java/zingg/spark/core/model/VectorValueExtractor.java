package zingg.spark.core.model;

import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

import org.apache.spark.sql.SparkSession;
import zingg.spark.core.similarity.SparkBaseTransformer;
import zingg.spark.core.util.SparkFnRegistrar;

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
	public void register(SparkSession spark) {

		SparkFnRegistrar.registerUDF1(spark, uid, this, DataTypes.DoubleType);
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
