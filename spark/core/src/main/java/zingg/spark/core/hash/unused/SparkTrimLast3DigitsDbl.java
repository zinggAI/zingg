package zingg.spark.core.hash.unused;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.spark.core.hash.SparkTrimLastDigitsDbl;

/**
 * Spark specific trim function for double last 3 digits
 * 
 * 
 * @author vikasgupta
 *
 */
public class SparkTrimLast3DigitsDbl extends SparkTrimLastDigitsDbl {
	
	public static final Log LOG = LogFactory.getLog(SparkTrimLast3DigitsDbl.class);

	public SparkTrimLast3DigitsDbl(){
	    super(3);
	}
	
}
