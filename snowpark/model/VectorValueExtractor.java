package zingg.snowpark.model;

import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.ml.util.Identifiable$;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Session;
import com.snowflake.snowpark_java.Functions;
import com.snowflake.snowpark_java.types.DataTypes;
import com.snowflake.snowpark_java.udf.JavaUDF1;
import com.snowflake.snowpark_java.udf.JavaUDF2;

import zingg.similarity.function.BaseTransformer;
import zingg.client.util.ColName;

public class VectorValueExtractor extends BaseTransformer implements JavaUDF1<Vector, Double>{
	
	@Override
	public Double call(Vector v) {
		return v.toArray()[1];
	}
	
	@Override
	public void register(Session snow) {
    	snow.udf().register(uid, (JavaUDF1) this, DataTypes.DoubleType);
    }
	
	@Override
	public String getUid() {
   	if (uid == null) {
   		uid = Identifiable$.MODULE$.randomUID("VectorValueExtractor");
   	}
   	return uid;
   }
	
	@Override	
	public DataFrame transform(DataFrame df){
		LOG.debug("transforming dataset for " + uid);
		transformSchema(df.schema());
		return df.withColumn(getOutputCol(), 
				Functions.callUDF(this.uid, df.col(getInputCol())));
	}

}
