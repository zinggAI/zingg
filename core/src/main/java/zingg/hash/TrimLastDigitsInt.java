package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public class TrimLastDigitsInt extends HashFunction implements UDF1<Integer, Integer> {
	int numDigits;
	static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};
	public TrimLastDigitsInt(int count) {
		super("trimLast" + count + "DigitsInt", DataTypes.IntegerType, DataTypes.IntegerType, true);
		this.numDigits = count;
	}

	@Override
	public Integer call(Integer field) {
		Integer r = null;
		if (field == null) {
			r = field;
		} else {
			r = field / POWERS_OF_10[numDigits];
		}
		return r;
	}

	public Object apply(Row ds, String column) {
		return call((Integer) ds.getAs(column));
	}
	
}
