package zingg.spark.core.util;

import zingg.util.DFWriter;
import zingg.common.client.pipe.Pipe;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;

import org.apache.spark.sql.SaveMode;
import org.apache.spark.storage.StorageLevel;
import zingg.spark.client.SparkFrame;
import zingg.common.client.ZFrame;

public class SparkDFWriter implements DFWriter<Dataset<Row>, Row, Column>{
    private DataFrameWriter writer;

    public SparkDFWriter(ZFrame<Dataset<Row>, Row, Column> toWriteOrig) {
        Dataset<Row> toWrite = toWriteOrig.df();
		this.writer = toWrite.write();
        
    }


    public void setMode(String s) {
        this.writer.mode(SaveMode.valueOf(s));

    }
    public DFWriter<Dataset<Row>, Row, Column> format(String f) {
        writer.format(f);
        return this;
    }
    public DFWriter<Dataset<Row>, Row, Column> option(String k, String v) {
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
