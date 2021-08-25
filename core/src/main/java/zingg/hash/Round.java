package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public class Round extends HashFunction implements UDF1<Double, Long>{
	
	public Round() {
		super("round", DataTypes.DoubleType, DataTypes.LongType);
	}
	

	 @Override
	 public Long call(Double field) {
		 return Math.round(field);
	 }

	 @Override
	 public Object apply(Row ds, String column) {
		 return call((Double) ds.getAs(column));
	}
}
