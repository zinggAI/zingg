package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

import zingg.client.ZFrame;

public class TruncateDouble<D,R,C,T> extends HashFunction<D,R,C,T> {
	int numDecimalPlaces;
	static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};
	public TruncateDouble(int numDecimalPlaces) {
		super("truncateDoubleTo" + numDecimalPlaces + "Places");//, DataTypes.DoubleType, DataTypes.DoubleType, true);
		this.numDecimalPlaces = numDecimalPlaces;
	}

	
	public Double call(Double field) {
		Double r = null;
		if (field == null) {
			r = field;
		} else {
			r = Math.floor(field * POWERS_OF_10[numDecimalPlaces]) / POWERS_OF_10[numDecimalPlaces];
		}
		return r;
	}

	

	@Override
	public ZFrame apply(ZFrame<D,R,C> ds, String column, String newColumn) {
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
