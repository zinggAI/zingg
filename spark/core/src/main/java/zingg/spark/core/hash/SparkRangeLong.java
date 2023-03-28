package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.core.hash.RangeLong;

public class SparkRangeLong extends SparkHashFunction<Long, Long>{
    
    private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(SparkRangeLong.class);
    
	public SparkRangeLong(long lower, long upper) {
	    setBaseHash(new RangeLong(lower ,upper));
		setDataType(DataTypes.LongType);
		setReturnType(DataTypes.LongType);
	}

}
