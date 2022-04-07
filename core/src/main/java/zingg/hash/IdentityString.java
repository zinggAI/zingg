package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
public abstract class IdentityString<D,R,C,T,T1> extends HashFunction<D,R,C,T,T1> implements UDF1<String, String>{
	
	public IdentityString() {
		super("identityString");
		//, DataTypes.StringType, DataTypes.StringType);
	}

	 @Override
	 public String call(String field) {
		 if (field == null) return field;
		 field = field.trim().toLowerCase();
		 return field;
	 }

	public Object apply(R ds, String column) {
		 return call((String) getAs(ds, column));
	}
}
