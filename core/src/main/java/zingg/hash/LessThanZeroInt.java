package zingg.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public class LessThanZeroInt extends HashFunction implements UDF1<Integer, Boolean> {
	public LessThanZeroInt() {
		super("lessThanZeroInt", DataTypes.IntegerType, DataTypes.BooleanType, true);
	}

	@Override
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

}
