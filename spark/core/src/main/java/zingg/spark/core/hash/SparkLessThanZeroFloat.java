package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.core.hash.LessThanZeroFloat;

public class SparkLessThanZeroFloat extends SparkHashFunction<Float, Boolean>{
	
    private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(SparkLessThanZeroFloat.class);
    
    public SparkLessThanZeroFloat() {
        setBaseHash(new LessThanZeroFloat());
        setDataType(DataTypes.FloatType);
        setReturnType(DataTypes.BooleanType);
    }

}
