package zingg.spark.core.hash.unused;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.spark.core.hash.SparkTruncateDouble;

/**
 * Spark specific trunc function for double (3 digit)
 * 
 * 
 *
 */
public class SparkTruncateDoubleTo3Places extends SparkTruncateDouble {
	
	public static final Log LOG = LogFactory.getLog(SparkTruncateDoubleTo3Places.class);

	public SparkTruncateDoubleTo3Places(){
	    super(3);
	}
	
}
