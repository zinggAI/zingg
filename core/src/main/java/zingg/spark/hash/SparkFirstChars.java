package zingg.spark.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import zingg.hash.FirstChars;


public class SparkFirstChars extends SparkHashFunction<String, String>{
	
	public static final Log LOG = LogFactory.getLog(SparkFirstChars.class);

	private FirstChars firstChars;
	
	public SparkFirstChars(int endIndex) {
	    this.firstChars = new FirstChars(endIndex);
	    setName(firstChars.getName());
		setDataType(DataTypes.StringType);
		setReturnType(DataTypes.StringType);
	}
	
	public String call(String field) {
	    return firstChars.call(field);
	}

    public int getEndIndex() {
        return firstChars.getEndIndex();
    }

}
