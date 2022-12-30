package zingg.spark.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Spark specific trim function for integer last 3 digits
 * 
 * 
 * @author vikasgupta
 *
 */
public class SparkTrimLast3DigitInt extends SparkTrimLastDigitsInt {
	
	public static final Log LOG = LogFactory.getLog(SparkTrimLast3DigitInt.class);

	public SparkTrimLast3DigitInt(){
	    super(3);
	}
	
}
