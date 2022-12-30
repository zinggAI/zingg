package zingg.spark.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Spark specific trunc function for double (2 digit)
 * 
 * 
 * @author vikasgupta
 *
 */
public class SparkTruncateDoubleTo2Place extends SparkTruncateDouble {
	
	public static final Log LOG = LogFactory.getLog(SparkTruncateDoubleTo2Place.class);

	public SparkTruncateDoubleTo2Place(){
	    super(2);
	}
	
}
