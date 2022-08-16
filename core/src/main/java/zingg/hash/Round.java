package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

import zingg.client.ZFrame;

public abstract class Round<D,R,C,T,T1> extends HashFunction<D,R,C,T,T1> implements UDF1<Double, Long>{
	
	public Round() {
		super("round");
		//, DataTypes.DoubleType, DataTypes.LongType);
	}
	

	 @Override
	 public Long call(Double field) {
		 return field == null ? null : Math.round(field);
	 }

	 @Override
	 public Object apply(R ds, String column) {
		 return call((Double) getAs(ds, column));
	}
}


