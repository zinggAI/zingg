package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.core.hash.LessThanZeroLong;

public class SparkLessThanZeroLong extends SparkHashFunction<Long, Boolean>{
    
    private static final long serialVersionUID = 1L;
	public static final Log LOG = LogFactory.getLog(SparkLessThanZeroLong.class);
	
    public SparkLessThanZeroLong() {
        setBaseHash(new LessThanZeroLong());
        setDataType(DataTypes.LongType);
        setReturnType(DataTypes.BooleanType);
    }    

}
