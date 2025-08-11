package zingg.spark.client.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import zingg.common.client.ZFrame;
import zingg.common.client.util.writer.IDFWriter;

public class SparkDFWriter implements IDFWriter<Dataset<Row>, Row, Column> {
    private final DataFrameWriter<Row> writer;

    public SparkDFWriter(ZFrame<Dataset<Row>, Row, Column> toWriteOrig) {
        Dataset<Row> toWrite = toWriteOrig.df();
		this.writer = toWrite.write();
        
    }


    public void setMode(String s) {
        this.writer.mode(SaveMode.valueOf(s));

    }
    public IDFWriter<Dataset<Row>, Row, Column> format(String f) {
        writer.format(f);
        return this;
    }
    public IDFWriter<Dataset<Row>, Row, Column> option(String k, String v) {
        writer.option(k,v);
        return this;
    }
    public void save(String location) {
        writer.save(location);
    }
    public void save() {
        writer.save();
    }
    
}
