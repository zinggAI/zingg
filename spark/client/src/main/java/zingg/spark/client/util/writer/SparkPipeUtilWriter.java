package zingg.spark.client.util.writer;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import zingg.common.client.ZFrame;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.writer.IDFWriter;
import zingg.common.client.util.writer.PipeUtilWriter;
import zingg.spark.client.util.SparkDFWriter;

public class SparkPipeUtilWriter extends PipeUtilWriter<Dataset<Row>, Row, Column> {
    @Override
    protected IDFWriter<Dataset<Row>, Row, Column> getWriter(ZFrame<Dataset<Row>, Row, Column> input, Pipe<Dataset<Row>, Row, Column> pipe) {
        return new SparkDFWriter(input, pipe);
    }
}
