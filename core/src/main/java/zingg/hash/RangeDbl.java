package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public class RangeDbl extends HashFunction implements UDF1<Double, Integer> {
	int lowerLimit;
	int upperLimit;

	public RangeDbl(int lower, int upper) {
		super("rangeBetween" + lower + "And" + upper + "Dbl", DataTypes.DoubleType, DataTypes.IntegerType, true);
		this.lowerLimit = lower;
		this.upperLimit = upper;
	}

	@Override
	public Integer call(Double field) {
		int withinRange = 0;
		if (field >= lowerLimit && field < upperLimit) {
			withinRange = 1;
		}
		return withinRange;
	}

	public Object apply(Row ds, String column) {
		return call((Double) ds.getAs(column));
	}

}
