package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public abstract class LastWord<D,R,C,T,T1> extends HashFunction<D,R,C,T,T1>{
	public LastWord() {
		super("lastWord");		
	}

	
	
			
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
