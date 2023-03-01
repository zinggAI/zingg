package zingg.spark.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.hash.LessThanZeroInt;

public class SparkLessThanZeroInt extends SparkHashFunction<Integer, Boolean>{
    
    public static final Log LOG = LogFactory.getLog(SparkLessThanZeroInt.class);
	
    public SparkLessThanZeroInt() {
        setBaseHash(new LessThanZeroInt());
        setDataType(DataTypes.IntegerType);
        setReturnType(DataTypes.BooleanType);
    }    

}
