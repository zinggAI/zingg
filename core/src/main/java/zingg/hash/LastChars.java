package zingg.hash;

import java.util.Arrays;

import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.udf.JavaUDF1;
import com.snowflake.snowpark_java.types.DataTypes;

public class LastChars extends HashFunction implements JavaUDF1<String, String>{
	int numChars;
	public LastChars(int endIndex) {
		super("last" + endIndex + "Chars", DataTypes.StringType, DataTypes.StringType, true);
		this.numChars = endIndex;
	}

	

		
		 @Override
		 public String call(String field) {
				String r = null;
				if (field == null ) {
					r = field;
				}
				else {
					field = field.trim().toLowerCase();
					r= field.trim().toLowerCase().substring(Math.max(field.length() - numChars, 0));
				}
				return r;
			 }

		 public Object apply(Row ds, int column) {
			 return call((String) ds.get(column));
		 }

}
