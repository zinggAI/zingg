package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

import zingg.client.ZFrame;

public class LessThanZeroInt<D,R,C,T> extends HashFunction<D,R,C,T> {
	public LessThanZeroInt() {
		super("lessThanZeroInt");//, DataTypes.IntegerType, DataTypes.BooleanType, true);
	}

	
	public Boolean call(Integer field) {
		Boolean r = false;
		if (field != null) {
			r = field < 0 ? true : false;
		}
		return r;
	}

	public Object apply(Row ds, String column) {
		return call((Integer) ds.getAs(column));
	}

	@Override
	public ZFrame apply(ZFrame ds, String column, String newColumn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAs(Object r, String column) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAs(Object df, Object r, String column) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object apply(Object r, String column) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object apply(Object df, Object r, String column) {
		// TODO Auto-generated method stub
		return null;
	}

}
