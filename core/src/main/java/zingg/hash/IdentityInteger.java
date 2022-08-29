package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public abstract class IdentityInteger<D,R,C,T> extends HashFunction<D,R,C,T> implements UDF1<Integer, Integer>{
	
	public IdentityInteger() {
		super("identityInteger");
		//, DataTypes.IntegerType, DataTypes.IntegerType);
	}

	 @Override
	 public Integer call(Integer field) {
		 return field;
	 }

	public Object apply(Row ds, String column) {
		 return call((Integer) ds.getAs(column));
	}

}
