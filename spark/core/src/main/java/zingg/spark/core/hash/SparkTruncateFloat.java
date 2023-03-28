package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.core.hash.TruncateFloat;

/**
 * Spark specific trunc function for Float
 * 
 * 
 *
 */
public class SparkTruncateFloat extends SparkHashFunction<Float, Float>{
	
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(SparkTruncateFloat.class);
	
	public SparkTruncateFloat(int count){
	    setBaseHash(new TruncateFloat(count));
	    setDataType(DataTypes.FloatType);
	    setReturnType(DataTypes.FloatType);
	}
	
}
