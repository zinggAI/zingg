package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public class TrimLastDigitsDbl extends HashFunction implements UDF1<Double, Double> {
	int numDigits;
	static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};
	public TrimLastDigitsDbl(int count) {
		super("trimLast" + count + "DigitsDbl", DataTypes.DoubleType, DataTypes.DoubleType, true);
		this.numDigits = count;
	}

	@Override
	public Double call(Double field) {
		Double r = null;
		if (field == null) {
			r = field;
		} else {
			r = Math.floor(field / POWERS_OF_10[numDigits]);
		}
		return r;
	}

	public Object apply(Row ds, String column) {
		return call((Double) ds.getAs(column));
	}
	
}
