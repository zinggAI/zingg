package zingg.snowpark.hash;

import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Functions;
import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.udf.JavaUDF1;
import com.snowflake.snowpark_java.types.DataType;
import com.snowflake.snowpark_java.types.DataTypes;

import zingg.client.ZFrame;
import zingg.hash.IsNullOrEmpty;

public class SnowIsNullOrEmpty extends IsNullOrEmpty<DataFrame,Row,Column,DataType> implements JavaUDF1<String, Boolean>{
	
	public SnowIsNullOrEmpty() {
		super();
		setDataType(DataTypes.StringType);
		setReturnType(DataTypes.BooleanType);
	}
	
	@Override
	public Object getAs(Row r, String column) {
		return r.getString(getColIdx(column));
	}



	@Override
	public ZFrame<DataFrame, Row, Column> apply(ZFrame<DataFrame, Row, Column> ds, String column,
			String newColumn) {
		return ds.withColumn(newColumn, Functions.callUDF(this.name, ds.col(column)));
	}

	public int getColIdx(String colName){
        String[] colNames = df.schema().names();
        int index = -1;
        for (int idx=0;idx<colNames.length;idx++){
            if (colNames[idx].equalsIgnoreCase(colName)){
                index = idx+1;
                break;
            }
        }
        return index;
    }
}

	
