package zingg.spark.hash;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

public abstract class SparkRangeBetween1000And10000Dbl extends SparkRangeDbl<Dataset<Row>,Row,Column,DataType> {

	public SparkRangeBetween1000And10000Dbl() {
		super(1000, 10000);
	}

}
