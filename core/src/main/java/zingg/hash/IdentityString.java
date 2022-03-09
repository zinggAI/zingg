package zingg.hash;

import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.udf.JavaUDF1;
import com.snowflake.snowpark_java.types.DataTypes;
public class IdentityString extends HashFunction implements JavaUDF1<String, String>{
	
	public IdentityString() {
		super("identityString", DataTypes.StringType, DataTypes.StringType);
	}

	 @Override
	 public String call(String field) {
		 if (field == null) return field;
		 field = field.trim().toLowerCase();
		 return field;
	 }

	public Object apply(Row ds, int column) {
		 return call((String) ds.get(column));
	}
}
