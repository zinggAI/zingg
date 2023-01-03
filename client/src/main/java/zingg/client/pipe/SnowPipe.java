package zingg.client.pipe;

import com.snowflake.snowpark_java.*;

public class SnowPipe extends Pipe<DataFrame, Row, Column> {
    
    protected SaveMode mode;

    public static final String avro = "avro";
    public static final String csv = "csv";
    public static final String json = "json";
    public static final String orc = "orc";
    public static final String parquet = "parquet";
    public static final String TABLE = "table";
    public static final String xml = "xml";
    
   


	
}
