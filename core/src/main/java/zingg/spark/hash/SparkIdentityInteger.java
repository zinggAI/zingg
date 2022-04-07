package zingg.spark.hash;

import zingg.client.ZFrame;
import zingg.hash.IdentityInteger;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.DataType;

public class SparkIdentityInteger extends IdentityInteger<Dataset<Row>, Row, Column,DataType,DataType> implements UDF1<Integer, Integer>{
	
	public SparkIdentityInteger() {
		super();
		setDataType(DataTypes.IntegerType);
		setReturnType(DataTypes.IntegerType);
	}

	@Override
	public Object getAs(Row r, String column) {
		return (Integer) r.getAs(column);
	}



	@Override
	public ZFrame<Dataset<Row>, Row, Column> apply(ZFrame<Dataset<Row>, Row, Column> ds, String column,
			String newColumn) {
		return ds.withColumn(newColumn, functions.callUDF(this.name, ds.col(column)));
	}

}
