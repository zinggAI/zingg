package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

import zingg.client.ZFrame;

public class LessThanZeroDbl<D,R,C,T> extends HashFunction<D,R,C,T> {

	public LessThanZeroDbl() {
		super("lessThanZeroDbl");
	}

	
	public Boolean call(Double field) {
		Boolean r = false;
		if (field != null) {
			r = field < 0 ? true : false;
		}
		return r;
	}

	@Override
	 public Object apply(R r, String column) {
		 return null;
	 }
 
 
	 @Override
	 public Object apply(D df, R r, String column) {
		 return null;
	 }
 

	@Override
	public Object getAs(R r, String column) {
		return null;
	}


	@Override
	public ZFrame<D, R, C> apply(ZFrame<D, R, C> ds, String column, String newColumn) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Object getAs(D df, R r, String column) {
		// TODO Auto-generated method stub
		return null;
	}

}
