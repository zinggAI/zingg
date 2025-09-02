package zingg.spark.client.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import zingg.common.client.ZFrame;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.writer.IDFWriter;
import zingg.common.client.util.writer.WriterStrategy;
import zingg.common.client.util.writer.WriterStrategyFactory;

public class SparkDFWriter implements IDFWriter<Dataset<Row>, Row, Column> {
    protected final DataFrameWriter<Row> writer;
    protected final ZFrame<Dataset<Row>, Row, Column> zFrameToWrite;

    public SparkDFWriter(ZFrame<Dataset<Row>, Row, Column> toWriteOrig, Pipe<Dataset<Row>, Row, Column> pipe) {
        this.zFrameToWrite = toWriteOrig;
        Dataset<Row> toWrite = toWriteOrig.df();
		this.writer = toWrite.write();
        initializeWriterForPipe(pipe);
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

    @Override
    public void write(Pipe<Dataset<Row>, Row, Column> pipe) throws Exception {
        WriterStrategy<Dataset<Row>, Row, Column> writerStrategy = getWriteStrategyFactory().getStrategy(pipe);
        writerStrategy.write(zFrameToWrite, pipe);
    }

    public void initializeWriterForPipe(Pipe<Dataset<Row>, Row, Column> pipe) {
        this.format(pipe.getFormat());
        this.setMode(pipe.getMode() != null ? pipe.getMode() : "Append");
        for (String key : pipe.getProps().keySet()) {
            //back compatibility
            if (FilePipe.LOCATION.equals(key)) {
                this.option(FilePipe.PATH, pipe.get(key));
            } else {
                this.option(key, pipe.get(key));
            }
        }
    }

    protected WriterStrategyFactory<Dataset<Row>, Row, Column> getWriteStrategyFactory() {
        return new WriterStrategyFactory<>(this);
    }
    
}
