package zingg.spark.core.sparkFrame;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.extension.ExtendWith;
import zingg.spark.core.TestSparkBase;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.zFrame.TestZFrameBase;
import zingg.spark.client.util.SparkDFObjectUtil;

@ExtendWith(TestSparkBase.class)
public class TestSparkFrame extends TestZFrameBase<SparkSession, Dataset<Row>, Row, Column> {
    public static IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();

    public TestSparkFrame(SparkSession sparkSession) {
        super(new SparkDFObjectUtil(iWithSession));
        iWithSession.setSession(sparkSession);
    }
}