package zingg.spark.core.preprocess;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import org.junit.jupiter.api.extension.ExtendWith;
import zingg.spark.core.TestSparkBase;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.preprocess.IPreprocessors;
import zingg.common.core.preprocess.TestPreprocessors;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.executor.SparkTrainingDataFinder;

@ExtendWith(TestSparkBase.class)
public class TestSparkPreprocessors extends TestPreprocessors<SparkSession, Dataset<Row>, Row, Column, DataType> {

    public static IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();
    public static ZinggSparkContext zsCTX = new ZinggSparkContext();

    public TestSparkPreprocessors(SparkSession sparkSession) throws ZinggClientException{
        super(new SparkDFObjectUtil(iWithSession), zsCTX);
        iWithSession.setSession(sparkSession);
        zsCTX.init(sparkSession);
    }

    @Override
    public IPreprocessors<SparkSession, Dataset<Row>, Row, Column, DataType> getPreprocessors() {
        return new SparkTrainingDataFinder(zsCTX);
    }
    
}
