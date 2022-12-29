package zingg.spark.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.hash.TrimLastDigitsDbl;

/**
 * Spark specific trim function for double last 1 digit
 * 
 * 
 * @author vikasgupta
 *
 */
public class SparkTrimLast1DigitDbl extends SparkTrimLastDigitsDbl {
	
	public static final Log LOG = LogFactory.getLog(SparkTrimLast1DigitDbl.class);

	public SparkTrimLast1DigitDbl(int count){
	    super(count);
	}
	
}
