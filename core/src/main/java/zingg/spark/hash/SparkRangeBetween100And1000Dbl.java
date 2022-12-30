package zingg.spark.hash;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

public abstract class SparkRangeBetween100And1000Dbl extends SparkRangeDbl<Dataset<Row>,Row,Column,DataType> {

	public SparkRangeBetween100And1000Dbl() {
		super(100, 1000);
	}

}
