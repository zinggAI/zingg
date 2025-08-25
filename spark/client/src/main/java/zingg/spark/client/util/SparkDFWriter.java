package zingg.spark.client.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import zingg.common.client.ZFrame;
import zingg.common.client.util.writer.IDFWriter;

public class SparkDFWriter implements IDFWriter<Dataset<Row>, Row, Column> {
    protected final DataFrameWriter<Row> writer;

    public SparkDFWriter(ZFrame<Dataset<Row>, Row, Column> toWriteOrig) {
        Dataset<Row> toWrite = toWriteOrig.df();
		this.writer = toWrite.write();
        
    }

    @Override
    public void setMode(String s) {
        this.writer.mode(SaveMode.valueOf(s));

    }

    @Override
    public IDFWriter<Dataset<Row>, Row, Column> format(String f) {
        writer.format(f);
        return this;
    }

    @Override
    public IDFWriter<Dataset<Row>, Row, Column> option(String k, String v) {
        writer.option(k,v);
        return this;
    }

    @Override
    public void save() {
        writer.save();
    }
    
}
