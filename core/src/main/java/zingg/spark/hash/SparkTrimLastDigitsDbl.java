package zingg.spark.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.hash.TrimLastDigitsDbl;

/**
 * Spark specific trim function for double
 * 
 * 
 * @author vikasgupta
 *
 */
public class SparkTrimLastDigitsDbl extends TrimLastDigitsDbl {
	
	public static final Log LOG = LogFactory.getLog(SparkTrimLastDigitsDbl.class);

	public SparkTrimLastDigitsDbl(int count){
	    super(count);
	}
	
}
