package zingg.spark.core.hash.unused;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.spark.core.hash.SparkTruncateDouble;

/**
 * Spark specific trunc function for double (2 digit)
 * 
 * 
 *
 */
public class SparkTruncateDoubleTo2Places extends SparkTruncateDouble {
	
	public static final Log LOG = LogFactory.getLog(SparkTruncateDoubleTo2Places.class);

	public SparkTruncateDoubleTo2Places(){
	    super(2);
	}
	
}
