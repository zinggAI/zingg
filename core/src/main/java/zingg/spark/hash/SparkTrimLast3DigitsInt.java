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
public class SparkTrimLast3DigitsInt extends SparkTrimLastDigitsInt {
	
	public static final Log LOG = LogFactory.getLog(SparkTrimLast3DigitsInt.class);

	public SparkTrimLast3DigitsInt(){
	    super(3);
	}
	
}
