package zingg.hash;

import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.udf.JavaUDF1;
import com.snowflake.snowpark_java.types.DataTypes;

public class Round extends HashFunction implements JavaUDF1<Double, Long>{
	
	public Round() {
		super("round", DataTypes.DoubleType, DataTypes.LongType);
	}
	

	 @Override
	 public Long call(Double field) {
		 return field == null ? null : Math.round(field);
	 }

	 @Override
	 public Object apply(Row ds, int column) {
		 return call((Double) ds.get(column));
	}
}
