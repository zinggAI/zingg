package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.core.hash.IdentityLong;

public class SparkIdentityLong extends SparkHashFunction<Long, Long>{
    
    private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(SparkIdentityLong.class);

    public SparkIdentityLong() {
	    setBaseHash(new IdentityLong());
		setDataType(DataTypes.LongType);
		setReturnType(DataTypes.LongType);
	}
	
}
