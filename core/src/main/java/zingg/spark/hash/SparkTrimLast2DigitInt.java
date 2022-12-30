package zingg.spark.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Spark specific trim function for integer last 2 digits
 * 
 * 
 * @author vikasgupta
 *
 */
public class SparkTrimLast2DigitInt extends SparkTrimLastDigitsInt {
	
	public static final Log LOG = LogFactory.getLog(SparkTrimLast2DigitInt.class);

	public SparkTrimLast2DigitInt(int count){
	    super(count);
	}
	
}
