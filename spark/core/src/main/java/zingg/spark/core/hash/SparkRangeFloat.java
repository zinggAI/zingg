package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.core.hash.RangeFloat;

public class SparkRangeFloat extends SparkHashFunction<Float, Integer>{
    
    private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(SparkRangeFloat.class);
    
	public SparkRangeFloat(int lower, int upper) {
	    setBaseHash(new RangeFloat(lower ,upper));
        setDataType(DataTypes.FloatType);
        setReturnType(DataTypes.IntegerType);
	}

}
