package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.core.hash.TruncateDouble;

/**
 * Spark specific trunc function for double
 * 
 * 
 *
 */
public class SparkTruncateDouble extends SparkHashFunction<Double, Double>{
	
	public static final Log LOG = LogFactory.getLog(SparkTruncateDouble.class);
	
	public SparkTruncateDouble(int count){
	    setBaseHash(new TruncateDouble(count));
	    setDataType(DataTypes.DoubleType);
	    setReturnType(DataTypes.DoubleType);
	}
	
}
