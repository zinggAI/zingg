package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public class RangeInt extends HashFunction implements UDF1<Integer, Integer> {
	int lowerLimit;
	int upperLimit;

	public RangeInt(int lower, int upper) {
		super("rangeBetween" + lower + "And" + upper + "Int", DataTypes.IntegerType, DataTypes.IntegerType, true);
		this.lowerLimit = lower;
		this.upperLimit = upper;
	}

	@Override
	public Integer call(Integer field) {
		int withinRange = 0;
		if (field != null && field >= lowerLimit && field < upperLimit) {
			withinRange = 1;
		}
		return withinRange;
	}

	public Object apply(Row ds, String column) {
		return call((Integer) ds.getAs(column));
	}

}
