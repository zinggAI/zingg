package zingg.spark.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.hash.TrimLastDigitsDbl;

/**
 * Spark specific trim function for double
 * 
 * 
 * @author vikasgupta
 *
 */
public class SparkTrimLastDigitsDbl extends SparkHashFunction<Double, Double>{
	
	public static final Log LOG = LogFactory.getLog(SparkTrimLastDigitsDbl.class);
 
	public SparkTrimLastDigitsDbl(int count){
	    setBaseHash(new TrimLastDigitsDbl(count));
        setDataType(DataTypes.DoubleType);
        setReturnType(DataTypes.DoubleType);
	}
    
}
