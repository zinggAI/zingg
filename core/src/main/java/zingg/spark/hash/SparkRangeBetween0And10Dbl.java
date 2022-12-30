package zingg.spark.hash;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

public abstract class SparkRangeBetween0And10Dbl extends SparkRangeDbl<Dataset<Row>,Row,Column,DataType> {

	public SparkRangeBetween0And10Dbl() {
		super(0, 10);
	}

}
