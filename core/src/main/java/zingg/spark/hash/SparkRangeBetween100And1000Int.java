package zingg.spark.hash;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

public abstract class SparkRangeBetween100And1000Int extends SparkRangeInt<Dataset<Row>,Row,Column,DataType> {

	public SparkRangeBetween100And1000Int() {
		super(100, 1000);
	}

}
