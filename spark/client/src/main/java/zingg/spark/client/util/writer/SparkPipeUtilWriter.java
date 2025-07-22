package zingg.spark.client.util.writer;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import zingg.common.client.ZFrame;
import zingg.common.client.util.DFWriter;
import zingg.common.client.util.writer.PipeUtilWriter;
import zingg.spark.client.util.SparkDFWriter;

public class SparkPipeUtilWriter extends PipeUtilWriter<Dataset<Row>, Row, Column> {
    @Override
    protected DFWriter<Dataset<Row>, Row, Column> getWriter(ZFrame<Dataset<Row>, Row, Column> input) {
        return new SparkDFWriter(input);
    }
}
