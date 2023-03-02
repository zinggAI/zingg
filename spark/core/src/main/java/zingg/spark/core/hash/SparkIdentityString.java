package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.hash.IdentityString;

public class SparkIdentityString extends SparkHashFunction<String, String>{
    
    public static final Log LOG = LogFactory.getLog(SparkIdentityString.class);
	
	public SparkIdentityString() {
	    setBaseHash(new IdentityString());
		setDataType(DataTypes.StringType);
		setReturnType(DataTypes.StringType);
	}
	
}
