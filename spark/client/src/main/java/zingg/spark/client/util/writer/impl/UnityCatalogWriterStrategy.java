package zingg.spark.client.util.writer.impl;

import org.apache.spark.sql.DataFrameWriter;
import zingg.common.client.ZFrame;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.writer.IDFWriter;
import zingg.common.client.util.writer.WriterStrategy;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import zingg.spark.client.util.SparkDFWriter;

public class UnityCatalogWriterStrategy implements WriterStrategy<Dataset<Row>, Row, Column> {

    private final IDFWriter<Dataset<Row>, Row, Column> dfWriter;

    public UnityCatalogWriterStrategy(IDFWriter<Dataset<Row>, Row, Column> dfWriter) {
        this.dfWriter = dfWriter;
    }

    @Override
    public void write(ZFrame<Dataset<Row>, Row, Column> zFrame, Pipe<Dataset<Row>, Row, Column> pipe) {
        dfWriter.format(Pipe.FORMAT_UNITYCATALOG);
        DataFrameWriter<Row> dataFrameWriter = ((SparkDFWriter)dfWriter).getDataFrameWriter();
        dataFrameWriter.saveAsTable(pipe.get(FilePipe.TABLE));
    }
}
