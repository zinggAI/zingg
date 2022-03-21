package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
public class IdentityString extends HashFunction implements UDF1<String, String>{
	
	public IdentityString() {
		super("identityString", DataTypes.StringType, DataTypes.StringType);
	}

	 @Override
	 public String call(String field) {
		 if (field == null) return field;
		 field = field.trim().toLowerCase();
		 return field;
	 }

	public Object apply(Row ds, String column) {
		 return call((String) ds.getAs(column));
	}
}
