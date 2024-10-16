package zingg.common.core.preprocess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import zingg.TestSparkBase;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.util.SparkStopWordRemoverUtility;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.context.ZinggSparkContext;

@ExtendWith(TestSparkBase.class)
public class TestSparkStopWords extends TestStopWordsBase<SparkSession, Dataset<Row>, Row, Column, DataType> {

    public static IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();
    public static ZinggSparkContext zsCTX = new ZinggSparkContext();

    public TestSparkStopWords(SparkSession sparkSession) throws ZinggClientException {
        super(new SparkDFObjectUtil(iWithSession), new SparkStopWordRemoverUtility(zsCTX), zsCTX);
        iWithSession.setSession(sparkSession);
        zsCTX.init(sparkSession);
    }
}
