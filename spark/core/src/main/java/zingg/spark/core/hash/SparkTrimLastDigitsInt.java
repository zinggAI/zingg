package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.hash.TrimLastDigitsInt;

/**
 * Spark specific trim function for Integer
 * 
 * 
 * @author vikasgupta
 *
 */
public class SparkTrimLastDigitsInt extends SparkHashFunction<Integer, Integer>{
	
	public static final Log LOG = LogFactory.getLog(SparkTrimLastDigitsInt.class);

	public SparkTrimLastDigitsInt(int count){
	    setBaseHash(new TrimLastDigitsInt(count));
        setDataType(DataTypes.IntegerType);
        setReturnType(DataTypes.IntegerType);
	}
	
}
