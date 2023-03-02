package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.hash.IdentityInteger;

public class SparkIdentityInteger extends SparkHashFunction<Integer, Integer>{
    
    public static final Log LOG = LogFactory.getLog(SparkIdentityInteger.class);

    public SparkIdentityInteger() {
	    setBaseHash(new IdentityInteger());
		setDataType(DataTypes.IntegerType);
		setReturnType(DataTypes.IntegerType);
	}
	
}
