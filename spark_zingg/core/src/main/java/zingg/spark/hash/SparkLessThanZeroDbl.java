package zingg.spark.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.hash.LessThanZeroDbl;

public class SparkLessThanZeroDbl extends SparkHashFunction<Double, Boolean>{
	
    public static final Log LOG = LogFactory.getLog(SparkLessThanZeroDbl.class);
    
    public SparkLessThanZeroDbl() {
        setBaseHash(new LessThanZeroDbl());
        setDataType(DataTypes.DoubleType);
        setReturnType(DataTypes.BooleanType);
    }

}
