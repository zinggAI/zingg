package zingg.spark.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.hash.RangeDbl;

public class SparkRangeDbl extends SparkHashFunction<Double, Integer>{
    
    public static final Log LOG = LogFactory.getLog(SparkRangeDbl.class);
    
	public SparkRangeDbl(int lower, int upper) {
	    setBaseHash(new RangeDbl(lower ,upper));
        setDataType(DataTypes.DoubleType);
        setReturnType(DataTypes.IntegerType);
	}

}
