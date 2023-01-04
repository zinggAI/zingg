package zingg.spark.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.hash.LastWord;

public class SparkLastWord extends SparkHashFunction<String, String>{
    
    public static final Log LOG = LogFactory.getLog(SparkLastWord.class);
	
	public SparkLastWord() {
	    setBaseHash(new LastWord());
		setDataType(DataTypes.StringType);
		setReturnType(DataTypes.StringType);		
	}

}
