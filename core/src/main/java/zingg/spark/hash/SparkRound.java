package zingg.spark.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import zingg.client.ZFrame;
import zingg.hash.Round;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
public class SparkRound extends Round<Dataset<Row>,Row,Column,DataType> implements UDF1<Double, Long>{
	
	public SparkRound() {
		setDataType(DataTypes.DoubleType);
		setReturnType(DataTypes.LongType);
	}
	

	

	@Override
	public ZFrame<Dataset<Row>,Row,Column> apply(ZFrame<Dataset<Row>,Row,Column> ds, String column, String newColumn) {
		return ds.withColumn(newColumn, functions.callUDF(this.name, ds.col(column)));
	}


	@Override
	public Object getAs(Row r, String column) {
		return (Double) r.getAs(column);
	}

}
