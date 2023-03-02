package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.hash.RangeInt;

public class SparkRangeInt extends SparkHashFunction<Integer, Integer>{
    
    public static final Log LOG = LogFactory.getLog(SparkRangeInt.class);
    
	public SparkRangeInt(int lower, int upper) {
	    setBaseHash(new RangeInt(lower ,upper));
        setDataType(DataTypes.IntegerType);
        setReturnType(DataTypes.IntegerType);
	}

}
