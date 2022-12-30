package zingg.spark.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Spark specific trim function for double last 3 digits
 * 
 * 
 * @author vikasgupta
 *
 */
public class SparkTrimLast3DigitDbl extends SparkTrimLastDigitsDbl {
	
	public static final Log LOG = LogFactory.getLog(SparkTrimLast3DigitDbl.class);

	public SparkTrimLast3DigitDbl(){
	    super(3);
	}
	
}
