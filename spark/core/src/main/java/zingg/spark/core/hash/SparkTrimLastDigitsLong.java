package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.core.hash.TrimLastDigitsLong;

/**
 * Spark specific trim function for Long
 * 
 *
 */
public class SparkTrimLastDigitsLong extends SparkHashFunction<Long, Long>{
	
	private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(SparkTrimLastDigitsLong.class);

	public SparkTrimLastDigitsLong(int count){
	    setBaseHash(new TrimLastDigitsLong(count));
		setDataType(DataTypes.LongType);
		setReturnType(DataTypes.LongType);
	}
	
}
