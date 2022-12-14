package zingg.spark.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.DataType;

import zingg.client.ZFrame;
import zingg.hash.First3CharsBox;

public class SparkFirst3CharsBox<D,R,C,T> extends First3CharsBox<Dataset<Row>,Row,Column,DataType> implements UDF1<String, Integer>{

	public SparkFirst3CharsBox() {
		super();
		setDataType(DataTypes.StringType);
		setReturnType(DataTypes.IntegerType);
	}


	@Override
	public Object getAs(Row r, String column) {
		return (String) r.getAs(column);
	}



	@Override
	public ZFrame<Dataset<Row>, Row, Column> apply(ZFrame<Dataset<Row>, Row, Column> ds, String column,
			String newColumn) {
		return ds.withColumn(newColumn, functions.callUDF(this.name, ds.col(column)));
	}
	

}