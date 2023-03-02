package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.hash.Round;
public class SparkRound extends SparkHashFunction<Double, Long>{
    
    public static final Log LOG = LogFactory.getLog(SparkRound.class);
	
	public SparkRound() {
	    setBaseHash(new Round());
		setDataType(DataTypes.DoubleType);
		setReturnType(DataTypes.LongType);
	}

}
