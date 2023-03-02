package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.core.hash.FirstChars;


public class SparkFirstChars extends SparkHashFunction<String, String>{
	
	public static final Log LOG = LogFactory.getLog(SparkFirstChars.class);
	
	public SparkFirstChars(int endIndex) {
	    setBaseHash(new FirstChars(endIndex));
		setDataType(DataTypes.StringType);
		setReturnType(DataTypes.StringType);
	}

}
