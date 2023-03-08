package zingg.spark.core.hash.unused;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.spark.core.hash.SparkTrimLastDigitsInt;

/**
 * Spark specific trim function for integer last 2 digits
 * 
 * 
 *
 */
public class SparkTrimLast2DigitsInt extends SparkTrimLastDigitsInt {
	
	public static final Log LOG = LogFactory.getLog(SparkTrimLast2DigitsInt.class);

	public SparkTrimLast2DigitsInt(){
	    super(2);
	}
	
}
