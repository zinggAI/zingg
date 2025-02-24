package zingg.spark.core.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.extension.ExtendWith;
import zingg.spark.core.TestSparkBase;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.util.TestVerticalDisplayUtility;
import zingg.spark.client.util.SparkDFObjectUtil;


@ExtendWith(TestSparkBase.class)
public class TestSparkVerticalDisplayUtility extends TestVerticalDisplayUtility<SparkSession, Dataset<Row>, Row, Column> {

    private IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();

    public TestSparkVerticalDisplayUtility(SparkSession sparkSession) {
        this.iWithSession = new WithSession<SparkSession>();
        iWithSession.setSession(sparkSession);
        initialize(new SparkDFObjectUtil(iWithSession));
    }
}
