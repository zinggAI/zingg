package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public class LessThanZeroDbl extends HashFunction implements UDF1<Double, Boolean> {
	public LessThanZeroDbl() {
		super("lessThanZeroDbl", DataTypes.DoubleType, DataTypes.BooleanType, true);
	}

	@Override
	public Boolean call(Double field) {
		Boolean r = false;
		if (field != null) {
			r = field < 0 ? true : false;
		}
		return r;
	}

	public Object apply(Row ds, String column) {
		return call((Double) ds.getAs(column));
	}

}
