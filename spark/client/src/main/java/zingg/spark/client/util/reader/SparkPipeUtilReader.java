package zingg.spark.client.util.reader;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import zingg.common.client.ZFrame;
import zingg.common.client.util.reader.IDFReader;
import zingg.common.client.util.reader.PipeUtilReader;
import zingg.spark.client.SparkFrame;
import zingg.spark.client.util.SparkDFReader;
import zingg.spark.client.util.SparkDSUtil;

public class SparkPipeUtilReader extends PipeUtilReader<SparkSession, Dataset<Row>, Row, Column> {

    public SparkPipeUtilReader(SparkSession sparkSession) {
        super(sparkSession);
    }

    @Override
    protected ZFrame<Dataset<Row>, Row, Column> addLineNo(ZFrame<Dataset<Row>, Row, Column> data) {
        return new SparkFrame(new SparkDSUtil(getSession()).addRowNumber(data).df());
    }

    @Override
    protected IDFReader<Dataset<Row>, Row, Column> getReader() {
        return new SparkDFReader(getSession());
    }
}
