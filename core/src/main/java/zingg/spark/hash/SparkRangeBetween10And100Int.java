package zingg.spark.hash;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

public abstract class SparkRangeBetween10And100Int extends SparkRangeInt<Dataset<Row>,Row,Column,DataType> {

	public SparkRangeBetween10And100Int() {
		super(10, 100);
	}

}
