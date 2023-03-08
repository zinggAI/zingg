package zingg.spark.core.hash.unused;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.spark.core.hash.SparkTrimLastDigitsDbl;

/**
 * Spark specific trim function for double last 1 digit
 * 
 *
 */
public class SparkTrimLast1DigitDbl extends SparkTrimLastDigitsDbl {
	
	public static final Log LOG = LogFactory.getLog(SparkTrimLast1DigitDbl.class);

	public SparkTrimLast1DigitDbl(){
	    super(1);
	}
	
}
