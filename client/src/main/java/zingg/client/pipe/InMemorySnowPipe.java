package zingg.client.pipe;

import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.SaveMode;
import com.snowflake.snowpark_java.types.DataType;
import com.snowflake.snowpark_java.types.StructType;




public class InMemorySnowPipe extends Pipe<DataFrame,Row,Column,DataType,StructType,SaveMode>{
    
	public InMemorySnowPipe() {
	}

	public InMemorySnowPipe(DataFrame ds){
		dataset = ds;
	}

    public DataFrame getRecords() {
		return dataset;
	}

	public InMemorySnowPipe(Pipe p) {
		clone(p);
	}
	
	@Override
	public String getFormat() {
		return Pipe.FORMAT_INMEMORY;
	}
}