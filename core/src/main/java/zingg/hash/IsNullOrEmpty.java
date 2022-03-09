package zingg.hash;

import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.udf.JavaUDF1;
import com.snowflake.snowpark_java.types.DataTypes;

public class IsNullOrEmpty extends HashFunction implements JavaUDF1<String, Boolean>{
	
	public IsNullOrEmpty() {
		super("isNullOrEmpty", DataTypes.StringType, DataTypes.BooleanType);
	}

	 @Override
	 public Boolean call(String field) {
		 return (field == null || ((String ) field).trim().length() == 0);
	 }

	public Object apply(Row ds, int column) {
		 return call((String) ds.get(column));
	}
}
