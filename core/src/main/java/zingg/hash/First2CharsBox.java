package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public abstract class First2CharsBox<D,R,C,T>extends HashFunction<D,R,C,T> implements UDF1<String, Integer>{

	public First2CharsBox() {
		super("first2CharsBox");
		//, DataTypes.StringType, DataTypes.IntegerType, true);
	}

	
	
	 @Override
	 public Integer call(String field) {
		 if (field == null || field.trim().length() <= 2) {
				return 0;
			} else {
				String sub = field.trim().toLowerCase().substring(0, 2);
				if (sub.compareTo("aa") >= 0 && sub.compareTo("jz") < 0) {
					return 1;
			} else if (sub.compareTo("jz") >= 0 && sub.compareTo("oz") < 0) {
					return 2;
			} else if (sub.compareTo("oz") >= 0) {
					return 3;
				} else {
					return 4;
				}
			}//else
	 }
	 
	 @Override
	 public Object apply(R ds, String column) {
		 return call((String) getAs(ds, column));
	}
}
