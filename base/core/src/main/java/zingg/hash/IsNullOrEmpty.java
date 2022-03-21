package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public class IsNullOrEmpty extends HashFunction implements UDF1<String, Boolean>{
	
	public IsNullOrEmpty() {
		super("isNullOrEmpty", DataTypes.StringType, DataTypes.BooleanType);
	}

	 @Override
	 public Boolean call(String field) {
		 return (field == null || ((String ) field).trim().length() == 0);
	 }

	public Object apply(Row ds, String column) {
		 return call((String) ds.getAs(column));
	}
}
