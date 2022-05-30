package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public class TruncateDouble extends HashFunction implements UDF1<Double, Double> {
	int numDecimalPlaces;
	static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};
	public TruncateDouble(int numDecimalPlaces) {
		super("truncateDoubleTo" + numDecimalPlaces + "Places", DataTypes.DoubleType, DataTypes.DoubleType, true);
		this.numDecimalPlaces = numDecimalPlaces;
	}

	@Override
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
	public Object apply(Row ds, String column) {
		return call((Double) ds.getAs(column));
	}
	
}
