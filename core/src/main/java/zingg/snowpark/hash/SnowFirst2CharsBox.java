package zingg.snowpark.hash;

import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Functions;
import com.snowflake.snowpark_java.udf.JavaUDF1;
import com.snowflake.snowpark_java.types.DataType;
import com.snowflake.snowpark_java.types.DataTypes;


import zingg.client.ZFrame;
import zingg.hash.First2CharsBox;

public class SnowFirst2CharsBox extends First2CharsBox<DataFrame, Row, Column,DataType> implements JavaUDF1<String, Integer>{

	public SnowFirst2CharsBox() {
		super();
		setDataType(DataTypes.StringType);
		setReturnType(DataTypes.IntegerType);
	}

	@Override
	public Object getAs(Row r, String column) {
		return r.getString(column);
	}



	@Override
	public ZFrame<DataFrame, Row, Column> apply(ZFrame<DataFrame, Row, Column> ds, String column,
			String newColumn) {
		return ds.withColumn(newColumn, Functions.callUDF(this.name, ds.col(column)));
	}


	
}
