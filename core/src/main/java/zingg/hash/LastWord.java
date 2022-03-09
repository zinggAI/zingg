package zingg.hash;

import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.udf.JavaUDF1;
import com.snowflake.snowpark_java.types.DataTypes;

public class LastWord extends HashFunction implements JavaUDF1<String, String>{
	public LastWord() {
		super("lastWord", DataTypes.StringType, DataTypes.StringType, true);
	}

	
	
			 @Override
			 public String call(String field) {
					String r = null;
					if (field == null ) {
						r = field;
					}
					else {
						String[] v= field.trim().toLowerCase().split(" ");
						return v[v.length-1];
					}
					return r;
				 }

			 public Object apply(Row ds, String column) {
				 return call((String) ds.getAs(column));
			 }

}
