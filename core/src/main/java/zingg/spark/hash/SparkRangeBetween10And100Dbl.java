package zingg.spark.hash;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

public class SparkRangeBetween10And100Dbl extends SparkRangeDbl<Dataset<Row>,Row,Column,DataType> {

	public SparkRangeBetween10And100Dbl() {
		super(10, 100);
	}

}
