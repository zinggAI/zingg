package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

import zingg.client.ZFrame;

public class RangeDbl<D,R,C,T> extends HashFunction<D,R,C,T> {
	int lowerLimit;
	int upperLimit;

	public RangeDbl(int lower, int upper) {
		super("rangeBetween" + lower + "And" + upper + "Dbl");//, DataTypes.DoubleType, DataTypes.IntegerType, true);
		this.lowerLimit = lower;
		this.upperLimit = upper;
	}

	
	public Integer call(Double field) {
		int withinRange = 0;
		if (field != null && field >= lowerLimit && field < upperLimit) {
			withinRange = 1;
		}
		return withinRange;
	}

	public Object apply(Row ds, String column) {
		return call((Double) ds.getAs(column));
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
