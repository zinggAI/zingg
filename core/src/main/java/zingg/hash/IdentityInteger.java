package zingg.hash;

import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.udf.JavaUDF1;
import com.snowflake.snowpark_java.types.DataTypes;

public class IdentityInteger extends HashFunction implements JavaUDF1<Integer, Integer>{
	
	public IdentityInteger() {
		super("identityInteger", DataTypes.IntegerType, DataTypes.IntegerType);
	}

	 @Override
	 public Integer call(Integer field) {
		 return field;
	 }

	public Object apply(Row ds, String column) {
		 return call((Integer) ds.getAs(column));
	}

}
