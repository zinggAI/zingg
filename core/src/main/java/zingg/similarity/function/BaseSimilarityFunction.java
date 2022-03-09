package zingg.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.param.Param;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.param.shared.HasInputCol;
import org.apache.spark.ml.param.shared.HasOutputCol;
import org.apache.spark.ml.util.Identifiable$;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Session;
import com.snowflake.snowpark_java.Functions;
import com.snowflake.snowpark_java.udf.JavaUDF2;
import com.snowflake.snowpark_java.types.DataTypes;
import com.snowflake.snowpark_java.types.StructType;

import zingg.client.util.ColName;


public abstract class BaseSimilarityFunction<T> extends BaseTransformer implements SimFunction<T>{
	
	public static final Log LOG = LogFactory.getLog(BaseSimilarityFunction.class);
	
	public BaseSimilarityFunction() {
		
	}

	public BaseSimilarityFunction(String name) {
		super(name);
	}
	
	

	@Override
	public int getNumFeatures() {
		return 1;
	}
	
	
    
}
