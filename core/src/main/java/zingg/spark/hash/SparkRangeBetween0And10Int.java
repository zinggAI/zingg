package zingg.spark.hash;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

public abstract class SparkRangeBetween0And10Int extends SparkRangeInt<Dataset<Row>,Row,Column,DataType> {

	public SparkRangeBetween0And10Int() {
		super(0, 10);
	}

}
