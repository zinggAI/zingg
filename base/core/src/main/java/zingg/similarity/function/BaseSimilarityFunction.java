package zingg.similarity.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.param.Param;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.param.shared.HasInputCol;
import org.apache.spark.ml.param.shared.HasOutputCol;
import org.apache.spark.ml.util.Identifiable$;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

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
