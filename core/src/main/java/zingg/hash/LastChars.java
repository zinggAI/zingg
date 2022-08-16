package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public abstract class LastChars<D,R,C,T,T1> extends HashFunction<D,R,C,T,T1> implements UDF1<String, String>{
	int numChars;
	
	public LastChars(int endIndex) {
		super("last" + endIndex + "Chars");
		// DataTypes.StringType, DataTypes.StringType, true);
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

		 public Object apply(R ds, String column) {
			 return call((String) getAs(ds, column));
		 }

}
