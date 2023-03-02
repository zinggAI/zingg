package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.core.hash.LastChars;

public class SparkLastChars extends SparkHashFunction<String, String>{
    
    public static final Log LOG = LogFactory.getLog(SparkLastChars.class);
	
	public SparkLastChars(int endIndex) {
	    setBaseHash(new LastChars(endIndex));
		setDataType(DataTypes.StringType); 
		setReturnType(DataTypes.StringType);
	}
	
}
