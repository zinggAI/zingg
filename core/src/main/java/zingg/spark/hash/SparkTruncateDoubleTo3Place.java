package zingg.spark.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Spark specific trunc function for double (3 digit)
 * 
 * 
 * @author vikasgupta
 *
 */
public class SparkTruncateDoubleTo3Place extends SparkTruncateDouble {
	
	public static final Log LOG = LogFactory.getLog(SparkTruncateDoubleTo3Place.class);

	public SparkTruncateDoubleTo3Place(){
	    super(3);
	}
	
}
