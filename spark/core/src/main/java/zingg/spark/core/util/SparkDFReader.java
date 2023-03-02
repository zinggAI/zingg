package zingg.spark.core.util;

import zingg.common.core.util.DFReader;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;

import zingg.spark.client.SparkFrame;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public class SparkDFReader implements DFReader<Dataset<Row>, Row, Column> {
    
    private SparkSession session;
    private DataFrameReader reader;

    public SparkDFReader(SparkSession s) {
        this.session = s;
        this.reader = s.read();
    }

    public DFReader<Dataset<Row>, Row, Column> getReader() {
        return this;
    }

    

    public DFReader<Dataset<Row>, Row, Column> format(String f) {
        this.reader.format(f);
        return this;
    }

    public DFReader<Dataset<Row>, Row, Column> option(String k, String v){
        this.reader.option(k,v);
        return this;
    }

    public DFReader<Dataset<Row>, Row, Column> setSchema(String s) {
        this.reader.schema(StructType.fromDDL(s));
        return this;
    }

    public ZFrame<Dataset<Row>, Row, Column> load() throws ZinggClientException {
        return new SparkFrame(this.reader.load());
    }

    public ZFrame<Dataset<Row>, Row, Column> load(String location) throws ZinggClientException{
        return new SparkFrame(this.reader.load(location));
    }
    
}
