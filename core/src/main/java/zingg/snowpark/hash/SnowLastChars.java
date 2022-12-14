package zingg.snowpark.hash;

import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Functions;
import com.snowflake.snowpark_java.udf.JavaUDF1;
import com.snowflake.snowpark_java.types.DataType;
import com.snowflake.snowpark_java.types.DataTypes;

import zingg.client.ZFrame;
import zingg.client.SnowFrame;
import zingg.hash.LastChars;

public class SnowLastChars extends LastChars<DataFrame,Row,Column,DataType> implements JavaUDF1<String, String>{
	
	public SnowLastChars(int endIndex) {
		super(endIndex);
		setDataType(DataTypes.StringType); 
		setReturnType(DataTypes.StringType);
	}

	@Override
	public Object getAs(DataFrame df, Row r, String column) {
		SnowFrame sf = new SnowFrame(df);
		return (String) sf.getAsString(r, column);
	}



	@Override
	public ZFrame<DataFrame, Row, Column> apply(ZFrame<DataFrame, Row, Column> ds, String column,
			String newColumn) {
		return ds.withColumn(newColumn, Functions.callUDF(this.name, ds.col(column)));
	}



}
