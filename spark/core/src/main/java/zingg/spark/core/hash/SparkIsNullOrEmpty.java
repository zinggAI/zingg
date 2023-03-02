package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.hash.IsNullOrEmpty;

public class SparkIsNullOrEmpty extends SparkHashFunction<String, Boolean>{
    
    public static final Log LOG = LogFactory.getLog(SparkIsNullOrEmpty.class);
    
	public SparkIsNullOrEmpty() {
	    setBaseHash(new IsNullOrEmpty());
		setDataType(DataTypes.StringType);
		setReturnType(DataTypes.BooleanType);
	}
	
}

	
